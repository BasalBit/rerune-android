package io.rerune.example.compose

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import rerune.ReRune
import rerune.compose.reRuneObserveRevision
import rerune.reRune

class MainActivity : ComponentActivity() {
  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(newBase.reRune())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          ComposeExampleScreen()
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ComposeExampleScreen() {
  reRuneObserveRevision {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    var redrawTick by remember { mutableIntStateOf(0) }
    val redrawTimestamp = remember(redrawTick) { currentRedrawTimestamp() }
    val pullRefreshState = rememberPullRefreshState(
      refreshing = refreshing,
      onRefresh = {
        scope.launch {
          refreshing = true
          val result = ReRune.checkForUpdates()
          status = result.status.name
          redrawTick += 1
          refreshing = false
        }
      },
    )

    Box(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .pullRefresh(pullRefreshState),
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Text(
          text = stringResource(R.string.title),
          style = MaterialTheme.typography.headlineMedium,
        )
        Text(
          text = stringResource(R.string.body),
          style = MaterialTheme.typography.bodyLarge,
        )
        Text(
          text = stringResource(R.string.sample_placeholder, 1, "RubinTXT"),
          style = MaterialTheme.typography.bodyLarge,
        )
        Text(
          text = stringResource(R.string.last_redraw, redrawTimestamp),
          style = MaterialTheme.typography.bodyMedium,
        )
        Button(
          onClick = {
            scope.launch {
              val result = ReRune.checkForUpdates()
              status = result.status.name
              redrawTick += 1
            }
          },
        ) {
          Text(text = stringResource(R.string.button))
        }

        if (status != null) {
          Text(
            text = "status change OTA: $status",
            style = MaterialTheme.typography.bodyMedium,
          )
        }
      }

      PullRefreshIndicator(
        refreshing = refreshing,
        state = pullRefreshState,
        modifier = Modifier.align(Alignment.TopCenter),
      )
    }
  }
}

private fun currentRedrawTimestamp(): String {
  val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
  return formatter.format(Date())
}
