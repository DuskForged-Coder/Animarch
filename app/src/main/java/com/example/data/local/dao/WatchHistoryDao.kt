package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {

  @Query("SELECT * FROM watch_history ORDER BY lastWatchedTimestamp DESC LIMIT :limit")
  fun getRecentHistory(limit: Int = 50): Flow<List<WatchHistoryEntity>>

  @Query("SELECT * FROM watch_history WHERE episodeId = :episodeId LIMIT 1")
  fun getHistoryForEpisode(episodeId: Long): Flow<WatchHistoryEntity?>

  @Query("SELECT * FROM watch_history WHERE episodeId = :episodeId LIMIT 1")
  suspend fun getHistoryForEpisodeDirect(episodeId: Long): WatchHistoryEntity?

  @Query("SELECT COUNT(DISTINCT animeId) FROM watch_history")
  fun getUniqueAnimeWatchedCount(): Flow<Int>

  @Query("SELECT COUNT(DISTINCT episodeId) FROM watch_history")
  fun getUniqueEpisodesCompletedCount(): Flow<Int>

  @Query("SELECT DISTINCT animeId FROM watch_history")
  fun getDistinctAnimeIds(): Flow<List<Long>>

  @Query("SELECT DISTINCT episodeId FROM watch_history")
  fun getDistinctEpisodeIds(): Flow<List<Long>>

  @Query("SELECT COALESCE(SUM(progressSeconds), 0) FROM watch_history")
  fun getTotalWatchTimeSeconds(): Flow<Long>

  @Query("SELECT lastWatchedTimestamp FROM watch_history ORDER BY lastWatchedTimestamp DESC")
  fun getWatchHistoryTimestamps(): Flow<List<Long>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertWatchHistory(history: WatchHistoryEntity)

  @Query("UPDATE watch_history SET progressSeconds = :progressSeconds, lastWatchedTimestamp = :timestamp WHERE episodeId = :episodeId")
  suspend fun updateProgress(episodeId: Long, progressSeconds: Long, timestamp: Long = System.currentTimeMillis())

  @Query("DELETE FROM watch_history WHERE episodeId = :episodeId")
  suspend fun deleteHistory(episodeId: Long)

  @Query("DELETE FROM watch_history")
  suspend fun clearAllHistory()
}
