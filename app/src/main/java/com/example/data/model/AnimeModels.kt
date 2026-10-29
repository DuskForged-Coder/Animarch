package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecentAnimeResponse(
  @Json(name = "ok") val ok: Boolean? = null,
  @Json(name = "anikoto_domains") val domains: List<String>? = null,
  @Json(name = "data") val data: List<AnimeItem>? = null
)

@JsonClass(generateAdapter = true)
data class SeriesDetailResponse(
  @Json(name = "ok") val ok: Boolean? = null,
  @Json(name = "anikoto_domains") val domains: List<String>? = null,
  @Json(name = "data") val data: SeriesDetailData? = null
)

@JsonClass(generateAdapter = true)
data class SeriesDetailData(
  @Json(name = "anime") val anime: AnimeItem? = null,
  @Json(name = "episodes") val episodes: List<EpisodeItem>? = null
)

@JsonClass(generateAdapter = true)
data class AnimeItem(
  @Json(name = "id") val id: Long,
  @Json(name = "title") val title: String,
  @Json(name = "alternative") val alternative: String? = null,
  @Json(name = "native") val nativeTitle: String? = null,
  @Json(name = "slug") val slug: String? = null,
  @Json(name = "rating") val rating: String? = null,
  @Json(name = "poster") val poster: String? = null,
  @Json(name = "is_dub") val isDub: Int? = null,
  @Json(name = "is_sub") val isSub: Int? = null,
  @Json(name = "description") val description: String? = null,
  @Json(name = "aired") val aired: String? = null,
  @Json(name = "season") val season: String? = null,
  @Json(name = "year") val year: Int? = null,
  @Json(name = "duration") val duration: String? = null,
  @Json(name = "status") val status: String? = null,
  @Json(name = "score") val score: String? = null,
  @Json(name = "episodes") val episodesCount: String? = null,
  @Json(name = "background_image") val backgroundImage: String? = null,
  @Json(name = "terms_by_type") val termsByType: TermsByType? = null,
  @Json(name = "mal_id") val malId: String? = null,
  @Json(name = "ani_id") val aniId: String? = null
)

