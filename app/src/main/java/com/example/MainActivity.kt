package com.example

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.navigation.AnimarchNavGraph
import com.example.ui.theme.AnimarchTheme
import com.example.ui.theme.OtakuDeepBlack

class MainActivity : ComponentActivity() {

  var isInPiPMode by mutableStateOf(false)
    private set

  var isPlayerActive by mutableStateOf(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val app = application as AnimarchApplication
    val appContainer = app.container

    setContent {
      AnimarchTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = OtakuDeepBlack
        ) {
          AnimarchNavGraph(
            appContainer = appContainer,
            isInPiPMode = isInPiPMode,
            onEnterPiP = { enterPiPMode() },
            onPlayerActiveChange = { active -> isPlayerActive = active }
          )
        }
      }
    }
  }

  fun enterPiPMode() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val aspectRatio = Rational(16, 9)
      val params = PictureInPictureParams.Builder()
        .setAspectRatio(aspectRatio)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        params.setAutoEnterEnabled(true)
      }

      try {
        enterPictureInPictureMode(params.build())
      } catch (_: Exception) {}
    }
  }

  override fun onUserLeaveHint() {
    super.onUserLeaveHint()
    if (isPlayerActive) {
      enterPiPMode()
    }
  }

  override fun onPictureInPictureModeChanged(
    isInPictureInPictureMode: Boolean,
    newConfig: Configuration
  ) {
    super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    isInPiPMode = isInPictureInPictureMode
  }
}
