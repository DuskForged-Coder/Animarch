package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.OtakuCardBorder
import com.example.ui.theme.OtakuCharcoal
import com.example.ui.theme.OtakuDeepBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun BrandingFooter(
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 20.dp, horizontal = 20.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Elegant ambient neon divider
    Box(
      modifier = Modifier
        .fillMaxWidth(0.5f)
        .height(1.dp)
        .background(
          Brush.horizontalGradient(
            listOf(
              Color.Transparent,
              NeonPurple.copy(alpha = 0.4f),
              Color.Transparent
            )
          )
        )
    )

    Spacer(modifier = Modifier.height(14.dp))

    Surface(
      color = OtakuCharcoal.copy(alpha = 0.8f),
      shape = RoundedCornerShape(20.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder),
      modifier = Modifier.testTag("branding_footer")
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(NeonPurple)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "ANIMARCH",
          style = MaterialTheme.typography.labelMedium.copy(
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
          )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "•",
          color = TextMuted,
          fontSize = 12.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Powered by VYRON",
          style = MaterialTheme.typography.labelSmall.copy(
            color = NeonPurpleLight,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.4.sp
          )
        )
      }
    }
  }
}

@Composable
fun BrandingBadge(
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier.testTag("branding_badge"),
    color = OtakuDeepBlack.copy(alpha = 0.75f),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, OtakuCardBorder)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.AutoAwesome,
        contentDescription = "VYRON Badge",
        tint = NeonPurpleLight,
        modifier = Modifier.size(13.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "VYRON",
        style = MaterialTheme.typography.labelSmall.copy(
          fontSize = 10.5.sp,
          color = TextPrimary,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
      )
    }
  }
}
