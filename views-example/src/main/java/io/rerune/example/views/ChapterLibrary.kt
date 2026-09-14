package io.rerune.example.views

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

internal fun ChapterWidgets.libraryContent(onSettings: () -> Unit): View {
  val state = model.reading.value
  return column().apply {
    setPadding(context.dp(24), context.dp(12), context.dp(24), context.dp(24))
    addView(libraryHeader(onSettings))
    addView(space(26))
    val (title, subtitle) = when (state.tab) {
      LibraryTab.Library -> R.string.welcome_title to R.string.welcome_subtitle
      LibraryTab.Discover -> R.string.discover_title to R.string.discover_subtitle
      LibraryTab.Saved -> R.string.saved_title to R.string.saved_subtitle
    }
    addView(label(title, 34f, bold = true).apply { chapterLineHeight(1.12f); letterSpacing = -.04f })
    addView(space(8))
    addView(label(subtitle, 16f, R.color.text_secondary))
    addView(space(26))
    when (state.tab) {
      LibraryTab.Library -> {
        if (context.resources.configuration.screenWidthDp - 48 >= 760) {
          addView(row().apply {
            gravity = Gravity.TOP
            addView(featuredStory(), LinearLayout.LayoutParams(0, Wrap, 6f))
            addView(View(context), LinearLayout.LayoutParams(context.dp(32), 1))
            addView(shelf(), LinearLayout.LayoutParams(0, Wrap, 5f))
          })
        } else {
          addView(featuredStory()); addView(space(28)); addView(shelf())
        }
      }
      LibraryTab.Discover -> {
        addView(filters()); addView(space(20))
        stories.filter { state.filter == null || it.id == state.filter }.forEach {
          addView(storyTile(it, expanded = true)); addView(space(16))
        }
      }
      LibraryTab.Saved -> {
        if (state.saved.isEmpty()) addView(emptyShelf())
        else stories.filter { it.id in state.saved }.forEach {
          addView(storyTile(it, expanded = true)); addView(space(16))
        }
      }
    }
    addView(space(30))
    addView(refresh(), LinearLayout.LayoutParams(Wrap, Wrap).apply { gravity = Gravity.CENTER_HORIZONTAL })
    addView(feedback(), LinearLayout.LayoutParams(Fill, Wrap))
    addView(space(16))
    addView(label(R.string.library_footer, 12f, R.color.text_secondary).apply { gravity = Gravity.CENTER })
  }
}

private fun ChapterWidgets.featuredStory() = column().apply {
  val story = model.reading.value.story()
  addView(row().apply {
    addView(View(context).apply { background = panel(R.color.accent_primary, 3) },
      LinearLayout.LayoutParams(context.dp(5), context.dp(5)).apply { marginEnd = context.dp(8) })
    addView(label(R.string.currently_reading, 13f, R.color.text_secondary), LinearLayout.LayoutParams(0, Wrap, 1f))
    addView(label(R.string.collection_issue, 13f, R.color.text_secondary))
  })
  addView(space(18))
  addView(StoryCoverView(context, story.id).apply {
    minimumHeight = context.dp(245)
    addView(column(22).apply {
      val ink = if (story.id == StoryId.Lantern) context.color(R.color.text_primary) else Color.rgb(25, 61, 61)
      addView(label(story.title, 29f).apply { setTextColor(ink); chapterLineHeight(1.14f); letterSpacing = -.025f })
      addView(space(6))
      addView(text(12f) { context.getString(R.string.story_by, story.author) }.apply { setTextColor(ink) })
      addView(space(105))
      addView(bookmark(story.id, onCover = true))
    }, FrameLayout.LayoutParams(Fill, Wrap))
  }, LinearLayout.LayoutParams(Fill, Wrap))
  addView(space(18)); addView(progress()); addView(space(18))
  addView(button({ context.getString(R.string.welcome_open_story_cta) }, "continue-reading") { model.open(story.id) }.apply {
    setIconResource(R.drawable.chapter_forward); iconGravity = MaterialButton.ICON_GRAVITY_TEXT_END
    iconTint = ColorStateList.valueOf(context.color(R.color.bg_primary)); iconPadding = context.dp(14)
  }, LinearLayout.LayoutParams(Fill, Wrap))
}

