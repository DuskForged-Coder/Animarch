package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val OtakuColorScheme = darkColorScheme(
  primary = NeonPurple,
  onPrimary = TextPrimary,
  primaryContainer = NeonPurpleDark,
  onPrimaryContainer = NeonPurpleLight,
  secondary = NeonPurpleLight,
  onSecondary = OtakuDeepBlack,
  secondaryContainer = OtakuCardSurface,
  onSecondaryContainer = TextPrimary,
  tertiary = ElectricCyan,
  onTertiary = OtakuDeepBlack,
  background = OtakuDeepBlack,
  onBackground = TextPrimary,
  surface = OtakuCharcoal,
  onSurface = TextPrimary,
  surfaceVariant = OtakuCardSurface,
  onSurfaceVariant = TextSecondary,
  outline = OtakuCardBorder,
  outlineVariant = OtakuCardBorder
)

@Composable
fun AnimarchTheme(
  content: @Composable () -> Unit
) {
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = OtakuDeepBlack.toArgb()
        window.navigationBarColor = OtakuDeepBlack.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
      }
    }
  }

  MaterialTheme(
    colorScheme = OtakuColorScheme,
    typography = Typography,
    content = content
  )
}
