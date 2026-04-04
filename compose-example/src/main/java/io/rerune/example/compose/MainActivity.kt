package io.rerune.example.compose

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.rerune.example.compose.ui.screens.StoryScreen
import io.rerune.example.compose.ui.screens.WelcomeScreen
import io.rerune.example.compose.ui.theme.ReRuneDemoTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rerune.ReRune
import rerune.reRune

class MainActivity : ComponentActivity() {
  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(newBase.reRune())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      ReRuneDemoTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          ReRuneDemoApp()
        }
      }
    }
  }
}

private enum class DemoScreen {
  Welcome,
  Story,
}

@Composable
private fun ReRuneDemoApp() {
  var currentScreen by rememberSaveable { mutableStateOf(DemoScreen.Welcome) }
  val refreshStateHolder = rememberReRuneRefreshStateHolder()

  BackHandler(enabled = currentScreen == DemoScreen.Story) {
    currentScreen = DemoScreen.Welcome
  }

  AnimatedContent(
    targetState = currentScreen,
    transitionSpec = {
      if (targetState == DemoScreen.Story) {
        slideInHorizontally(
          animationSpec = tween(durationMillis = 420),
          initialOffsetX = { fullWidth -> fullWidth / 5 },
        ) + fadeIn(animationSpec = tween(durationMillis = 240)) togetherWith
          slideOutHorizontally(
            animationSpec = tween(durationMillis = 420),
            targetOffsetX = { fullWidth -> -fullWidth / 8 },
          ) + fadeOut(animationSpec = tween(durationMillis = 220))
      } else {
        slideInHorizontally(
          animationSpec = tween(durationMillis = 420),
          initialOffsetX = { fullWidth -> -fullWidth / 5 },
        ) + fadeIn(animationSpec = tween(durationMillis = 240)) togetherWith
          slideOutHorizontally(
            animationSpec = tween(durationMillis = 420),
            targetOffsetX = { fullWidth -> fullWidth / 8 },
          ) + fadeOut(animationSpec = tween(durationMillis = 220))
      }
    },
    label = "screen_transition",
  ) { screen ->
    when (screen) {
      DemoScreen.Welcome -> {
        WelcomeScreen(
          isRefreshing = refreshStateHolder.isRefreshing,
          refreshStateResId = refreshStateHolder.messageResId,
          lastSyncedText = refreshStateHolder.lastSyncedText,
          onRefresh = refreshStateHolder::refresh,
          onOpenStory = { currentScreen = DemoScreen.Story },
        )
      }

      DemoScreen.Story -> {
        StoryScreen(
          isRefreshing = refreshStateHolder.isRefreshing,
          onBack = { currentScreen = DemoScreen.Welcome },
          onRefresh = refreshStateHolder::refresh,
        )
      }
    }
  }
}

@Composable
private fun rememberReRuneRefreshStateHolder(): ReRuneRefreshStateHolder {
  val scope = rememberCoroutineScope()
  return remember(scope) {
    ReRuneRefreshStateHolder(scope = scope)
  }
}

@Stable
private class ReRuneRefreshStateHolder(
  private val scope: CoroutineScope,
) {
  var isRefreshing by mutableStateOf(false)
    private set

  @get:StringRes
  var messageResId by mutableStateOf(R.string.welcome_refresh_state_idle)
    private set

  var lastSyncedText by mutableStateOf(currentTimestamp())
    private set

  fun refresh() {
    if (isRefreshing) return

    scope.launch {
      isRefreshing = true
      try {
        messageResId = R.string.welcome_refresh_state_checking
        delay(400)

        messageResId = R.string.welcome_refresh_state_downloading
        try {
          ReRune.checkForUpdates()
        } catch (_: Throwable) {
          // Keep the demo state flow deterministic even if the request fails.
        }

        messageResId = R.string.welcome_refresh_state_applying
        delay(500)

        messageResId = R.string.welcome_refresh_state_success
        lastSyncedText = currentTimestamp()
        delay(1400)
      } finally {
        isRefreshing = false
        messageResId = R.string.welcome_refresh_state_idle
      }
    }
  }
}

private fun currentTimestamp(): String {
  val formatter = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
  return formatter.format(Date())
}
