package com.example.data.repository

import android.content.Context
import com.example.data.api.AniListApiService
import com.example.data.api.AnikotoApiService
import com.example.data.local.dao.CachedAnimeDao
import com.example.data.local.dao.FavoriteEpisodeDao
import com.example.data.local.entity.CachedAnimeEntity
import com.example.data.model.AniListQueryRequest
import com.example.data.model.AnimeItem
import com.example.data.model.EpisodeItem
import com.example.data.model.SeriesDetailData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

enum class WatchStatus(val label: String) {
  NONE("All"),
  WATCHING("Watching"),
  COMPLETED("Completed"),
  PLAN_TO_WATCH("Plan to Watch"),
  PAUSED("Paused"),
  DROPPED("Dropped")
}

interface AnimeRepository {
  suspend fun getRecentAnime(page: Int = 1, perPage: Int = 50): Result<List<AnimeItem>>
  suspend fun searchAnimeDatabase(query: String): Result<List<AnimeItem>>
  suspend fun getSeriesDetails(id: Long): Result<SeriesDetailData>
  suspend fun getRelatedAnime(animeId: Long, title: String? = null): Result<List<AnimeItem>>
  suspend fun getRecommendedAnime(animeId: Long, title: String? = null, genres: List<String>? = emptyList()): Result<List<AnimeItem>>
  fun isBookmarked(id: Long): Boolean
  fun toggleBookmark(id: Long)
  fun getWatchStatus(id: Long): WatchStatus
  fun setWatchStatus(id: Long, status: WatchStatus)
  val watchStatusMapFlow: StateFlow<Map<Long, WatchStatus>>
  val bookmarksFlow: StateFlow<Set<Long>>
  val cachedAnimeFlow: Flow<List<AnimeItem>>
}

