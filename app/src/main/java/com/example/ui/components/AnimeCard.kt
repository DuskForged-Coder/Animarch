package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.AnimeItem
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

@Composable
fun AnimeCard(
  anime: AnimeItem,
  isBookmarked: Boolean,
  onClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val cardShape = RoundedCornerShape(16.dp)

  Card(
    shape = cardShape,
    colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .clip(cardShape)
      .border(1.dp, OtakuCardBorder, cardShape)
      .clickable { onClick() }
      .testTag("anime_card_${anime.id}")
  ) {
    Column {
      // Poster Image Container (Aspect ratio ~0.70)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(0.70f)
          .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
          .background(OtakuDeepBlack)
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

        // Gradient overlay
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Black.copy(alpha = 0.4f),
                  Color.Transparent,
                  Color.Black.copy(alpha = 0.85f)
                ),
                startY = 0f,
                endY = Float.POSITIVE_INFINITY
              )
            )
        )

        // Rating badge top-left
        if (!anime.score.isNullOrBlank()) {
          Box(
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(8.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(OtakuDeepBlack.copy(alpha = 0.8f))
              .padding(horizontal = 6.dp, vertical = 3.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = AmberRating,
                modifier = Modifier.size(11.dp)
              )
              Text(
                text = anime.score,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                )
              )
            }
          }
        }

        // Bookmark button top-right
        IconButton(
          onClick = onBookmarkClick,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(6.dp)
            .size(30.dp)
            .clip(CircleShape)
            .background(OtakuDeepBlack.copy(alpha = 0.75f))
            .testTag("bookmark_button_${anime.id}")
        ) {
          Icon(
            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
            contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark anime",
            tint = if (isBookmarked) NeonPurpleLight else TextMuted,
            modifier = Modifier.size(16.dp)
          )
        }

        // Bottom chips on poster: SUB/DUB & Episode Count
        Row(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (anime.isSub == 1) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(NeonPurple.copy(alpha = 0.9f))
                  .padding(horizontal = 5.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "SUB",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                )
              }
            }
            if (anime.isDub == 1) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(EmeraldDub.copy(alpha = 0.9f))
                  .padding(horizontal = 5.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "DUB",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                )
              }
            }
          }

          val epLabel = when {
            anime.episodesCount != null && anime.episodesCount != "1" -> "Ep ${anime.episodesCount}"
            anime.termsByType?.type?.contains("Movie") == true -> "Movie"
            else -> "Ep 1"
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(OtakuDeepBlack.copy(alpha = 0.85f))
              .padding(horizontal = 5.dp, vertical = 2.dp)
          ) {
            Text(
              text = epLabel,
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
              )
            )
          }
        }
      }

      // Title & Subtitle metadata
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 10.dp)
      ) {
        Text(
          text = anime.title,
          style = MaterialTheme.typography.titleSmall.copy(
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            lineHeight = 17.sp
          ),
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        val genreLabel = anime.termsByType?.genre?.take(2)?.joinToString(" • ")
        val typeOrYear = listOfNotNull(
          anime.year?.toString(),
          anime.season,
          genreLabel
        ).firstOrNull() ?: "Anime"

        Text(
          text = typeOrYear,
          style = MaterialTheme.typography.bodySmall.copy(
            color = TextMuted,
            fontSize = 11.sp
          ),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
fun AnimeHorizontalSection(
  title: String,
  subtitle: String? = null,
  animeList: List<AnimeItem>,
  bookmarks: Set<Long>,
  onAnimeClick: (Long) -> Unit,
  onBookmarkClick: (Long) -> Unit,
  modifier: Modifier = Modifier
) {
  if (animeList.isEmpty()) return

  Column(
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
          color = TextPrimary,
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp
        )
      )
      if (!subtitle.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall.copy(
            color = TextMuted,
            fontSize = 12.sp
          )
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(animeList, key = { it.id }) { anime ->
        AnimeCard(
          anime = anime,
          isBookmarked = bookmarks.contains(anime.id),
          onClick = { onAnimeClick(anime.id) },
          onBookmarkClick = { onBookmarkClick(anime.id) },
          modifier = Modifier.width(135.dp)
        )
      }
    }
  }
}
