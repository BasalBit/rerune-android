package io.rerune.example.compose.ui.components

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.ConfigurationCompat
import io.rerune.example.compose.R
import io.rerune.example.compose.SyncState
import io.rerune.example.compose.ui.theme.*
import java.util.Locale
import rerune.ReRune
import rerune.ReRuneVariant

@Composable
internal fun LibraryHeader(onLocale: (String?) -> Unit, onSettings: () -> Unit) {
  Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    Image(painterResource(R.drawable.logo), contentDescription = null,
      modifier = Modifier.size(40.dp).background(Color.Black, RoundedCornerShape(8.dp)))
    // The wordmark is branding, while every story and control continues to respect text scaling.
    Text(stringResource(R.string.app_name), Modifier.weight(1f), fontSize = (26 / LocalDensity.current.fontScale).sp,
      fontWeight = FontWeight.Bold, letterSpacing = (-1).sp, maxLines = 1)
    LanguageMenu(onLocale)
    IconButton(onSettings, Modifier.size(48.dp).background(BgSecondary, CircleShape).testTag("reading-settings")) {
      ChapterIcon(R.drawable.chapter_settings, stringResource(R.string.reading_settings))
    }
  }
}

@Composable
internal fun LanguageMenu(onLocale: (String?) -> Unit) {
  var open by remember { mutableStateOf(false) }
  val available by ReRune.availableLocalesFlow.collectAsState()
  val current = ConfigurationCompat.getLocales(LocalConfiguration.current)[0] ?: Locale.ENGLISH
  val selected = AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag()
  val names = linkedMapOf("de" to "Deutsch", "en" to "English", "es" to "Español", "it" to "Italiano", "pt" to "Português")
  available.forEach { names[it.localeTag] = it.name?.takeIf(String::isNotBlank) ?: names[it.localeTag] ?: it.localeTag }
  val label = stringResource(R.string.welcome_locale_label)
  Box {
    TextButton({ open = true }, Modifier.heightIn(min = 48.dp).testTag("language-menu").semantics { contentDescription = label },
      contentPadding = PaddingValues(horizontal = 4.dp)) {
      ChapterIcon(R.drawable.chapter_language, modifier = Modifier.size(19.dp))
      Spacer(Modifier.width(5.dp))
      Text(current.language.uppercase(Locale.ROOT), color = TextSecondary, fontSize = 13.sp)
      ChapterIcon(R.drawable.chapter_down, modifier = Modifier.size(16.dp))
    }
    DropdownMenu(open, { open = false }, Modifier.widthIn(min = 220.dp, max = 300.dp)) {
      DropdownMenuItem(text = { Text(stringResource(R.string.welcome_locale_system_default)) },
        onClick = { open = false; onLocale(null) },
        leadingIcon = { if (selected == null) ChapterIcon(R.drawable.chapter_check) })
      names.forEach { (tag, name) ->
        DropdownMenuItem(text = { Text(name) }, onClick = { open = false; onLocale(tag) },
          leadingIcon = { if (selected == tag) ChapterIcon(R.drawable.chapter_check) })
      }
    }
  }
}

@Composable
internal fun ReadingSettings(sync: SyncState, scroll: ScrollState, onVip: (Boolean) -> Unit, onRefresh: () -> Unit) {
  Column(Modifier.fillMaxWidth().testTag("reading-settings-content").verticalScroll(scroll).padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(stringResource(R.string.reading_settings), style = MaterialTheme.typography.headlineMedium)
    Text(stringResource(R.string.settings_description), color = TextSecondary)
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
      Column(Modifier.weight(1f)) {
        Text(stringResource(R.string.translation_variant), style = MaterialTheme.typography.titleMedium)
        if (sync.variantReady) Text(if (sync.variant == ReRuneVariant.Main) "Main" else sync.variant.slug.orEmpty())
      }
      val description = stringResource(R.string.translation_variant)
      Switch(checked = sync.variant == ReRuneVariant.fromSlug("vip"), onCheckedChange = onVip,
        enabled = !sync.variantBusy, modifier = Modifier.testTag("translation-variant-toggle").semantics { contentDescription = description })
    }
    HorizontalDivider(color = BorderSubtle)
    Text(stringResource(R.string.publish_date, "14.07.2026"), color = TextSecondary)
    Column {
      Text(pluralStringResource(R.plurals.plural_sample, 1, 1), color = TextSecondary)
      Text(pluralStringResource(R.plurals.plural_sample, 2, 2), color = TextSecondary)
    }
    RefreshButton(sync, onRefresh, Modifier.fillMaxWidth())
    SyncFeedback(sync)
    Text(stringResource(R.string.session_note), color = TextSecondary, style = MaterialTheme.typography.labelMedium)
  }
}
