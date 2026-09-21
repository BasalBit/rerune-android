package io.rerune.example.compose

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import rerune.ReRune
import rerune.ReRuneUpdateResult
import rerune.ReRuneVariant

internal enum class StoryId { Atlas, Lantern, Garden }
internal enum class LibraryTab { Library, Discover, Saved }

internal data class ChapterResources(@StringRes val title: Int, val paragraphs: List<Int>)
internal data class StoryResources(
  val id: StoryId,
  @StringRes val title: Int,
  val author: String,
  @StringRes val genre: Int,
  @StringRes val description: Int,
  val chapters: List<ChapterResources>,
)

// Resource identities, never resolved text, can safely outlive a locale or revision.
internal val stories = listOf(
  StoryResources(StoryId.Atlas, R.string.story_title, "Nora Vale", R.string.genre_wonder,
    R.string.atlas_description, listOf(
      ChapterResources(R.string.atlas_chapter_one, listOf(R.string.story_body_primary, R.string.story_body_secondary)),
      ChapterResources(R.string.atlas_chapter_two, listOf(R.string.atlas_body_three, R.string.atlas_body_four)),
    )),
  StoryResources(StoryId.Lantern, R.string.lantern_title, "Elias North", R.string.genre_adventure,
    R.string.lantern_description, listOf(
      ChapterResources(R.string.lantern_chapter_one, listOf(R.string.lantern_body_one, R.string.lantern_body_two)),
      ChapterResources(R.string.lantern_chapter_two, listOf(R.string.lantern_body_three, R.string.lantern_body_four)),
    )),
  StoryResources(StoryId.Garden, R.string.garden_title, "Mila Sol", R.string.genre_nature,
    R.string.garden_description, listOf(
      ChapterResources(R.string.garden_chapter_one, listOf(R.string.garden_body_one, R.string.garden_body_two)),
      ChapterResources(R.string.garden_chapter_two, listOf(R.string.garden_body_three, R.string.garden_body_four)),
    )),
)

internal data class ReadingState(
  val tab: LibraryTab = LibraryTab.Library,
  val filter: StoryId? = null,
  val current: StoryId = StoryId.Atlas,
  val readerOpen: Boolean = false,
  val saved: Set<StoryId> = emptySet(),
  val completed: Map<StoryId, Int> = emptyMap(),
) {
  fun completed(id: StoryId): Int = completed[id] ?: 0
  fun story(id: StoryId = current): StoryResources = stories.first { it.id == id }
  fun chapterIndex(id: StoryId = current): Int = completed(id).coerceAtMost(story(id).chapters.lastIndex)
  fun progress(id: StoryId = current): Float = completed(id).toFloat() / story(id).chapters.size
  fun open(id: StoryId) = copy(current = id, readerOpen = true)
  fun toggleSaved(id: StoryId) = copy(saved = if (id in saved) saved - id else saved + id)
  fun advance(): ReadingState {
    val count = if (progress() == 1f) 0 else completed(current) + 1
    return copy(completed = completed + (current to count))
  }
}

internal data class SyncState(
  val refreshing: Boolean = false,
  @StringRes val feedback: Int? = null,
  val variant: ReRuneVariant = ReRuneVariant.Main,
  val variantBusy: Boolean = true,
  val variantReady: Boolean = false,
  val staging: Boolean = false,
  val stagingBusy: Boolean = false,
)

internal interface StorybookUpdates {
  val isStaging: Boolean
  suspend fun refresh(): ReRuneUpdateResult
  suspend fun getVariant(): ReRuneVariant
  suspend fun setVariant(variant: ReRuneVariant)
  suspend fun setStaging(staging: Boolean): ReRuneUpdateResult?
}

internal object SdkStorybookUpdates : StorybookUpdates {
  override val isStaging: Boolean
    get() = ReRune.isStaging
  override suspend fun refresh() = ReRune.checkForUpdates()
  override suspend fun getVariant() = ReRune.getVariant()
  override suspend fun setVariant(variant: ReRuneVariant) = ReRune.setVariant(variant, persist = true)
  override suspend fun setStaging(staging: Boolean) = ReRune.setStaging(staging)
}

