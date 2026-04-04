package io.rerune.example.views

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import io.rerune.example.views.databinding.ActivityStoryBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rerune.ReRune
import rerune.reRune
import rerune.views.reRuneOnStringsUpdated

class StoryActivity : AppCompatActivity() {
  private lateinit var binding: ActivityStoryBinding
  private var refreshRunning = false

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(newBase.reRune())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = android.graphics.Color.TRANSPARENT
    window.navigationBarColor = ContextCompat.getColor(this, R.color.bg_primary)

    binding = ActivityStoryBinding.inflate(layoutInflater)
    setContentView(binding.root)

    applyInsets()
    setupActions()
    observeStringUpdates()
  }

  private fun applyInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(binding.screenContainer) { _, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      binding.contentScroll.updatePadding(top = systemBars.top + dp(20), bottom = systemBars.bottom + dp(24))
      insets
    }
    ViewCompat.requestApplyInsets(binding.screenContainer)
  }

  private fun setupActions() {
    binding.backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    binding.refreshButton.setOnClickListener { refreshTexts() }
  }

  private fun observeStringUpdates() {
    reRuneOnStringsUpdated(lifecycleScope) {
      renderText()
    }
  }

  private fun renderText() {
    binding.captionText.text = getString(R.string.story_caption)
    binding.titleText.text = getString(R.string.story_title)
    binding.primaryBodyText.text = getString(R.string.story_body_primary)
    binding.secondaryBodyText.text = getString(R.string.story_body_secondary)
    binding.refreshButton.text = getString(R.string.story_refresh_cta)
  }

  private fun refreshTexts() {
    if (refreshRunning) return

    lifecycleScope.launch {
      refreshRunning = true
      binding.refreshProgress.visibility = View.VISIBLE
      binding.refreshButton.isEnabled = false

      try {
        delay(250)
        try {
          ReRune.checkForUpdates()
        } catch (_: Throwable) {
          // Keep the demo flow stable even if the network call fails.
        }
        delay(600)
      } finally {
        binding.refreshProgress.visibility = View.GONE
        binding.refreshButton.isEnabled = true
        refreshRunning = false
      }
    }
  }

  private fun dp(value: Int): Int {
    return (value * resources.displayMetrics.density).toInt()
  }
}
