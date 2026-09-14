package io.rerune.example.compose

import android.app.Application
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import java.io.File
import java.util.Locale
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import rerune.*

/** Opt-in, read-only delivery check against the consuming app's Maven SDK and OTA ID. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ChapterDeliveryTest {
  @Test fun configuredTargetDeliversAndRefreshReportsItsActualOutcome() = runBlocking {
    assumeTrue("Enable with -Prerune.liveDelivery=true", System.getProperty("rerune.liveDelivery") == "true")
    val app = ApplicationProvider.getApplicationContext<Application>()
    ReRune.setup(app, BuildConfig.RERUNE_OTA_PUBLISH_ID,
      updatePolicy = ReRuneUpdatePolicy(checkOnStart = false, periodicIntervalInDays = 0))
    val first = ReRune.checkForUpdates()
    val second = ReRune.checkForUpdates()
    val locales = ReRune.availableLocalesFlow.value
    val output = File(requireNotNull(System.getProperty("example.reports")), "rerune-delivery.txt")
    requireNotNull(output.parentFile).mkdirs()
    output.writeText("OTA ID: ${BuildConfig.RERUNE_OTA_PUBLISH_ID}\nFirst refresh: $first\nSecond refresh: $second\n" +
      "Locales: $locales\nNative title: ${app.reRune().getString(R.string.story_title)}\n")
    println(output.readText())
    assertNull("Live delivery failed: ${first.errorMessage}", first.errorMessage)
    assertEquals(ReRuneUpdateResult.ReRuneUpdateStatus.Updated, first.status)
    assertTrue("The SDK must cache every published locale", locales.isNotEmpty() && locales.all { it.isCached })
    assertNull("Repeat refresh failed: ${second.errorMessage}", second.errorMessage)
    assertEquals(ReRuneUpdateResult.ReRuneUpdateStatus.NoChange, second.status)

    val english = app.createConfigurationContext(Configuration(app.resources.configuration).apply {
      setLocale(Locale.ENGLISH)
    }).reRune()
    fun textSample() = listOf(
      english.getString(R.string.welcome_title),
      english.getString(R.string.reading_settings),
      english.getString(R.string.publish_date, "14.09.2026"),
      english.getString(R.string.story_by, "Nora Vale"),
      english.getString(R.string.chapter_progress, 1, 2),
      english.resources.getQuantityString(R.plurals.plural_sample, 2, 2),
    )
    ReRune.setVariant(ReRuneVariant.Main, persist = true)
    val main = textSample()
    val vip = ReRuneVariant.fromSlug("vip")
    try {
      ReRune.setVariant(vip, persist = true)
      assertEquals(vip, ReRune.getVariant())
      val variant = textSample()
      output.appendText("Main text: $main\nVIP text: $variant\n")
      assertNotEquals("The library heading must show live VIP text", main[0], variant[0])
      for (sample in listOf(main, variant)) {
        assertTrue(sample[2].contains("14.09.2026"))
        assertTrue(sample[3].contains("Nora Vale"))
        assertTrue(sample[4].contains("1") && sample[4].contains("2"))
      }
    } finally {
      ReRune.setVariant(ReRuneVariant.Main, persist = true)
    }
    assertEquals(main, textSample())
    output.appendText("Returned to Main: ${textSample()}\n")
    val french = app.createConfigurationContext(Configuration(app.resources.configuration).apply {
      setLocale(Locale.FRENCH)
    }).reRune().getString(R.string.welcome_title)
    assertNotEquals("French must resolve from OTA because it is not bundled", main[0], french)
    output.appendText("French heading from OTA: $french\n")
  }
}
