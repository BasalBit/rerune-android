package io.rerune.example.compose

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import android.view.View
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.util.ReflectionHelpers
import org.robolectric.util.ReflectionHelpers.ClassParameter
import org.robolectric.shadows.ShadowWindowManagerGlobal
import rerune.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w390dp-h844dp-mdpi", application = ChapterTestApplication::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@OptIn(ExperimentalTestApi::class)
class ChapterUiTest {
  @get:Rule val compose = createAndroidComposeRule<MainActivity>()

  private fun tap(tag: String) { compose.onNodeWithTag(tag).performScrollTo().performClick(); compose.waitForIdle() }
  private fun model() = ViewModelProvider(compose.activity)[StorybookModel::class.java]
  private fun scrollPosition(tag: String) = compose.onNodeWithTag(tag).fetchSemanticsNode()
    .config[SemanticsProperties.VerticalScrollAxisRange].value()
  private fun scrollBy(tag: String, pixels: Float) {
    compose.onNodeWithTag(tag).performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, pixels) }
    compose.waitForIdle()
  }

  @Test fun libraryReaderDiscoveryAndSaved() {
    tap("continue-reading")
    tap("advance-chapter")
    compose.onNodeWithContentDescription("Back to library").performClick()
    compose.onNodeWithText("Saved").performClick()
    compose.onNodeWithText("A shelf of your own").assertExists()
    compose.onNodeWithText("Explore stories").performClick()
    compose.onAllNodesWithText("Adventure")[0].performClick()
    compose.onNodeWithTag("open-atlas").assertDoesNotExist()
    tap("bookmark-lantern")
    compose.onNodeWithText("Saved").performClick()
    tap("open-lantern")
    compose.onNodeWithTag("chapter-title").assertTextEquals("A light at the station")
    tap("advance-chapter")
    compose.onNodeWithTag("chapter-title").assertTextEquals("The other side of night")
    compose.onNodeWithContentDescription("Back to library").performClick()
    tap("open-lantern")
    compose.onNodeWithTag("chapter-title").assertTextEquals("The other side of night")
    tap("advance-chapter")
    compose.onNodeWithText("Story completed").assertExists()
    tap("advance-chapter")
    compose.onNodeWithTag("chapter-title").assertTextEquals("A light at the station")
    tap("bookmark-lantern")
    compose.onNodeWithContentDescription("Back to library").performClick()
    compose.onNodeWithText("A shelf of your own").assertExists()
  }

  @Test fun gardenReaderChapters() {
    tap("open-garden")
    compose.onNodeWithTag("chapter-title").assertTextEquals(compose.activity.getString(R.string.garden_chapter_one))
    tap("advance-chapter")
    compose.onNodeWithTag("chapter-title").assertTextEquals(compose.activity.getString(R.string.garden_chapter_two))
  }

  @Test @Config(sdk = [32]) fun languageChangeOnOpenChapterRetainsReadingState() {
    tap("bookmark-atlas")
    tap("continue-reading")
    tap("advance-chapter")
    scrollBy("chapter-scroll", 90f)
    val position = scrollPosition("chapter-scroll")
    assertTrue(position > 0f)
    compose.onNodeWithTag("language-menu").performClick()
    compose.onNodeWithText("Deutsch").performClick()
    compose.waitForIdle()
    compose.onNodeWithTag("chapter-title").assertTextEquals("Was die Karte bewahrt")
    compose.onNodeWithContentDescription("Aus Sammlung entfernen").assertExists()
    assertEquals(position, scrollPosition("chapter-scroll"), 1f)
    compose.runOnIdle {
      assertEquals(1, model().reading.value.completed(StoryId.Atlas))
      assertTrue(model().reading.value.readerOpen)
    }
  }

  @Test @Config(application = ChapterVariantScrollTestApplication::class, qualifiers = "w320dp-h568dp-mdpi")
  fun variantChangesKeepTheSameSettingsWindowOpen() {
    tap("bookmark-atlas")
    compose.runOnIdle { model().advance() }
    val reading = model().reading.value
    tap("reading-settings")
    compose.waitForIdle()
    compose.onNodeWithTag("staging-mode-toggle").assertIsDisplayed()
    fun sheetWindow(): View {
      val manager = ReflectionHelpers.callStaticMethod<Any>(Class.forName("android.view.WindowManagerGlobal"), "getInstance")
      return ReflectionHelpers.getField<ArrayList<View>>(manager, "mViews").last()
    }
    val original = sheetWindow()
    var detachments = 0
    compose.runOnIdle {
      original.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) = Unit
        override fun onViewDetachedFromWindow(view: View) { detachments++ }
      })
    }
    scrollBy("reading-settings-content", 48f)
    val position = scrollPosition("reading-settings-content")
    assertTrue(position > 0f)
    repeat(6) { index ->
      compose.onNodeWithTag("translation-variant-toggle").performClick()
      compose.waitUntil { !model().sync.value.variantBusy }
      compose.waitForIdle()
      assertSame("Variant updates must not replace the modal window", original, sheetWindow())
      assertEquals("The sheet must never detach during a text revision", 0, detachments)
      compose.onNodeWithText(if (index % 2 == 0) "VIP reading settings" else "Reading settings").assertExists()
      assertEquals(position, scrollPosition("reading-settings-content"), 1f)
      assertEquals(reading, model().reading.value)
    }
  }

  @Test @Config(application = ChapterVariantTestApplication::class)
  fun sdkRevisionChangesOpenTextWithoutDiscardingState() {
    tap("bookmark-atlas")
    tap("continue-reading")
    compose.runOnIdle { model().setVip(true) }
    compose.waitUntil { !model().sync.value.variantBusy }
    compose.onNodeWithText("The atlas of tomorrow").assertExists()
    compose.onNodeWithContentDescription("Remove from saved").assertExists()
    compose.runOnIdle { assertEquals(0, model().reading.value.completed(StoryId.Atlas)) }
    compose.runOnIdle { model().setVip(false) }
    compose.waitUntil { !model().sync.value.variantBusy }
    compose.onNodeWithText("The quiet atlas").assertExists()
  }

  @Test @Config(application = ChapterKeyboardApplication::class)
  fun keyboardActivationAndNativeBack() {
    compose.runOnIdle { compose.activity.window.decorView.dispatchWindowFocusChanged(true) }
    compose.onRoot().performKeyInput { pressKey(Key.Tab) }
    val button = compose.onNodeWithTag("continue-reading").performScrollTo()
    button.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
    button.assertIsFocused().performKeyInput { pressKey(Key.Enter) }
    compose.onNodeWithTag("chapter-title").assertTextEquals("The river without a name")
    compose.runOnIdle { compose.activity.onBackPressedDispatcher.onBackPressed() }
    compose.onNodeWithTag("nav-library").assertIsSelected()
  }

  @Test @Config(qualifiers = "de-rDE-w390dp-h844dp-mdpi", application = GermanLargeTextApplication::class)
  fun germanAtLargeTextScale() {
    compose.onNodeWithText("Zeit für eine Geschichte.").assertExists()
    walkLargeLayout()
  }

  @Test @Config(qualifiers = "w320dp-h568dp-mdpi") fun narrowLayout() { walkLargeLayout() }
  @Test @Config(qualifiers = "w1024dp-h768dp-mdpi") fun wideLayout() { walkLargeLayout() }

  private fun walkLargeLayout() {
    tap("continue-reading")
    tap("advance-chapter")
    compose.onNodeWithTag("chapter-title").assertTextEquals(compose.activity.getString(R.string.atlas_chapter_two))
    compose.onNodeWithContentDescription(compose.activity.getString(R.string.back_library)).performClick()
    compose.onNodeWithTag("nav-discover").performClick()
    tap("bookmark-garden")
    compose.onNodeWithTag("nav-saved").performClick()
    tap("open-garden")
    tap("advance-chapter")
    compose.onNodeWithTag("chapter-title").assertTextEquals(compose.activity.getString(R.string.garden_chapter_two))
    compose.onNodeWithContentDescription(compose.activity.getString(R.string.back_library)).performClick()
    compose.onNodeWithTag("reading-settings").performScrollTo().performClick()
    compose.onAllNodesWithTag("refresh-texts").onLast().assertExists()
  }

}

