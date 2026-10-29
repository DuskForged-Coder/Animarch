package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleDark
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.OtakuCardBorder
import com.example.ui.theme.OtakuDeepBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onNavigateToHome: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scale = remember { Animatable(0.7f) }
  val alpha = remember { Animatable(0f) }
  val glowScale = remember { Animatable(1f) }

  LaunchedEffect(Unit) {
    // Entrance animation
    scale.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )
    alpha.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 600)
    )
    // Continuous subtle pulse
    glowScale.animateTo(
      targetValue = 1.15f,
      animationSpec = infiniteRepeatable(
        animation = tween(1200, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
      )
    )
  }

  LaunchedEffect(Unit) {
    delay(2000)
    onNavigateToHome()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(OtakuDeepBlack)
      .systemBarsPadding()
      .displayCutoutPadding()
      .clickable { onNavigateToHome() }
      .testTag("splash_screen"),
    contentAlignment = Alignment.Center
  ) {
    // Ambient background radial glow
    Box(
      modifier = Modifier
        .size(320.dp)
        .scale(glowScale.value)
        .alpha(0.25f)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(NeonPurple, NeonPurpleDark, Color.Transparent)
          )
        )
    )

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .scale(scale.value)
        .alpha(alpha.value)
        .padding(24.dp)
    ) {
      // Emblem logo
      Box(
        modifier = Modifier
          .size(125.dp)
          .clip(CircleShape)
          .border(2.dp, NeonPurple, CircleShape)
          .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.ic_animarch_logo_1791862659045),
          contentDescription = "Animarch Logo",
          modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // App Title
      Text(
        text = "ANIMARCH",
        style = MaterialTheme.typography.headlineLarge.copy(
          color = TextPrimary,
          fontWeight = FontWeight.Black,
          fontSize = 34.sp,
          letterSpacing = 4.sp
        )
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Glowing subtitle
      Text(
        text = "THE ULTIMATE ANIME EXPERIENCE",
        style = MaterialTheme.typography.labelMedium.copy(
          color = NeonPurpleLight,
          fontWeight = FontWeight.Bold,
          letterSpacing = 2.sp,
          fontSize = 11.sp
        )
      )

      Spacer(modifier = Modifier.height(36.dp))

      // Clean branding card
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(OtakuDeepBlack)
          .border(1.dp, OtakuCardBorder, RoundedCornerShape(20.dp))
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .testTag("splash_branding")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = NeonPurple,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Powered by VYRON",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = TextPrimary,
              fontWeight = FontWeight.Medium,
              fontSize = 12.5.sp,
              letterSpacing = 0.3.sp
            )
          )
        }
      }
    }

    // Tap to enter hint at bottom
    Text(
      text = "Tap anywhere to begin",
      style = MaterialTheme.typography.labelSmall.copy(
        color = TextMuted,
        fontSize = 11.sp
      ),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 32.dp)
    )
  }
}
