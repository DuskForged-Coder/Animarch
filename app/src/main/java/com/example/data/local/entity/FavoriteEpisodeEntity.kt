package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_episodes")
data class FavoriteEpisodeEntity(
  @PrimaryKey val episodeId: Long,
  val animeId: Long,
  val seriesTitle: String,
  val episodeNumber: String,
  val episodeTitle: String,
  val posterUrl: String?,
  val bookmarkedTimestamp: Long = System.currentTimeMillis()
)
