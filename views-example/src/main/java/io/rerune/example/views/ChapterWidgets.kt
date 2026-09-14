package io.rerune.example.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.ConfigurationCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.text.NumberFormat
import kotlin.math.roundToInt

internal const val Fill = ViewGroup.LayoutParams.MATCH_PARENT
internal const val Wrap = ViewGroup.LayoutParams.WRAP_CONTENT
internal fun Context.dp(value: Int) = (value * resources.displayMetrics.density).toInt()
internal fun Context.color(id: Int) = ContextCompat.getColor(this, id)
internal fun TextView.chapterLineHeight(multiplier: Float) {
  // Reset spacing even when the requested height equals the font's natural height.
  setLineSpacing(((textSize * multiplier).roundToInt() - paint.getFontMetricsInt(null)).toFloat(), 1f)
}

/** Small native View builders. Binders update existing Views on a text revision. */
internal class ChapterWidgets(val context: Context, val model: StorybookModel) {
  private val bindings = mutableListOf<() -> Unit>()
  fun bind(action: () -> Unit) { bindings += action; action() }
  fun rebind() = bindings.forEach { it() }

  fun column(padding: Int = 0) = LinearLayout(context).apply {
    orientation = LinearLayout.VERTICAL
    setPadding(context.dp(padding), context.dp(padding), context.dp(padding), context.dp(padding))
  }
  fun row() = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
  fun space(height: Int) = View(context).apply { layoutParams = LinearLayout.LayoutParams(1, context.dp(height)) }
  fun panel(fill: Int = R.color.bg_secondary, radius: Int = 22, outlined: Boolean = false) = GradientDrawable().apply {
    setColor(context.color(fill)); cornerRadius = context.dp(radius).toFloat()
    if (outlined) setStroke(context.dp(1), context.color(R.color.border_subtle))
  }
  fun text(size: Float = 16f, tint: Int = R.color.text_primary, bold: Boolean = false,
    serif: Boolean = false, tag: String? = null, value: () -> CharSequence): TextView = TextView(context).apply {
    this.tag = tag
    textSize = size
    setTextColor(context.color(tint))
    typeface = ResourcesCompat.getFont(context, if (serif) R.font.lora_regular else if (bold) R.font.instrument_sans_bold else R.font.instrument_sans_regular)
    includeFontPadding = false
    chapterLineHeight(if (serif) 1.85f else 1.4f)
    bind { text = value() }
  }
  fun label(id: Int, size: Float = 16f, tint: Int = R.color.text_primary, bold: Boolean = false) =
    text(size, tint, bold) { context.getString(id) }

  fun icon(icon: Int, description: () -> String, tag: String, onClick: () -> Unit) = ImageButton(context).apply {
    this.tag = tag
    minimumWidth = context.dp(48); minimumHeight = context.dp(48)
    layoutParams = LinearLayout.LayoutParams(context.dp(48), context.dp(48))
    setPadding(context.dp(12), context.dp(12), context.dp(12), context.dp(12))
    setImageResource(icon)
    imageTintList = ColorStateList.valueOf(context.color(R.color.text_secondary))
    background = panel(R.color.bg_primary, 24)
    bind { contentDescription = description() }
    setOnClickListener { onClick() }
  }

  fun button(value: () -> String, tag: String, outlined: Boolean = false, action: () -> Unit) = MaterialButton(context).apply {
    this.tag = tag
    textSize = 15f
    typeface = ResourcesCompat.getFont(context, R.font.instrument_sans_bold)
    isAllCaps = false
    minHeight = context.dp(if (outlined) 48 else 52)
    minimumHeight = minHeight
    insetTop = 0; insetBottom = 0
    cornerRadius = context.dp(16)
    setPadding(context.dp(20), context.dp(14), context.dp(20), context.dp(14))
    backgroundTintList = ColorStateList.valueOf(if (outlined) Color.TRANSPARENT else context.color(R.color.accent_primary))
    setTextColor(context.color(if (outlined) R.color.text_secondary else R.color.bg_primary))
    if (outlined) { strokeWidth = context.dp(1); strokeColor = ColorStateList.valueOf(context.color(R.color.border_subtle)) }
    bind { text = value() }
    setOnClickListener { action() }
  }

  fun bookmark(id: StoryId, onCover: Boolean = false) = icon(R.drawable.chapter_bookmark,
    { context.getString(if (id in model.reading.value.saved) R.string.remove_saved else R.string.save_story) },
    "bookmark-${id.name.lowercase()}", { model.bookmark(id) }).apply {
    if (onCover) background = panel(R.color.bg_primary, 24).apply { alpha = 225 }
    else background = null
    bind {
      val saved = id in model.reading.value.saved
      setImageResource(if (saved) R.drawable.chapter_bookmark_filled else R.drawable.chapter_bookmark)
      imageTintList = ColorStateList.valueOf(context.color(if (saved) R.color.accent_primary else R.color.text_primary))
    }
  }

  fun progress() = column().apply {
    addView(row().apply {
      addView(text(16f, bold = true) {
        val reading = model.reading.value
        if (reading.progress() == 1f) context.getString(R.string.story_finished)
        else context.getString(R.string.chapter_progress, reading.chapterIndex() + 1, reading.story().chapters.size)
      }, LinearLayout.LayoutParams(0, Wrap, 1f))
      addView(text(14f, R.color.text_secondary) {
        NumberFormat.getPercentInstance(ConfigurationCompat.getLocales(context.resources.configuration)[0]).format(model.reading.value.progress())
      })
    })
    addView(space(10))
    addView(LinearProgressIndicator(context).apply {
      isIndeterminate = false; max = 100; trackThickness = context.dp(4); trackCornerRadius = context.dp(2); trackStopIndicatorSize = 0
      setIndicatorColor(context.color(R.color.accent_primary)); trackColor = context.color(R.color.border_subtle)
      bind { progress = (model.reading.value.progress() * 100).toInt() }
    }, LinearLayout.LayoutParams(Fill, context.dp(4)))
  }

  fun refresh() = button({ context.getString(if (model.sync.value.refreshing) R.string.welcome_refresh_state_checking else R.string.story_refresh_cta) },
    "refresh-texts", outlined = true, model::refresh).apply {
    setIconResource(R.drawable.chapter_refresh); iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
    iconSize = context.dp(18); iconTint = ColorStateList.valueOf(context.color(R.color.text_secondary))
    bind { isEnabled = !model.sync.value.refreshing }
  }

  fun feedback() = text(14f, R.color.text_secondary, tag = "sync-feedback") {
    model.sync.value.feedback?.let(context::getString).orEmpty()
  }.apply {
    gravity = Gravity.CENTER
    accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
    bind { visibility = if (model.sync.value.feedback == null) View.GONE else View.VISIBLE }
  }

  fun centered(child: View, maxWidth: Int, verticalPadding: Int = 0) = FrameLayout(context).apply {
    setPadding(0, context.dp(verticalPadding), 0, context.dp(verticalPadding))
    val width = minOf(context.resources.configuration.screenWidthDp, maxWidth)
    addView(child, FrameLayout.LayoutParams(context.dp(width), Wrap, Gravity.TOP or Gravity.CENTER_HORIZONTAL))
  }
}
