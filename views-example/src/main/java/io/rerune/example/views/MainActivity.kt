package io.rerune.example.views

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import io.rerune.example.views.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import rerune.reRune
import rerune.views.reRuneOnStringsUpdated

class MainActivity : AppCompatActivity() {
  internal val model by lazy { ViewModelProvider(this)[StorybookModel::class.java] }
  internal var settings: ReadingSettingsDialog? = null
    private set
  private lateinit var binding: ActivityMainBinding
  private lateinit var widgets: ChapterWidgets
  private var rendered: ReadingState? = null
  private var scroll: ScrollView? = null
  private var scrollKey = ""
  private val back = object : OnBackPressedCallback(false) {
    override fun handleOnBackPressed() = model.back()
  }

  override fun attachBaseContext(newBase: Context) = super.attachBaseContext(newBase.reRune())

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = android.graphics.Color.TRANSPARENT
    window.navigationBarColor = color(R.color.bg_primary)
    WindowCompat.getInsetsController(window, window.decorView).apply {
      isAppearanceLightStatusBars = false; isAppearanceLightNavigationBars = false
    }
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    ViewCompat.setOnApplyWindowInsetsListener(binding.screenContainer) { view, insets ->
      val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
      view.updatePadding(left = bars.left, top = bars.top, right = bars.right, bottom = bars.bottom)
      insets
    }
    ViewCompat.requestApplyInsets(binding.screenContainer)
    onBackPressedDispatcher.addCallback(this, back)
    render(model.reading.value)
    reRuneOnStringsUpdated(lifecycleScope) { rebind() }
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch { model.reading.collect(::render) }
        launch { model.sync.collect { rebind() } }
      }
    }
    if (savedInstanceState?.getBoolean("settings-open") == true) showSettings()
  }

  private fun rebind() {
    widgets.rebind()
    settings?.rebind()
  }

  private fun saveScroll() {
    scroll?.let { model.scrollPositions[scrollKey] = it.scrollY }
  }

  private fun render(state: ReadingState) {
    back.isEnabled = state.readerOpen
    val previous = rendered
    // Bookmark changes update icons in place unless the saved list's membership changes.
    if (previous != null && previous.copy(saved = state.saved) == state &&
      (state.readerOpen || state.tab != LibraryTab.Saved || previous.saved == state.saved)) {
      rendered = state
      rebind()
      return
    }
    saveScroll()
    rendered = state
    widgets = ChapterWidgets(this, model)
    scrollKey = if (state.readerOpen) "reader-${state.current}-${state.completed(state.current)}" else "library-${state.tab}"
    val position = model.scrollPositions[scrollKey] ?: 0
    val root = widgets.column()
    if (state.readerOpen) root.addView(widgets.centered(widgets.readerHeader(), 720))
    scroll = ScrollView(this).apply {
      tag = "chapter-scroll"
      isFillViewport = true
      isVerticalScrollBarEnabled = false
      val content = if (state.readerOpen) widgets.readerContent() else widgets.libraryContent(::showSettings)
      addView(widgets.centered(content, if (state.readerOpen) 720 else 1040))
    }
    root.addView(scroll, LinearLayout.LayoutParams(Fill, 0, 1f))
    if (!state.readerOpen) root.addView(navigation(), LinearLayout.LayoutParams(Fill, dp(80)))
    binding.sceneHost.removeAllViews()
    binding.sceneHost.addView(root, FrameLayout.LayoutParams(Fill, Fill, Gravity.TOP or Gravity.CENTER_HORIZONTAL))
    scroll?.post { scroll?.scrollTo(0, position) }
  }

  private fun navigation() = BottomNavigationView(this).apply {
    tag = "chapter-navigation"
    setBackgroundColor(color(R.color.bg_primary))
    elevation = 0f
    labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
    isItemHorizontalTranslationEnabled = false
    itemIconSize = dp(24)
    itemTextAppearanceActive = R.style.ChapterNavigationText
    itemTextAppearanceInactive = R.style.ChapterNavigationText
    itemActiveIndicatorColor = ColorStateList.valueOf(android.graphics.Color.rgb(40, 35, 28))
    val colors = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
      intArrayOf(color(R.color.accent_primary), color(R.color.text_secondary)))
    itemTextColor = colors; itemIconTintList = colors
    val ids = listOf(R.id.chapter_library, R.id.chapter_discover, R.id.chapter_saved)
    val labels = listOf(R.string.nav_library, R.string.nav_discover, R.string.nav_saved)
    val icons = listOf(R.drawable.chapter_book, R.drawable.chapter_compass, R.drawable.chapter_bookmark)
    LibraryTab.entries.forEachIndexed { index, _ ->
      menu.add(0, ids[index], index, getString(labels[index])).setIcon(icons[index])
      widgets.bind { menu.findItem(ids[index]).title = getString(labels[index]) }
    }
    selectedItemId = ids[model.reading.value.tab.ordinal]
    setOnItemSelectedListener { item ->
      model.selectTab(LibraryTab.entries[ids.indexOf(item.itemId)])
      true
    }
  }

  private fun showSettings() {
    if (settings != null) return
    settings = ReadingSettingsDialog(this, model).apply {
      setOnDismissListener { settings = null }
      show()
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    saveScroll()
    outState.putBoolean("settings-open", settings?.isShowing == true)
    super.onSaveInstanceState(outState)
  }

  override fun onDestroy() {
    settings?.dismiss()
    super.onDestroy()
  }
}
