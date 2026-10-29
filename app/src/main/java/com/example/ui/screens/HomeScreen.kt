package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.local.entity.WatchHistoryEntity
import com.example.data.model.AnimeItem
import com.example.data.model.LevelInfo
import com.example.data.repository.GamificationRepository
import com.example.ui.components.AnimeCard
import com.example.ui.components.BrandingFooter
import com.example.ui.components.GenreFilterRow
import com.example.ui.components.OtakuSearchBar
import com.example.ui.theme.AmberRating
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.OtakuCardBorder
import com.example.ui.theme.OtakuCardSurface
import com.example.ui.theme.OtakuDeepBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HomeUiState
import com.example.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
  viewModel: HomeViewModel,
  gamificationRepository: GamificationRepository,
  onAnimeClick: (Long) -> Unit,
  onEpisodeClick: (animeId: Long, seriesTitle: String, episodeId: Long) -> Unit,
  onProfileClick: () -> Unit = {},
  onAiClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedGenre by viewModel.selectedGenre.collectAsState()
  val selectedStatus by viewModel.selectedStatus.collectAsState()
  val showOnlyBookmarks by viewModel.showOnlyBookmarks.collectAsState()
  val filteredList by viewModel.filteredAnimeList.collectAsState()
  val bookmarks by viewModel.bookmarks.collectAsState()
  val watchHistory by viewModel.watchHistory.collectAsState()
  val isSearchingApi by viewModel.isSearchingApi.collectAsState()

  Scaffold(
    topBar = {
      HomeTopBar(
        onRefresh = { viewModel.loadRecentAnime() },
        onProfileClick = onProfileClick,
        onAiClick = onAiClick
      )
    },
    containerColor = OtakuDeepBlack,
    modifier = modifier.testTag("home_screen")
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .navigationBarsPadding()
    ) {
      // Search Bar directly beneath compact header
      OtakuSearchBar(
        query = searchQuery,
        onQueryChange = { viewModel.onSearchQueryChanged(it) },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
      )

      // Compact Watch-Status & Genre Filter Row
      GenreFilterRow(
        selectedGenre = selectedGenre,
        onGenreSelect = { viewModel.onGenreSelected(it) },
        showOnlyBookmarks = showOnlyBookmarks,
        onToggleBookmarks = { viewModel.toggleBookmarksFilter() },
        selectedStatus = selectedStatus,
        onStatusSelect = { viewModel.onStatusFilterSelected(it) },
        modifier = Modifier.padding(bottom = 6.dp)
      )

      // Main Content Area
      when (val state = uiState) {
        is HomeUiState.Loading -> {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              CircularProgressIndicator(
                color = NeonPurple,
                modifier = Modifier.testTag("loading_indicator")
              )
              Text(
                text = "Fetching anime catalogue...",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
              )
            }
          }
        }

        is HomeUiState.Error -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
              Text(
                text = "Failed to load anime",
                style = MaterialTheme.typography.titleMedium.copy(
                  color = TextPrimary,
                  fontWeight = FontWeight.Bold
                )
              )
              Text(
                text = state.message,
                style = MaterialTheme.typography.bodyMedium.copy(
                  color = TextMuted,
                  fontSize = 13.sp
                ),
                maxLines = 3
              )
              Button(
                onClick = { viewModel.loadRecentAnime() },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("retry_button")
              ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
              }
            }
          }
        }

        is HomeUiState.Success -> {
          if (filteredList.isEmpty() && watchHistory.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Text(
                  text = if (showOnlyBookmarks) "No bookmarked anime yet" else "No anime found",
                  style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                  )
                )
                Text(
                  text = if (showOnlyBookmarks) {
                    "Tap the bookmark icon on any anime card to save it here."
                  } else {
                    "Try adjusting your search query or status filter."
                  },
                  style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
                  fontSize = 13.sp
                )
              }
            }
          } else {
            LazyVerticalGrid(
              columns = GridCells.Adaptive(minSize = 150.dp),
              contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 110.dp),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp),
              modifier = Modifier
                .fillMaxSize()
                .testTag("recent_anime_grid")
            ) {
              // 1. FEATURED ANIME - SHOW THIS EARLY AT THE TOP!
              if (searchQuery.isBlank() && selectedGenre == "All" && !showOnlyBookmarks) {
                val featuredAnime = filteredList.firstOrNull()
                if (featuredAnime != null) {
                  item(span = { GridItemSpan(maxLineSpan) }) {
                    FeaturedHeroBanner(
                      anime = featuredAnime,
                      onWatchClick = { onAnimeClick(featuredAnime.id) }
                    )
                  }
                }
              }

              // 2. CONTINUE WATCHING (When user has viewing progress)
              if (watchHistory.isNotEmpty() && searchQuery.isBlank()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                  ContinueWatchingSection(
                    history = watchHistory,
                    onItemClick = { item ->
                      onEpisodeClick(item.animeId, item.seriesTitle, item.episodeId)
                    },
                    onRemoveItem = { item ->
                      viewModel.removeHistoryItem(item.episodeId)
                    }
                  )
                }
              }

              // 3. COMPACT ANIMARCH ASCENSION SUMMARY BAR
              if (searchQuery.isBlank() && selectedGenre == "All" && !showOnlyBookmarks) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                  val levelInfo by gamificationRepository.levelInfo.collectAsState()

                  CompactAscensionSummaryBar(
                    levelInfo = levelInfo,
                    onProfileClick = onProfileClick,
                    modifier = Modifier.padding(vertical = 2.dp)
                  )
                }
              }

              // 4. SECTION HEADER
              item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = when {
                      showOnlyBookmarks -> "Saved Collection (${filteredList.size})"
                      searchQuery.isNotBlank() -> "Search Results (${filteredList.size})"
                      selectedGenre != "All" -> "$selectedGenre Anime (${filteredList.size})"
                      else -> "Recent Releases"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                      color = TextPrimary,
                      fontWeight = FontWeight.Bold,
                      fontSize = 17.sp
                    )
                  )

                  if (isSearchingApi) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      CircularProgressIndicator(
                        color = NeonPurple,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(12.dp)
                      )
                      Text(
                        text = "Searching API...",
                        style = MaterialTheme.typography.labelSmall.copy(
                          color = NeonPurpleLight,
                          fontSize = 11.sp
                        )
                      )
                    }
                  } else {
                    Text(
                      text = if (searchQuery.isNotBlank()) "Global Database" else "Live Catalog",
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = NeonPurpleLight,
                        fontWeight = FontWeight.SemiBold
                      )
                    )
                  }
                }
              }

              // 5. ANIME CARDS GRID
              items(
                items = filteredList,
                key = { it.id }
              ) { anime ->
                AnimeCard(
                  anime = anime,
                  isBookmarked = bookmarks.contains(anime.id),
                  onClick = { onAnimeClick(anime.id) },
                  onBookmarkClick = { viewModel.toggleBookmark(anime.id) }
                )
              }

              // 6. BRANDING FOOTER AT BOTTOM
              item(span = { GridItemSpan(maxLineSpan) }) {
                BrandingFooter()
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CompactAscensionSummaryBar(
  levelInfo: LevelInfo,
  onProfileClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
    border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
    modifier = modifier
      .fillMaxWidth()
      .clickable { onProfileClick() }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = NeonPurpleLight,
          modifier = Modifier.size(18.dp)
        )
        Text(
          text = "Level ${levelInfo.level} ${levelInfo.title}",
          style = MaterialTheme.typography.titleSmall.copy(
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp
          )
        )
        Text(
          text = "•",
          color = TextMuted,
          fontSize = 12.sp
        )
        Text(
          text = "${levelInfo.xpInCurrentLevel}/${levelInfo.xpForNextLevel} XP",
          style = MaterialTheme.typography.labelSmall.copy(
            color = NeonPurpleLight,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
          )
        )
      }

      Text(
        text = "View Journey →",
        style = MaterialTheme.typography.labelSmall.copy(
          color = NeonPurpleLight,
          fontWeight = FontWeight.Bold,
          fontSize = 11.5.sp
        )
      )
    }
  }
}

