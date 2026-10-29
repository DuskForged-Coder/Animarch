package com.example.ui.screens

import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.example.data.repository.WatchStatus
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.EpisodeItem
import com.example.ui.components.AnimeHorizontalSection
import com.example.ui.components.BrandingFooter
import com.example.ui.components.OtakuSearchBar
import com.example.ui.theme.AmberRating
import com.example.ui.theme.EmeraldDub
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.OtakuCardBorder
import com.example.ui.theme.OtakuCardSurface
import com.example.ui.theme.OtakuDeepBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.DetailUiState
import com.example.ui.viewmodel.DetailViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
  viewModel: DetailViewModel,
  onBackClick: () -> Unit,
  onAnimeClick: (Long) -> Unit = {},
  onEpisodeClick: (animeId: Long, seriesTitle: String, episodeId: Long) -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val isAscending by viewModel.isAscending.collectAsState()
  val episodeFilterQuery by viewModel.episodeFilterQuery.collectAsState()
  val episodes by viewModel.processedEpisodes.collectAsState()
  val favoriteEpisodeIds by viewModel.favoriteEpisodeIds.collectAsState()
  val relatedAnime by viewModel.relatedAnime.collectAsState()
  val recommendedAnime by viewModel.recommendedAnime.collectAsState()
  val bookmarks by viewModel.bookmarks.collectAsState()

  Scaffold(
    containerColor = OtakuDeepBlack,
    modifier = modifier.testTag("detail_screen")
  ) { innerPadding ->
    when (val state = uiState) {
      is DetailUiState.Loading -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            CircularProgressIndicator(color = NeonPurple)
            Text(
              text = "Loading series details & episodes...",
              style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
          }
        }
      }

      is DetailUiState.Error -> {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Text(
              text = "Failed to load details",
              style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary)
            )
            Text(
              text = state.message,
              style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted),
              fontSize = 13.sp
            )
            Button(
              onClick = { viewModel.loadSeriesDetails() },
              colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Retry")
            }
          }
        }
      }

      is DetailUiState.Success -> {
        val anime = state.anime
        val isBookmarked = viewModel.isBookmarked()
        val watchStatusMap by viewModel.watchStatusMap.collectAsState()
        var isSynopsisExpanded by remember { mutableStateOf(false) }
        var showStatusMenu by remember { mutableStateOf(false) }

        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .navigationBarsPadding()
        ) {
          // Hero Backdrop & Top Bar
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
            ) {
              val backdropUrl = anime.backgroundImage ?: anime.poster
              AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                  .data(backdropUrl)
                  .crossfade(true)
                  .build(),
                contentDescription = anime.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )

              // Vignette Gradient
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(
                        OtakuDeepBlack.copy(alpha = 0.6f),
                        Color.Transparent,
                        OtakuDeepBlack
                      )
                    )
                  )
              )

              // Top Bar Navigation
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .statusBarsPadding()
                  .displayCutoutPadding()
                  .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                IconButton(
                  onClick = onBackClick,
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(OtakuDeepBlack.copy(alpha = 0.75f))
                    .testTag("detail_back_button")
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate Back",
                    tint = TextPrimary
                  )
                }

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  // Watch Status Selector Pill
                  val currentStatus = watchStatusMap[anime.id] ?: WatchStatus.NONE
                  Box {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(OtakuDeepBlack.copy(alpha = 0.85f))
                        .border(1.dp, OtakuCardBorder, RoundedCornerShape(20.dp))
                        .clickable { showStatusMenu = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                      ) {
                        Text(
                          text = if (currentStatus == WatchStatus.NONE) "+ Library" else currentStatus.label,
                          style = MaterialTheme.typography.labelMedium.copy(
                            color = if (currentStatus == WatchStatus.NONE) TextSecondary else NeonPurpleLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                          )
                        )
                        Icon(
                          imageVector = Icons.Default.KeyboardArrowDown,
                          contentDescription = "Select status",
                          tint = TextMuted,
                          modifier = Modifier.size(16.dp)
                        )
                      }
                    }

                    DropdownMenu(
                      expanded = showStatusMenu,
                      onDismissRequest = { showStatusMenu = false },
                      modifier = Modifier.background(OtakuCardSurface)
                    ) {
                      WatchStatus.entries.forEach { status ->
                        DropdownMenuItem(
                          text = {
                            Text(
                              text = if (status == WatchStatus.NONE) "Clear Status" else status.label,
                              style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (status == currentStatus) NeonPurpleLight else TextPrimary,
                                fontWeight = if (status == currentStatus) FontWeight.Bold else FontWeight.Medium
                              )
                            )
                          },
                          onClick = {
                            viewModel.setWatchStatus(status)
                            showStatusMenu = false
                          }
                        )
                      }
                    }
                  }

                  IconButton(
                    onClick = { viewModel.toggleBookmark() },
                    modifier = Modifier
                      .size(40.dp)
                      .clip(CircleShape)
                      .background(OtakuDeepBlack.copy(alpha = 0.75f))
                      .testTag("detail_bookmark_button")
                  ) {
                    Icon(
                      imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                      contentDescription = if (isBookmarked) "Unbookmark" else "Bookmark",
                      tint = if (isBookmarked) NeonPurpleLight else TextPrimary
                    )
                  }
                }
              }
            }
          }

          // Series Information Card
          item {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                // Poster
                Card(
                  shape = RoundedCornerShape(14.dp),
                  border = androidx.compose.foundation.BorderStroke(1.5.dp, OtakuCardBorder),
                  colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
                  elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                  modifier = Modifier
                    .width(110.dp)
                    .aspectRatio(0.70f)
                ) {
                  AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                      .data(anime.poster)
                      .crossfade(true)
                      .build(),
                    contentDescription = anime.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                  )
                }

                // Metadata Column
                Column(
                  modifier = Modifier.weight(1f)
                ) {
                  Text(
                    text = anime.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                      color = TextPrimary,
                      fontWeight = FontWeight.Bold,
                      fontSize = 20.sp,
                      lineHeight = 25.sp
                    )
                  )

                  if (!anime.nativeTitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                      text = anime.nativeTitle,
                      style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 12.sp
                      )
                    )
                  }

                  Spacer(modifier = Modifier.height(10.dp))

                  // Score & Badges
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    if (!anime.score.isNullOrBlank()) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                          .clip(RoundedCornerShape(6.dp))
                          .background(OtakuCardSurface)
                          .border(0.8.dp, OtakuCardBorder, RoundedCornerShape(6.dp))
                          .padding(horizontal = 8.dp, vertical = 4.dp)
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
                            fontSize = 12.sp
                          )
                        )
                      }
                    }

                    if (!anime.status.isNullOrBlank()) {
                      Text(
                        text = anime.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                          color = NeonPurpleLight,
                          fontWeight = FontWeight.SemiBold,
                          fontSize = 11.5.sp
                        ),
                        modifier = Modifier
                          .clip(RoundedCornerShape(6.dp))
                          .background(OtakuCardSurface)
                          .border(0.8.dp, OtakuCardBorder, RoundedCornerShape(6.dp))
                          .padding(horizontal = 8.dp, vertical = 4.dp)
                      )
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Watch First Episode Button
              val firstEp = state.episodes.firstOrNull()
              if (firstEp != null) {
                Button(
                  onClick = {
                    onEpisodeClick(anime.id, anime.title, firstEp.id)
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("start_watching_button")
                ) {
                  Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Watch Episode ${firstEp.displayEpisodeNumber()}",
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      fontSize = 15.sp
                    )
                  )
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Genre Chips
              val genres = anime.termsByType?.genre.orEmpty()
              if (genres.isNotEmpty()) {
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  genres.forEach { genre ->
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(OtakuCardSurface)
                        .border(1.dp, OtakuCardBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                      Text(
                        text = genre,
                        style = MaterialTheme.typography.labelSmall.copy(
                          color = TextSecondary,
                          fontSize = 11.5.sp
                        )
                      )
                    }
                  }
                }
              }

              // Synopsis Section
              val rawDesc = anime.description.orEmpty()
              if (rawDesc.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                val cleanDesc = Html.fromHtml(rawDesc, Html.FROM_HTML_MODE_COMPACT).toString().trim()

                Text(
                  text = "Synopsis",
                  style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.5.sp
                  )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = cleanDesc,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp
                  ),
                  maxLines = if (isSynopsisExpanded) Int.MAX_VALUE else 3,
                  overflow = TextOverflow.Ellipsis
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .clickable { isSynopsisExpanded = !isSynopsisExpanded }
                    .padding(vertical = 6.dp)
                ) {
                  Text(
                    text = if (isSynopsisExpanded) "Show Less" else "Read More",
                    style = MaterialTheme.typography.labelMedium.copy(
                      color = NeonPurpleLight,
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.5.sp
                    )
                  )
                  Spacer(modifier = Modifier.width(2.dp))
                  Icon(
                    imageVector = if (isSynopsisExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = NeonPurpleLight,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(16.dp))
            }
          }

          // Episodes List Header & Filter
          item {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Episodes (${state.episodes.size})",
                  style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                  )
                )

                IconButton(
                  onClick = { viewModel.toggleSortOrder() },
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(OtakuCardSurface)
                    .border(1.dp, OtakuCardBorder, CircleShape)
                    .testTag("sort_episodes_button")
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = if (isAscending) "Sort Descending" else "Sort Ascending",
                    tint = NeonPurpleLight,
                    modifier = Modifier.size(20.dp)
                  )
                }
              }

              if (state.episodes.size > 8) {
                Spacer(modifier = Modifier.height(8.dp))
                OtakuSearchBar(
                  query = episodeFilterQuery,
                  onQueryChange = { viewModel.onEpisodeQueryChanged(it) },
                  placeholder = "Filter episode # or title..."
                )
              }

              Spacer(modifier = Modifier.height(10.dp))
            }
          }

          // Episode List Items
          if (episodes.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(32.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "No episodes match your filter.",
                  style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                )
              }
            }
          } else {
            items(
              items = episodes,
              key = { it.id }
            ) { ep ->
              EpisodeCardItem(
                episode = ep,
                isFavorited = favoriteEpisodeIds.contains(ep.id),
                onFavoriteClick = { viewModel.toggleFavoriteEpisode(ep, anime) },
                onClick = {
                  onEpisodeClick(anime.id, anime.title, ep.id)
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
              )
            }
          }

          // Related Anime Section
          if (relatedAnime.isNotEmpty()) {
            item {
              AnimeHorizontalSection(
                title = "Related & Sequel Series",
                subtitle = "Prequels, sequels, movies & spin-offs",
                animeList = relatedAnime,
                bookmarks = bookmarks,
                onAnimeClick = onAnimeClick,
                onBookmarkClick = { viewModel.toggleBookmarkForAnime(it) },
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
              )
            }
          }

          // Recommended Anime Section
          if (recommendedAnime.isNotEmpty()) {
            item {
              AnimeHorizontalSection(
                title = "Recommended Anime",
                subtitle = "You might also enjoy watching",
                animeList = recommendedAnime,
                bookmarks = bookmarks,
                onAnimeClick = onAnimeClick,
                onBookmarkClick = { viewModel.toggleBookmarkForAnime(it) },
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
              )
            }
          }

          // Branding Footer at bottom of Details
          item {
            BrandingFooter()
          }
        }
      }
    }
  }
}

