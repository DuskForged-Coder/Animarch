package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.repository.AnimarchAiRepository
import com.example.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnimarchAiViewModel(
  private val aiRepository: AnimarchAiRepository,
  private val animeRepository: AnimeRepository
) : ViewModel() {

  val chatHistory: StateFlow<List<ChatMessage>> = aiRepository.chatHistory

  private val _isProcessing = MutableStateFlow(false)
  val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

  private val _userQuery = MutableStateFlow("")
  val userQuery: StateFlow<String> = _userQuery.asStateFlow()

  fun onQueryChanged(newQuery: String) {
    _userQuery.value = newQuery
  }

  fun sendMessage(query: String) {
    if (query.isBlank()) return
    _userQuery.value = ""
    viewModelScope.launch {
      _isProcessing.value = true
      aiRepository.processQuery(query.trim(), animeRepository)
      _isProcessing.value = false
    }
  }

  fun clearChat() {
    aiRepository.clearHistory()
  }

  companion object {
    fun provideFactory(
      aiRepository: AnimarchAiRepository,
      animeRepository: AnimeRepository
    ): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return AnimarchAiViewModel(aiRepository, animeRepository) as T
        }
      }
  }
}
