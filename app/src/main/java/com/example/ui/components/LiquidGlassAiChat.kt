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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import com.example.data.model.AiRecommendationResult
import com.example.data.model.MessageSender
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleDark
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.OtakuCardBorder
import com.example.ui.theme.OtakuCardSurface
import com.example.ui.theme.OtakuDeepBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AnimarchAiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidGlassAiChatSheet(
  viewModel: AnimarchAiViewModel,
  onDismiss: () -> Unit,
  onAnimeClick: (Long) -> Unit,
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val chatHistory by viewModel.chatHistory.collectAsState()
  val isProcessing by viewModel.isProcessing.collectAsState()
  val userQuery by viewModel.userQuery.collectAsState()

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color(0xED141326), // Translucent Liquid Glass Graphite
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    modifier = modifier.testTag("liquid_glass_ai_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      // Liquid Glass Assistant Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(NeonPurple.copy(alpha = 0.25f))
              .border(1.dp, NeonPurpleLight, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = "ANIMARCH AI",
              tint = NeonPurpleLight,
              modifier = Modifier.size(20.dp)
            )
          }

          Column {
            Text(
              text = "ANIMARCH AI ASSISTANT",
              style = MaterialTheme.typography.titleMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                fontSize = 16.sp
              )
            )
            Text(
              text = "Hybrid Recommendation Engine",
              style = MaterialTheme.typography.labelSmall.copy(
                color = NeonPurpleLight,
                fontSize = 10.5.sp
              )
            )
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          IconButton(
            onClick = { viewModel.clearChat() },
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(OtakuCardSurface)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Reset Chat",
              tint = TextMuted,
              modifier = Modifier.size(18.dp)
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(OtakuCardSurface)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = TextMuted,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // Chat Messages Stream
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
      ) {
        items(chatHistory, key = { it.id }) { msg ->
          if (msg.sender == MessageSender.USER) {
            // User Message Bubble
            Box(
              modifier = Modifier.fillMaxWidth(),
              contentAlignment = Alignment.CenterEnd
            ) {
              Surface(
                shape = RoundedCornerShape(18.dp),
                color = NeonPurple,
                modifier = Modifier.fillMaxWidth(0.85f)
              ) {
                Text(
                  text = msg.text,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    fontSize = 14.sp
                  ),
                  modifier = Modifier.padding(14.dp)
                )
              }
            }
          } else {
            // AI Response Bubble
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Surface(
                shape = RoundedCornerShape(18.dp),
                color = OtakuCardSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Text(
                    text = msg.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      color = TextPrimary,
                      fontSize = 14.sp,
                      lineHeight = 20.sp
                    )
                  )

                  // Interpreted Filter Chips
                  if (msg.structuredFilterChips.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                      text = "INTERPRETED FILTERS:",
                      style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                      )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                      items(msg.structuredFilterChips) { chip ->
                        Box(
                          modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonPurpleDark.copy(alpha = 0.5f))
                            .border(0.8.dp, NeonPurpleLight, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                          Text(
                            text = chip,
                            style = MaterialTheme.typography.labelSmall.copy(
                              color = NeonPurpleLight,
                              fontWeight = FontWeight.Bold,
                              fontSize = 10.5.sp
                            )
                          )
                        }
                      }
                    }
                  }
                }
              }

              // Ranked Recommendation Cards Grid
              if (msg.recommendations.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                  msg.recommendations.forEach { rec ->
                    AiRecommendationCard(
                      item = rec,
                      onClick = { onAnimeClick(rec.anime.id) }
                    )
                  }
                }
              }
            }
          }
        }

        if (isProcessing) {
          item {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.padding(8.dp)
            ) {
              CircularProgressIndicator(color = NeonPurpleLight, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
              Text(
                text = "Parsing intent & computing hybrid scores...",
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 12.sp)
              )
            }
          }
        }
      }

      // Query Input Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = userQuery,
          onValueChange = { viewModel.onQueryChanged(it) },
          placeholder = { Text("E.g., Dark psychological thriller, ≤24 eps, 2024+", color = TextMuted, fontSize = 13.sp) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = OtakuDeepBlack,
            unfocusedContainerColor = OtakuDeepBlack,
            focusedBorderColor = NeonPurple,
            unfocusedBorderColor = OtakuCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.weight(1f)
        )

        IconButton(
          onClick = { viewModel.sendMessage(userQuery) },
          enabled = userQuery.isNotBlank() && !isProcessing,
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (userQuery.isNotBlank()) NeonPurple else OtakuCardSurface)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Send Query",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun AiRecommendationCard(
  item: AiRecommendationResult,
  onClick: () -> Unit
) {
  val anime = item.anime
  val matchPercent = (item.score.totalScore).toInt().coerceIn(1, 100)

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
    border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Poster Thumbnail
      Card(
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
        modifier = Modifier
          .width(72.dp)
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

      // Details & Explanation
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = anime.title,
            style = MaterialTheme.typography.titleSmall.copy(
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )

          // Match Score Pill
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(NeonPurple.copy(alpha = 0.25f))
              .border(0.8.dp, NeonPurpleLight, RoundedCornerShape(6.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "$matchPercent% Match",
              style = MaterialTheme.typography.labelSmall.copy(
                color = NeonPurpleLight,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
            )
          }
        }

        // Verified Explanation
        Text(
          text = item.matchExplanation,
          style = MaterialTheme.typography.bodySmall.copy(
            color = TextSecondary,
            fontSize = 11.5.sp,
            lineHeight = 15.sp
          ),
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.padding(top = 2.dp)
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = NeonPurpleLight,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "Tap to view series",
            style = MaterialTheme.typography.labelSmall.copy(
              color = NeonPurpleLight,
              fontWeight = FontWeight.Bold,
              fontSize = 10.5.sp
            )
          )
        }
      }
    }
  }
}
