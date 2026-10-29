package com.example.data.model

data class StructuredIntent(
  val genres: List<String> = emptyList(),
  val moods: List<String> = emptyList(),
  val maxEpisodes: Int? = null,
  val minYear: Int? = null,
  val maxYear: Int? = null,
  val format: String? = null,
  val audioPreference: String? = null,
  val excludedGenres: List<String> = emptyList(),
  val rawQuery: String = ""
)

data class RankingScore(
  val intentMatch: Float = 0f,
  val tasteMatch: Float = 0f,
  val globalTrend: Float = 0f,
  val freshness: Float = 0f,
  val discovery: Float = 0f,
  val totalScore: Float = 0f
)

data class AiRecommendationResult(
  val anime: AnimeItem,
  val matchExplanation: String,
  val score: RankingScore,
  val appliedTags: List<String>
)

enum class MessageSender {
  USER, AI
}

data class ChatMessage(
  val id: String,
  val sender: MessageSender,
  val text: String,
  val recommendations: List<AiRecommendationResult> = emptyList(),
  val structuredFilterChips: List<String> = emptyList(),
  val timestamp: Long = System.currentTimeMillis()
)
