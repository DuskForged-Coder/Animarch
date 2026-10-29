package com.example.ui.player

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.OtakuDeepBlack

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun OtakuVideoPlayer(
  streamUrl: String,
  isFullscreen: Boolean,
  onFullscreenChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val webView = remember {
    WebView(context).apply {
      layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
      )
      setBackgroundColor(Color.parseColor("#0A0A0C"))
      setLayerType(View.LAYER_TYPE_HARDWARE, null)

      settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        mediaPlaybackRequiresUserGesture = false
        loadWithOverviewMode = true
        useWideViewPort = true
        allowContentAccess = true
        allowFileAccess = false
        cacheMode = WebSettings.LOAD_DEFAULT
        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile; Animarch) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
      }

      webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
          // Keep navigation inside iframe
          return false
        }
      }

      webChromeClient = object : WebChromeClient() {
        private var customView: View? = null
        private var customViewCallback: CustomViewCallback? = null

        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
          customView = view
          customViewCallback = callback
          onFullscreenChange(true)
        }

        override fun onHideCustomView() {
          customView = null
          customViewCallback?.onCustomViewHidden()
          customViewCallback = null
          onFullscreenChange(false)
        }
      }
    }
  }

  // Load stream HTML when streamUrl changes
  DisposableEffect(streamUrl) {
    if (streamUrl.isNotBlank()) {
      val htmlContent = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <style>
            * { margin: 0; padding: 0; box-sizing: border-box; background-color: #0A0A0C; }
            html, body {
              width: 100%;
              height: 100%;
              overflow: hidden;
              background-color: #0A0A0C;
              display: flex;
              align-items: center;
              justify-content: center;
            }
            .video-container {
              position: relative;
              width: 100%;
              height: 100%;
              display: flex;
              align-items: center;
              justify-content: center;
            }
            iframe {
              width: 100%;
              height: 100%;
              border: 0;
              outline: none;
            }
          </style>
        </head>
        <body>
          <div class="video-container">
            <iframe
              src="$streamUrl"
              allowfullscreen="true"
              webkitallowfullscreen="true"
              mozallowfullscreen="true"
              allow="autoplay; fullscreen; encrypted-media; picture-in-picture"
              scrolling="no"
              frameborder="0">
            </iframe>
          </div>
        </body>
        </html>
      """.trimIndent()

      webView.loadDataWithBaseURL(
        "https://megaplay.buzz",
        htmlContent,
        "text/html",
        "UTF-8",
        null
      )
    }

    onDispose {
      // Pause playback if URL changes
      webView.evaluateJavascript("if (document.querySelector('video')) { document.querySelector('video').pause(); }", null)
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      webView.stopLoading()
      webView.loadUrl("about:blank")
      webView.destroy()
    }
  }

  val boxModifier = if (isFullscreen) {
    modifier
      .fillMaxSize()
      .background(OtakuDeepBlack)
  } else {
    modifier
      .fillMaxWidth()
      .aspectRatio(16f / 9f)
      .background(OtakuDeepBlack)
  }

  Box(
    modifier = boxModifier.testTag("otaku_video_player")
  ) {
    AndroidView(
      factory = { webView },
      modifier = Modifier.fillMaxSize()
    )
  }
}
