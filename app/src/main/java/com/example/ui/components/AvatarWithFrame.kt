package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.OtakuCardSurface

@Composable
fun AvatarWithFrame(
  avatarUrl: String? = null,
  equippedFrameResId: Int? = null,
  modifier: Modifier = Modifier,
  size: Dp = 96.dp,
  onClick: (() -> Unit)? = null
) {
  Box(
    modifier = modifier
      .size(size)
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
      .testTag("avatar_with_frame"),
    contentAlignment = Alignment.Center
  ) {
    // Layer 1 - Profile Picture (Circular, scale cover, z-index 1)
    Box(
      modifier = Modifier
        .fillMaxSize(0.78f)
        .clip(CircleShape)
        .background(OtakuCardSurface)
        .border(1.5.dp, NeonPurple.copy(alpha = 0.6f), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      if (!avatarUrl.isNullOrBlank()) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current)
            .data(avatarUrl)
            .crossfade(true)
            .build(),
          contentDescription = "Profile avatar",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      } else {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = "Default avatar",
          tint = NeonPurpleLight,
          modifier = Modifier.fillMaxSize(0.65f)
        )
      }
    }

    // Layer 2 - Frame Overlay (Transparent PNG positioned over avatar, scale fit, z-index 2)
    if (equippedFrameResId != null && equippedFrameResId != 0) {
      Image(
        painter = painterResource(id = equippedFrameResId),
        contentDescription = "Equipped profile frame overlay",
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize()
      )
    }
  }
}
