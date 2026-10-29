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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CosmeticCategory
import com.example.data.model.CosmeticItem
import com.example.data.model.MembershipRank
import com.example.data.repository.GamificationRepository
import com.example.ui.components.BrandingFooter
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.OtakuCardBorder
import com.example.ui.theme.OtakuCardSurface
import com.example.ui.theme.OtakuDeepBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ShopScreen(
  gamificationRepository: GamificationRepository,
  modifier: Modifier = Modifier
) {
  val totalXp by gamificationRepository.totalXp.collectAsState()
  val activeRank by gamificationRepository.membershipRank.collectAsState()
  val cosmetics by gamificationRepository.cosmetics.collectAsState()

  var selectedTab by remember { mutableStateOf("Membership") }
  var selectedCategory by remember { mutableStateOf(CosmeticCategory.FRAME) }

  Scaffold(
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(OtakuDeepBlack)
          .statusBarsPadding()
          .displayCutoutPadding()
          .padding(horizontal = 16.dp, vertical = 12.dp),
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
              .background(OtakuCardSurface)
              .border(1.dp, NeonPurple, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ShoppingBag,
              contentDescription = null,
              tint = NeonPurpleLight,
              modifier = Modifier.size(20.dp)
            )
          }

          Column {
            Text(
              text = "ANIMARCH SHOP",
              style = MaterialTheme.typography.titleMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                fontSize = 17.sp
              )
            )
            Text(
              text = "Membership & Cosmetics",
              style = MaterialTheme.typography.labelSmall.copy(
                color = NeonPurpleLight,
                fontSize = 10.sp
              )
            )
          }
        }

        // Available XP balance chip
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(OtakuCardSurface)
            .border(1.dp, OtakuCardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = NeonPurpleLight,
              modifier = Modifier.size(14.dp)
            )
            Text(
              text = "$totalXp XP",
              style = MaterialTheme.typography.labelMedium.copy(
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            )
          }
        }
      }
    },
    containerColor = OtakuDeepBlack,
    modifier = modifier.testTag("shop_screen")
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .navigationBarsPadding()
        .verticalScroll(rememberScrollState())
    ) {
      // Primary Tab Switcher (Membership vs Cosmetics)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("Membership", "Cosmetics").forEach { tab ->
          val isSelected = selectedTab == tab
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) NeonPurple else OtakuCardSurface)
              .border(1.dp, if (isSelected) NeonPurpleLight else OtakuCardBorder, RoundedCornerShape(12.dp))
              .clickable { selectedTab = tab }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = tab,
              style = MaterialTheme.typography.labelMedium.copy(
                color = if (isSelected) Color.White else TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (selectedTab == "Membership") {
        // Membership Tiers List
        Column(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          MembershipRank.entries.forEach { rank ->
            val isActive = activeRank == rank
            Card(
              shape = RoundedCornerShape(18.dp),
              colors = CardDefaults.cardColors(containerColor = OtakuCardSurface),
              border = androidx.compose.foundation.BorderStroke(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) rank.color else OtakuCardBorder
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    Text(text = rank.emblem, fontSize = 28.sp)
                    Column {
                      Text(
                        text = rank.label,
                        style = MaterialTheme.typography.titleMedium.copy(
                          color = TextPrimary,
                          fontWeight = FontWeight.Bold,
                          fontSize = 18.sp
                        )
                      )
                      Text(
                        text = rank.monthlyPrice + "/mo",
                        style = MaterialTheme.typography.bodySmall.copy(
                          color = rank.color,
                          fontWeight = FontWeight.Bold,
                          fontSize = 12.sp
                        )
                      )
                    }
                  }

                  if (isActive) {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(rank.color.copy(alpha = 0.2f))
                        .border(1.dp, rank.color, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                      Text(
                        text = "ACTIVE TIER",
                        style = MaterialTheme.typography.labelSmall.copy(
                          color = rank.color,
                          fontWeight = FontWeight.Bold,
                          fontSize = 10.5.sp
                        )
                      )
                    }
                  } else {
                    Button(
                      onClick = { gamificationRepository.updateMembershipRank(rank) },
                      colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                      shape = RoundedCornerShape(10.dp),
                      contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                      Text("Upgrade", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                  }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = rank.description,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                  )
                )
              }
            }
          }
        }
      } else {
        // Cosmetics Category Filter Pills
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(CosmeticCategory.entries) { cat ->
            val isSelected = selectedCategory == cat
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) NeonPurple else OtakuCardSurface)
                .border(1.dp, if (isSelected) NeonPurpleLight else OtakuCardBorder, RoundedCornerShape(20.dp))
                .clickable { selectedCategory = cat }
                .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
              Text(
                text = cat.label,
                style = MaterialTheme.typography.labelMedium.copy(
                  color = if (isSelected) Color.White else TextMuted,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cosmetic Items Grid
        val filteredCosmetics = cosmetics.filter { it.category == selectedCategory }
        Column(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          filteredCosmetics.forEach { item ->
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  if (item.drawableResId != null) {
                    com.example.ui.components.AvatarWithFrame(
                      equippedFrameResId = item.drawableResId,
                      size = 56.dp
                    )
                  } else {
                    Text(text = item.iconEmoji, fontSize = 28.sp)
                  }

                  Column {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                          color = TextPrimary,
                          fontWeight = FontWeight.Bold,
                          fontSize = 14.sp
                        )
                      )

                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(4.dp))
                          .background(item.rarity.color.copy(alpha = 0.2f))
                          .padding(horizontal = 5.dp, vertical = 1.dp)
                      ) {
                        Text(
                          text = item.rarity.label,
                          style = MaterialTheme.typography.labelSmall.copy(
                            color = item.rarity.color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp
                          )
                        )
                      }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                      text = "${item.priceXp} XP",
                      style = MaterialTheme.typography.bodySmall.copy(
                        color = NeonPurpleLight,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp
                      )
                    )
                  }
                }

                if (item.isOwned) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(10.dp))
                      .background(if (item.isEquipped) NeonPurple else OtakuCardSurface)
                      .border(1.dp, if (item.isEquipped) NeonPurpleLight else OtakuCardBorder, RoundedCornerShape(10.dp))
                      .clickable { gamificationRepository.buyOrEquipCosmetic(item.id) }
                      .padding(horizontal = 12.dp, vertical = 6.dp)
                  ) {
                    Text(
                      text = if (item.isEquipped) "Equipped ✓" else "Equip",
                      style = MaterialTheme.typography.labelMedium.copy(
                        color = if (item.isEquipped) Color.White else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                      )
                    )
                  }
                } else {
                  Button(
                    onClick = { gamificationRepository.buyOrEquipCosmetic(item.id) },
                    enabled = totalXp >= item.priceXp,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                  ) {
                    Text("Unlock", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(80.dp))

      BrandingFooter()
    }
  }
}
