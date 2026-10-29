package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AnimeItem
import com.example.data.model.EpisodeItem
import com.example.data.model.StreamType
import com.example.data.repository.AnimeRepository
import com.example.data.repository.FavoritesRepository
import com.example.data.repository.GamificationRepository
import com.example.data.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerViewModel(
  val animeId: Long,
  initialTitle: String,
  initialEpisodeId: Long,
  private val repository: AnimeRepository,
  private val watchHistoryRepository: WatchHistoryRepository,
  private val favoritesRepository: FavoritesRepository,
  private val gamificationRepository: GamificationRepository? = null
) : ViewModel() {

  private val _seriesTitle = MutableStateFlow(initialTitle)
  val seriesTitle: String get() = _seriesTitle.value

  private val _posterUrl = MutableStateFlow<String?>(null)

  private val _episodes = MutableStateFlow<List<EpisodeItem>>(emptyList())
  val episodes: StateFlow<List<EpisodeItem>> = _episodes.asStateFlow()

  private val _currentEpisode = MutableStateFlow<EpisodeItem?>(null)
  val currentEpisode: StateFlow<EpisodeItem?> = _currentEpisode.asStateFlow()

  private val _streamType = MutableStateFlow(StreamType.SUB)
  val streamType: StateFlow<StreamType> = _streamType.asStateFlow()

  private val _currentStreamUrl = MutableStateFlow("")
  val currentStreamUrl: StateFlow<String> = _currentStreamUrl.asStateFlow()

  private val _currentProgressSeconds = MutableStateFlow(0L)
  val currentProgressSeconds: StateFlow<Long> = _currentProgressSeconds.asStateFlow()

  private val _isFullscreen = MutableStateFlow(false)
  val isFullscreen: StateFlow<Boolean> = _isFullscreen.asStateFlow()

  val bookmarks: StateFlow<Set<Long>> = repository.bookmarksFlow

  private val _relatedAnime = MutableStateFlow<List<AnimeItem>>(emptyList())
  val relatedAnime: StateFlow<List<AnimeItem>> = _relatedAnime.asStateFlow()

  private val _recommendedAnime = MutableStateFlow<List<AnimeItem>>(emptyList())
  val recommendedAnime: StateFlow<List<AnimeItem>> = _recommendedAnime.asStateFlow()

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val isCurrentEpisodeFavorited: StateFlow<Boolean> = _currentEpisode
    .flatMapLatest { ep ->
      if (ep != null) favoritesRepository.isEpisodeFavorited(ep.id) else flowOf(false)
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = false
    )

  init {
    viewModelScope.launch {
      val result = repository.getSeriesDetails(animeId)
      result.onSuccess { data ->
        val anime = data.anime
        anime?.title?.let { _seriesTitle.value = it }
        _posterUrl.value = anime?.poster
        val epList = data.episodes.orEmpty()
        _episodes.value = epList

        val targetEp = epList.find { it.id == initialEpisodeId } ?: epList.firstOrNull()
        if (targetEp != null) {
          selectEpisode(targetEp)
        }

        val titleToSearch = anime?.title ?: initialTitle
        val genresToSearch = anime?.termsByType?.genre
        launch {
          repository.getRelatedAnime(animeId, titleToSearch).onSuccess { list ->
            _relatedAnime.value = list
          }
        }
        launch {
          repository.getRecommendedAnime(animeId, titleToSearch, genresToSearch).onSuccess { list ->
            _recommendedAnime.value = list
          }
        }
      }
    }
  }

  private fun resolveCurrentUrl(episode: EpisodeItem?, type: StreamType): String {
    return episode?.resolveStreamUrl(type) ?: ""
  }

  fun selectEpisode(episode: EpisodeItem) {
    _currentEpisode.value = episode
    val url = resolveCurrentUrl(episode, _streamType.value)
    _currentStreamUrl.value = url

    // Award +10 XP once for unique episode completion!
    gamificationRepository?.recordEpisodeCompleted(episode.id, animeId)

    // Save to Room WatchHistory
    viewModelScope.launch {
      watchHistoryRepository.recordWatchProgress(
        episodeId = episode.id,
        animeId = animeId,
        seriesTitle = _seriesTitle.value,
        episodeNumber = episode.displayEpisodeNumber(),
        episodeTitle = episode.title ?: "Episode ${episode.displayEpisodeNumber()}",
        posterUrl = _posterUrl.value,
        streamUrl = url,
        streamType = _streamType.value.label,
        progressSeconds = _currentProgressSeconds.value,
        durationSeconds = 1440
      )
    }
  }

  fun setStreamType(type: StreamType) {
    if (_streamType.value != type) {
      _streamType.value = type
      val url = resolveCurrentUrl(_currentEpisode.value, type)
      _currentStreamUrl.value = url

      val ep = _currentEpisode.value
      if (ep != null) {
        viewModelScope.launch {
          watchHistoryRepository.recordWatchProgress(
            episodeId = ep.id,
            animeId = animeId,
            seriesTitle = _seriesTitle.value,
            episodeNumber = ep.displayEpisodeNumber(),
            episodeTitle = ep.title ?: "Episode ${ep.displayEpisodeNumber()}",
            posterUrl = _posterUrl.value,
            streamUrl = url,
            streamType = type.label,
            progressSeconds = _currentProgressSeconds.value
          )
        }
      }
    }
  }

  fun toggleStreamType() {
    val nextType = if (_streamType.value == StreamType.SUB) StreamType.DUB else StreamType.SUB
    setStreamType(nextType)
  }

  fun updateProgress(seconds: Long) {
    _currentProgressSeconds.value = seconds
    val ep = _currentEpisode.value
    if (ep != null) {
      viewModelScope.launch {
        watchHistoryRepository.updateProgress(ep.id, seconds)
      }
    }
  }

  fun toggleFavoriteCurrentEpisode() {
    val ep = _currentEpisode.value ?: return
    viewModelScope.launch {
      favoritesRepository.toggleFavoriteEpisode(
        episodeId = ep.id,
        animeId = animeId,
        seriesTitle = _seriesTitle.value,
        episodeNumber = ep.displayEpisodeNumber(),
        episodeTitle = ep.title ?: "Episode ${ep.displayEpisodeNumber()}",
        posterUrl = _posterUrl.value
      )
    }
  }

  fun nextEpisode(): Boolean {
    val current = _currentEpisode.value ?: return false
    val allList = _episodes.value
    val currentIndex = allList.indexOfFirst { it.id == current.id }
    if (currentIndex in 0 until allList.size - 1) {
      selectEpisode(allList[currentIndex + 1])
      return true
    }
    return false
  }

  fun previousEpisode(): Boolean {
    val current = _currentEpisode.value ?: return false
    val allList = _episodes.value
    val currentIndex = allList.indexOfFirst { it.id == current.id }
    if (currentIndex > 0) {
      selectEpisode(allList[currentIndex - 1])
      return true
    }
    return false
  }

  fun toggleFullscreen() {
    _isFullscreen.value = !_isFullscreen.value
  }

  fun setFullscreen(fullscreen: Boolean) {
    _isFullscreen.value = fullscreen
  }

  fun toggleBookmark(targetAnimeId: Long) {
    repository.toggleBookmark(targetAnimeId)
  }

  companion object {
    fun provideFactory(
      animeId: Long,
      initialTitle: String,
      initialEpisodeId: Long,
      repository: AnimeRepository,
      watchHistoryRepository: WatchHistoryRepository,
      favoritesRepository: FavoritesRepository,
      gamificationRepository: GamificationRepository? = null
    ): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return PlayerViewModel(
            animeId,
            initialTitle,
            initialEpisodeId,
            repository,
            watchHistoryRepository,
            favoritesRepository,
            gamificationRepository
          ) as T
        }
      }
  }
}
