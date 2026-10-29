package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EpisodeItem
import com.example.data.model.StreamType
import com.example.ui.components.AnimeHorizontalSection
import com.example.ui.components.BrandingBadge
import com.example.ui.components.BrandingFooter
import com.example.ui.player.OtakuVideoPlayer
import com.example.ui.theme.EmeraldDub
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.OtakuCardBorder
import com.example.ui.theme.OtakuCardSurface
import com.example.ui.theme.OtakuCharcoal
import com.example.ui.theme.OtakuDeepBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PlayerViewModel

@Composable
fun PlayerScreen(
  viewModel: PlayerViewModel,
  onBackClick: () -> Unit,
  onAnimeClick: (Long) -> Unit = {},
  isInPiPMode: Boolean = false,
  onEnterPiP: () -> Unit = {},
  onPlayerActiveChange: (Boolean) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val streamUrl by viewModel.currentStreamUrl.collectAsState()
  val currentEpisode by viewModel.currentEpisode.collectAsState()
  val streamType by viewModel.streamType.collectAsState()
  val isFullscreen by viewModel.isFullscreen.collectAsState()
  val episodes by viewModel.episodes.collectAsState()
  val isCurrentEpisodeFavorited by viewModel.isCurrentEpisodeFavorited.collectAsState()
  val relatedAnime by viewModel.relatedAnime.collectAsState()
  val recommendedAnime by viewModel.recommendedAnime.collectAsState()
  val bookmarks by viewModel.bookmarks.collectAsState()

  DisposableEffect(Unit) {
    onPlayerActiveChange(true)
    onDispose {
      onPlayerActiveChange(false)
    }
  }

  BackHandler {
    if (isFullscreen) {
      viewModel.setFullscreen(false)
    } else {
      onBackClick()
    }
  }

  if (isInPiPMode) {
    // Picture in Picture Floating Window Mode (Video only, zero overlays)
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(OtakuDeepBlack)
        .testTag("player_screen_pip")
    ) {
      OtakuVideoPlayer(
        streamUrl = streamUrl,
        isFullscreen = true,
        onFullscreenChange = {},
        modifier = Modifier.fillMaxSize()
      )
    }
  } else if (isFullscreen) {
    // Fullscreen View
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(OtakuDeepBlack)
        .testTag("player_screen_fullscreen")
    ) {
      OtakuVideoPlayer(
        streamUrl = streamUrl,
        isFullscreen = true,
        onFullscreenChange = { viewModel.setFullscreen(it) },
        modifier = Modifier.fillMaxSize()
      )

      // Exit fullscreen floating button
      IconButton(
        onClick = { viewModel.setFullscreen(false) },
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(16.dp)
          .size(40.dp)
          .clip(CircleShape)
          .background(OtakuDeepBlack.copy(alpha = 0.7f))
          .testTag("exit_fullscreen_button")
      ) {
        Icon(
          imageVector = Icons.Default.FullscreenExit,
          contentDescription = "Exit Fullscreen",
          tint = TextPrimary
        )
      }
    }
  } else {
    // Portrait View
    Scaffold(
      topBar = {
        PlayerTopBar(
          title = viewModel.seriesTitle,
          onBackClick = onBackClick
        )
      },
      containerColor = OtakuDeepBlack,
      modifier = modifier.testTag("player_screen")
    ) { innerPadding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .navigationBarsPadding()
          .verticalScroll(rememberScrollState())
      ) {
        // Video Player Container
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(OtakuDeepBlack)
        ) {
          OtakuVideoPlayer(
            streamUrl = streamUrl,
            isFullscreen = false,
            onFullscreenChange = { viewModel.setFullscreen(it) }
          )
        }

        // Quick Controls Bar (SUB/DUB Switch + Favorite + Prev/Next + PiP + Fullscreen)
        PlayerControlsBar(
          streamType = streamType,
          isFavorited = isCurrentEpisodeFavorited,
          onToggleFavorite = { viewModel.toggleFavoriteCurrentEpisode() },
          onToggleStreamType = { viewModel.toggleStreamType() },
          onPrevClick = { viewModel.previousEpisode() },
          onNextClick = { viewModel.nextEpisode() },
          onEnterPiP = onEnterPiP,
          onFullscreenClick = { viewModel.toggleFullscreen() }
        )

        // Episode Information Card
        EpisodeInfoCard(
          seriesTitle = viewModel.seriesTitle,
          currentEpisode = currentEpisode,
          streamType = streamType,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Episode Selector Horizontal Strip
        Text(
          text = "Select Episode",
          style = MaterialTheme.typography.titleMedium.copy(
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.5.sp
          ),
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("episode_selector_row")
        ) {
          items(episodes, key = { it.id }) { ep ->
            val isCurrent = ep.id == currentEpisode?.id
            EpisodePill(
              episode = ep,
              isSelected = isCurrent,
              onClick = { viewModel.selectEpisode(ep) }
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Related Anime Section
        if (relatedAnime.isNotEmpty()) {
          AnimeHorizontalSection(
            title = "Related & Sequel Series",
            subtitle = "Prequels, sequels, movies & spin-offs",
            animeList = relatedAnime,
            bookmarks = bookmarks,
            onAnimeClick = onAnimeClick,
            onBookmarkClick = { viewModel.toggleBookmark(it) },
            modifier = Modifier.padding(vertical = 8.dp)
          )
        }

        // Recommended Anime Section
        if (recommendedAnime.isNotEmpty()) {
          AnimeHorizontalSection(
            title = "Recommended Anime",
            subtitle = "You might also enjoy watching",
            animeList = recommendedAnime,
            bookmarks = bookmarks,
            onAnimeClick = onAnimeClick,
            onBookmarkClick = { viewModel.toggleBookmark(it) },
            modifier = Modifier.padding(vertical = 8.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Branding Footer
        BrandingFooter()
      }
    }
  }
}

@Composable
private fun PlayerTopBar(
  title: String,
  onBackClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(OtakuDeepBlack)
      .statusBarsPadding()
      .displayCutoutPadding()
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.weight(1f)
    ) {
      IconButton(
        onClick = onBackClick,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(OtakuCardSurface)
          .testTag("player_back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = TextPrimary,
          modifier = Modifier.size(20.dp)
        )
      }

      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
          color = TextPrimary,
          fontWeight = FontWeight.Bold,
          fontSize = 15.5.sp
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    BrandingBadge()
  }
}

@Composable
private fun PlayerControlsBar(
  streamType: StreamType,
  isFavorited: Boolean,
  onToggleFavorite: () -> Unit,
  onToggleStreamType: () -> Unit,
  onPrevClick: () -> Unit,
  onNextClick: () -> Unit,
  onEnterPiP: () -> Unit,
  onFullscreenClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(OtakuCharcoal)
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // SUB / DUB Toggle Pill
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = OtakuCardSurface,
      border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
      modifier = Modifier
        .clickable { onToggleStreamType() }
        .testTag("toggle_sub_dub_button")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text(
          text = "AUDIO:",
          style = MaterialTheme.typography.labelSmall.copy(
            color = TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        )
        Text(
          text = streamType.label,
          style = MaterialTheme.typography.labelMedium.copy(
            color = if (streamType == StreamType.SUB) NeonPurpleLight else EmeraldDub,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        )
      }
    }

    // Favorite + Prev / Next + PiP + Fullscreen Buttons
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      IconButton(
        onClick = onToggleFavorite,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(if (isFavorited) NeonPurple.copy(alpha = 0.2f) else OtakuCardSurface)
          .testTag("player_favorite_button")
      ) {
        Icon(
          imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
          contentDescription = if (isFavorited) "Remove favorite episode" else "Favorite episode",
          tint = if (isFavorited) NeonPurpleLight else TextPrimary,
          modifier = Modifier.size(18.dp)
        )
      }

      IconButton(
        onClick = onPrevClick,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(OtakuCardSurface)
          .testTag("prev_episode_button")
      ) {
        Icon(
          imageVector = Icons.Default.SkipPrevious,
          contentDescription = "Previous Episode",
          tint = TextPrimary,
          modifier = Modifier.size(20.dp)
        )
      }

      IconButton(
        onClick = onNextClick,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(OtakuCardSurface)
          .testTag("next_episode_button")
      ) {
        Icon(
          imageVector = Icons.Default.SkipNext,
          contentDescription = "Next Episode",
          tint = TextPrimary,
          modifier = Modifier.size(20.dp)
        )
      }

      IconButton(
        onClick = onEnterPiP,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(OtakuCardSurface)
          .testTag("pip_button")
      ) {
        Icon(
          imageVector = Icons.Default.PictureInPictureAlt,
          contentDescription = "Picture in Picture Mode",
          tint = TextPrimary,
          modifier = Modifier.size(18.dp)
        )
      }

      IconButton(
        onClick = onFullscreenClick,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(OtakuCardSurface)
          .testTag("fullscreen_button")
      ) {
        Icon(
          imageVector = Icons.Default.Fullscreen,
          contentDescription = "Fullscreen",
          tint = TextPrimary,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
private fun EpisodeInfoCard(
  seriesTitle: String,
  currentEpisode: EpisodeItem?,
  streamType: StreamType,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(14.dp)

  Card(
    shape = shape,
    colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
    border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = seriesTitle,
          style = MaterialTheme.typography.titleMedium.copy(
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (streamType == StreamType.SUB) NeonPurple else EmeraldDub)
            .padding(horizontal = 7.dp, vertical = 2.5.dp)
        ) {
          Text(
            text = streamType.label,
            style = MaterialTheme.typography.labelSmall.copy(
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      val epNumber = currentEpisode?.displayEpisodeNumber() ?: "1"
      val epTitle = currentEpisode?.title?.takeIf { it != "Full" } ?: "Episode $epNumber"

      Text(
        text = "Episode $epNumber: $epTitle",
        style = MaterialTheme.typography.bodyMedium.copy(
          color = NeonPurpleLight,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.5.sp
        )
      )

      if (!currentEpisode?.updatedAt.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Updated: ${currentEpisode?.updatedAt}",
          style = MaterialTheme.typography.bodySmall.copy(
            color = TextMuted,
            fontSize = 11.sp
          )
        )
      }
    }
  }
}

@Composable
private fun EpisodePill(
  episode: EpisodeItem,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val shape = RoundedCornerShape(10.dp)

  Box(
    modifier = Modifier
      .clip(shape)
      .background(if (isSelected) NeonPurple else OtakuCardSurface)
      .border(
        width = 1.dp,
        color = if (isSelected) NeonPurpleLight else OtakuCardBorder,
        shape = shape
      )
      .clickable { onClick() }
      .padding(horizontal = 14.dp, vertical = 10.dp)
      .testTag("episode_pill_${episode.id}"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "EP ${episode.displayEpisodeNumber()}",
      style = MaterialTheme.typography.labelMedium.copy(
        color = if (isSelected) Color.White else TextSecondary,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        fontSize = 12.5.sp
      )
    )
  }
}