@Composable
private fun ContinueWatchingSection(
  history: List<WatchHistoryEntity>,
  onItemClick: (WatchHistoryEntity) -> Unit,
  onRemoveItem: (WatchHistoryEntity) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
      .testTag("continue_watching_section")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(
          imageVector = Icons.Default.History,
          contentDescription = null,
          tint = NeonPurple,
          modifier = Modifier.size(18.dp)
        )
        Text(
          text = "Continue Watching",
          style = MaterialTheme.typography.titleMedium.copy(
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.5.sp
          )
        )
      }

      Text(
        text = "${history.size} in History",
        style = MaterialTheme.typography.labelSmall.copy(
          color = TextMuted,
          fontSize = 11.sp
        )
      )
    }

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(end = 8.dp)
    ) {
      items(history, key = { it.episodeId }) { item ->
        ContinueWatchingCard(
          item = item,
          onClick = { onItemClick(item) },
          onRemove = { onRemoveItem(item) }
        )
      }
    }
  }
}

@Composable
private fun ContinueWatchingCard(
  item: WatchHistoryEntity,
  onClick: () -> Unit,
  onRemove: () -> Unit
) {
  val shape = RoundedCornerShape(14.dp)

  Card(
    shape = shape,
    colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
    border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
    modifier = Modifier
      .width(210.dp)
      .clickable { onClick() }
      .testTag("history_card_${item.episodeId}")
  ) {
    Column {
      // Thumbnail Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(115.dp)
          .background(OtakuDeepBlack)
      ) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(item.posterUrl)
            .crossfade(true)
            .build(),
          contentDescription = item.seriesTitle,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Black.copy(alpha = 0.3f),
                  Color.Black.copy(alpha = 0.85f)
                )
              )
            )
        )

        Box(
          modifier = Modifier
            .align(Alignment.Center)
            .size(36.dp)
            .clip(CircleShape)
            .background(NeonPurple.copy(alpha = 0.9f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Resume",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }

        IconButton(
          onClick = onRemove,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(6.dp)
            .size(24.dp)
            .clip(CircleShape)
            .background(OtakuDeepBlack.copy(alpha = 0.75f))
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove from history",
            tint = TextMuted,
            modifier = Modifier.size(14.dp)
          )
        }

        LinearProgressIndicator(
          progress = { item.progressPercentage },
          color = NeonPurple,
          trackColor = OtakuDeepBlack.copy(alpha = 0.7f),
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(3.dp)
        )
      }

      Column(modifier = Modifier.padding(10.dp)) {
        Text(
          text = item.seriesTitle,
          style = MaterialTheme.typography.bodyMedium.copy(
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.5.sp
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "EP ${item.episodeNumber}",
            style = MaterialTheme.typography.labelSmall.copy(
              color = NeonPurpleLight,
              fontWeight = FontWeight.SemiBold,
              fontSize = 11.sp
            )
          )

          Text(
            text = item.streamType,
            style = MaterialTheme.typography.labelSmall.copy(
              color = TextMuted,
              fontSize = 10.5.sp
            )
          )
        }
      }
    }
  }
}

