package com.example.data.model

enum class PostCategory(val label: String) {
  FOR_YOU("For You"),
  FOLLOWING("Following"),
  FAN_ART("Fan Art"),
  EDITS("Edits"),
  MEMES("Memes"),
  DISCUSSIONS("Discussions")
}

data class CommunityPost(
  val id: String,
  val userId: String,
  val username: String,
  val displayName: String,
  val userAvatarUrl: String? = null,
  val equippedFrameResId: Int? = null,
  val userRank: MembershipRank = MembershipRank.MORTAL,
  val isCreator: Boolean = true,
  val category: PostCategory = PostCategory.FAN_ART,
  val imageUrl: String,
  val imageUrls: List<String> = emptyList(),
  val caption: String,
  val animeTitle: String? = null,
  val animeId: Long? = null,
  val hashtags: List<String> = emptyList(),
  val isSpoiler: Boolean = false,
  val likesCount: Int = 0,
  val isLiked: Boolean = false,
  val commentsCount: Int = 0,
  val isSaved: Boolean = false,
  val isFollowingAuthor: Boolean = false,
  val timestamp: Long = System.currentTimeMillis()
)

data class StoryItem(
  val id: String,
  val userId: String,
  val username: String,
  val userAvatarUrl: String? = null,
  val equippedFrameResId: Int? = null,
  val imageUrl: String,
  val caption: String? = null,
  val hasUnseen: Boolean = true,
  val timestamp: Long = System.currentTimeMillis()
)

data class CommentItem(
  val id: String,
  val postId: String,
  val username: String,
  val userAvatarUrl: String? = null,
  val equippedFrameResId: Int? = null,
  val text: String,
  val timestamp: Long = System.currentTimeMillis()
)

data class NotificationItem(
  val id: String,
  val title: String,
  val message: String,
  val iconType: String = "LIKE", // LIKE, COMMENT, FOLLOW, MENTION
  val timestamp: Long = System.currentTimeMillis(),
  val isRead: Boolean = false
)

data class DirectMessage(
  val id: String,
  val senderUsername: String,
  val text: String,
  val isFromMe: Boolean = false,
  val timestamp: Long = System.currentTimeMillis()
)