@Composable
private fun EpisodeCardItem(
  episode: EpisodeItem,
  isFavorited: Boolean,
  onFavoriteClick: () -> Unit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(14.dp)

  Card(
    shape = shape,
    colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
    border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("episode_card_${episode.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        // Episode Number Badge
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(OtakuDeepBlack)
            .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = episode.displayEpisodeNumber(),
            style = MaterialTheme.typography.titleMedium.copy(
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 14.5.sp
            )
          )
        }

        // Episode Title
        Column {
          val epTitle = if (!episode.title.isNullOrBlank() && episode.title != "Full") {
            episode.title
          } else {
            "Episode ${episode.displayEpisodeNumber()}"
          }

          Text(
            text = epTitle,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = TextPrimary,
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.5.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Spacer(modifier = Modifier.height(3.dp))

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (episode.embedUrl?.sub != null || episode.episodeEmbedId != null) {
              Text(
                text = "SUB",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = NeonPurpleLight,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp
                )
              )
            }
            if (episode.embedUrl?.dub != null) {
              Text(
                text = "DUB",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = EmeraldDub,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp
                )
              )
            }
          }
        }
      }

      // Actions: Favorite Heart & Play
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        IconButton(
          onClick = onFavoriteClick,
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (isFavorited) NeonPurple.copy(alpha = 0.2f) else OtakuDeepBlack.copy(alpha = 0.5f))
            .testTag("favorite_episode_${episode.id}")
        ) {
          Icon(
            imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isFavorited) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorited) NeonPurpleLight else TextMuted,
            modifier = Modifier.size(18.dp)
          )
        }

        // Play Icon Button
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(NeonPurple.copy(alpha = 0.15f))
            .border(1.dp, NeonPurple.copy(alpha = 0.6f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play Episode",
            tint = NeonPurple,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}
