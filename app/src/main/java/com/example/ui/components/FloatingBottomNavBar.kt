package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.OtakuCardBorder
import com.example.ui.theme.OtakuCardSurface
import com.example.ui.theme.TextMuted

sealed class BottomNavDestination(
  val route: String,
  val label: String,
  val icon: ImageVector
) {
  data object Home : BottomNavDestination("home", "Home", Icons.Default.Home)
  data object Community : BottomNavDestination("community", "Community", Icons.Default.Forum)
  data object Profile : BottomNavDestination("profile", "Profile", Icons.Default.Person)
  data object Shop : BottomNavDestination("shop", "Shop", Icons.Default.ShoppingBag)
}

@Composable
fun FloatingBottomNavBar(
  currentRoute: String?,
  onNavigate: (BottomNavDestination) -> Unit,
  modifier: Modifier = Modifier
) {
  val destinations = listOf(
    BottomNavDestination.Home,
    BottomNavDestination.Community,
    BottomNavDestination.Profile,
    BottomNavDestination.Shop
  )

  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 16.dp, vertical = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = OtakuCardSurface.copy(alpha = 0.94f),
      border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
      shadowElevation = 8.dp,
      modifier = Modifier
        .widthIn(max = 480.dp)
        .fillMaxWidth()
        .testTag("floating_bottom_nav")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        destinations.forEach { dest ->
          val isSelected = currentRoute == dest.route
          val iconScale by animateFloatAsState(if (isSelected) 1.1f else 1.0f, label = "nav_scale")
          val tintColor by animateColorAsState(if (isSelected) NeonPurpleLight else TextMuted, label = "nav_tint")

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(if (isSelected) NeonPurple.copy(alpha = 0.2f) else Color.Transparent)
              .clickable { onNavigate(dest) }
              .padding(horizontal = 14.dp, vertical = 8.dp)
              .testTag("nav_item_${dest.route}"),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = dest.icon,
                contentDescription = dest.label,
                tint = tintColor,
                modifier = Modifier
                  .size(22.dp)
                  .scale(iconScale)
              )

              Spacer(modifier = Modifier.height(2.dp))

              Text(
                text = dest.label,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isSelected) Color.White else TextMuted,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 10.5.sp
                )
              )
            }
          }
        }
      }
    }
  }
}
