package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.WatchStatus
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.OtakuCardBorder
import com.example.ui.theme.OtakuCardSurface
import com.example.ui.theme.TextMuted

@Composable
fun GenreFilterRow(
  selectedGenre: String,
  onGenreSelect: (String) -> Unit,
  showOnlyBookmarks: Boolean,
  onToggleBookmarks: () -> Unit,
  modifier: Modifier = Modifier,
  selectedStatus: WatchStatus = WatchStatus.NONE,
  onStatusSelect: (WatchStatus) -> Unit = {}
) {
  val genres = listOf(
    "All", "Action", "Adventure", "Fantasy", "Sci-Fi",
    "Comedy", "Drama", "Romance", "Military", "Magic", "Movie"
  )

  val libraryStatuses = listOf(
    WatchStatus.WATCHING,
    WatchStatus.COMPLETED,
    WatchStatus.PLAN_TO_WATCH,
    WatchStatus.PAUSED,
    WatchStatus.DROPPED
  )

  LazyRow(
    modifier = modifier,
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Saved / Bookmarks Filter Chip
    item {
      val isBookmarkActive = showOnlyBookmarks
      val chipShape = RoundedCornerShape(20.dp)

      val bgColor by animateColorAsState(
        targetValue = if (isBookmarkActive) NeonPurple else OtakuCardSurface,
        label = "bookmark_chip_bg"
      )

      Box(
        modifier = Modifier
          .clip(chipShape)
          .background(bgColor)
          .border(
            width = 1.dp,
            color = if (isBookmarkActive) NeonPurpleLight else OtakuCardBorder,
            shape = chipShape
          )
          .clickable { onToggleBookmarks() }
          .padding(horizontal = 14.dp, vertical = 7.dp)
          .testTag("filter_bookmarks"),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Bookmark,
            contentDescription = "Bookmarks",
            tint = if (isBookmarkActive) Color.White else TextMuted,
            modifier = Modifier.size(15.dp)
          )
          Text(
            text = "Saved",
            style = MaterialTheme.typography.labelMedium.copy(
              color = if (isBookmarkActive) Color.White else TextMuted,
              fontWeight = if (isBookmarkActive) FontWeight.Bold else FontWeight.Medium,
              fontSize = 12.5.sp
            )
          )
        }
      }
    }

    // Library Watch Status Filter Chips
    items(libraryStatuses) { status ->
      val isSelected = !showOnlyBookmarks && selectedStatus == status
      val chipShape = RoundedCornerShape(20.dp)

      val bgColor by animateColorAsState(
        targetValue = if (isSelected) NeonPurple else OtakuCardSurface,
        label = "status_chip_bg"
      )

      Box(
        modifier = Modifier
          .clip(chipShape)
          .background(bgColor)
          .border(
            width = 1.dp,
            color = if (isSelected) NeonPurpleLight else OtakuCardBorder,
            shape = chipShape
          )
          .clickable {
            if (isSelected) {
              onStatusSelect(WatchStatus.NONE)
            } else {
              onStatusSelect(status)
            }
          }
          .padding(horizontal = 13.dp, vertical = 7.dp)
          .testTag("status_chip_${status.name}"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = status.label,
          style = MaterialTheme.typography.labelMedium.copy(
            color = if (isSelected) Color.White else TextMuted,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.5.sp
          )
        )
      }
    }

    // Genre Chips
    items(genres) { genre ->
      val isSelected = !showOnlyBookmarks && selectedStatus == WatchStatus.NONE && genre == selectedGenre
      val chipShape = RoundedCornerShape(20.dp)

      val bgColor by animateColorAsState(
        targetValue = if (isSelected) NeonPurple else OtakuCardSurface,
        label = "genre_chip_bg"
      )

      Box(
        modifier = Modifier
          .clip(chipShape)
          .background(bgColor)
          .border(
            width = 1.dp,
            color = if (isSelected) NeonPurpleLight else OtakuCardBorder,
            shape = chipShape
          )
          .clickable {
            onStatusSelect(WatchStatus.NONE)
            onGenreSelect(genre)
          }
          .padding(horizontal = 15.dp, vertical = 7.dp)
          .testTag("genre_chip_$genre"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = genre,
          style = MaterialTheme.typography.labelMedium.copy(
            color = if (isSelected) Color.White else TextMuted,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.5.sp
          )
        )
      }
    }
  }
}
