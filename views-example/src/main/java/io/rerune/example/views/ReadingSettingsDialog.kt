package io.rerune.example.views

import android.content.Context
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.materialswitch.MaterialSwitch
import rerune.ReRuneVariant

/** Rebind text and switch state in place, retaining the modal window and its scroll. */
internal class ReadingSettingsDialog(context: Context, model: StorybookModel) : BottomSheetDialog(context, R.style.ChapterBottomSheet) {
  private val widgets = ChapterWidgets(context, model)
  private var rebinding = false

  init {
    val content = with(widgets) {
      column(24).apply {
        tag = "reading-settings-content"
        addView(View(context).apply { background = panel(R.color.text_secondary, 3).apply { alpha = 110 } },
          LinearLayout.LayoutParams(context.dp(32), context.dp(4)).apply { gravity = Gravity.CENTER_HORIZONTAL; bottomMargin = context.dp(24) })
        addView(label(R.string.reading_settings, 26f, bold = true))
        addView(space(12)); addView(label(R.string.settings_description, 16f, R.color.text_secondary))
        addView(space(20))
        addView(row().apply {
          addView(column().apply {
            addView(label(R.string.translation_variant, 17f, bold = true))
            addView(text { if (!model.sync.value.variantReady) "" else if (model.sync.value.variant == ReRuneVariant.Main) "Main" else model.sync.value.variant.slug.orEmpty() })
          }, LinearLayout.LayoutParams(0, Wrap, 1f))
          addView(MaterialSwitch(context).apply {
            tag = "translation-variant-toggle"
            minimumHeight = context.dp(48)
            trackTintList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
              intArrayOf(context.color(R.color.accent_primary), context.color(R.color.border_subtle)))
            thumbTintList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
              intArrayOf(context.color(R.color.bg_primary), context.color(R.color.text_secondary)))
            bind {
              rebinding = true
              isChecked = model.sync.value.variant == ReRuneVariant.fromSlug("vip")
              isEnabled = !model.sync.value.variantBusy
              contentDescription = context.getString(R.string.translation_variant)
              rebinding = false
            }
            setOnCheckedChangeListener { _, checked -> if (!rebinding) model.setVip(checked) }
          })
        })
        addView(space(20))
        addView(row().apply {
          addView(column().apply {
            addView(label(R.string.staging_mode, 17f, bold = true))
            addView(text(14f, R.color.text_secondary) { context.getString(R.string.staging_mode_description) })
          }, LinearLayout.LayoutParams(0, Wrap, 1f))
          addView(MaterialSwitch(context).apply {
            tag = "staging-mode-toggle"
            minimumHeight = context.dp(48)
            trackTintList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
              intArrayOf(context.color(R.color.accent_primary), context.color(R.color.border_subtle)))
            thumbTintList = ColorStateList(arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
              intArrayOf(context.color(R.color.bg_primary), context.color(R.color.text_secondary)))
            bind {
              rebinding = true
              isChecked = model.sync.value.staging
              isEnabled = !model.sync.value.stagingBusy
              contentDescription = context.getString(R.string.staging_mode)
              rebinding = false
            }
            setOnCheckedChangeListener { _, checked -> if (!rebinding) model.setStaging(checked) }
          })
        })
        addView(space(20))
        addView(View(context).apply { setBackgroundColor(context.color(R.color.border_subtle)) }, LinearLayout.LayoutParams(Fill, context.dp(1)))
        addView(space(12))
        addView(text(16f, R.color.text_secondary) { context.getString(R.string.publish_date, "14.07.2026") })
        addView(space(12))
        for (count in 1..2) addView(text(16f, R.color.text_secondary) { context.resources.getQuantityString(R.plurals.plural_sample, count, count) })
        addView(space(12)); addView(refresh(), LinearLayout.LayoutParams(Fill, Wrap))
        addView(feedback(), LinearLayout.LayoutParams(Fill, Wrap))
        addView(space(12)); addView(label(R.string.session_note, 12f, R.color.text_secondary))
        addView(space(8))
      }
    }
    setContentView(ScrollView(context).apply { isFillViewport = false; addView(content) })
    behavior.isFitToContents = true
    behavior.skipCollapsed = true
    behavior.maxWidth = context.dp(640)
    setOnShowListener {
      findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.background = widgets.panel(radius = 28)
      behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
  }

  fun rebind() = widgets.rebind()
}
