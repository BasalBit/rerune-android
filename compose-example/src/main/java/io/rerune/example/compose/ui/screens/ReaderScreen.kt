package io.rerune.example.compose.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.rerune.example.compose.R
import io.rerune.example.compose.*
import io.rerune.example.compose.ui.components.*
import io.rerune.example.compose.ui.theme.*

@Composable
internal fun ReaderScreen(
  state: ReadingState, sync: SyncState, scroll: ScrollState, onBack: () -> Unit,
  onBookmark: (StoryId) -> Unit, onAdvance: () -> Unit, onRefresh: () -> Unit, onLocale: (String?) -> Unit,
) {
  val story = state.story()
  val finished = state.progress() == 1f
  Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
      IconButton(onBack, Modifier.size(48.dp)) { ChapterIcon(R.drawable.chapter_back, stringResource(R.string.back_library)) }
      Text(stringResource(story.title), Modifier.weight(1f).padding(horizontal = 8.dp),
        textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
      LanguageMenu(onLocale)
    }
    Column(Modifier.fillMaxWidth().testTag("chapter-scroll").verticalScroll(scroll).padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp)) {
      StoryCover(story.id, Modifier.fillMaxWidth().height(200.dp)) {
        Box(Modifier.align(Alignment.BottomEnd).padding(14.dp)) {
          BookmarkButton(story.id, state, { onBookmark(story.id) }, onCover = true)
        }
      }
      ReadingProgress(state)
      if (finished) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
          ChapterIcon(R.drawable.chapter_book, tint = AccentPrimary, modifier = Modifier.size(40.dp))
          Text(stringResource(R.string.reader_end_title), Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
          Text(stringResource(R.string.reader_end_body), color = TextSecondary, textAlign = TextAlign.Center)
        }
      } else {
        val chapter = story.chapters[state.chapterIndex()]
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(stringResource(chapter.title), Modifier.testTag("chapter-title"),
            fontFamily = ReaderFont, fontSize = 30.sp, lineHeight = 37.5.sp)
          Text(stringResource(R.string.story_by, story.author), color = TextSecondary, style = MaterialTheme.typography.labelMedium)
        }
        SelectionContainer {
          Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            chapter.paragraphs.forEach { paragraph ->
              Text(stringResource(paragraph), color = ReaderText, fontFamily = ReaderFont, fontSize = 19.sp, lineHeight = 35.15.sp)
            }
          }
        }
      }
      ReadingButton(stringResource(when {
        finished -> R.string.read_again
        state.chapterIndex() == story.chapters.lastIndex -> R.string.finish_story
        else -> R.string.next_chapter
      }), onAdvance, Modifier.fillMaxWidth().testTag("advance-chapter"))
      RefreshButton(sync, onRefresh, Modifier.fillMaxWidth())
      SyncFeedback(sync)
    }
  }
}
