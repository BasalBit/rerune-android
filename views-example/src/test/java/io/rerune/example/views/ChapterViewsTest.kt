package io.rerune.example.views

import android.app.Application
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ListView
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper
import org.robolectric.util.ReflectionHelpers
import rerune.*
import rerune.views.ReRuneViews

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w390dp-h844dp-mdpi", application = ChapterViewsApplication::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChapterViewsTest {
  private lateinit var controller: ActivityController<MainActivity>
  private val activity get() = controller.get()
  private val root get() = activity.window.decorView

  @Before fun launch() {
    controller = Robolectric.buildActivity(MainActivity::class.java).setup().visible()
    settle { !activity.model.sync.value.variantBusy }
  }
  @After fun close() { controller.pause().stop().destroy() }

  private fun settle(condition: () -> Boolean = { true }) {
    repeat(200) {
      ShadowLooper.idleMainLooper(100, java.util.concurrent.TimeUnit.MILLISECONDS)
      if (condition()) return
      Thread.sleep(10)
    }
    assertTrue("UI update timed out", condition())
  }
  private fun tap(tag: String) {
    val view = requireNotNull(root.findViewWithTag<View>(tag)) { "Missing $tag" }
    assertTrue("Disabled $tag", view.isEnabled)
    assertTrue(view.performClick())
    settle()
  }
  private fun tab(id: Int) {
    root.findViewWithTag<BottomNavigationView>("chapter-navigation").selectedItemId = id
    settle()
  }
  private fun title(expected: Int) = assertEquals(activity.getString(expected), root.findViewWithTag<TextView>("chapter-title").text.toString())
  private fun allText(view: View = root): List<String> = when (view) {
    is TextView -> listOf(view.text.toString())
    is ViewGroup -> (0 until view.childCount).flatMap { allText(view.getChildAt(it)) }
    else -> emptyList()
  }

  @Test fun libraryReaderDiscoveryAndSaved() {
    tap("continue-reading"); title(R.string.atlas_chapter_one)
    val heading = root.findViewWithTag<TextView>("chapter-title")
    assertEquals("Reader headings must use the reference line spacing", heading.textSize * 1.25f, heading.lineHeight.toFloat(), 1f)
    tap("advance-chapter"); title(R.string.atlas_chapter_two)
    tap("back-library")
    tab(R.id.chapter_saved); assertTrue(allText().contains(activity.getString(R.string.saved_empty_title)))
    tap("explore-stories")
    tap("filter-lantern")
    assertNull(root.findViewWithTag<View>("open-atlas"))
    tap("bookmark-lantern"); tab(R.id.chapter_saved)
    tap("open-lantern"); title(R.string.lantern_chapter_one)
    tap("advance-chapter"); title(R.string.lantern_chapter_two)
    tap("back-library"); tap("open-lantern"); title(R.string.lantern_chapter_two)
    tap("advance-chapter"); assertNotNull(root.findViewWithTag<View>("story-completed"))
    tap("advance-chapter"); title(R.string.lantern_chapter_one)
    tap("bookmark-lantern"); tap("back-library")
    assertTrue(allText().contains(activity.getString(R.string.saved_empty_title)))
    tab(R.id.chapter_discover); tap("filter-all"); tap("open-garden"); title(R.string.garden_chapter_one)
    tap("advance-chapter"); title(R.string.garden_chapter_two)
    activity.onBackPressedDispatcher.onBackPressed(); settle()
    assertFalse(activity.model.reading.value.readerOpen)
  }

  @Test @Config(application = ChapterViewsVariantScrollApplication::class, qualifiers = "w320dp-h568dp-mdpi")
  fun variantChangesUpdateTextWithoutReplacingOrDetachingSettings() {
    tap("bookmark-atlas")
    activity.model.advance(); settle()
    val reading = activity.model.reading.value
    tap("reading-settings")
    val sheet = requireNotNull(activity.settings)
    val original = requireNotNull(sheet.window).decorView
    assertNotNull(original.findViewWithTag<MaterialSwitch>("staging-mode-toggle"))
    var detachments = 0
    original.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
      override fun onViewAttachedToWindow(view: View) = Unit
      override fun onViewDetachedFromWindow(view: View) { detachments++ }
    })
    val toggle = original.findViewWithTag<MaterialSwitch>("translation-variant-toggle")
    val scroll = original.findViewWithTag<View>("reading-settings-content").parent as ScrollView
    scroll.scrollTo(0, 48); settle()
    val position = scroll.scrollY
    assertTrue(position > 0)
    repeat(6) { index ->
      toggle.performClick()
      settle { !activity.model.sync.value.variantBusy }
      assertEquals(index % 2 == 0, toggle.isChecked)
      assertSame(sheet, activity.settings)
      assertSame(original, requireNotNull(activity.settings?.window).decorView)
      assertTrue(sheet.isShowing)
      assertEquals(0, detachments)
      assertTrue(allText(original).contains(if (index % 2 == 0) "VIP reading settings" else "Reading settings"))
      assertEquals(position, scroll.scrollY)
      assertEquals(reading, activity.model.reading.value)
    }
    assertTrue(allText().contains("The quiet atlas"))
    assertTrue(StoryId.Atlas in activity.model.reading.value.saved)
    sheet.dismiss(); settle()
    tap("continue-reading")
    assertTrue(allText().contains("The quiet atlas"))
  }

  @Test @Config(sdk = [32]) fun languagePickerKeepsChapterAndBookmarksAfterRecreation() {
    tap("bookmark-atlas"); tap("continue-reading"); tap("advance-chapter")
    val original = activity.model
    root.findViewWithTag<ScrollView>("chapter-scroll").scrollTo(0, 90); settle()
    val position = root.findViewWithTag<ScrollView>("chapter-scroll").scrollY
    assertTrue(position > 0)
    tap("language-menu")
    val manager = ReflectionHelpers.callStaticMethod<Any>(Class.forName("android.view.WindowManagerGlobal"), "getInstance")
    val windows = ReflectionHelpers.getField<ArrayList<View>>(manager, "mViews")
    fun descendants(view: View): List<View> = listOf(view) + if (view is ViewGroup)
      (0 until view.childCount).flatMap { descendants(view.getChildAt(it)) } else emptyList()
    val list = windows.flatMap(::descendants).filterIsInstance<ListView>().last()
    assertTrue(list.performItemClick(list.getChildAt(1), 1, list.adapter.getItemId(1))) // Deutsch, after system default.
    settle()
    controller.recreate().visible(); settle()
    assertSame(original, activity.model)
    assertEquals("Was die Karte bewahrt", root.findViewWithTag<TextView>("chapter-title").text.toString())
    assertTrue(StoryId.Atlas in activity.model.reading.value.saved)
    assertEquals(1, activity.model.reading.value.completed(StoryId.Atlas))
    assertEquals(position, root.findViewWithTag<ScrollView>("chapter-scroll").scrollY)
  }

  @Test @Config(qualifiers = "w320dp-h568dp-mdpi") fun narrowLayout() = walkLayout()
  @Test @Config(qualifiers = "w1024dp-h768dp-mdpi") fun wideLayout() = walkLayout()
  @Test @Config(qualifiers = "de-rDE-w390dp-h844dp-mdpi", application = GermanViewsApplication::class)
  fun germanLargeTextLayout() = walkLayout()

  private fun walkLayout() {
    tap("continue-reading"); title(R.string.atlas_chapter_one)
    tap("back-library"); tab(R.id.chapter_discover)
    tap("bookmark-garden"); tab(R.id.chapter_saved)
    tap("reading-settings")
    assertNotNull(activity.settings?.window?.decorView?.findViewWithTag<View>("refresh-texts"))
  }

}

open class ChapterViewsApplication : Application() {
  protected open val withVariant = false
  override fun onCreate() {
    super.onCreate()
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(resources.configuration.locales[0].toLanguageTag()))
    ReRune.setup(this, "chapter-test", updatePolicy = ReRuneUpdatePolicy(checkOnStart = false, periodicIntervalInDays = 0), cacheStore = ChapterViewsCache(withVariant))
    ReRuneViews.install(this)
    runBlocking { ReRune.getVariant() }
  }
}
class ChapterViewsVariantScrollApplication : ChapterViewsApplication() {
  override val withVariant = true
  override fun onCreate() {
    super.onCreate()
    RuntimeEnvironment.setFontScale(1.8f)
  }
}

class GermanViewsApplication : ChapterViewsApplication() {
  override fun onCreate() { super.onCreate(); RuntimeEnvironment.setFontScale(1.8f) }
}

private class ChapterViewsCache(private val withVariant: Boolean) : ReRuneCacheStore {
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
