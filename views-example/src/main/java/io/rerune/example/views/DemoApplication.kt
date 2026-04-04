package io.rerune.example.views

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import rerune.ReRune
import rerune.ReRuneLogLevel
import rerune.ReRuneUpdatePolicy
import rerune.views.ReRuneViews

class DemoApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    ReRune.setup(
      app = this,
      otaPublishId = BuildConfig.RERUNE_OTA_PUBLISH_ID,
      logLevel = ReRuneLogLevel.Info,
      updatePolicy = ReRuneUpdatePolicy(periodicIntervalInDays = 1),
    )
    ReRuneViews.install(this)
  }
}