private fun ChapterWidgets.shelf() = column().apply {
  addView(row().apply {
    addView(label(R.string.next_chapter_shelf, 22f, bold = true), LinearLayout.LayoutParams(0, Wrap, 1f))
    addView(button({ context.getString(R.string.see_all) }, "see-all", outlined = true) { model.selectTab(LibraryTab.Discover) }.apply {
      strokeWidth = 0; minWidth = 0; minimumWidth = 0
      setPadding(context.dp(10), context.dp(12), context.dp(10), context.dp(12))
      setTextColor(context.color(R.color.accent_primary))
    })
  })
  stories.filter { it.id != model.reading.value.current }.forEach { addView(space(14)); addView(storyTile(it)) }
  addView(space(14))
  addView(column(20).apply {
    background = panel(R.color.bg_primary, 20, outlined = true)
    addView(label(R.string.reading_moment, 12f, R.color.accent_primary))
    addView(space(10))
    addView(text(21f, serif = true) { context.getString(R.string.reading_quote) }.apply { chapterLineHeight(1.5f) })
  })
}

private fun ChapterWidgets.storyTile(story: StoryResources, expanded: Boolean = false) = row().apply {
  background = panel()
  val stacked = context.resources.configuration.fontScale >= 1.5f
  val content = if (stacked) column() else row()
  content.apply {
    tag = "open-${story.id.name.lowercase()}"
    setPadding(context.dp(12), context.dp(12), context.dp(12), context.dp(12))
    isFocusable = true
    setOnClickListener { model.open(story.id) }
    addView(StoryCoverView(context, story.id), LinearLayout.LayoutParams(
      if (stacked) Fill else context.dp(if (expanded) 88 else 70), context.dp(if (stacked) 110 else if (expanded) 118 else 92)))
    val labels = column().apply {
      if (stacked) addView(space(12))
      addView(label(story.genre, 12f, R.color.accent_strong)); addView(space(5))
      addView(label(story.title, 17f, bold = true)); addView(space(4))
      addView(text(12f, R.color.text_secondary) { story.author })
      if (expanded) { addView(space(8)); addView(label(story.description, 16f, R.color.text_secondary)) }
    }
    addView(labels, if (stacked) LinearLayout.LayoutParams(Fill, Wrap) else LinearLayout.LayoutParams(0, Wrap, 1f).apply { marginStart = context.dp(14) })
  }
  addView(content, LinearLayout.LayoutParams(0, Wrap, 1f))
  addView(bookmark(story.id), LinearLayout.LayoutParams(context.dp(48), context.dp(48)).apply { marginEnd = context.dp(4) })
}

private fun ChapterWidgets.filters() = ChipGroup(context).apply {
  chipSpacingHorizontal = context.dp(8); chipSpacingVertical = context.dp(8)
  (listOf(null to R.string.filter_all) + stories.map { it.id to it.genre }).forEach { (id, title) ->
    addView(Chip(context).apply {
      tag = "filter-${id?.name?.lowercase() ?: "all"}"
      textSize = 13f; isCheckable = true; isChecked = model.reading.value.filter == id
      chipBackgroundColor = ColorStateList.valueOf(context.color(if (isChecked) R.color.bg_secondary else R.color.bg_primary))
      setTextColor(context.color(if (isChecked) R.color.accent_primary else R.color.text_secondary))
      chipStrokeColor = ColorStateList.valueOf(context.color(R.color.border_subtle)); chipStrokeWidth = context.dp(1).toFloat()
      bind { text = context.getString(title) }
      setOnClickListener { model.filter(if (model.reading.value.filter == id) null else id) }
    })
  }
}

private fun ChapterWidgets.emptyShelf() = column(28).apply {
  background = panel(radius = 24); gravity = Gravity.CENTER_HORIZONTAL
  addView(icon(R.drawable.chapter_bookmark, { "" }, "empty-shelf-icon") {}.apply {
    background = null; isClickable = false; isFocusable = false
    imageTintList = ColorStateList.valueOf(context.color(R.color.accent_primary))
    importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
  })
  addView(space(16))
  addView(label(R.string.saved_empty_title, 22f, bold = true).apply { gravity = Gravity.CENTER })
  addView(space(16))
  addView(label(R.string.saved_empty_body, 16f, R.color.text_secondary).apply { gravity = Gravity.CENTER })
  addView(space(16))
  addView(button({ context.getString(R.string.explore_stories) }, "explore-stories") { model.selectTab(LibraryTab.Discover) })
}