open class ChapterTestApplication : Application() {
  protected open val withVariant = false
  override fun onCreate() {
    super.onCreate()
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(resources.configuration.locales[0].toLanguageTag()))
    ReRune.setup(this, "chapter-test", updatePolicy = ReRuneUpdatePolicy(checkOnStart = false, periodicIntervalInDays = 0),
      cacheStore = ChapterTestCache(withVariant))
    runBlocking { ReRune.getVariant() }
  }
}

class ChapterVariantTestApplication : ChapterTestApplication() {
  override val withVariant = true
}

class ChapterVariantScrollTestApplication : ChapterTestApplication() {
  override val withVariant = true
  override fun onCreate() {
    super.onCreate()
    RuntimeEnvironment.setFontScale(1.8f)
  }
}

class ChapterKeyboardApplication : ChapterTestApplication() {
  override fun onCreate() {
    super.onCreate()
    // Headless windows start in touch mode. A hardware keyboard leaves it.
    ReflectionHelpers.callStaticMethod<Void>(ShadowWindowManagerGlobal::class.java, "setInTouchMode",
      ClassParameter.from(Boolean::class.javaPrimitiveType, false))
  }
}

class GermanLargeTextApplication : ChapterTestApplication() {
  override fun onCreate() {
    super.onCreate()
    RuntimeEnvironment.setFontScale(1.8f)
  }
}

private class ChapterTestCache(private val withVariant: Boolean) : ReRuneCacheStore {
  private var variant: ReRuneVariant? = null
  override suspend fun reRuneReadVariant(otaPublishId: String) = variant
  override suspend fun reRuneWriteVariant(otaPublishId: String, variant: ReRuneVariant) { this.variant = variant }
  override suspend fun reRuneReadManifest() = ReRuneCachedManifest("""{"main_language":"en","locales":{"en":{"version":1,"minimum_delta_base_version":1,"url":"https://example.invalid/en"}}}""")
  override suspend fun reRuneWriteManifest(manifest: ReRuneCachedManifest) = Unit
  override suspend fun reRuneReadLocaleBundle(localeTag: String) = reRuneReadAllLocaleBundles().firstOrNull { it.localeTag == localeTag }
  override suspend fun reRuneWriteLocaleBundle(bundle: ReRuneCachedLocaleBundle) = Unit
  override suspend fun reRuneReadAllLocaleBundles() = if (!withVariant) emptyList() else listOf(ReRuneCachedLocaleBundle("en",
    """[{"type":"key","key":"story_title","values":[{"lang":"en","value":"The quiet atlas","variations":[{"variation":"vip","value":"The atlas of tomorrow"}]}]},{"type":"key","key":"reading_settings","values":[{"lang":"en","value":"Reading settings","variations":[{"variation":"vip","value":"VIP reading settings"}]}]}]""",
    1, 1, "English", "https://example.invalid/en"))
  override suspend fun reRuneDeleteLocaleBundle(localeTag: String) = Unit
}
