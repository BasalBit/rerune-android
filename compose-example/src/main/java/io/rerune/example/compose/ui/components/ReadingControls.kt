package io.rerune.example.compose.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.rerune.example.compose.R
import io.rerune.example.compose.*
import io.rerune.example.compose.ui.theme.*
import java.text.NumberFormat

@Composable
internal fun ChapterIcon(@DrawableRes icon: Int, description: String? = null, tint: Color = TextSecondary, modifier: Modifier = Modifier) {
  Icon(painterResource(icon), description, modifier.size(24.dp), tint)
}

@Composable
internal fun BookmarkButton(id: StoryId, state: ReadingState, onClick: () -> Unit, onCover: Boolean = false) {
  val saved = id in state.saved
  IconButton(onClick, Modifier.size(48.dp).testTag("bookmark-${id.name.lowercase()}")
    .then(if (onCover) Modifier.background(BgPrimary.copy(alpha = .88f), CircleShape) else Modifier)) {
    ChapterIcon(if (saved) R.drawable.chapter_bookmark_filled else R.drawable.chapter_bookmark,
      stringResource(if (saved) R.string.remove_saved else R.string.save_story),
      if (saved) AccentPrimary else TextPrimary)
  }
}

@Composable
internal fun ReadingProgress(state: ReadingState) {
  val progress = state.progress()
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(if (progress == 1f) stringResource(R.string.story_finished)
        else stringResource(R.string.chapter_progress, state.chapterIndex() + 1, state.story().chapters.size),
        Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
      Text(NumberFormat.getPercentInstance().format(progress), color = TextSecondary)
    }
    LinearProgressIndicator(progress = { progress }, Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
      color = AccentPrimary, trackColor = BorderSubtle)
  }
}

@Composable
internal fun ReadingButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, arrow: Boolean = false) {
  Button(onClick, modifier.heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)) {
    Text(text, Modifier.weight(1f, fill = false), textAlign = TextAlign.Center)
    if (arrow) {
      Spacer(Modifier.width(14.dp))
      ChapterIcon(R.drawable.chapter_forward, tint = BgPrimary)
    }
  }
}

@Composable
internal fun RefreshButton(sync: SyncState, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
  OutlinedButton(onRefresh, modifier.heightIn(min = 48.dp).testTag("refresh-texts"),
    enabled = !sync.refreshing, shape = RoundedCornerShape(16.dp)) {
    if (sync.refreshing) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
    else ChapterIcon(R.drawable.chapter_refresh)
    Spacer(Modifier.width(8.dp))
    Text(stringResource(if (sync.refreshing) R.string.welcome_refresh_state_checking else R.string.story_refresh_cta),
      Modifier.weight(1f, fill = false), textAlign = TextAlign.Center, color = TextSecondary)
  }
}

@Composable
internal fun SyncFeedback(sync: SyncState) {
  sync.feedback?.let { message ->
    Text(stringResource(message), Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
      color = TextSecondary, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
  }
}
