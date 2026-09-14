package io.rerune.example.views

import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout

internal fun ChapterWidgets.readerContent() = column().apply {
  setPadding(context.dp(24), 0, context.dp(24), context.dp(32))
  val state = model.reading.value
  val story = state.story()
  val finished = state.progress() == 1f
  addView(StoryCoverView(context, story.id).apply {
    addView(bookmark(story.id, onCover = true), FrameLayout.LayoutParams(context.dp(48), context.dp(48), Gravity.BOTTOM or Gravity.END).apply {
      bottomMargin = context.dp(14); marginEnd = context.dp(14)
    })
  }, LinearLayout.LayoutParams(Fill, context.dp(200)))
  addView(space(24)); addView(progress()); addView(space(24))
  if (finished) {
    addView(label(R.string.reader_end_title, 26f, bold = true).apply { gravity = Gravity.CENTER; tag = "story-completed" })
    addView(space(20))
    addView(label(R.string.reader_end_body, 16f, R.color.text_secondary).apply { gravity = Gravity.CENTER })
  } else {
    val chapter = story.chapters[state.chapterIndex()]
    addView(text(30f, serif = true, tag = "chapter-title") { context.getString(chapter.title) }.apply { chapterLineHeight(1.25f) })
    addView(space(12))
    addView(text(12f, R.color.text_secondary) { context.getString(R.string.story_by, story.author) })
    chapter.paragraphs.forEach { paragraph ->
      addView(space(24))
      addView(text(19f, R.color.reader_text, serif = true) { context.getString(paragraph) }.apply { setTextIsSelectable(true) })
    }
  }
  addView(space(24))
  addView(button({ context.getString(when {
    finished -> R.string.read_again
    state.chapterIndex() == story.chapters.lastIndex -> R.string.finish_story
    else -> R.string.next_chapter
  }) }, "advance-chapter", action = model::advance), LinearLayout.LayoutParams(Fill, Wrap))
  addView(space(24)); addView(refresh(), LinearLayout.LayoutParams(Fill, Wrap))
  addView(space(24)); addView(feedback(), LinearLayout.LayoutParams(Fill, Wrap))
}
