package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AnimeItem
import com.example.data.model.EpisodeItem
import com.example.data.repository.AnimeRepository
import com.example.data.repository.FavoritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DetailUiState {
  data object Loading : DetailUiState
  data class Success(
    val anime: AnimeItem,
    val episodes: List<EpisodeItem>
  ) : DetailUiState
  data class Error(val message: String) : DetailUiState
}

class DetailViewModel(
  private val animeId: Long,
  private val repository: AnimeRepository,
  private val favoritesRepository: FavoritesRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
  val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

  private val _isAscending = MutableStateFlow(true)
  val isAscending: StateFlow<Boolean> = _isAscending.asStateFlow()

  private val _episodeFilterQuery = MutableStateFlow("")
  val episodeFilterQuery: StateFlow<String> = _episodeFilterQuery.asStateFlow()

  val bookmarks: StateFlow<Set<Long>> = repository.bookmarksFlow
  val watchStatusMap: StateFlow<Map<Long, com.example.data.repository.WatchStatus>> = repository.watchStatusMapFlow

  val favoriteEpisodeIds: StateFlow<Set<Long>> = favoritesRepository.allFavoriteEpisodes
    .map { list -> list.map { it.episodeId }.toSet() }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptySet()
    )

  private val _rawEpisodes = MutableStateFlow<List<EpisodeItem>>(emptyList())

  val processedEpisodes: StateFlow<List<EpisodeItem>> = combine(
    _rawEpisodes,
    _isAscending,
    _episodeFilterQuery
  ) { episodes, asc, query ->
    val filtered = if (query.isBlank()) {
      episodes
    } else {
      episodes.filter { ep ->
        val epNum = ep.displayEpisodeNumber()
        val epTitle = ep.title.orEmpty()
        epNum.contains(query, ignoreCase = true) || epTitle.contains(query, ignoreCase = true)
      }
    }

    if (asc) {
      filtered.sortedBy { it.number ?: 0.0 }
    } else {
      filtered.sortedByDescending { it.number ?: 0.0 }
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  private val _relatedAnime = MutableStateFlow<List<AnimeItem>>(emptyList())
  val relatedAnime: StateFlow<List<AnimeItem>> = _relatedAnime.asStateFlow()

  private val _recommendedAnime = MutableStateFlow<List<AnimeItem>>(emptyList())
  val recommendedAnime: StateFlow<List<AnimeItem>> = _recommendedAnime.asStateFlow()

  init {
    loadSeriesDetails()
  }

  fun loadSeriesDetails() {
    viewModelScope.launch {
      _uiState.value = DetailUiState.Loading
      val result = repository.getSeriesDetails(animeId)
      result.fold(
        onSuccess = { data ->
          val anime = data.anime
          val epList = data.episodes.orEmpty()
          if (anime != null) {
            _rawEpisodes.value = epList
            _uiState.value = DetailUiState.Success(
              anime = anime,
              episodes = epList
            )

            // Fetch related and recommended anime in background
            launch {
              repository.getRelatedAnime(animeId, anime.title).onSuccess { list ->
                _relatedAnime.value = list
              }
            }
            launch {
              repository.getRecommendedAnime(animeId, anime.title, anime.termsByType?.genre).onSuccess { list ->
                _recommendedAnime.value = list
              }
            }
          } else {
            _uiState.value = DetailUiState.Error("Anime details could not be found.")
          }
        },
        onFailure = { error ->
          _uiState.value = DetailUiState.Error(
            error.localizedMessage ?: "Unable to fetch series details."
          )
        }
      )
    }
  }

  fun toggleSortOrder() {
    _isAscending.value = !_isAscending.value
  }

  fun onEpisodeQueryChanged(query: String) {
    _episodeFilterQuery.value = query
  }

  fun toggleBookmark() {
    repository.toggleBookmark(animeId)
  }

  fun toggleBookmarkForAnime(targetAnimeId: Long) {
    repository.toggleBookmark(targetAnimeId)
  }

  fun isBookmarked(): Boolean {
    return repository.isBookmarked(animeId)
  }

  fun getWatchStatus(): com.example.data.repository.WatchStatus {
    return repository.getWatchStatus(animeId)
  }

  fun setWatchStatus(status: com.example.data.repository.WatchStatus) {
    repository.setWatchStatus(animeId, status)
  }

  fun toggleFavoriteEpisode(episode: EpisodeItem, anime: AnimeItem) {
    viewModelScope.launch {
      favoritesRepository.toggleFavoriteEpisode(
        episodeId = episode.id,
        animeId = anime.id,
        seriesTitle = anime.title,
        episodeNumber = episode.displayEpisodeNumber(),
        episodeTitle = episode.title ?: "Episode ${episode.displayEpisodeNumber()}",
        posterUrl = anime.poster
      )
    }
  }

  companion object {
    fun provideFactory(
      animeId: Long,
      repository: AnimeRepository,
      favoritesRepository: FavoritesRepository
    ): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return DetailViewModel(animeId, repository, favoritesRepository) as T
        }
      }
  }
}