@Composable
private fun HomeTopBar(
  onRefresh: () -> Unit,
  onProfileClick: () -> Unit,
  onAiClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(OtakuDeepBlack)
      .statusBarsPadding()
      .displayCutoutPadding()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .border(1.5.dp, NeonPurple, CircleShape)
          .clickable { onProfileClick() }
      ) {
        Image(
          painter = painterResource(id = R.drawable.ic_animarch_logo_1791862659045),
          contentDescription = "Animarch Logo",
          modifier = Modifier.fillMaxSize()
        )
      }

      Column(
        modifier = Modifier.clickable { onProfileClick() }
      ) {
        Text(
          text = "ANIMARCH",
          style = MaterialTheme.typography.titleMedium.copy(
            color = TextPrimary,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            fontSize = 16.5.sp
          )
        )
        Text(
          text = "Anime Streaming Hub",
          style = MaterialTheme.typography.labelSmall.copy(
            color = NeonPurpleLight,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium
          )
        )
      }
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Liquid Glass AI Assistant Trigger Button
      IconButton(
        onClick = onAiClick,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(NeonPurple.copy(alpha = 0.25f))
          .border(1.dp, NeonPurpleLight, CircleShape)
          .testTag("ai_assistant_button")
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = "AI Assistant",
          tint = NeonPurpleLight,
          modifier = Modifier.size(18.dp)
        )
      }

      IconButton(
        onClick = onProfileClick,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(OtakuCardSurface)
          .testTag("profile_button")
      ) {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = "Profile",
          tint = TextPrimary,
          modifier = Modifier.size(18.dp)
        )
      }

      IconButton(
        onClick = onRefresh,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(OtakuCardSurface)
          .testTag("refresh_home_button")
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Refresh Anime List",
          tint = TextPrimary,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

@Composable
private fun FeaturedHeroBanner(
  anime: AnimeItem,
  onWatchClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val bannerShape = RoundedCornerShape(18.dp)

  Card(
    shape = bannerShape,
    colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
    border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
    modifier = modifier
      .fillMaxWidth()
      .height(210.dp)
      .clip(bannerShape)
      .clickable { onWatchClick() }
      .testTag("featured_hero_banner")
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      val imageUrl = anime.backgroundImage ?: anime.poster
      AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
          .data(imageUrl)
          .crossfade(true)
          .build(),
        contentDescription = anime.title,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )

      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color.Transparent,
                OtakuDeepBlack.copy(alpha = 0.5f),
                OtakuDeepBlack.copy(alpha = 0.95f)
              ),
              startY = 0f,
              endY = Float.POSITIVE_INFINITY
            )
          )
      )

      Column(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(16.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(NeonPurple)
              .padding(horizontal = 7.dp, vertical = 2.5.dp)
          ) {
            Text(
              text = "FEATURED",
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
                letterSpacing = 0.5.sp
              )
            )
          }

          if (!anime.score.isNullOrBlank()) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = AmberRating,
                modifier = Modifier.size(13.dp)
              )
              Text(
                text = anime.score,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = AmberRating,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.5.sp
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = anime.title,
          style = MaterialTheme.typography.titleMedium.copy(
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Button(
            onClick = onWatchClick,
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            modifier = Modifier
              .height(36.dp)
              .testTag("hero_watch_button")
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Watch Now",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp
              )
            )
          }

          val genres = anime.termsByType?.genre?.take(2)?.joinToString(" • ") ?: "Recent Release"
          Text(
            text = genres,
            style = MaterialTheme.typography.bodySmall.copy(
              color = TextSecondary,
              fontSize = 11.5.sp
            )
          )
        }
      }
    }
  }
}
