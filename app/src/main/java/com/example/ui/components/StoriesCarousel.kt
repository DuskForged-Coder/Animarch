package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StoryItem
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleDark
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.OtakuCardBorder
import com.example.ui.theme.OtakuCardSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun StoriesCarousel(
  stories: List<StoryItem>,
  onStoryClick: (StoryItem) -> Unit,
  onAddStoryClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyRow(
    modifier = modifier.testTag("stories_carousel"),
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(14.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Add Story Button
    item {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onAddStoryClick() }
      ) {
        Box(
          modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(OtakuCardSurface)
            .border(1.5.dp, OtakuCardBorder, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Story",
            tint = NeonPurpleLight,
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Your Story",
          style = MaterialTheme.typography.labelSmall.copy(
            color = TextMuted,
            fontSize = 11.sp
          ),
          maxLines = 1
        )
      }
    }

    // User Stories
    items(stories, key = { it.id }) { story ->
      val ringBrush = if (story.hasUnseen) {
        Brush.sweepGradient(listOf(NeonPurple, NeonPurpleLight, NeonPurpleDark, NeonPurple))
      } else {
        Brush.linearGradient(listOf(OtakuCardBorder, OtakuCardBorder))
      }

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onStoryClick(story) }
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(ringBrush)
            .padding(2.5.dp),
          contentAlignment = Alignment.Center
        ) {
          AvatarWithFrame(
            avatarUrl = story.userAvatarUrl,
            equippedFrameResId = story.equippedFrameResId,
            size = 66.dp
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = story.username,
          style = MaterialTheme.typography.labelSmall.copy(
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}
