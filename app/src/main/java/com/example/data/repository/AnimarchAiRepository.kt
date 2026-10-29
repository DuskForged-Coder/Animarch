package com.example.data.repository

import com.example.data.model.AiRecommendationResult
import com.example.data.model.AnimeItem
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.model.RankingScore
import com.example.data.model.StructuredIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

interface AnimarchAiRepository {
  val chatHistory: StateFlow<List<ChatMessage>>
  suspend fun processQuery(query: String, animeRepository: AnimeRepository): ChatMessage
  fun parseIntent(query: String): StructuredIntent
  fun clearHistory()
}

class DefaultAnimarchAiRepository : AnimarchAiRepository {

  private val _chatHistory = MutableStateFlow<List<ChatMessage>>(
    listOf(
      ChatMessage(
        id = "init_msg",
        sender = MessageSender.AI,
        text = "Welcome to ANIMARCH AI! Ask me for anime recommendations using natural language, like: 'Recommend a dark psychological thriller released from 2024 onward with no more than 24 episodes'."
      )
    )
  )
  override val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

  override fun clearHistory() {
    _chatHistory.value = listOf(
      ChatMessage(
        id = "init_msg",
        sender = MessageSender.AI,
        text = "Chat reset. How can I help you discover anime today?"
      )
    )
  }

  override fun parseIntent(query: String): StructuredIntent {
    val q = query.lowercase()

    // 1. Max Episodes Extraction
    val maxEpisodes = when {
      q.contains("12 episodes") || q.contains("12 eps") || q.contains("under 12") -> 12
      q.contains("24 episodes") || q.contains("24 eps") || q.contains("no more than 24") || q.contains("under 24") -> 24
      q.contains("13 episodes") || q.contains("13 eps") -> 13
      q.contains("50 episodes") || q.contains("50 eps") -> 50
      else -> null
    }

    // 2. Year Boundary Extraction
    val minYear = when {
      q.contains("2024") || q.contains("2024 onward") || q.contains("after 2023") -> 2024
      q.contains("2023") || q.contains("after 2022") -> 2023
      q.contains("2022") -> 2022
      q.contains("2025") -> 2025
      else -> null
    }

    // 3. Genre & Mood Extraction
    val genres = mutableListOf<String>()
    if (q.contains("psychological")) genres.add("Psychological")
    if (q.contains("thriller")) genres.add("Thriller")
    if (q.contains("action")) genres.add("Action")
    if (q.contains("fantasy")) genres.add("Fantasy")
    if (q.contains("sci-fi") || q.contains("scifi")) genres.add("Sci-Fi")
    if (q.contains("comedy") || q.contains("funny")) genres.add("Comedy")
    if (q.contains("romance")) genres.add("Romance")
    if (q.contains("drama")) genres.add("Drama")
    if (q.contains("mystery")) genres.add("Mystery")

    // 4. Excluded Genres
    val excluded = mutableListOf<String>()
    if (q.contains("no romance") || q.contains("without romance")) excluded.add("Romance")
    if (q.contains("no mecha")) excluded.add("Mecha")
    if (q.contains("no ecchi")) excluded.add("Ecchi")

    return StructuredIntent(
      genres = genres,
      moods = if (q.contains("dark")) listOf("Dark") else emptyList(),
      maxEpisodes = maxEpisodes,
      minYear = minYear,
      excludedGenres = excluded,
      rawQuery = query
    )
  }

