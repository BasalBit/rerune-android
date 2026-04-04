package io.rerune.example.compose.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.rerune.example.compose.R
import io.rerune.example.compose.ui.components.BadgeChip
import io.rerune.example.compose.ui.theme.AccentPrimary
import io.rerune.example.compose.ui.theme.DemoBackground
import io.rerune.example.compose.ui.theme.TextSecondary
import rerune.compose.reRuneObserveRevision

@Composable
fun StoryScreen(
  isRefreshing: Boolean,
  onBack: () -> Unit,
  onRefresh: () -> Unit,
) {
  reRuneObserveRevision {
    DemoBackground(modifier = Modifier.fillMaxSize()) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .navigationBarsPadding()
          .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.offset(x = (-12).dp),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.story_back_cta),
          )
        }

        Image(
          painter = painterResource(R.drawable.blacksmith),
          contentDescription = null,
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 320.dp)
            .clip(RoundedCornerShape(28.dp)),
          contentScale = ContentScale.Crop,
        )

        BadgeChip(text = stringResource(R.string.story_caption))

        Text(
          text = stringResource(R.string.story_title),
          style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.SemiBold,
            lineHeight = 40.sp,
          ),
        )

        Text(
          text = stringResource(R.string.story_body_primary),
          style = MaterialTheme.typography.bodyLarge,
          color = TextSecondary,
        )

        Text(
          text = stringResource(R.string.story_body_secondary),
          style = MaterialTheme.typography.bodyLarge,
          color = TextSecondary,
        )

        Button(
          onClick = onRefresh,
          enabled = !isRefreshing,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = AccentPrimary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
          ),
        ) {
          Text(
            text = stringResource(R.string.story_refresh_cta),
            modifier = Modifier.padding(vertical = 4.dp),
          )
        }
      }
    }
  }
}
