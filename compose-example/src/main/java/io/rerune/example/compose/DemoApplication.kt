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
      otaPublishId = "03141fc5dde6e5a1f9debf99ee68bbb125dc830412fdfb85af4834d3de341b3b",
      logLevel = ReRuneLogLevel.Verbose,
      updatePolicy = ReRuneUpdatePolicy(periodicIntervalInDays = 1),
    )
  }
}
