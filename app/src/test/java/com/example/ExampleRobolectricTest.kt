package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.AniListApiService
import com.example.data.api.AnikotoApiService
import com.example.data.local.AnimarchDatabase
import com.example.data.local.entity.CachedAnimeEntity
import com.example.data.model.AniListMediaItem
import com.example.data.model.AniListPageData
import com.example.data.model.AniListQueryRequest
import com.example.data.model.AniListResponseData
import com.example.data.model.AniListSearchResponse
import com.example.data.model.AniListTitle
import com.example.data.model.AnimeItem
import com.example.data.model.EmbedUrl
import com.example.data.model.EpisodeItem
import com.example.data.model.RecentAnimeResponse
import com.example.data.model.SeriesDetailResponse
import com.example.data.model.StreamType
import com.example.data.repository.DefaultAnimeRepository
import com.example.data.repository.DefaultFavoritesRepository
import com.example.data.repository.DefaultWatchHistoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var db: AnimarchDatabase

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AnimarchDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Animarch", appName)
  }

  @Test
  fun `episode model resolves correct sub and dub stream URLs`() {
    val episode = EpisodeItem(
      id = 1234,
      title = "Episode 1",
      number = 1.0,
      episodeEmbedId = "998877",
      embedUrl = EmbedUrl(
        sub = "https://megaplay.buzz/stream/s-2/998877/sub",
        dub = "https://megaplay.buzz/stream/s-2/998877/dub"
      )
    )

    assertEquals("1", episode.displayEpisodeNumber())
    assertEquals(
      "https://megaplay.buzz/stream/s-2/998877/sub",
      episode.resolveStreamUrl(StreamType.SUB)
    )
    assertEquals(
      "https://megaplay.buzz/stream/s-2/998877/dub",
      episode.resolveStreamUrl(StreamType.DUB)
    )
  }

  @Test
  fun `episode model resolves stream URL from malId`() {
    val episode = EpisodeItem(
      id = 5555,
      title = "Episode 1",
      number = 1.0,
      malId = 20L
    )

    assertEquals(
      "https://megaplay.buzz/stream/mal/20/1/sub",
      episode.resolveStreamUrl(StreamType.SUB)
    )
    assertEquals(
      "https://megaplay.buzz/stream/mal/20/1/dub",
      episode.resolveStreamUrl(StreamType.DUB)
    )
  }

  @Test
  fun `watch history repository saves and updates watch progress in Room`() = runBlocking {
    val watchHistoryRepo = DefaultWatchHistoryRepository(db.watchHistoryDao())

    watchHistoryRepo.recordWatchProgress(
      episodeId = 101L,
      animeId = 2001L,
      seriesTitle = "Jujutsu Kaisen",
      episodeNumber = "1",
      episodeTitle = "Ryomen Sukuna",
      posterUrl = "https://example.com/poster.jpg",
      streamUrl = "https://example.com/stream/101",
      streamType = "SUB",
      progressSeconds = 300L,
      durationSeconds = 1440L
    )

    val historyList = watchHistoryRepo.recentHistory.first()
    assertEquals(1, historyList.size)
    assertEquals("Jujutsu Kaisen", historyList[0].seriesTitle)
    assertEquals(300L, historyList[0].progressSeconds)
    assertEquals("05:00", historyList[0].formattedProgress)

    // Update playback progress
    watchHistoryRepo.updateProgress(101L, 600L)
    val updated = watchHistoryRepo.getHistoryForEpisode(101L).first()
    assertNotNull(updated)
    assertEquals(600L, updated?.progressSeconds)

    // Remove history item
    watchHistoryRepo.removeHistory(101L)
    val emptyList = watchHistoryRepo.recentHistory.first()
    assertTrue(emptyList.isEmpty())
  }

  @Test
  fun `favorites repository persists favorite episodes in Room`() = runBlocking {
    val favoritesRepo = DefaultFavoritesRepository(db.favoriteEpisodeDao())

    assertFalse(favoritesRepo.isEpisodeFavoritedDirect(999L))

    favoritesRepo.toggleFavoriteEpisode(
      episodeId = 999L,
      animeId = 505L,
      seriesTitle = "Chainsaw Man",
      episodeNumber = "12",
      episodeTitle = "Katana vs Chainsaw",
      posterUrl = "https://example.com/csm.jpg"
    )

    assertTrue(favoritesRepo.isEpisodeFavoritedDirect(999L))
    val favList = favoritesRepo.allFavoriteEpisodes.first()
    assertEquals(1, favList.size)
    assertEquals("Chainsaw Man", favList[0].seriesTitle)

    // Toggle again removes favorite
    favoritesRepo.toggleFavoriteEpisode(
      episodeId = 999L,
      animeId = 505L,
      seriesTitle = "Chainsaw Man",
      episodeNumber = "12",
      episodeTitle = "Katana vs Chainsaw",
      posterUrl = "https://example.com/csm.jpg"
    )
    assertFalse(favoritesRepo.isEpisodeFavoritedDirect(999L))
  }

  @Test
  fun `anime repository falls back to Room cached data on network error`() = runBlocking {
    val cachedDao = db.cachedAnimeDao()

    val cachedItem = CachedAnimeEntity(
      id = 777L,
      title = "Attack on Titan",
      alternative = "Shingeki no Kyojin",
      nativeTitle = "進撃の巨人",
      slug = "aot",
      rating = "R",
      poster = "https://example.com/aot.jpg",
      isDub = 1,
      isSub = 1,
      description = "Titans attack",
      aired = "2013",
      season = "Spring",
      year = 2013,
      duration = "24m",
      status = "Finished",
      score = "9.0",
      episodesCount = "25",
      backgroundImage = null,
      genresCommaSeparated = "Action,Drama",
      sortOrder = 0
    )
    cachedDao.insertAll(listOf(cachedItem))

    val failingApi = object : AnikotoApiService {
      override suspend fun getRecentAnime(page: Int, perPage: Int): RecentAnimeResponse {
        throw IOException("Simulated network connection timeout")
      }
      override suspend fun getSeriesDetails(id: Long): SeriesDetailResponse {
        throw IOException("Network error")
      }
    }

    val repository = DefaultAnimeRepository(
      apiService = failingApi,
      cachedAnimeDao = cachedDao,
      favoriteEpisodeDao = db.favoriteEpisodeDao()
    )

    val result = repository.getRecentAnime()
    assertTrue(result.isSuccess)
    val list = result.getOrNull()
    assertNotNull(list)
    assertEquals(1, list?.size)
    assertEquals("Attack on Titan", list?.get(0)?.title)
    assertEquals("9.0", list?.get(0)?.score)
  }

  @Test
  fun `anime repository searchAnimeDatabase queries and returns matching anime from API`() = runBlocking {
    val animeItem = AnimeItem(
      id = 888L,
      title = "As a Reincarnated Aristocrat, I'll Use My Appraisal Skill to Rise in the World",
      score = "8.2"
    )
    val mockApi = object : AnikotoApiService {
      override suspend fun getRecentAnime(page: Int, perPage: Int): RecentAnimeResponse {
        return RecentAnimeResponse(ok = true, data = listOf(animeItem))
      }
      override suspend fun getSeriesDetails(id: Long): SeriesDetailResponse = SeriesDetailResponse(ok = true)
    }

    val repository = DefaultAnimeRepository(
      apiService = mockApi,
      cachedAnimeDao = db.cachedAnimeDao(),
      favoriteEpisodeDao = db.favoriteEpisodeDao()
    )

    val searchResult = repository.searchAnimeDatabase("Aristocrat")
    assertTrue(searchResult.isSuccess)
    val list = searchResult.getOrNull()
    assertNotNull(list)
    assertEquals(1, list?.size)
    assertTrue(list?.get(0)?.title?.contains("Aristocrat") == true)
  }

  @Test
  fun `anime repository searchAnimeDatabase queries and returns global anime from AniList`() = runBlocking {
    val mockAniList = object : AniListApiService {
      override suspend fun searchGlobalAnime(request: AniListQueryRequest): AniListSearchResponse {
        val media = listOf(
          AniListMediaItem(
            id = 20L,
            idMal = 20L,
            title = AniListTitle(english = "Naruto", romaji = "NARUTO"),
            episodes = 220,
            averageScore = 80
          )
        )
        return AniListSearchResponse(AniListResponseData(AniListPageData(media)))
      }
    }
    val emptyApi = object : AnikotoApiService {
      override suspend fun getRecentAnime(page: Int, perPage: Int): RecentAnimeResponse = RecentAnimeResponse(ok = true, data = emptyList())
      override suspend fun getSeriesDetails(id: Long): SeriesDetailResponse = SeriesDetailResponse(ok = true)
    }

    val repository = DefaultAnimeRepository(
      apiService = emptyApi,
      aniListApiService = mockAniList,
      cachedAnimeDao = db.cachedAnimeDao(),
      favoriteEpisodeDao = db.favoriteEpisodeDao()
    )

    val searchResult = repository.searchAnimeDatabase("Naruto")
    assertTrue(searchResult.isSuccess)
    val list = searchResult.getOrNull()
    assertNotNull(list)
    assertEquals(1, list?.size)
    assertEquals("Naruto", list?.get(0)?.title)
    assertEquals("20", list?.get(0)?.malId)

    // Verify series details fallback generation for global anime
    val detailsResult = repository.getSeriesDetails(20L)
    assertTrue(detailsResult.isSuccess)
    val seriesData = detailsResult.getOrNull()
    assertNotNull(seriesData)
    assertEquals(220, seriesData?.episodes?.size)
    assertEquals("https://megaplay.buzz/stream/mal/20/1/sub", seriesData?.episodes?.get(0)?.resolveStreamUrl(StreamType.SUB))
  }
}