  override suspend fun processQuery(
    query: String,
    animeRepository: AnimeRepository
  ): ChatMessage = withContext(Dispatchers.IO) {
    val userMsg = ChatMessage(
      id = "user_${System.currentTimeMillis()}",
      sender = MessageSender.USER,
      text = query
    )
    val updatedHistory = _chatHistory.value.toMutableList()
    updatedHistory.add(userMsg)
    _chatHistory.value = updatedHistory

    val intent = parseIntent(query)

    // Fetch full anime catalog candidates
    val rawListResult = animeRepository.searchAnimeDatabase(intent.genres.firstOrNull() ?: query)
    val candidates = rawListResult.getOrDefault(emptyList())

    // STEP 1: Hard Constraint Filtering (Episode limit, Year, Excluded genres)
    val eligible = candidates.filter { anime ->
      val epCount = anime.episodesCount?.toIntOrNull()
      val satisfiesMaxEps = if (intent.maxEpisodes != null && epCount != null) {
        epCount <= intent.maxEpisodes
      } else true

      val animeYear = anime.year ?: 2024
      val satisfiesYear = if (intent.minYear != null) {
        animeYear >= intent.minYear
      } else true

      val animeGenres = anime.termsByType?.genre.orEmpty()
      val satisfiesExclusions = if (intent.excludedGenres.isNotEmpty()) {
        animeGenres.none { g -> intent.excludedGenres.any { ex -> ex.equals(g, ignoreCase = true) } }
      } else true

      satisfiesMaxEps && satisfiesYear && satisfiesExclusions
    }

    // STEP 2: Normalized Ranking Model (50% Intent, 25% Taste, 10% Trend, 10% Freshness, 5% Discovery)
    val rankedResults = eligible.map { anime ->
      val epCount = anime.episodesCount?.toIntOrNull() ?: 12
      val animeGenres = anime.termsByType?.genre.orEmpty()

      // Intent Match Score
      var genreMatches = 0
      intent.genres.forEach { reqG ->
        if (animeGenres.any { it.contains(reqG, ignoreCase = true) }) genreMatches++
      }
      val intentScore = if (intent.genres.isNotEmpty()) (genreMatches.toFloat() / intent.genres.size) else 0.85f

      // Taste Score
      val isBookmarked = animeRepository.isBookmarked(anime.id)
      val tasteScore = if (isBookmarked) 0.95f else 0.75f

      // Global Trend / Rating Score
      val rawScore = anime.score?.toFloatOrNull() ?: 7.5f
      val trendScore = (rawScore / 10.0f).coerceIn(0f, 1f)

      // Freshness
      val year = anime.year ?: 2024
      val freshnessScore = if (year >= 2024) 1.0f else 0.70f

      // Discovery
      val discoveryScore = 0.80f

      // Weighted Total Score (0 - 100)
      val total = 100f * (0.50f * intentScore + 0.25f * tasteScore + 0.10f * trendScore + 0.10f * freshnessScore + 0.05f * discoveryScore)

      val scoreObj = RankingScore(
        intentMatch = intentScore,
        tasteMatch = tasteScore,
        globalTrend = trendScore,
        freshness = freshnessScore,
        discovery = discoveryScore,
        totalScore = total
      )

      // Verified Data-Backed Match Explanation
      val explanationParts = mutableListOf<String>()
      if (animeGenres.isNotEmpty()) {
        explanationParts.add("Matches ${animeGenres.take(2).joinToString(" & ")} preference")
      }
      if (intent.maxEpisodes != null) {
        explanationParts.add("Has $epCount episodes (within your ${intent.maxEpisodes} limit)")
      }
      if (anime.year != null) {
        explanationParts.add("${anime.year} release")
      }

      val explanation = explanationParts.joinToString(" • ").ifBlank { "Matches your current anime search" }

      AiRecommendationResult(
        anime = anime,
        matchExplanation = explanation,
        score = scoreObj,
        appliedTags = animeGenres.take(3)
      )
    }.sortedByDescending { it.score.totalScore }

    val filterChips = mutableListOf<String>()
    intent.genres.forEach { filterChips.add(it) }
    if (intent.maxEpisodes != null) filterChips.add("≤${intent.maxEpisodes} eps")
    if (intent.minYear != null) filterChips.add("${intent.minYear}+")

    val responseText = if (rankedResults.isNotEmpty()) {
      "I found ${rankedResults.size} anime matching your criteria! Here are the best matches ranked by relevance:"
    } else {
      "No anime in the catalogue matched all mandatory filters. Try relaxing the episode limit or release year!"
    }

    val aiMsg = ChatMessage(
      id = "ai_${System.currentTimeMillis()}",
      sender = MessageSender.AI,
      text = responseText,
      recommendations = rankedResults.take(6),
      structuredFilterChips = filterChips
    )

    val finalHistory = _chatHistory.value.toMutableList()
    finalHistory.add(aiMsg)
    _chatHistory.value = finalHistory

    aiMsg
  }
}
