package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.GamificationRepository
import com.example.data.repository.WatchStatus
import com.example.ui.components.BrandingFooter
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleDark
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.OtakuCardBorder
import com.example.ui.theme.OtakuCardSurface
import com.example.ui.theme.OtakuDeepBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
  viewModel: AuthViewModel,
  gamificationRepository: GamificationRepository,
  onBackClick: () -> Unit,
  onLoginClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val levelInfo by gamificationRepository.levelInfo.collectAsState()
  val membershipRank by gamificationRepository.membershipRank.collectAsState()
  val completedEpisodes by gamificationRepository.completedEpisodeIds.collectAsState()
  val completedAnime by gamificationRepository.completedAnimeIds.collectAsState()
  val streakDays by gamificationRepository.watchStreakDays.collectAsState()
  val quests by gamificationRepository.quests.collectAsState()
  val achievements by gamificationRepository.achievements.collectAsState()

  var selectedTab by remember { mutableStateOf("Stats") }
  var showEditDialog by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }

  var editDisplayName by remember { mutableStateOf("") }
  var editBio by remember { mutableStateOf("") }

  val equippedFrameResId by gamificationRepository.equippedFrameResId.collectAsState()

  Scaffold(
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(OtakuDeepBlack)
          .statusBarsPadding()
          .displayCutoutPadding()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(OtakuCardSurface)
            .testTag("profile_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimary
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
          text = "Profile & Progression",
          style = MaterialTheme.typography.titleMedium.copy(
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
        )
      }
    },
    containerColor = OtakuDeepBlack,
    modifier = modifier.testTag("profile_screen")
  ) { innerPadding ->
    val user = currentUser
    if (user == null) {
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
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(64.dp)
          )
          Text(
            text = "You are not logged in",
            style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Log in to view your profile, sync watchlist, and track ascension stats.",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted, fontSize = 13.sp)
          )
          Button(
            onClick = onLoginClick,
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Log In / Sign Up")
          }
        }
      }
    } else {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .navigationBarsPadding()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Avatar with Live Profile Frame Overlay!
        com.example.ui.components.AvatarWithFrame(
          avatarUrl = user.avatarUrl,
          equippedFrameResId = equippedFrameResId,
          size = 112.dp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = user.displayName,
            style = MaterialTheme.typography.titleLarge.copy(
              color = TextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 22.sp
            )
          )

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(membershipRank.color.copy(alpha = 0.2f))
              .border(0.8.dp, membershipRank.color, RoundedCornerShape(6.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "${membershipRank.emblem} ${membershipRank.label}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = membershipRank.color,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp
              )
            )
          }
        }

        Text(
          text = "@${user.username} • Level ${levelInfo.level} ${levelInfo.title}",
          style = MaterialTheme.typography.bodySmall.copy(
            color = TextMuted,
            fontSize = 12.5.sp
          )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Ascension Level Progress Box
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
          border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Level ${levelInfo.level} • ${levelInfo.title}",
                style = MaterialTheme.typography.titleSmall.copy(
                  color = TextPrimary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
              )
              Text(
                text = "${levelInfo.xpInCurrentLevel} / ${levelInfo.xpForNextLevel} XP",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = NeonPurpleLight,
                  fontWeight = FontWeight.Bold,
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
              style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs: Stats, Quests & Achievements, About
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("Stats", "Quests", "Account").forEach { tab ->
            val isSelected = selectedTab == tab
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) NeonPurple else OtakuCardSurface)
                .border(1.dp, if (isSelected) NeonPurpleLight else OtakuCardBorder, RoundedCornerShape(12.dp))
                .clickable { selectedTab = tab }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = tab,
                style = MaterialTheme.typography.labelMedium.copy(
                  color = if (isSelected) Color.White else TextSecondary,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.5.sp
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
          "Stats" -> {
            // Stats Grid
            val totalEpisodes = completedEpisodes.size
            val totalAnime = completedAnime.size
            val estimatedHours = String.format(java.util.Locale.US, "%.1f", (totalEpisodes * 24) / 60.0)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                // Anime Watched
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
                  border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
                  modifier = Modifier.weight(1f)
                ) {
                  Column(modifier = Modifier.padding(14.dp)) {
                    Icon(imageVector = Icons.Default.Movie, contentDescription = null, tint = NeonPurpleLight)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "$totalAnime", style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                    Text(text = "Anime watched", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.5.sp))
                  }
                }

                // Episodes Completed
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
                  border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
                  modifier = Modifier.weight(1f)
                ) {
                  Column(modifier = Modifier.padding(14.dp)) {
                    Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = NeonPurpleLight)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "$totalEpisodes", style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                    Text(text = "Episodes completed", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.5.sp))
                  }
                }
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                // Estimated Watch Time
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
                  border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
                  modifier = Modifier.weight(1f)
                ) {
                  Column(modifier = Modifier.padding(14.dp)) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = NeonPurpleLight)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "${estimatedHours}h", style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                    Text(text = "Watch time (est.)", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.5.sp))
                  }
                }

                // Watch Streak
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
                  border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
                  modifier = Modifier.weight(1f)
                ) {
                  Column(modifier = Modifier.padding(14.dp)) {
                    Icon(imageVector = Icons.Default.Whatshot, contentDescription = null, tint = NeonPurpleLight)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "$streakDays Days", style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                    Text(text = "Watch streak", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.5.sp))
                  }
                }
              }
            }
          }

          "Quests" -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(text = "Daily Quests", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))

              quests.forEach { quest ->
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
                  border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(text = quest.title, style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(text = quest.description, style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 12.sp))
                    }

                    if (quest.isCompleted && !quest.isClaimed) {
                      Button(
                        onClick = { gamificationRepository.claimQuestReward(quest.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                      ) {
                        Text("Claim +${quest.xpReward} XP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                      }
                    } else if (quest.isClaimed) {
                      Text(text = "Claimed ✓", style = MaterialTheme.typography.labelSmall.copy(color = NeonPurpleLight, fontWeight = FontWeight.Bold))
                    } else {
                      Text(text = "${quest.currentProgress}/${quest.targetProgress}", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              Text(text = "Achievements", style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))

              achievements.forEach { ach ->
                Card(
                  shape = RoundedCornerShape(14.dp),
                  colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
                  border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                  ) {
                    Text(text = ach.icon, fontSize = 24.sp)
                    Column(modifier = Modifier.weight(1f)) {
                      Text(text = ach.title, style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                      Text(text = ach.description, style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.5.sp))
                    }
                    if (ach.isUnlocked) {
                      Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = NeonPurpleLight, modifier = Modifier.size(20.dp))
                    } else {
                      Text(text = "+${ach.xpReward} XP", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                    }
                  }
                }
              }
            }
          }

          "Account" -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(text = "About Me", style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                    IconButton(
                      onClick = {
                        editDisplayName = user.displayName
                        editBio = user.bio
                        showEditDialog = true
                      },
                      modifier = Modifier.size(28.dp)
                    ) {
                      Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = NeonPurpleLight, modifier = Modifier.size(16.dp))
                    }
                  }
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(text = user.bio, style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp))
                }
              }

              Button(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = OtakuCardSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
              ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = TextPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Log Out", color = TextPrimary, fontWeight = FontWeight.Bold)
              }

              OutlinedButton(
                onClick = { showDeleteDialog = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
              ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Delete Account", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(80.dp))

        BrandingFooter()
      }
    }
  }

  // Edit Profile Dialog
  if (showEditDialog) {
    AlertDialog(
      onDismissRequest = { showEditDialog = false },
      title = { Text("Edit Profile", color = TextPrimary) },
      text = {
        Column {
          OutlinedTextField(
            value = editDisplayName,
            onValueChange = { editDisplayName = it },
            label = { Text("Display Name") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = OtakuCardSurface,
              unfocusedContainerColor = OtakuCardSurface,
              focusedBorderColor = NeonPurple,
              unfocusedBorderColor = OtakuCardBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )
          Spacer(modifier = Modifier.height(12.dp))
          OutlinedTextField(
            value = editBio,
            onValueChange = { editBio = it },
            label = { Text("Bio") },
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = OtakuCardSurface,
              unfocusedContainerColor = OtakuCardSurface,
              focusedBorderColor = NeonPurple,
              unfocusedBorderColor = OtakuCardBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateProfile(editDisplayName, editBio, null, emptyList())
            showEditDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditDialog = false }) {
          Text("Cancel", color = TextMuted)
        }
      },
      containerColor = OtakuCardSurface,
      shape = RoundedCornerShape(16.dp)
    )
  }

  // Delete Account Dialog
  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = { Text("Delete Account?", color = TextPrimary) },
      text = {
        Text("Are you sure you want to delete your account? This action cannot be undone.", color = TextSecondary, fontSize = 13.sp)
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteAccount()
            showDeleteDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) {
          Text("Cancel", color = TextMuted)
        }
      },
      containerColor = OtakuCardSurface,
      shape = RoundedCornerShape(16.dp)
    )
  }
}
