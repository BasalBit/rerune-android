package io.rerune.example.compose

import android.app.Application
import rerune.ReRune
import rerune.ReRuneLogLevel
import rerune.ReRuneUpdatePolicy

class DemoApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    ReRune.setup(
      app = this,
      otaPublishId = BuildConfig.RERUNE_OTA_PUBLISH_ID,
      logLevel = ReRuneLogLevel.Info,
      updatePolicy = ReRuneUpdatePolicy(periodicIntervalInDays = 1),
    )
  }
}
