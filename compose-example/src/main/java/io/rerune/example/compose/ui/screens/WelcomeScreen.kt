package io.rerune.example.compose.ui.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.rerune.example.compose.R
import io.rerune.example.compose.ui.components.BadgeChip
import io.rerune.example.compose.ui.components.StatusCard
import io.rerune.example.compose.ui.components.StatusItem
import io.rerune.example.compose.ui.theme.AccentPrimary
import io.rerune.example.compose.ui.theme.BgPrimary
import io.rerune.example.compose.ui.theme.DemoBackground
import io.rerune.example.compose.ui.theme.Success
import io.rerune.example.compose.ui.theme.TextSecondary
import rerune.compose.reRuneObserveRevision

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WelcomeScreen(
  isRefreshing: Boolean,
  @StringRes refreshStateResId: Int,
  lastSyncedText: String,
  onRefresh: () -> Unit,
  onOpenStory: () -> Unit,
) {
  reRuneObserveRevision {
    val pullRefreshState = rememberPullRefreshState(
      refreshing = isRefreshing,
      onRefresh = onRefresh,
    )

    Box(
      modifier = Modifier
        .fillMaxSize()
        .pullRefresh(pullRefreshState),
    ) {
      DemoBackground(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
          modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
          val heroHeight = (maxHeight - 372.dp).coerceAtLeast(280.dp)

          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState()),
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
              BadgeChip(text = stringResource(R.string.welcome_badge))

              Text(
                text = stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.displaySmall.copy(
                  fontWeight = FontWeight.SemiBold,
                  lineHeight = 42.sp,
                ),
              )

              Text(
                text = stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
              )
            }

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(28.dp))
                .height(heroHeight),
            ) {
              Image(
                painter = painterResource(R.drawable.writer_orb),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
              )

              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(Color.Transparent, BgPrimary.copy(alpha = 0.18f), BgPrimary.copy(alpha = 0.76f)),
                    ),
                  ),
              )
            }

            Column(
              modifier = Modifier.padding(top = 20.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
              StatusCard(
                items = listOf(
                  StatusItem(R.string.welcome_locale_label, R.string.welcome_locale_value),
                  StatusItem(labelResId = R.string.welcome_last_synced_label, value = lastSyncedText),
                ),
              )

              Text(
                text = stringResource(refreshStateResId),
                style = MaterialTheme.typography.bodyMedium,
                color = if (refreshStateResId == R.string.welcome_refresh_state_success) Success else TextSecondary,
              )

              Button(
                onClick = onOpenStory,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = AccentPrimary,
                  contentColor = BgPrimary,
                ),
              ) {
                Text(
                  text = stringResource(R.string.welcome_open_story_cta),
                  modifier = Modifier.padding(vertical = 4.dp),
                )
              }
            }
          }
        }
      }

      PullRefreshIndicator(
        refreshing = isRefreshing,
        state = pullRefreshState,
        modifier = Modifier
          .align(Alignment.TopCenter)
          .windowInsetsPadding(WindowInsets.safeDrawing),
        backgroundColor = Color(0xFF161D2C),
        contentColor = AccentPrimary,
      )
    }
  }
}
