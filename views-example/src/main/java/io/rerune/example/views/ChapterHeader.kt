package io.rerune.example.views

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import com.google.android.material.button.MaterialButton
import java.util.Locale
import rerune.ReRune

internal fun ChapterWidgets.languageButton() = button({
  val locale = ConfigurationCompat.getLocales(context.resources.configuration)[0] ?: Locale.ENGLISH
  locale.language.uppercase(Locale.ROOT)
}, "language-menu", outlined = true) {}.apply {
  strokeWidth = 0
  minWidth = 0; minimumWidth = 0
  textSize = 13f
  setPadding(context.dp(4), 0, context.dp(4), 0)
  setIconResource(R.drawable.chapter_language); iconSize = context.dp(19)
  iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
  iconTint = ColorStateList.valueOf(context.color(R.color.text_secondary))
  bind { contentDescription = context.getString(R.string.welcome_locale_label) }
  setOnClickListener {
    val names = linkedMapOf("de" to "Deutsch", "en" to "English", "es" to "Español", "it" to "Italiano", "pt" to "Português")
    ReRune.availableLocalesFlow.value.forEach { names[it.localeTag] = it.name?.takeIf(String::isNotBlank) ?: names[it.localeTag] ?: it.localeTag }
    val choices = listOf(null to context.getString(R.string.welcome_locale_system_default)) + names.toList()
    PopupMenu(context, this).apply {
      choices.forEachIndexed { index, (locale, label) ->
        menu.add(0, index, index, label).apply {
          isCheckable = true
          isChecked = AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag() == locale
        }
      }
      setOnMenuItemClickListener { item ->
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(choices[item.itemId].first.orEmpty()))
        true
      }
    }.show()
  }
}

internal fun ChapterWidgets.libraryHeader(onSettings: () -> Unit) = row().apply {
  addView(ImageView(context).apply {
    setImageResource(R.drawable.logo)
    importantForAccessibility = android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
    background = GradientDrawable().apply { setColor(Color.BLACK); cornerRadius = context.dp(8).toFloat() }
  }, LinearLayout.LayoutParams(context.dp(40), context.dp(40)).apply { marginEnd = context.dp(8) })
  addView(text(26f, bold = true) { context.getString(R.string.app_name) }.apply {
    setTextSize(android.util.TypedValue.COMPLEX_UNIT_DIP, 26f); letterSpacing = -.04f
  }, LinearLayout.LayoutParams(0, Wrap, 1f))
  addView(languageButton(), LinearLayout.LayoutParams(context.dp(74), context.dp(48)))
  addView(icon(R.drawable.chapter_settings, { context.getString(R.string.reading_settings) }, "reading-settings", onSettings).apply {
    background = panel(R.color.bg_secondary, 24)
  })
}

internal fun ChapterWidgets.readerHeader() = row().apply {
  setPadding(context.dp(16), context.dp(6), context.dp(16), context.dp(6))
  addView(icon(R.drawable.chapter_back, { context.getString(R.string.back_library) }, "back-library", model::back))
  addView(text(17f, bold = true) { context.getString(model.reading.value.story().title) }.apply {
    gravity = Gravity.CENTER
    setPadding(context.dp(8), 0, context.dp(8), 0)
  }, LinearLayout.LayoutParams(0, Wrap, 1f))
  addView(languageButton(), LinearLayout.LayoutParams(context.dp(74), context.dp(48)))
}