@JsonClass(generateAdapter = true)
data class TermsByType(
  @Json(name = "genre") val genre: List<String>? = null,
  @Json(name = "producers") val producers: List<String>? = null,
  @Json(name = "studios") val studios: List<String>? = null,
  @Json(name = "type") val type: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class EpisodeItem(
  @Json(name = "id") val id: Long,
  @Json(name = "title") val title: String? = null,
  @Json(name = "jp_title") val jpTitle: String? = null,
  @Json(name = "number") val number: Double? = null,
  @Json(name = "episode_embed_id") val episodeEmbedId: String? = null,
  @Json(name = "embed_url") val embedUrl: EmbedUrl? = null,
  @Json(name = "updated_at") val updatedAt: String? = null,
  @Json(name = "mal_id") val malId: Long? = null
) {
  fun displayEpisodeNumber(): String {
    val num = number ?: 1.0
    return if (num % 1.0 == 0.0) {
      num.toInt().toString()
    } else {
      num.toString()
    }
  }

  fun resolveStreamUrl(streamType: StreamType): String {
    val suffix = if (streamType == StreamType.DUB) "dub" else "sub"
    val directUrl = if (streamType == StreamType.DUB) embedUrl?.dub else embedUrl?.sub
    if (!directUrl.isNullOrBlank()) {
      return directUrl
    }
    if (malId != null && malId > 0) {
      val epNum = (number ?: 1.0).toInt().coerceAtLeast(1)
      return "https://megaplay.buzz/stream/mal/$malId/$epNum/$suffix"
    }
    val embedId = episodeEmbedId ?: id.toString()
    return "https://megaplay.buzz/stream/s-2/$embedId/$suffix"
  }
}

@JsonClass(generateAdapter = true)
data class EmbedUrl(
  @Json(name = "sub") val sub: String? = null,
  @Json(name = "dub") val dub: String? = null
)

enum class StreamType(val label: String) {
  SUB("SUB"),
  DUB("DUB")
}

// ── AniList Global Anime Database Models ──────────────────────────────────────

@JsonClass(generateAdapter = true)
data class AniListQueryRequest(
  @Json(name = "query") val query: String
)

@JsonClass(generateAdapter = true)
data class AniListSearchResponse(
  @Json(name = "data") val data: AniListResponseData? = null
)

@JsonClass(generateAdapter = true)
data class AniListResponseData(
  @Json(name = "Page") val page: AniListPageData? = null
)

@JsonClass(generateAdapter = true)
data class AniListPageData(
  @Json(name = "media") val media: List<AniListMediaItem>? = null
)

@JsonClass(generateAdapter = true)
data class AniListMediaItem(
  @Json(name = "id") val id: Long,
  @Json(name = "idMal") val idMal: Long? = null,
  @Json(name = "title") val title: AniListTitle? = null,
  @Json(name = "episodes") val episodes: Int? = null,
  @Json(name = "description") val description: String? = null,
  @Json(name = "bannerImage") val bannerImage: String? = null,
  @Json(name = "coverImage") val coverImage: AniListCoverImage? = null,
  @Json(name = "genres") val genres: List<String>? = null,
  @Json(name = "averageScore") val averageScore: Int? = null,
  @Json(name = "seasonYear") val seasonYear: Int? = null,
  @Json(name = "status") val status: String? = null
) {
  fun toAnimeItem(): AnimeItem {
    val displayTitle = title?.english ?: title?.romaji ?: "Anime"
    val scoreStr = averageScore?.let { String.format(java.util.Locale.US, "%.1f", it / 10.0) } ?: "8.0"
    val cleanDesc = description?.replace(Regex("<[^>]*>"), "")?.trim()
    return AnimeItem(
      id = idMal ?: id,
      title = displayTitle,
      alternative = title?.romaji,
      nativeTitle = title?.native,
      slug = displayTitle.lowercase().replace(Regex("[^a-z0-9]+"), "-"),
      rating = "PG-13",
      poster = coverImage?.extraLarge ?: coverImage?.large,
      isDub = 1,
      isSub = 1,
      description = cleanDesc,
      aired = seasonYear?.toString(),
      season = null,
      year = seasonYear,
      duration = "24m",
      status = status ?: "Finished",
      score = scoreStr,
      episodesCount = (episodes ?: 12).toString(),
      backgroundImage = bannerImage ?: coverImage?.extraLarge,
      termsByType = TermsByType(genre = genres),
      malId = (idMal ?: id).toString(),
      aniId = id.toString()
    )
  }
}

@JsonClass(generateAdapter = true)
data class AniListTitle(
  @Json(name = "english") val english: String? = null,
  @Json(name = "romaji") val romaji: String? = null,
  @Json(name = "native") val native: String? = null
)

@JsonClass(generateAdapter = true)
data class AniListCoverImage(
  @Json(name = "large") val large: String? = null,
  @Json(name = "extraLarge") val extraLarge: String? = null
)

@JsonClass(generateAdapter = true)
data class AniListMediaDetailResponse(
  @Json(name = "data") val data: AniListMediaDetailData? = null
)

@JsonClass(generateAdapter = true)
data class AniListMediaDetailData(
  @Json(name = "Media") val media: AniListMediaDetail? = null
)

@JsonClass(generateAdapter = true)
data class AniListMediaDetail(
  @Json(name = "id") val id: Long,
  @Json(name = "relations") val relations: AniListRelations? = null,
  @Json(name = "recommendations") val recommendations: AniListRecommendations? = null
)

@JsonClass(generateAdapter = true)
data class AniListRelations(
  @Json(name = "edges") val edges: List<AniListRelationEdge>? = null
)

@JsonClass(generateAdapter = true)
data class AniListRelationEdge(
  @Json(name = "relationType") val relationType: String? = null,
  @Json(name = "node") val node: AniListMediaItem? = null
)

@JsonClass(generateAdapter = true)
data class AniListRecommendations(
  @Json(name = "nodes") val nodes: List<AniListRecommendationNode>? = null
)

@JsonClass(generateAdapter = true)
data class AniListRecommendationNode(
  @Json(name = "mediaRecommendation") val mediaRecommendation: AniListMediaItem? = null
)