internal class StorybookModel(
  private val updates: StorybookUpdates = SdkStorybookUpdates,
) : ViewModel() {
  private val readingState = MutableStateFlow(ReadingState())
  val reading = readingState.asStateFlow()
  private val syncState = MutableStateFlow(SyncState(staging = updates.isStaging))
  val sync = syncState.asStateFlow()

  init { restoreVariant() }

  fun open(id: StoryId) = readingState.update { it.open(id) }
  fun back() = readingState.update { it.copy(readerOpen = false) }
  fun selectTab(tab: LibraryTab) = readingState.update { it.copy(tab = tab) }
  fun filter(id: StoryId?) = readingState.update { it.copy(filter = id) }
  fun bookmark(id: StoryId) = readingState.update { it.toggleSaved(id) }
  fun advance() = readingState.update { it.advance() }

  fun refresh() {
    if (syncState.value.refreshing) return
    syncState.update { it.copy(refreshing = true, feedback = null) }
    viewModelScope.launch {
      try {
        val result = updates.refresh()
        val message = when {
          result.errorMessage != null || result.status == ReRuneUpdateResult.ReRuneUpdateStatus.Failed -> R.string.refresh_error
          result.status == ReRuneUpdateResult.ReRuneUpdateStatus.Updated -> R.string.welcome_refresh_state_success
          else -> R.string.refresh_current
        }
        syncState.update { it.copy(feedback = message) }
      } catch (cancelled: CancellationException) {
        throw cancelled
      } catch (_: Exception) {
        syncState.update { it.copy(feedback = R.string.refresh_error) }
      } finally {
        syncState.update { it.copy(refreshing = false) }
      }
    }
  }

  private fun restoreVariant() {
    viewModelScope.launch {
      try {
        val variant = updates.getVariant()
        syncState.update { it.copy(variant = variant, variantReady = true) }
      } catch (cancelled: CancellationException) {
        throw cancelled
      } catch (_: Exception) {
        syncState.update { it.copy(feedback = R.string.edition_change_error) }
      } finally {
        syncState.update { it.copy(variantBusy = false) }
      }
    }
  }

  fun setVip(enabled: Boolean) {
    if (syncState.value.variantBusy) return
    val variant = if (enabled) ReRuneVariant.fromSlug("vip") else ReRuneVariant.Main
    syncState.update { it.copy(variantBusy = true, feedback = null) }
    viewModelScope.launch {
      try {
        updates.setVariant(variant)
        syncState.update { it.copy(variant = variant, variantReady = true) }
      } catch (cancelled: CancellationException) {
        throw cancelled
      } catch (_: Exception) {
        // Android persists before activation, so a failed write leaves the old edition active.
        syncState.update { it.copy(feedback = R.string.edition_change_error) }
      } finally {
        syncState.update { it.copy(variantBusy = false) }
      }
    }
  }

  fun setStaging(enabled: Boolean) {
    if (syncState.value.stagingBusy) return
    syncState.update { it.copy(stagingBusy = true, feedback = null) }
    viewModelScope.launch {
      try {
        val result = updates.setStaging(enabled)
        syncState.update {
          it.copy(staging = updates.isStaging, feedback = stagingFeedback(result))
        }
      } catch (cancelled: CancellationException) {
        throw cancelled
      } catch (_: Exception) {
        syncState.update {
          it.copy(staging = updates.isStaging, feedback = R.string.staging_mode_error)
        }
      } finally {
        syncState.update { it.copy(stagingBusy = false) }
      }
    }
  }

  private fun stagingFeedback(result: ReRuneUpdateResult?): Int = when {
    result == null -> R.string.refresh_current
    result.errorMessage != null || result.status == ReRuneUpdateResult.ReRuneUpdateStatus.Failed -> R.string.staging_mode_error
    result.status == ReRuneUpdateResult.ReRuneUpdateStatus.Updated -> R.string.welcome_refresh_state_success
    else -> R.string.refresh_current
  }
}
