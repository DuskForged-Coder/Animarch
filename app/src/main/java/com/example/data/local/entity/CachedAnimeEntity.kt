package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AnimeItem
import com.example.data.model.TermsByType

@Entity(tableName = "cached_recent_anime")
data class CachedAnimeEntity(
  @PrimaryKey val id: Long,
  val title: String,
  val alternative: String?,
  val nativeTitle: String?,
  val slug: String?,
  val rating: String?,
  val poster: String?,
  val isDub: Int?,
  val isSub: Int?,
  val description: String?,
  val aired: String?,
  val season: String?,
  val year: Int?,
  val duration: String?,
  val status: String?,
  val score: String?,
  val episodesCount: String?,
  val backgroundImage: String?,
  val genresCommaSeparated: String?,
  val sortOrder: Int,
  val cachedAt: Long = System.currentTimeMillis()
) {
  fun toAnimeItem(): AnimeItem {
    val genreList = genresCommaSeparated?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
    return AnimeItem(
      id = id,
      title = title,
      alternative = alternative,
      nativeTitle = nativeTitle,
      slug = slug,
      rating = rating,
      poster = poster,
      isDub = isDub,
      isSub = isSub,
      description = description,
      aired = aired,
      season = season,
      year = year,
      duration = duration,
      status = status,
      score = score,
      episodesCount = episodesCount,
      backgroundImage = backgroundImage,
      termsByType = TermsByType(genre = genreList)
    )
  }

  companion object {
    fun fromAnimeItem(item: AnimeItem, sortIndex: Int): CachedAnimeEntity {
      return CachedAnimeEntity(
        id = item.id,
        title = item.title,
        alternative = item.alternative,
        nativeTitle = item.nativeTitle,
        slug = item.slug,
        rating = item.rating,
        poster = item.poster,
        isDub = item.isDub,
        isSub = item.isSub,
        description = item.description,
        aired = item.aired,
        season = item.season,
        year = item.year,
        duration = item.duration,
        status = item.status,
        score = item.score,
        episodesCount = item.episodesCount,
        backgroundImage = item.backgroundImage,
        genresCommaSeparated = item.termsByType?.genre?.joinToString(","),
        sortOrder = sortIndex
      )
    }
  }
}
