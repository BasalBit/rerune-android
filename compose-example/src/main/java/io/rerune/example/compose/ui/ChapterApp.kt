package io.rerune.example.compose.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import io.rerune.example.compose.R
import io.rerune.example.compose.*
import io.rerune.example.compose.ui.components.*
import io.rerune.example.compose.ui.screens.*
import io.rerune.example.compose.ui.theme.*
import rerune.compose.reRuneObserveRevision

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChapterApp(model: StorybookModel, onLocale: (String?) -> Unit) {
  val reading by model.reading.collectAsState()
  val sync by model.sync.collectAsState()
  var settingsOpen by rememberSaveable { mutableStateOf(false) }
  // The SDK observer keys its content by revision. Keep state and scroll owners above it.
  val libraryScrolls = LibraryTab.entries.map { rememberScrollState() }
  val readerScroll = key(reading.current, reading.completed(reading.current)) { rememberScrollState() }
  val settingsScroll = rememberScrollState()
  BackHandler(reading.readerOpen && !settingsOpen) { model.back() }

  reRuneObserveRevision {
    Scaffold(containerColor = BgPrimary, contentWindowInsets = WindowInsets.safeDrawing,
      bottomBar = {
        if (!reading.readerOpen) ChapterNavigation(reading.tab, model::selectTab)
      }) { padding ->
      Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
        if (reading.readerOpen) {
          ReaderScreen(reading, sync, readerScroll, model::back, model::bookmark, model::advance, model::refresh, onLocale)
        } else {
          LibraryScreen(reading, sync, libraryScrolls[reading.tab.ordinal], model::open,
            model::bookmark, model::selectTab, model::filter, model::refresh, onLocale, { settingsOpen = true })
        }
      }
    }
  }
  // Keep the modal window and its animation outside the revision-keyed subtree.
  // Only its resource-backed content needs to be recreated when words change.
  if (settingsOpen) {
    ModalBottomSheet(onDismissRequest = { settingsOpen = false }, containerColor = BgSecondary,
      tonalElevation = 0.dp, scrimColor = Color.Black.copy(alpha = .54f),
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
      reRuneObserveRevision {
        ReadingSettings(sync, settingsScroll, model::setVip, model::refresh)
      }
    }
  }
}

@Composable
private fun ChapterNavigation(tab: LibraryTab, onSelect: (LibraryTab) -> Unit) {
  val density = LocalDensity.current
  NavigationBar(containerColor = BgPrimary) {
    val labels = listOf(R.string.nav_library, R.string.nav_discover, R.string.nav_saved)
    val icons = listOf(R.drawable.chapter_book, R.drawable.chapter_compass, R.drawable.chapter_bookmark)
    LibraryTab.entries.forEachIndexed { index, item ->
      NavigationBarItem(selected = tab == item, onClick = { onSelect(item) },
        modifier = Modifier.testTag("nav-${item.name.lowercase()}"),
        icon = { ChapterIcon(icons[index], tint = if (tab == item) AccentPrimary else TextSecondary) },
        label = {
          // Match the reference navigation's bounded label scaling.
          CompositionLocalProvider(LocalDensity provides Density(density.density, density.fontScale.coerceAtMost(1.3f))) {
            Text(androidx.compose.ui.res.stringResource(labels[index]),
              style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
          }
        },
        colors = NavigationBarItemDefaults.colors(selectedTextColor = AccentPrimary, unselectedTextColor = TextSecondary))
    }
  }
}
