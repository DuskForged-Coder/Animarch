package com.example.data.repository

import com.example.data.local.dao.FavoriteEpisodeDao
import com.example.data.local.entity.FavoriteEpisodeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface FavoritesRepository {
  val allFavoriteEpisodes: Flow<List<FavoriteEpisodeEntity>>
  val favoriteAnimeIds: Flow<List<Long>>
  fun isEpisodeFavorited(episodeId: Long): Flow<Boolean>
  suspend fun isEpisodeFavoritedDirect(episodeId: Long): Boolean
  suspend fun toggleFavoriteEpisode(
    episodeId: Long,
    animeId: Long,
    seriesTitle: String,
    episodeNumber: String,
    episodeTitle: String,
    posterUrl: String?
  )
  suspend fun removeFavorite(episodeId: Long)
}

class DefaultFavoritesRepository(
  private val favoriteEpisodeDao: FavoriteEpisodeDao
) : FavoritesRepository {

  override val allFavoriteEpisodes: Flow<List<FavoriteEpisodeEntity>> =
    favoriteEpisodeDao.getAllFavorites()

  override val favoriteAnimeIds: Flow<List<Long>> =
    favoriteEpisodeDao.getFavoriteAnimeIds()

  override fun isEpisodeFavorited(episodeId: Long): Flow<Boolean> =
    favoriteEpisodeDao.isEpisodeFavorited(episodeId)

  override suspend fun isEpisodeFavoritedDirect(episodeId: Long): Boolean =
    withContext(Dispatchers.IO) {
      favoriteEpisodeDao.isFavoriteDirect(episodeId)
    }

  override suspend fun toggleFavoriteEpisode(
    episodeId: Long,
    animeId: Long,
    seriesTitle: String,
    episodeNumber: String,
    episodeTitle: String,
    posterUrl: String?
  ) = withContext(Dispatchers.IO) {
    if (favoriteEpisodeDao.isFavoriteDirect(episodeId)) {
      favoriteEpisodeDao.deleteFavorite(episodeId)
    } else {
      val entity = FavoriteEpisodeEntity(
        episodeId = episodeId,
        animeId = animeId,
        seriesTitle = seriesTitle,
        episodeNumber = episodeNumber,
        episodeTitle = episodeTitle,
        posterUrl = posterUrl,
        bookmarkedTimestamp = System.currentTimeMillis()
      )
      favoriteEpisodeDao.insertFavorite(entity)
    }
  }

  override suspend fun removeFavorite(episodeId: Long) = withContext(Dispatchers.IO) {
    favoriteEpisodeDao.deleteFavorite(episodeId)
  }
}
