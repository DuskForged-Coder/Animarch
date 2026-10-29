package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CommentItem
import com.example.data.model.CommunityPost
import com.example.data.model.DirectMessage
import com.example.data.model.MembershipRank
import com.example.data.model.NotificationItem
import com.example.data.model.PostCategory
import com.example.data.model.StoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface CommunityRepository {
  val posts: StateFlow<List<CommunityPost>>
  val stories: StateFlow<List<StoryItem>>
  val notifications: StateFlow<List<NotificationItem>>
  val directMessages: StateFlow<List<DirectMessage>>

  fun getCommentsForPost(postId: String): List<CommentItem>
  fun toggleLikePost(postId: String)
  fun toggleSavePost(postId: String)
  fun toggleFollowUser(username: String)
  fun addComment(postId: String, text: String, username: String)
  fun createPost(
    caption: String,
    imageUrl: String,
    category: PostCategory,
    animeTitle: String?,
    isSpoiler: Boolean,
    hashtags: List<String>,
    username: String,
    displayName: String
  )
  fun createStory(imageUrl: String, caption: String?, username: String)
  fun sendDirectMessage(text: String)
  fun markStorySeen(storyId: String)
  fun markNotificationsRead()
}

class DefaultCommunityRepository(
  context: Context,
  private val gamificationRepository: GamificationRepository? = null
) : CommunityRepository {

  private val _posts = MutableStateFlow(loadInitialPosts())
  override val posts: StateFlow<List<CommunityPost>> = _posts.asStateFlow()

  private val _stories = MutableStateFlow(loadInitialStories())
  override val stories: StateFlow<List<StoryItem>> = _stories.asStateFlow()

  private val _notifications = MutableStateFlow(loadInitialNotifications())
  override val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

  private val _directMessages = MutableStateFlow(loadInitialMessages())
  override val directMessages: StateFlow<List<DirectMessage>> = _directMessages.asStateFlow()

  private val commentsMap = mutableMapOf<String, MutableList<CommentItem>>()

  init {
    // Populate starter comments
    commentsMap["post_1"] = mutableListOf(
      CommentItem("c1", "post_1", "OtakuKing", null, R.drawable.frame_dragon_flame, "Sung Jin-woo's shadow army animation was unreal! 🔥"),
      CommentItem("c2", "post_1", "AnimeQueen", null, R.drawable.frame_void_prism, "A-1 Pictures went all out on this episode!")
    )
    commentsMap["post_2"] = mutableListOf(
      CommentItem("c3", "post_2", "LuffyFan99", null, R.drawable.frame_celestial_crystal, "Egghead arc keeps blowing my mind every week!")
    )
  }

  override fun getCommentsForPost(postId: String): List<CommentItem> {
    return commentsMap[postId].orEmpty()
  }

  override fun toggleLikePost(postId: String) {
    val currentList = _posts.value.toMutableList()
    val idx = currentList.indexOfFirst { it.id == postId }
    if (idx != -1) {
      val post = currentList[idx]
      val newIsLiked = !post.isLiked
      val newLikes = if (newIsLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
      currentList[idx] = post.copy(isLiked = newIsLiked, likesCount = newLikes)
      _posts.value = currentList
    }
  }

  override fun toggleSavePost(postId: String) {
    val currentList = _posts.value.toMutableList()
    val idx = currentList.indexOfFirst { it.id == postId }
    if (idx != -1) {
      val post = currentList[idx]
      currentList[idx] = post.copy(isSaved = !post.isSaved)
      _posts.value = currentList
    }
  }

  override fun toggleFollowUser(username: String) {
    val currentList = _posts.value.map { post ->
      if (post.username == username) {
        post.copy(isFollowingAuthor = !post.isFollowingAuthor)
      } else post
    }
    _posts.value = currentList
  }

  override fun addComment(postId: String, text: String, username: String) {
    if (text.isBlank()) return
    val list = commentsMap.getOrPut(postId) { mutableListOf() }
    val newComment = CommentItem(
      id = "comment_${System.currentTimeMillis()}",
      postId = postId,
      username = username,
      equippedFrameResId = R.drawable.frame_void_prism,
      text = text.trim()
    )
    list.add(newComment)

    // Update comment count on post
    val currentList = _posts.value.toMutableList()
    val idx = currentList.indexOfFirst { it.id == postId }
    if (idx != -1) {
      currentList[idx] = currentList[idx].copy(commentsCount = list.size)
      _posts.value = currentList
    }

    // Award +5 XP for active community discussion!
    gamificationRepository?.claimQuestReward("daily_explore_3")
  }

  override fun createPost(
    caption: String,
    imageUrl: String,
    category: PostCategory,
    animeTitle: String?,
    isSpoiler: Boolean,
    hashtags: List<String>,
    username: String,
    displayName: String
  ) {
    val newPost = CommunityPost(
      id = "post_${System.currentTimeMillis()}",
      userId = "user_self",
      username = username.ifBlank { "otaku_creator" },
      displayName = displayName.ifBlank { "Otaku Creator" },
      equippedFrameResId = R.drawable.frame_dragon_flame,
      userRank = MembershipRank.DEITY,
      category = category,
      imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&q=80" },
      caption = caption,
      animeTitle = animeTitle,
      hashtags = hashtags,
      isSpoiler = isSpoiler,
      likesCount = 1,
      isLiked = true,
      commentsCount = 0
    )

    val list = _posts.value.toMutableList()
    list.add(0, newPost)
    _posts.value = list

    // Award +20 XP for posting original fan art / edits / theories!
    gamificationRepository?.claimQuestReward("daily_ep_1")
  }

  override fun createStory(imageUrl: String, caption: String?, username: String) {
    val newStory = StoryItem(
      id = "story_${System.currentTimeMillis()}",
      userId = "user_self",
      username = username.ifBlank { "You" },
      equippedFrameResId = R.drawable.frame_dragon_flame,
      imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=800&q=80" },
      caption = caption,
      hasUnseen = false
    )

    val list = _stories.value.toMutableList()
    list.add(0, newStory)
    _stories.value = list
  }

  override fun sendDirectMessage(text: String) {
    if (text.isBlank()) return
    val newMsg = DirectMessage(
      id = "msg_${System.currentTimeMillis()}",
      senderUsername = "You",
      text = text.trim(),
      isFromMe = true
    )
    val list = _directMessages.value.toMutableList()
    list.add(newMsg)
    _directMessages.value = list
  }

  override fun markStorySeen(storyId: String) {
    val list = _stories.value.map { story ->
      if (story.id == storyId) story.copy(hasUnseen = false) else story
    }
    _stories.value = list
  }

  override fun markNotificationsRead() {
    val list = _notifications.value.map { it.copy(isRead = true) }
    _notifications.value = list
  }

  private fun loadInitialPosts(): List<CommunityPost> {
    return listOf(
      CommunityPost(
        id = "post_1",
        userId = "u1",
        username = "ShadowMonarch",
        displayName = "Jin-Woo Fanatic",
        equippedFrameResId = R.drawable.frame_dragon_flame,
        userRank = MembershipRank.PRIMORDIAL,
        category = PostCategory.FAN_ART,
        imageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&q=80",
        caption = "Arise! Created this digital painting of Sung Jin-woo in his shadow armor.",
        animeTitle = "Solo Leveling",
        hashtags = listOf("SoloLeveling", "FanArt", "AnimeArt"),
        isSpoiler = false,
        likesCount = 1420,
        commentsCount = 84
      ),
      CommunityPost(
        id = "post_2",
        userId = "u2",
        username = "Gear5Luffy",
        displayName = "Sun God Nika",
        equippedFrameResId = R.drawable.frame_void_prism,
        userRank = MembershipRank.DEITY,
        category = PostCategory.DISCUSSIONS,
        imageUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=800&q=80",
        caption = "MAJOR SPOILERS FOR EPISODE 1100! The lore breakdown on Egghead Island is unbelievable.",
        animeTitle = "One Piece",
        hashtags = listOf("OnePiece", "AnimeTheory", "EggheadArc"),
        isSpoiler = true,
        likesCount = 890,
        commentsCount = 120
      ),
      CommunityPost(
        id = "post_3",
        userId = "u3",
        username = "TanjiroSun",
        displayName = "Demon Slayer Central",
        equippedFrameResId = R.drawable.frame_celestial_crystal,
        userRank = MembershipRank.IMMORTAL,
        category = PostCategory.EDITS,
        imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&q=80",
        caption = "4K Wallpaper Edit from Hashira Training Arc climax!",
        animeTitle = "Demon Slayer",
        hashtags = listOf("DemonSlayer", "AnimeEdit", "Hashira"),
        isSpoiler = false,
        likesCount = 2100,
        commentsCount = 45
      )
    )
  }

  private fun loadInitialStories(): List<StoryItem> {
    return listOf(
      StoryItem("s0", "user_self", "Your Story", null, R.drawable.frame_dragon_flame, "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&q=80", "Creating new edit!", false),
      StoryItem("s1", "ShadowMonarch", "ShadowMonarch", null, R.drawable.frame_dragon_flame, "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=800&q=80", "New artwork dropping tonight! 🔥", true),
      StoryItem("s2", "Gear5Luffy", "Gear5Luffy", null, R.drawable.frame_void_prism, "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=800&q=80", "One Piece manga spoilers inside ⚡", true),
      StoryItem("s3", "TanjiroSun", "TanjiroSun", null, R.drawable.frame_celestial_crystal, "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800&q=80", "Hashira training reaction live!", false)
    )
  }

  private fun loadInitialNotifications(): List<NotificationItem> {
    return listOf(
      NotificationItem("n1", "New Like", "ShadowMonarch liked your fan art post.", "LIKE", System.currentTimeMillis() - 3600000, false),
      NotificationItem("n2", "Comment Reply", "Gear5Luffy replied to your comment on One Piece theory.", "COMMENT", System.currentTimeMillis() - 7200000, false),
      NotificationItem("n3", "New Follower", "TanjiroSun started following your anime journey.", "FOLLOW", System.currentTimeMillis() - 14400000, true)
    )
  }

  private fun loadInitialMessages(): List<DirectMessage> {
    return listOf(
      DirectMessage("m1", "ShadowMonarch", "Hey! Loved your recent anime edit! Which software did you use?"),
      DirectMessage("m2", "You", "Thanks! I used After Effects with custom glow plugins.", isFromMe = true),
      DirectMessage("m3", "ShadowMonarch", "Awesome! Are you planning to watch the new Solo Leveling episode together in Watch Party?")
    )
  }
}
