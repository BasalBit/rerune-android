package io.rerune.example.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import io.rerune.example.views.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rerune.ReRune
import rerune.reRune
import rerune.views.reRuneOnStringsUpdated

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private var refreshMessageResId = R.string.welcome_refresh_state_idle
  private var refreshJobRunning = false

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(newBase.reRune())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = android.graphics.Color.TRANSPARENT
    window.navigationBarColor = ContextCompat.getColor(this, R.color.bg_primary)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    applyInsets()
    setupRefresh()
    setupActions()
    bindRuntimeValues()
    observeStringUpdates()
  }

  private fun applyInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(binding.screenContainer) { _, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      binding.contentColumn.updatePadding(top = systemBars.top + dp(20), bottom = systemBars.bottom + dp(24))
      insets
    }
    ViewCompat.requestApplyInsets(binding.screenContainer)
  }

  private fun setupRefresh() {
    binding.swipeRefresh.setProgressBackgroundColorSchemeColor(
      ContextCompat.getColor(this, R.color.bg_secondary),
    )
    binding.swipeRefresh.setColorSchemeColors(ContextCompat.getColor(this, R.color.accent_primary))
    binding.swipeRefresh.setOnRefreshListener { refreshTexts() }
  }

  private fun setupActions() {
    binding.openStoryButton.setOnClickListener {
      startActivity(Intent(this, StoryActivity::class.java))
    }
  }

  private fun bindRuntimeValues() {
    binding.lastSyncedValue.text = currentTimestamp()
  }

  private fun observeStringUpdates() {
    reRuneOnStringsUpdated(lifecycleScope) {
      renderText()
    }
  }

  private fun renderText() {
    binding.badgeText.text = getString(R.string.welcome_badge)
    binding.titleText.text = getString(R.string.welcome_title)
    binding.subtitleText.text = getString(R.string.welcome_subtitle)

    binding.localeLabel.text = getString(R.string.welcome_locale_label)
    binding.localeValue.text = resolvedLocaleCode()
    binding.lastSyncedLabel.text = getString(R.string.welcome_last_synced_label)
    binding.lastSyncedValue.text = currentTimestamp()

    binding.refreshStateLabel.text = getString(refreshMessageResId)
    binding.refreshStateLabel.setTextColor(
      ContextCompat.getColor(
        this,
        if (refreshMessageResId == R.string.welcome_refresh_state_success) R.color.success else R.color.text_secondary,
      ),
    )
    binding.openStoryButton.text = getString(R.string.welcome_open_story_cta)
  }

  private fun refreshTexts() {
    if (refreshJobRunning) return

    lifecycleScope.launch {
      refreshJobRunning = true
      binding.swipeRefresh.isRefreshing = true

      try {
        showRefreshState(R.string.welcome_refresh_state_checking)
        delay(400)

        showRefreshState(R.string.welcome_refresh_state_downloading)
        try {
          ReRune.checkForUpdates()
        } catch (_: Throwable) {
          // Keep the demo flow stable even if the network call fails.
        }

        showRefreshState(R.string.welcome_refresh_state_applying)
        delay(500)

        showRefreshState(R.string.welcome_refresh_state_success)
        delay(1400)
      } finally {
        binding.swipeRefresh.isRefreshing = false
        refreshJobRunning = false
        showRefreshState(R.string.welcome_refresh_state_idle)
      }
    }
  }

  private fun showRefreshState(resId: Int) {
    refreshMessageResId = resId
    renderText()
  }

  private fun dp(value: Int): Int {
    return (value * resources.displayMetrics.density).toInt()
  }

  private fun currentTimestamp(): String {
    val formatter = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
    return formatter.format(Date())
  }

  private fun resolvedLocaleCode(): String {
    val localeOverride = ReRune.localeOverrideFlow.value
    if (!localeOverride.isNullOrBlank()) {
      return localeOverride
    }

    val locale = ConfigurationCompat.getLocales(resources.configuration)[0] ?: Locale.getDefault()
    return locale.toLanguageTag()
      .takeUnless { it.isBlank() || it == "und" }
      ?: locale.language.takeIf { it.isNotBlank() }
      ?: "und"
  }
}
