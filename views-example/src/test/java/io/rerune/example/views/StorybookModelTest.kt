package io.rerune.example.views

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import rerune.ReRuneUpdateResult
import rerune.ReRuneVariant

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class StorybookModelTest {
  private val dispatcher = StandardTestDispatcher()
  @Before fun setup() { Dispatchers.setMain(dispatcher) }
  @After fun teardown() { Dispatchers.resetMain() }

  @Test fun `every story completes and restarts independently without changing saved ids`() {
    var state = ReadingState()
    for (id in StoryId.entries) {
      state = state.open(id).toggleSaved(id).advance()
      assertEquals(.5f, state.progress(), 0f)
      state = state.copy(readerOpen = false).open(id)
      assertEquals(1, state.chapterIndex())
      state = state.advance()
      assertEquals(1f, state.progress(), 0f)
    }
    state = state.open(StoryId.Lantern).advance()
    assertEquals(0f, state.progress(), 0f)
    assertEquals(2, state.completed(StoryId.Atlas))
    assertEquals(2, state.completed(StoryId.Garden))
    assertEquals(StoryId.entries.toSet(), state.saved)
    StoryId.entries.forEach { state = state.toggleSaved(it) }
    assertTrue(state.saved.isEmpty())
  }

  @Test fun `refresh prevents overlap and retains route progress bookmark tab and filter`() = runTest(dispatcher) {
    val gateway = FakeUpdates()
    val pending = CompletableDeferred<ReRuneUpdateResult>()
    gateway.pending = pending
    val model = StorybookModel(gateway)
    model.selectTab(LibraryTab.Discover)
    model.filter(StoryId.Lantern)
    model.open(StoryId.Lantern)
    model.bookmark(StoryId.Lantern)
    model.advance()
    val before = model.reading.value
    model.refresh(); model.refresh()
    assertTrue(model.sync.value.refreshing)
    runCurrent()
    assertEquals(1, gateway.calls)
    pending.complete(ReRuneUpdateResult(ReRuneUpdateResult.ReRuneUpdateStatus.Updated))
    advanceUntilIdle()
    assertFalse(model.sync.value.refreshing)
    assertEquals(R.string.welcome_refresh_state_success, model.sync.value.feedback)
    assertEquals(before, model.reading.value)
  }

  @Test fun `unchanged terminal partial and thrown failures are reported truthfully`() = runTest(dispatcher) {
    val gateway = FakeUpdates()
    val model = StorybookModel(gateway)
    model.refresh(); advanceUntilIdle()
    assertEquals(R.string.refresh_current, model.sync.value.feedback)
    for (status in ReRuneUpdateResult.ReRuneUpdateStatus.entries) {
      gateway.result = ReRuneUpdateResult(status, errorMessage = "Locale failed")
      model.refresh(); advanceUntilIdle()
      assertEquals(R.string.refresh_error, model.sync.value.feedback)
    }
    gateway.failRefresh = true
    model.refresh(); advanceUntilIdle()
    assertEquals(R.string.refresh_error, model.sync.value.feedback)
    assertFalse(model.sync.value.refreshing)
  }

  @Test fun `restored variant survives failed persistent change and overlap is ignored`() = runTest(dispatcher) {
    val gateway = FakeUpdates()
    gateway.variant = ReRuneVariant.fromSlug("vip")
    val model = StorybookModel(gateway)
    advanceUntilIdle()
    assertEquals(gateway.variant, model.sync.value.variant)
    assertTrue(model.sync.value.variantReady)
    gateway.failVariant = true
    model.setVip(false); model.setVip(false)
    advanceUntilIdle()
    assertEquals(1, gateway.variantWrites)
    assertEquals(ReRuneVariant.fromSlug("vip"), model.sync.value.variant)
    assertEquals(R.string.edition_change_error, model.sync.value.feedback)
    gateway.failVariant = false
    model.setVip(false); advanceUntilIdle()
    assertEquals(ReRuneVariant.Main, model.sync.value.variant)
  }

  @Test fun `staging switch reports the triggered sync and ignores overlap and failures`() = runTest(dispatcher) {
    val gateway = FakeUpdates()
    val pending = CompletableDeferred<ReRuneUpdateResult?>()
    gateway.pendingStaging = pending
    val model = StorybookModel(gateway)
    advanceUntilIdle()

    model.setStaging(true); model.setStaging(false)
    assertTrue(model.sync.value.stagingBusy)
    runCurrent()
    assertEquals(1, gateway.stagingWrites)
    pending.complete(ReRuneUpdateResult(ReRuneUpdateResult.ReRuneUpdateStatus.Updated))
    advanceUntilIdle()
    assertTrue(model.sync.value.staging)
    assertEquals(R.string.welcome_refresh_state_success, model.sync.value.feedback)
    assertFalse(model.sync.value.stagingBusy)

    gateway.pendingStaging = null
    gateway.stagingResult = null
    model.setStaging(false); advanceUntilIdle()
    assertFalse(model.sync.value.staging)
    assertEquals(R.string.refresh_current, model.sync.value.feedback)

    gateway.stagingResult = ReRuneUpdateResult(ReRuneUpdateResult.ReRuneUpdateStatus.Failed, errorMessage = "offline")
    model.setStaging(true); advanceUntilIdle()
    assertTrue(model.sync.value.staging)
    assertEquals(R.string.staging_mode_error, model.sync.value.feedback)

    gateway.failStaging = true
    model.setStaging(false); advanceUntilIdle()
    assertFalse(model.sync.value.staging)
    assertEquals(R.string.staging_mode_error, model.sync.value.feedback)
    assertFalse(model.sync.value.stagingBusy)
  }

  @Test fun `staging state starts from the active SDK mode`() = runTest(dispatcher) {
    val gateway = FakeUpdates().apply { isStaging = true }

    val model = StorybookModel(gateway)

    assertTrue(model.sync.value.staging)
  }

  private class FakeUpdates : StorybookUpdates {
    override var isStaging: Boolean = false
    var variant = ReRuneVariant.Main
    var calls = 0
    var variantWrites = 0
    var failVariant = false
    var failRefresh = false
    var pending: CompletableDeferred<ReRuneUpdateResult>? = null
    var result = ReRuneUpdateResult(ReRuneUpdateResult.ReRuneUpdateStatus.NoChange)
    override suspend fun getVariant() = variant
    override suspend fun setVariant(variant: ReRuneVariant) {
      variantWrites++
      if (failVariant) error("Storage rejected write")
      this.variant = variant
    }
    override suspend fun refresh(): ReRuneUpdateResult {
      calls++
      if (failRefresh) error("Network unavailable")
      return pending?.await() ?: result
    }

    var stagingWrites = 0
    var failStaging = false
    var pendingStaging: CompletableDeferred<ReRuneUpdateResult?>? = null
    var stagingResult: ReRuneUpdateResult? = null
    override suspend fun setStaging(staging: Boolean): ReRuneUpdateResult? {
      stagingWrites++
      isStaging = staging
      if (failStaging) error("Storage rejected write")
      return pendingStaging?.await() ?: stagingResult
    }
  }
}
