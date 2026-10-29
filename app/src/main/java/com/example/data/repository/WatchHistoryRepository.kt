package com.example.data.repository

import com.example.data.local.dao.WatchHistoryDao
import com.example.data.local.entity.WatchHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface WatchHistoryRepository {
  val recentHistory: Flow<List<WatchHistoryEntity>>
  val uniqueAnimeCount: Flow<Int>
  val uniqueEpisodeCount: Flow<Int>
  val distinctAnimeIds: Flow<List<Long>>
  val distinctEpisodeIds: Flow<List<Long>>
  val totalWatchTimeSeconds: Flow<Long>
  val watchTimestamps: Flow<List<Long>>
  fun getHistoryForEpisode(episodeId: Long): Flow<WatchHistoryEntity?>
  suspend fun recordWatchProgress(
    episodeId: Long,
    animeId: Long,
    seriesTitle: String,
    episodeNumber: String,
    episodeTitle: String,
    posterUrl: String?,
    streamUrl: String,
    streamType: String,
    progressSeconds: Long = 0,
    durationSeconds: Long = 1440
  )
  suspend fun updateProgress(episodeId: Long, progressSeconds: Long)
  suspend fun removeHistory(episodeId: Long)
  suspend fun clearAllHistory()
}

class DefaultWatchHistoryRepository(
  private val watchHistoryDao: WatchHistoryDao
) : WatchHistoryRepository {

  override val recentHistory: Flow<List<WatchHistoryEntity>> =
    watchHistoryDao.getRecentHistory()

  override val uniqueAnimeCount: Flow<Int> =
    watchHistoryDao.getUniqueAnimeWatchedCount()

  override val uniqueEpisodeCount: Flow<Int> =
    watchHistoryDao.getUniqueEpisodesCompletedCount()

  override val distinctAnimeIds: Flow<List<Long>> =
    watchHistoryDao.getDistinctAnimeIds()

  override val distinctEpisodeIds: Flow<List<Long>> =
    watchHistoryDao.getDistinctEpisodeIds()

  override val totalWatchTimeSeconds: Flow<Long> =
    watchHistoryDao.getTotalWatchTimeSeconds()

  override val watchTimestamps: Flow<List<Long>> =
    watchHistoryDao.getWatchHistoryTimestamps()

  override fun getHistoryForEpisode(episodeId: Long): Flow<WatchHistoryEntity?> =
    watchHistoryDao.getHistoryForEpisode(episodeId)

  override suspend fun recordWatchProgress(
    episodeId: Long,
    animeId: Long,
    seriesTitle: String,
    episodeNumber: String,
    episodeTitle: String,
    posterUrl: String?,
    streamUrl: String,
    streamType: String,
    progressSeconds: Long,
    durationSeconds: Long
  ) = withContext(Dispatchers.IO) {
    val existing = watchHistoryDao.getHistoryForEpisodeDirect(episodeId)
    val effectiveProgress = if (progressSeconds > 0) progressSeconds else (existing?.progressSeconds ?: 0L)
    val entity = WatchHistoryEntity(
      episodeId = episodeId,
      animeId = animeId,
      seriesTitle = seriesTitle,
      episodeNumber = episodeNumber,
      episodeTitle = episodeTitle,
      posterUrl = posterUrl,
      streamUrl = streamUrl,
      streamType = streamType,
      progressSeconds = effectiveProgress,
      durationSeconds = if (durationSeconds > 0) durationSeconds else (existing?.durationSeconds ?: 1440L),
      lastWatchedTimestamp = System.currentTimeMillis()
    )
    watchHistoryDao.upsertWatchHistory(entity)
  }

  override suspend fun updateProgress(
    episodeId: Long,
    progressSeconds: Long
  ) = withContext(Dispatchers.IO) {
    watchHistoryDao.updateProgress(episodeId, progressSeconds)
  }

  override suspend fun removeHistory(episodeId: Long) = withContext(Dispatchers.IO) {
    watchHistoryDao.deleteHistory(episodeId)
  }

  override suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
    watchHistoryDao.clearAllHistory()
  }
}
