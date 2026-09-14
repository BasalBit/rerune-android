package io.rerune.example.compose.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.rerune.example.compose.R
import io.rerune.example.compose.*
import io.rerune.example.compose.ui.components.*
import io.rerune.example.compose.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LibraryScreen(
  state: ReadingState, sync: SyncState, scroll: ScrollState,
  onOpen: (StoryId) -> Unit, onBookmark: (StoryId) -> Unit,
  onTab: (LibraryTab) -> Unit, onFilter: (StoryId?) -> Unit,
  onRefresh: () -> Unit, onLocale: (String?) -> Unit, onSettings: () -> Unit,
) {
  Column(Modifier.widthIn(max = 1040.dp).fillMaxWidth().verticalScroll(scroll)
    .padding(horizontal = 24.dp).padding(top = 12.dp, bottom = 24.dp)) {
    LibraryHeader(onLocale, onSettings)
    Spacer(Modifier.height(26.dp))
    val title = when (state.tab) {
      LibraryTab.Library -> R.string.welcome_title
      LibraryTab.Discover -> R.string.discover_title
      LibraryTab.Saved -> R.string.saved_title
    }
    val subtitle = when (state.tab) {
      LibraryTab.Library -> R.string.welcome_subtitle
      LibraryTab.Discover -> R.string.discover_subtitle
      LibraryTab.Saved -> R.string.saved_subtitle
    }
    Text(stringResource(title), style = MaterialTheme.typography.displaySmall)
    Spacer(Modifier.height(8.dp))
    Text(stringResource(subtitle), color = TextSecondary)
    Spacer(Modifier.height(26.dp))
    when (state.tab) {
      LibraryTab.Library -> BoxWithConstraints {
        if (maxWidth >= 760.dp) {
          Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            FeaturedStory(state, onOpen, onBookmark, Modifier.weight(6f))
            Shelf(state, onOpen, onBookmark, onTab, Modifier.weight(5f))
          }
        } else {
          Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
            FeaturedStory(state, onOpen, onBookmark)
            Shelf(state, onOpen, onBookmark, onTab)
          }
        }
      }
      LibraryTab.Discover -> {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(state.filter == null, { onFilter(null) }, label = { Text(stringResource(R.string.filter_all)) })
          stories.forEach { story ->
            FilterChip(state.filter == story.id, { onFilter(if (state.filter == story.id) null else story.id) },
              label = { Text(stringResource(story.genre)) })
          }
        }
        Spacer(Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          stories.filter { state.filter == null || it.id == state.filter }.forEach {
            StoryTile(it, state, onOpen, onBookmark, expanded = true)
          }
        }
      }
      LibraryTab.Saved -> {
        if (state.saved.isEmpty()) {
          Surface(shape = RoundedCornerShape(24.dp), color = BgSecondary) {
            Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(16.dp)) {
              ChapterIcon(R.drawable.chapter_bookmark, tint = AccentPrimary, modifier = Modifier.size(32.dp))
              Text(stringResource(R.string.saved_empty_title), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
              Text(stringResource(R.string.saved_empty_body), color = TextSecondary, textAlign = TextAlign.Center)
              ReadingButton(stringResource(R.string.explore_stories), { onTab(LibraryTab.Discover) })
            }
          }
        } else Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          stories.filter { it.id in state.saved }.forEach { StoryTile(it, state, onOpen, onBookmark, expanded = true) }
        }
      }
    }
    Spacer(Modifier.height(30.dp))
    RefreshButton(sync, onRefresh, Modifier.align(Alignment.CenterHorizontally))
    SyncFeedback(sync)
    Spacer(Modifier.height(16.dp))
    Text(stringResource(R.string.library_footer), Modifier.fillMaxWidth(), color = TextSecondary,
      textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FeaturedStory(state: ReadingState, onOpen: (StoryId) -> Unit, onBookmark: (StoryId) -> Unit, modifier: Modifier = Modifier) {
  val story = state.story()
  Column(modifier, verticalArrangement = Arrangement.spacedBy(18.dp)) {
    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(5.dp).background(AccentPrimary, CircleShape))
        Text(stringResource(R.string.currently_reading), color = TextSecondary, style = MaterialTheme.typography.labelMedium)
      }
      Text(stringResource(R.string.collection_issue), color = TextSecondary, style = MaterialTheme.typography.labelMedium)
    }
    StoryCover(story.id, Modifier.fillMaxWidth().heightIn(min = 245.dp)) {
      Column(Modifier.padding(22.dp)) {
        val ink = if (story.id == StoryId.Lantern) TextPrimary else Color(0xFF193D3D)
        Text(stringResource(story.title), color = ink, fontSize = 29.sp, lineHeight = 33.sp, letterSpacing = (-.8).sp)
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.story_by, story.author), color = if (story.id == StoryId.Lantern) ink else Color(0xFF294C49),
          fontSize = 12.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(105.dp))
        BookmarkButton(story.id, state, { onBookmark(story.id) }, onCover = true)
      }
    }
    ReadingProgress(state)
    ReadingButton(stringResource(R.string.welcome_open_story_cta), { onOpen(story.id) },
      Modifier.fillMaxWidth().testTag("continue-reading"), arrow = true)
  }
}

