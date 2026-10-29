package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LevelInfo
import com.example.data.model.QuestItem
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleDark
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.OtakuCardBorder
import com.example.ui.theme.OtakuCardSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AscensionBannerWidget(
  levelInfo: LevelInfo,
  animeWatchedCount: Int,
  episodesCompletedCount: Int,
  todayQuest: QuestItem?,
  onClaimQuest: (String) -> Unit,
  onProfileClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(20.dp)

  Card(
    shape = shape,
    colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
    border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
    modifier = modifier
      .fillMaxWidth()
      .clickable { onProfileClick() }
      .testTag("ascension_banner_widget")
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
      // Top Header Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "ANIMARCH ASCENSION",
            style = MaterialTheme.typography.labelSmall.copy(
              color = NeonPurpleLight,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.2.sp,
              fontSize = 10.5.sp
            )
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Your Anime Journey",
            style = MaterialTheme.typography.titleLarge.copy(
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp
            )
          )
          Text(
            text = "Level ${levelInfo.level} • ${levelInfo.title}",
            style = MaterialTheme.typography.bodySmall.copy(
              color = TextMuted,
              fontSize = 12.5.sp
            )
          )
        }

        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(OtakuCardSurface)
            .border(1.5.dp, NeonPurple, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "Ascension Rank",
            tint = NeonPurpleLight,
            modifier = Modifier.size(24.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Level Progress Bar Box
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Level ${levelInfo.level}",
              style = MaterialTheme.typography.labelMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            )

            Text(
              text = "${levelInfo.xpInCurrentLevel} / ${levelInfo.xpForNextLevel} XP",
              style = MaterialTheme.typography.labelMedium.copy(
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
              )
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          LinearProgressIndicator(
            progress = { levelInfo.progressFraction },
            color = NeonPurple,
            trackColor = NeonPurpleDark.copy(alpha = 0.5f),
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp))
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "${levelInfo.xpRemaining} XP to Level ${levelInfo.level + 1}",
            style = MaterialTheme.typography.bodySmall.copy(
              color = TextMuted,
              fontSize = 11.sp
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Stats Cards Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Anime Watched Card
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Icon(
              imageVector = Icons.Default.Movie,
              contentDescription = null,
              tint = NeonPurpleLight,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "$animeWatchedCount",
              style = MaterialTheme.typography.titleLarge.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp
              )
            )
            Text(
              text = "Anime watched",
              style = MaterialTheme.typography.bodySmall.copy(
                color = TextMuted,
                fontSize = 11.5.sp
              )
            )
          }
        }

        // Episodes Completed Card
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Icon(
              imageVector = Icons.Default.PlayCircle,
              contentDescription = null,
              tint = NeonPurpleLight,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "$episodesCompletedCount",
              style = MaterialTheme.typography.titleLarge.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp
              )
            )
            Text(
              text = "Episodes completed",
              style = MaterialTheme.typography.bodySmall.copy(
                color = TextMuted,
                fontSize = 11.5.sp
              )
            )
          }
        }
      }

      // Today's Quest Section
      if (todayQuest != null) {
        Spacer(modifier = Modifier.height(12.dp))

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = "Today's Quest",
                  style = MaterialTheme.typography.titleSmall.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                  )
                )

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(NeonPurple.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "+${todayQuest.xpReward} XP",
                    style = MaterialTheme.typography.labelSmall.copy(
                      color = NeonPurpleLight,
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.5.sp
                    )
                  )
                }
              }

              Spacer(modifier = Modifier.height(2.dp))

              Text(
                text = todayQuest.description,
                style = MaterialTheme.typography.bodySmall.copy(
                  color = TextMuted,
                  fontSize = 12.sp
                )
              )
            }

            if (todayQuest.isCompleted && !todayQuest.isClaimed) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(NeonPurple)
                  .clickable { onClaimQuest(todayQuest.id) }
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Text(
                  text = "Claim",
                  style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                  )
                )
              }
            } else if (todayQuest.isClaimed) {
              Text(
                text = "Completed ✓",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = NeonPurpleLight,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.5.sp
                )
              )
            }
          }
        }
      }
    }
  }
}