class DefaultAnimeRepository(
  private val apiService: AnikotoApiService,
  private val aniListApiService: AniListApiService? = null,
  private val cachedAnimeDao: CachedAnimeDao? = null,
  private val favoriteEpisodeDao: FavoriteEpisodeDao? = null,
  context: Context? = null
) : AnimeRepository {

  private val prefs = context?.getSharedPreferences("animarch_prefs", Context.MODE_PRIVATE)
  private val _bookmarks = MutableStateFlow<Set<Long>>(loadSavedBookmarks())
  override val bookmarksFlow: StateFlow<Set<Long>> = _bookmarks.asStateFlow()

  private val _watchStatusMap = MutableStateFlow<Map<Long, WatchStatus>>(loadSavedWatchStatuses())
  override val watchStatusMapFlow: StateFlow<Map<Long, WatchStatus>> = _watchStatusMap.asStateFlow()

  // In-memory catalog cache across fetched pages & searches
  private val fullCatalogCache = mutableMapOf<Long, AnimeItem>()

  override val cachedAnimeFlow: Flow<List<AnimeItem>> =
    cachedAnimeDao?.getAllCachedAnime()?.map { list ->
      list.map { it.toAnimeItem() }
    } ?: flowOf(emptyList())

  private fun loadSavedBookmarks(): Set<Long> {
    val stringSet = prefs?.getStringSet("bookmarked_ids", emptySet()) ?: emptySet()
    return stringSet.mapNotNull { it.toLongOrNull() }.toSet()
  }

  private fun loadSavedWatchStatuses(): Map<Long, WatchStatus> {
    val allPrefs = prefs?.all.orEmpty()
    val result = mutableMapOf<Long, WatchStatus>()
    allPrefs.forEach { (key, value) ->
      if (key.startsWith("status_") && value is String) {
        val id = key.removePrefix("status_").toLongOrNull()
        if (id != null) {
          try {
            result[id] = WatchStatus.valueOf(value)
          } catch (_: Exception) {}
        }
      }
    }
    return result
  }

  override fun isBookmarked(id: Long): Boolean {
    return _bookmarks.value.contains(id)
  }

  override fun toggleBookmark(id: Long) {
    val current = _bookmarks.value.toMutableSet()
    if (current.contains(id)) {
      current.remove(id)
    } else {
      current.add(id)
    }
    _bookmarks.value = current
    prefs?.edit()?.putStringSet("bookmarked_ids", current.map { it.toString() }.toSet())?.apply()
  }

  override fun getWatchStatus(id: Long): WatchStatus {
    return _watchStatusMap.value[id] ?: WatchStatus.NONE
  }

  override fun setWatchStatus(id: Long, status: WatchStatus) {
    val current = _watchStatusMap.value.toMutableMap()
    if (status == WatchStatus.NONE) {
      current.remove(id)
      prefs?.edit()?.remove("status_$id")?.apply()
    } else {
      current[id] = status
      prefs?.edit()?.putString("status_$id", status.name)?.apply()
    }
    _watchStatusMap.value = current
  }

  override suspend fun getRecentAnime(page: Int, perPage: Int): Result<List<AnimeItem>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getRecentAnime(page = page, perPage = perPage)
      val list = response.data.orEmpty()
      if (list.isNotEmpty()) {
        list.forEach { fullCatalogCache[it.id] = it }
        if (cachedAnimeDao != null && page == 1) {
          val entities = list.mapIndexed { index, item ->
            CachedAnimeEntity.fromAnimeItem(item, index)
          }
          cachedAnimeDao.replaceCache(entities)
        }
      }
      Result.success(list)
    } catch (e: Exception) {
      val cached = cachedAnimeDao?.getAllCachedAnimeDirect().orEmpty()
      if (cached.isNotEmpty()) {
        val mapped = cached.map { it.toAnimeItem() }
        mapped.forEach { fullCatalogCache[it.id] = it }
        Result.success(mapped)
      } else {
        Result.failure(e)
      }
    }
  }

  override suspend fun searchAnimeDatabase(query: String): Result<List<AnimeItem>> = withContext(Dispatchers.IO) {
    val cleanQuery = query.trim()
    if (cleanQuery.isBlank()) {
      return@withContext getRecentAnime()
    }

    try {
      val (anikotoResults, globalResults) = coroutineScope {
        val anikotoDeferred = async {
          try {
            (1..5).map { pageNum ->
              async {
                try {
                  apiService.getRecentAnime(page = pageNum, perPage = 100).data.orEmpty()
                } catch (e: Exception) {
                  emptyList()
                }
              }
            }.awaitAll().flatten()
          } catch (e: Exception) {
            emptyList()
          }
        }

        val globalDeferred = async {
          if (aniListApiService != null) {
            try {
              val escaped = cleanQuery.replace("\"", "\\\"")
              val queryGql = "query{Page(perPage:25){media(search:\"$escaped\",type:ANIME,sort:SEARCH_MATCH){id idMal title{english romaji native} episodes description bannerImage coverImage{large extraLarge} genres averageScore seasonYear status}}}"
              val resp = aniListApiService.searchGlobalAnime(AniListQueryRequest(queryGql))
              resp.data?.page?.media.orEmpty().map { it.toAnimeItem() }
            } catch (e: Exception) {
              emptyList()
            }
          } else {
            emptyList()
          }
        }

        Pair(anikotoDeferred.await(), globalDeferred.await())
      }

      // Populate into catalog cache
      anikotoResults.forEach { fullCatalogCache[it.id] = it }
      globalResults.forEach { fullCatalogCache[it.id] = it }

      // Also incorporate existing Room database items
      val localCached = cachedAnimeDao?.getAllCachedAnimeDirect().orEmpty().map { it.toAnimeItem() }
      localCached.forEach { fullCatalogCache[it.id] = it }

      // Combined search across all sources: AniList global anime + Anikoto catalog + Room cache
      val combinedList = (globalResults + anikotoResults + localCached).distinctBy { it.id }

      val matched = combinedList.filter { anime ->
        anime.title.contains(cleanQuery, ignoreCase = true) ||
          (anime.alternative?.contains(cleanQuery, ignoreCase = true) == true) ||
          (anime.nativeTitle?.contains(cleanQuery, ignoreCase = true) == true) ||
          (anime.slug?.contains(cleanQuery, ignoreCase = true) == true) ||
          (anime.termsByType?.genre?.any { it.contains(cleanQuery, ignoreCase = true) } == true)
      }.sortedByDescending { anime ->
        when {
          anime.title.equals(cleanQuery, ignoreCase = true) -> 4
          anime.title.startsWith(cleanQuery, ignoreCase = true) -> 3
          anime.title.contains(cleanQuery, ignoreCase = true) -> 2
          else -> 1
        }
      }

      // Persist newly discovered items to Room database
      if (matched.isNotEmpty() && cachedAnimeDao != null) {
        val entities = matched.take(100).mapIndexed { index, item ->
          CachedAnimeEntity.fromAnimeItem(item, index)
        }
        cachedAnimeDao.insertAll(entities)
      }

      Result.success(matched)
    } catch (e: Exception) {
      val localCached = cachedAnimeDao?.getAllCachedAnimeDirect().orEmpty().map { it.toAnimeItem() }
      val matched = localCached.filter { anime ->
        anime.title.contains(cleanQuery, ignoreCase = true) ||
          (anime.alternative?.contains(cleanQuery, ignoreCase = true) == true) ||
          (anime.nativeTitle?.contains(cleanQuery, ignoreCase = true) == true)
      }
      if (matched.isNotEmpty()) {
        Result.success(matched)
      } else {
        Result.failure(e)
      }
    }
  }

  override suspend fun getSeriesDetails(id: Long): Result<SeriesDetailData> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getSeriesDetails(id)
      val data = response.data
      if (data != null && !data.episodes.isNullOrEmpty()) {
        return@withContext Result.success(data)
      }
    } catch (_: Exception) {
      // Fallback to synthesizing series details from catalog for global anime database entries
    }

    // Look up anime from full catalog cache or Room cache
    val anime = fullCatalogCache[id] ?: cachedAnimeDao?.getAllCachedAnimeDirect()
      ?.find { it.id == id }?.toAnimeItem()

    if (anime != null) {
      val malIdLong = anime.malId?.toLongOrNull() ?: id
      val count = (anime.episodesCount?.toIntOrNull() ?: 12).coerceIn(1, 1000)
      val generatedEpisodes = (1..count).map { epNum ->
        EpisodeItem(
          id = (id * 10000L) + epNum,
          title = "Episode $epNum",
          number = epNum.toDouble(),
          malId = malIdLong
        )
      }

      Result.success(
        SeriesDetailData(
          anime = anime,
          episodes = generatedEpisodes
        )
      )
    } else {
      Result.failure(IllegalStateException("No series details found for id $id"))
    }
  }

  override suspend fun getRelatedAnime(animeId: Long, title: String?): Result<List<AnimeItem>> = withContext(Dispatchers.IO) {
    val currentAnime = fullCatalogCache[animeId] ?: cachedAnimeDao?.getAllCachedAnimeDirect()?.find { it.id == animeId }?.toAnimeItem()
    val searchTitle = title ?: currentAnime?.title.orEmpty()
    val resultList = mutableListOf<AnimeItem>()

    if (searchTitle.isNotBlank() && aniListApiService != null) {
      try {
        val escaped = searchTitle.replace("\"", "\\\"")
        val queryGql = "query{Media(search:\"$escaped\",type:ANIME){id relations{edges{relationType node{id idMal title{english romaji native} episodes description bannerImage coverImage{large extraLarge} genres averageScore seasonYear status}}}}}"
        val response = aniListApiService.getMediaDetail(AniListQueryRequest(queryGql))
        val edges = response.data?.media?.relations?.edges.orEmpty()
        val apiRelated = edges.mapNotNull { it.node?.toAnimeItem() }.filter { it.id != animeId }
        apiRelated.forEach { fullCatalogCache[it.id] = it }
        resultList.addAll(apiRelated)
      } catch (_: Exception) {}
    }

    // Fallback: Franchise keyword search in catalog / search if API relations were empty
    if (resultList.isEmpty() && searchTitle.isNotBlank()) {
      val firstWord = searchTitle.split(" ").take(2).joinToString(" ").replace(Regex("[^a-zA-Z0-9 ]"), "").trim()
      if (firstWord.length >= 3) {
        val catalogMatches = fullCatalogCache.values.filter { anime ->
          anime.id != animeId && anime.title.contains(firstWord, ignoreCase = true)
        }
        resultList.addAll(catalogMatches)
      }
    }

    Result.success(resultList.distinctBy { it.id })
  }

  override suspend fun getRecommendedAnime(
    animeId: Long,
    title: String?,
    genres: List<String>?
  ): Result<List<AnimeItem>> = withContext(Dispatchers.IO) {
    val currentAnime = fullCatalogCache[animeId] ?: cachedAnimeDao?.getAllCachedAnimeDirect()?.find { it.id == animeId }?.toAnimeItem()
    val searchTitle = title ?: currentAnime?.title.orEmpty()
    val currentGenres = genres?.takeIf { it.isNotEmpty() } ?: currentAnime?.termsByType?.genre.orEmpty()
    val resultList = mutableListOf<AnimeItem>()

    if (searchTitle.isNotBlank() && aniListApiService != null) {
      try {
        val escaped = searchTitle.replace("\"", "\\\"")
        val queryGql = "query{Media(search:\"$escaped\",type:ANIME){id recommendations(perPage:15){nodes{mediaRecommendation{id idMal title{english romaji native} episodes description bannerImage coverImage{large extraLarge} genres averageScore seasonYear status}}}}}"
        val response = aniListApiService.getMediaDetail(AniListQueryRequest(queryGql))
        val nodes = response.data?.media?.recommendations?.nodes.orEmpty()
        val apiRecs = nodes.mapNotNull { it.mediaRecommendation?.toAnimeItem() }.filter { it.id != animeId }
        apiRecs.forEach { fullCatalogCache[it.id] = it }
        resultList.addAll(apiRecs)
      } catch (_: Exception) {}
    }

    // Fallback: Genre or Popular Catalog Matching
    if (resultList.isEmpty()) {
      val catalogMatches = fullCatalogCache.values.filter { anime ->
        anime.id != animeId && (currentGenres.isEmpty() || anime.termsByType?.genre?.any { g -> currentGenres.contains(g) } == true)
      }.sortedByDescending { it.score ?: "0" }
      resultList.addAll(catalogMatches)
    }

    // Secondary fallback: if catalog is sparse, fetch recent anime
    if (resultList.isEmpty()) {
      getRecentAnime().onSuccess { recent ->
        resultList.addAll(recent.filter { it.id != animeId })
      }
    }

    Result.success(resultList.distinctBy { it.id })
  }
}
