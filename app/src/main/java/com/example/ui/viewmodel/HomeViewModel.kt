package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.WatchHistoryEntity
import com.example.data.model.AnimeItem
import com.example.data.repository.AnimeRepository
import com.example.data.repository.WatchHistoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface HomeUiState {
  data object Loading : HomeUiState
  data class Success(
    val animeList: List<AnimeItem>,
    val isFromCache: Boolean = false
  ) : HomeUiState
  data class Error(val message: String) : HomeUiState
}

private data class FilterCriteria(
  val query: String,
  val genre: String,
  val statusFilter: com.example.data.repository.WatchStatus,
  val onlyBookmarks: Boolean,
  val savedBookmarks: Set<Long>,
  val watchStatusMap: Map<Long, com.example.data.repository.WatchStatus>
)

class HomeViewModel(
  private val repository: AnimeRepository,
  private val watchHistoryRepository: WatchHistoryRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
  val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _isSearchingApi = MutableStateFlow(false)
  val isSearchingApi: StateFlow<Boolean> = _isSearchingApi.asStateFlow()

  private val _apiSearchResults = MutableStateFlow<List<AnimeItem>>(emptyList())

  private val _selectedGenre = MutableStateFlow("All")
  val selectedGenre: StateFlow<String> = _selectedGenre.asStateFlow()

  private val _selectedStatus = MutableStateFlow(com.example.data.repository.WatchStatus.NONE)
  val selectedStatus: StateFlow<com.example.data.repository.WatchStatus> = _selectedStatus.asStateFlow()

  private val _showOnlyBookmarks = MutableStateFlow(false)
  val showOnlyBookmarks: StateFlow<Boolean> = _showOnlyBookmarks.asStateFlow()

  val bookmarks: StateFlow<Set<Long>> = repository.bookmarksFlow
  val watchStatusMap: StateFlow<Map<Long, com.example.data.repository.WatchStatus>> = repository.watchStatusMapFlow

  val watchHistory: StateFlow<List<WatchHistoryEntity>> = watchHistoryRepository.recentHistory
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  private val _rawAnimeList = MutableStateFlow<List<AnimeItem>>(emptyList())
  private var searchJob: Job? = null

  private val statusCriteria = combine(_selectedStatus, repository.watchStatusMapFlow) { status, map ->
    Pair(status, map)
  }

  private val filterCriteria = combine(
    _searchQuery,
    _selectedGenre,
    _showOnlyBookmarks,
    bookmarks,
    statusCriteria
  ) { query, genre, onlyBookmarks, savedBookmarks, statusPair ->
    FilterCriteria(query, genre, statusPair.first, onlyBookmarks, savedBookmarks, statusPair.second)
  }

  val filteredAnimeList: StateFlow<List<AnimeItem>> = combine(
    _rawAnimeList,
    _apiSearchResults,
    filterCriteria
  ) { rawList, apiResults, filter ->
    val cleanQuery = filter.query.trim()
    val baseList = when {
      cleanQuery.isNotEmpty() && apiResults.isNotEmpty() -> apiResults
      cleanQuery.isNotEmpty() -> {
        rawList.filter { anime ->
          anime.title.contains(cleanQuery, ignoreCase = true) ||
            (anime.alternative?.contains(cleanQuery, ignoreCase = true) == true) ||
            (anime.nativeTitle?.contains(cleanQuery, ignoreCase = true) == true)
        }
      }
      else -> rawList
    }

    baseList.filter { anime ->
      val matchesGenre = if (filter.genre == "All") {
        true
      } else {
        anime.termsByType?.genre?.any { it.equals(filter.genre, ignoreCase = true) } == true
      }

      val matchesBookmark = if (filter.onlyBookmarks) {
        filter.savedBookmarks.contains(anime.id)
      } else {
        true
      }

      val matchesStatus = if (filter.statusFilter == com.example.data.repository.WatchStatus.NONE) {
        true
      } else {
        filter.watchStatusMap[anime.id] == filter.statusFilter
      }

      matchesGenre && matchesBookmark && matchesStatus
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  init {
    loadRecentAnime()
  }

  fun loadRecentAnime() {
    viewModelScope.launch {
      _uiState.value = HomeUiState.Loading
      val result = repository.getRecentAnime(page = 1, perPage = 50)
      result.fold(
        onSuccess = { list ->
          _rawAnimeList.value = list
          _uiState.value = HomeUiState.Success(list)
        },
        onFailure = { error ->
          _uiState.value = HomeUiState.Error(
            error.localizedMessage ?: "Failed to load recent anime. Please check your connection."
          )
        }
      )
    }
  }

  fun onSearchQueryChanged(newQuery: String) {
    _searchQuery.value = newQuery
    searchJob?.cancel()

    val clean = newQuery.trim()
    if (clean.isEmpty()) {
      _apiSearchResults.value = emptyList()
      _isSearchingApi.value = false
      return
    }

    searchJob = viewModelScope.launch {
      delay(250)
      _isSearchingApi.value = true
      val result = repository.searchAnimeDatabase(clean)
      result.fold(
        onSuccess = { results ->
          _apiSearchResults.value = results
          _isSearchingApi.value = false
        },
        onFailure = {
          _isSearchingApi.value = false
        }
      )
    }
  }

  fun onGenreSelected(genre: String) {
    _selectedGenre.value = genre
  }

  fun onStatusFilterSelected(status: com.example.data.repository.WatchStatus) {
    _selectedStatus.value = status
  }

  fun toggleBookmarksFilter() {
    _showOnlyBookmarks.value = !_showOnlyBookmarks.value
  }

  fun toggleBookmark(id: Long) {
    repository.toggleBookmark(id)
  }

  fun removeHistoryItem(episodeId: Long) {
    viewModelScope.launch {
      watchHistoryRepository.removeHistory(episodeId)
    }
  }

  fun clearAllHistory() {
    viewModelScope.launch {
      watchHistoryRepository.clearAllHistory()
    }
  }

  companion object {
    fun provideFactory(
      repository: AnimeRepository,
      watchHistoryRepository: WatchHistoryRepository
    ): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return HomeViewModel(repository, watchHistoryRepository) as T
        }
      }
  }
}
