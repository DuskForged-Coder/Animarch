package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CommentItem
import com.example.data.model.CommunityPost
import com.example.data.model.DirectMessage
import com.example.data.model.NotificationItem
import com.example.data.model.PostCategory
import com.example.data.model.StoryItem
import com.example.data.repository.CommunityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CommunityViewModel(
  private val communityRepository: CommunityRepository
) : ViewModel() {

  private val _selectedCategory = MutableStateFlow(PostCategory.FOR_YOU)
  val selectedCategory: StateFlow<PostCategory> = _selectedCategory.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  val rawPosts: StateFlow<List<CommunityPost>> = communityRepository.posts
  val stories: StateFlow<List<StoryItem>> = communityRepository.stories
  val notifications: StateFlow<List<NotificationItem>> = communityRepository.notifications
  val directMessages: StateFlow<List<DirectMessage>> = communityRepository.directMessages

  val filteredPosts: StateFlow<List<CommunityPost>> = combine(
    rawPosts,
    _selectedCategory,
    _searchQuery
  ) { postsList, category, query ->
    val cleanQuery = query.trim()
    postsList.filter { post ->
      val matchesCategory = when (category) {
        PostCategory.FOR_YOU -> true
        PostCategory.FOLLOWING -> post.isFollowingAuthor
        else -> post.category == category
      }

      val matchesQuery = if (cleanQuery.isBlank()) {
        true
      } else {
        post.caption.contains(cleanQuery, ignoreCase = true) ||
          post.username.contains(cleanQuery, ignoreCase = true) ||
          post.animeTitle?.contains(cleanQuery, ignoreCase = true) == true ||
          post.hashtags.any { it.contains(cleanQuery, ignoreCase = true) }
      }

      matchesCategory && matchesQuery
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
  )

  fun selectCategory(category: PostCategory) {
    _selectedCategory.value = category
  }

  fun onSearchQueryChanged(query: String) {
    _searchQuery.value = query
  }

  fun toggleLikePost(postId: String) {
    communityRepository.toggleLikePost(postId)
  }

  fun toggleSavePost(postId: String) {
    communityRepository.toggleSavePost(postId)
  }

  fun toggleFollowUser(username: String) {
    communityRepository.toggleFollowUser(username)
  }

  fun getCommentsForPost(postId: String): List<CommentItem> {
    return communityRepository.getCommentsForPost(postId)
  }

  fun addComment(postId: String, text: String, username: String) {
    communityRepository.addComment(postId, text, username)
  }

  fun createPost(
    caption: String,
    imageUrl: String,
    category: PostCategory,
    animeTitle: String?,
    isSpoiler: Boolean,
    hashtags: List<String>,
    username: String,
    displayName: String
  ) {
    communityRepository.createPost(
      caption = caption,
      imageUrl = imageUrl,
      category = category,
      animeTitle = animeTitle,
      isSpoiler = isSpoiler,
      hashtags = hashtags,
      username = username,
      displayName = displayName
    )
  }

  fun createStory(imageUrl: String, caption: String?, username: String) {
    communityRepository.createStory(imageUrl, caption, username)
  }

  fun sendDirectMessage(text: String) {
    communityRepository.sendDirectMessage(text)
  }

  fun markStorySeen(storyId: String) {
    communityRepository.markStorySeen(storyId)
  }

  fun markNotificationsRead() {
    communityRepository.markNotificationsRead()
  }

  companion object {
    fun provideFactory(communityRepository: CommunityRepository): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return CommunityViewModel(communityRepository) as T
        }
      }
  }
}