@Composable
private fun Shelf(state: ReadingState, onOpen: (StoryId) -> Unit, onBookmark: (StoryId) -> Unit, onTab: (LibraryTab) -> Unit, modifier: Modifier = Modifier) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(stringResource(R.string.next_chapter_shelf), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
      TextButton({ onTab(LibraryTab.Discover) }) { Text(stringResource(R.string.see_all)) }
    }
    stories.filter { it.id != state.current }.forEach { StoryTile(it, state, onOpen, onBookmark) }
    Surface(color = BgPrimary, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, BorderSubtle)) {
      Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.reading_moment), color = AccentPrimary, style = MaterialTheme.typography.labelMedium)
        Text(stringResource(R.string.reading_quote), fontFamily = ReaderFont, fontSize = 21.sp, lineHeight = 31.5.sp)
      }
    }
  }
}

@Composable
private fun StoryTile(story: StoryResources, state: ReadingState, onOpen: (StoryId) -> Unit, onBookmark: (StoryId) -> Unit, expanded: Boolean = false) {
  Surface(shape = RoundedCornerShape(22.dp), color = BgSecondary) {
    // Large text gets a vertical card so the bookmark cannot squeeze prose into a sliver.
    val stacked = LocalDensity.current.fontScale >= 1.5f
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      val openLabel = stringResource(story.title)
      Column(Modifier.weight(1f).clickable(onClickLabel = openLabel) { onOpen(story.id) }
        .testTag("open-${story.id.name.lowercase()}").padding(12.dp)) {
        if (stacked) {
          StoryCover(story.id, Modifier.fillMaxWidth().height(110.dp))
          Spacer(Modifier.height(12.dp))
          TileText(story, expanded)
        } else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
          StoryCover(story.id, Modifier.width(if (expanded) 88.dp else 70.dp).height(if (expanded) 118.dp else 92.dp))
          Column(Modifier.weight(1f)) { TileText(story, expanded) }
        }
      }
      BookmarkButton(story.id, state, { onBookmark(story.id) })
      Spacer(Modifier.width(4.dp))
    }
  }
}

@Composable
private fun TileText(story: StoryResources, expanded: Boolean) {
  Text(stringResource(story.genre), color = AccentStrong, style = MaterialTheme.typography.labelMedium)
  Spacer(Modifier.height(5.dp))
  Text(stringResource(story.title), style = MaterialTheme.typography.titleMedium)
  Spacer(Modifier.height(4.dp))
  Text(story.author, color = TextSecondary, style = MaterialTheme.typography.labelMedium)
  if (expanded) {
    Spacer(Modifier.height(8.dp))
    Text(stringResource(story.description), color = TextSecondary)
  }
}
