package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
  @PrimaryKey val episodeId: Long,
  val animeId: Long,
  val seriesTitle: String,
  val episodeNumber: String,
  val episodeTitle: String,
  val posterUrl: String?,
  val streamUrl: String,
  val streamType: String, // "SUB" or "DUB"
  val progressSeconds: Long = 0,
  val durationSeconds: Long = 1440, // standard anime episode ~24m
  val lastWatchedTimestamp: Long = System.currentTimeMillis()
) {
  val progressPercentage: Float
    get() = if (durationSeconds > 0) {
      (progressSeconds.toFloat() / durationSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

  val formattedProgress: String
    get() {
      val min = progressSeconds / 60
      val sec = progressSeconds % 60
      return String.format("%02d:%02d", min, sec)
    }
}
