package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.CommentItem
import com.example.data.model.CommunityPost
import com.example.data.model.PostCategory
import com.example.ui.components.AvatarWithFrame
import com.example.ui.components.BrandingFooter
import com.example.ui.components.CreatePostDialog
import com.example.ui.components.OtakuSearchBar
import com.example.ui.components.StoriesCarousel
import com.example.ui.theme.EmeraldDub
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.OtakuCardBorder
import com.example.ui.theme.OtakuCardSurface
import com.example.ui.theme.OtakuDeepBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CommunityViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CommunityScreen(
  viewModel: CommunityViewModel,
  modifier: Modifier = Modifier,
  onNavigateToDirectMessages: () -> Unit = {}
) {
  val posts by viewModel.filteredPosts.collectAsState()
  val stories by viewModel.stories.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedCategory by viewModel.selectedCategory.collectAsState()
  val notifications by viewModel.notifications.collectAsState()

  var showCreateDialog by remember { mutableStateOf(false) }
  var activeCommentPostId by remember { mutableStateOf<String?>(null) }

  Scaffold(
    topBar = {
      CommunityTopBar(
        unreadNotificationsCount = notifications.count { !it.isRead },
        onOpenDirectMessages = onNavigateToDirectMessages,
        onNotificationsClick = { viewModel.markNotificationsRead() }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showCreateDialog = true },
        containerColor = NeonPurple,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier
          .padding(bottom = 80.dp)
          .testTag("create_post_fab")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Create Post", modifier = Modifier.size(26.dp))
      }
    },
    containerColor = OtakuDeepBlack,
    modifier = modifier.testTag("community_screen")
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .navigationBarsPadding()
    ) {
      // Search Bar
      OtakuSearchBar(
        query = searchQuery,
        onQueryChange = { viewModel.onSearchQueryChanged(it) },
        placeholder = "Search creators, fan art, #hashtags...",
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
      )

      LazyColumn(
        contentPadding = PaddingValues(bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        // Stories Carousel
        item {
          StoriesCarousel(
            stories = stories,
            onStoryClick = { viewModel.markStorySeen(it.id) },
            onAddStoryClick = { showCreateDialog = true },
            modifier = Modifier.padding(vertical = 4.dp)
          )
        }

        // Feed Category Selector Tabs
        item {
          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(PostCategory.entries) { cat ->
              val isSelected = selectedCategory == cat
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .background(if (isSelected) NeonPurple else OtakuCardSurface)
                  .border(1.dp, if (isSelected) NeonPurpleLight else OtakuCardBorder, RoundedCornerShape(20.dp))
                  .clickable { viewModel.selectCategory(cat) }
                  .padding(horizontal = 14.dp, vertical = 7.dp)
              ) {
                Text(
                  text = cat.label,
                  style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isSelected) Color.White else TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                  )
                )
              }
            }
          }
        }

        // Community Feed Post Cards
        items(
          items = posts,
          key = { it.id }
        ) { post ->
          PostCardItem(
            post = post,
            onLikeClick = { viewModel.toggleLikePost(post.id) },
            onSaveClick = { viewModel.toggleSavePost(post.id) },
            onFollowClick = { viewModel.toggleFollowUser(post.username) },
            onCommentClick = { activeCommentPostId = post.id },
            modifier = Modifier.padding(horizontal = 16.dp)
          )
        }

        item {
          BrandingFooter()
        }
      }
    }
  }

  // Create Post Dialog
  if (showCreateDialog) {
    CreatePostDialog(
      onDismiss = { showCreateDialog = false },
      onSubmit = { caption, imageUrl, category, animeTitle, isSpoiler ->
        viewModel.createPost(caption, imageUrl, category, animeTitle, isSpoiler, emptyList(), "SelfUser", "Self User")
        showCreateDialog = false
      }
    )
  }

  // Comments Bottom Sheet
  val activePostId = activeCommentPostId
  if (activePostId != null) {
    val sheetState = rememberModalBottomSheetState()
    var newCommentText by remember { mutableStateOf("") }
    val commentsList = viewModel.getCommentsForPost(activePostId)

    ModalBottomSheet(
      onDismissRequest = { activeCommentPostId = null },
      sheetState = sheetState,
      containerColor = OtakuCardSurface,
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .navigationBarsPadding()
      ) {
        Text(
          text = "Comments (${commentsList.size})",
          style = MaterialTheme.typography.titleMedium.copy(
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Emoji Quick Reactions
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("🔥", "😱", "👑", "⚔️", "❤️").forEach { emoji ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(OtakuDeepBlack)
                .border(1.dp, OtakuCardBorder, RoundedCornerShape(12.dp))
                .clickable { newCommentText += " $emoji" }
                .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
              Text(text = emoji, fontSize = 16.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (commentsList.isEmpty()) {
          Text(
            text = "No comments yet. Be the first to comment (+5 XP)!",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
            modifier = Modifier.padding(vertical = 16.dp)
          )
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            commentsList.forEach { comment ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                AvatarWithFrame(
                  equippedFrameResId = comment.equippedFrameResId,
                  size = 40.dp
                )
                Column {
                  Text(
                    text = "@${comment.username}",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = NeonPurpleLight,
                      fontWeight = FontWeight.Bold
                    )
                  )
                  Text(
                    text = comment.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      color = TextPrimary,
                      fontSize = 13.sp
                    )
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Comment Input Row
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = newCommentText,
            onValueChange = { newCommentText = it },
            placeholder = { Text("Add a comment...", color = TextMuted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = OtakuDeepBlack,
              unfocusedContainerColor = OtakuDeepBlack,
              focusedBorderColor = NeonPurple,
              unfocusedBorderColor = OtakuCardBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
          )

          IconButton(
            onClick = {
              if (newCommentText.isNotBlank()) {
                viewModel.addComment(activePostId, newCommentText, "YourName")
                newCommentText = ""
              }
            },
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(NeonPurple)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Send",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PostCardItem(
  post: CommunityPost,
  onLikeClick: () -> Unit,
  onSaveClick: () -> Unit,
  onFollowClick: () -> Unit,
  onCommentClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(18.dp)
  var isSpoilerRevealed by remember { mutableStateOf(!post.isSpoiler) }

  Card(
    shape = shape,
    colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
    border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
    modifier = modifier
      .fillMaxWidth()
      .testTag("post_card_${post.id}")
  ) {
    Column {
      // Header: Creator Avatar + Username + Creator Badge + Rank + Follow
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          AvatarWithFrame(
            avatarUrl = post.userAvatarUrl,
            equippedFrameResId = post.equippedFrameResId,
            size = 48.dp
          )

          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = post.displayName,
                style = MaterialTheme.typography.titleSmall.copy(
                  color = TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.5.sp
                )
              )

              if (post.isCreator) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(EmeraldDub.copy(alpha = 0.2f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                  Text(
                    text = "🎨 Creator",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = EmeraldDub,
                      fontWeight = FontWeight.Bold,
                      fontSize = 9.5.sp
                    )
                  )
                }
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(post.userRank.color.copy(alpha = 0.2f))
                  .padding(horizontal = 5.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "${post.userRank.emblem} ${post.userRank.label}",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = post.userRank.color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.5.sp
                  )
                )
              }
            }

            Text(
              text = "@${post.username}",
              style = MaterialTheme.typography.bodySmall.copy(
                color = TextMuted,
                fontSize = 11.5.sp
              )
            )
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (post.isFollowingAuthor) OtakuDeepBlack else NeonPurple)
            .border(1.dp, if (post.isFollowingAuthor) OtakuCardBorder else NeonPurpleLight, RoundedCornerShape(8.dp))
            .clickable { onFollowClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Text(
            text = if (post.isFollowingAuthor) "Following" else "+ Follow",
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (post.isFollowingAuthor) TextMuted else Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          )
        }
      }

      // Media Artwork Box (with Double-Tap To Like & Spoiler Overlay)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(16f / 10f)
          .background(OtakuDeepBlack)
          .clickable { onLikeClick() }
      ) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(post.imageUrl)
            .crossfade(true)
            .build(),
          contentDescription = post.caption,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        // Spoiler Overlay if not revealed
        if (post.isSpoiler && !isSpoilerRevealed) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.92f)),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.VisibilityOff,
                contentDescription = null,
                tint = NeonPurpleLight,
                modifier = Modifier.size(32.dp)
              )
              Text(
                text = "SPOILER ALERT",
                style = MaterialTheme.typography.titleSmall.copy(
                  color = TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
              )
              Button(
                onClick = { isSpoilerRevealed = true },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Tap to Reveal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }
        }
      }

      // Actions Bar (Like, Comment, Share, Save)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Like Button
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.clickable { onLikeClick() }
          ) {
            Icon(
              imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = "Like",
              tint = if (post.isLiked) MaterialTheme.colorScheme.error else TextMuted,
              modifier = Modifier.size(22.dp)
            )
            Text(
              text = "${post.likesCount}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (post.isLiked) TextPrimary else TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            )
          }

          // Comment Button
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.clickable { onCommentClick() }
          ) {
            Icon(
              imageVector = Icons.Default.ChatBubbleOutline,
              contentDescription = "Comments",
              tint = TextMuted,
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "${post.commentsCount}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = TextMuted,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
              )
            )
          }

          // Share Button
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share",
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
          )
        }

        // Interactive Anime Title Tag
        if (!post.animeTitle.isNullOrBlank()) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(NeonPurple.copy(alpha = 0.2f))
              .border(0.8.dp, NeonPurpleLight, RoundedCornerShape(6.dp))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = NeonPurpleLight,
                modifier = Modifier.size(12.dp)
              )
              Text(
                text = post.animeTitle,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = NeonPurpleLight,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.5.sp
                )
              )
            }
          }
        }

        // Save Bookmark Button
        IconButton(
          onClick = onSaveClick,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            contentDescription = "Save Post",
            tint = if (post.isSaved) NeonPurpleLight else TextMuted,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      // Caption & Hashtags
      Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
        Text(
          text = post.caption,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = TextPrimary,
            fontSize = 13.5.sp,
            lineHeight = 18.sp
          ),
          maxLines = 3,
          overflow = TextOverflow.Ellipsis
        )

        if (post.hashtags.isNotEmpty()) {
          Spacer(modifier = Modifier.height(4.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            post.hashtags.forEach { tag ->
              Text(
                text = "#$tag",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = NeonPurpleLight,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 11.5.sp
                )
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
    }
  }
}

@Composable
private fun CommunityTopBar(
  unreadNotificationsCount: Int,
  onOpenDirectMessages: () -> Unit,
  onNotificationsClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(OtakuDeepBlack)
      .statusBarsPadding()
      .displayCutoutPadding()
      .padding(horizontal = 16.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .border(1.5.dp, NeonPurple, CircleShape)
      ) {
        Image(
          painter = painterResource(id = R.drawable.ic_animarch_logo_1791862659045),
          contentDescription = "Animarch Logo",
          modifier = Modifier.fillMaxSize()
        )
      }

      Column {
        Text(
          text = "COMMUNITY",
          style = MaterialTheme.typography.titleMedium.copy(
            color = TextPrimary,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            fontSize = 17.sp
          )
        )
        Text(
          text = "Anime Social Network",
          style = MaterialTheme.typography.labelSmall.copy(
            color = NeonPurpleLight,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
          )
        )
      }
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Notifications Button with Unread Badge
      IconButton(
        onClick = onNotificationsClick,
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(OtakuCardSurface)
          .testTag("notifications_button")
      ) {
        Box(contentAlignment = Alignment.TopEnd) {
          Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications",
            tint = TextPrimary,
            modifier = Modifier.size(20.dp)
          )
          if (unreadNotificationsCount > 0) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(NeonPurpleLight)
            )
          }
        }
      }

      // Direct Messages Button
      IconButton(
        onClick = onOpenDirectMessages,
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(OtakuCardSurface)
          .testTag("direct_messages_button")
      ) {
        Icon(
          imageVector = Icons.Default.Forum,
          contentDescription = "Direct Messages",
          tint = NeonPurpleLight,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
