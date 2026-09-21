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
      otaPublishId = "03141fc5dde6e5a1f9debf99ee68bbb125dc830412fdfb85af4834d3de341b3b",
      logLevel = ReRuneLogLevel.Verbose,
      updatePolicy = ReRuneUpdatePolicy(periodicIntervalInDays = 1),
    )
    ReRuneViews.install(this)
  }
}
