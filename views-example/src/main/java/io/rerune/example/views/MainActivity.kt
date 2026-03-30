package io.rerune.example.views

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.rerune.example.views.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import rerune.ReRune
import rerune.reRune
import rerune.views.reRuneOnStringsUpdated

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(newBase.reRune())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    bindDynamicStrings()
    setupPullToRefresh()
    setupActions()
    observeStringUpdates()
  }

  private fun setupPullToRefresh() {
    binding.swipeRefresh.setOnRefreshListener {
      lifecycleScope.launch {
        val result = ReRune.checkForUpdates()
        binding.swipeRefresh.isRefreshing = false
        Toast.makeText(
          this@MainActivity,
          "Pull refresh: ${result.status.name}",
          Toast.LENGTH_SHORT,
        ).show()
        recreate()
      }
    }
  }

  private fun setupActions() {
    binding.checkUpdatesButton.setOnClickListener {
      lifecycleScope.launch {
        val result = ReRune.checkForUpdates()
        Toast.makeText(
          this@MainActivity,
          result.status.name,
          Toast.LENGTH_SHORT,
        ).show()
        recreate()
      }
    }

    binding.applyProgrammaticTextsButton.setOnClickListener {
      binding.titleText.text = getString(R.string.title)
      binding.subtitleText.text = getString(R.string.body)
      Toast.makeText(this, "Applied title/body programmatically", Toast.LENGTH_SHORT).show()
    }
  }

  private fun observeStringUpdates() {
    reRuneOnStringsUpdated(lifecycleScope) {
      Toast.makeText(
        this,
        "Strings updated. Refresh UI if needed.",
        Toast.LENGTH_SHORT,
      ).show()
    }
  }

  private fun bindDynamicStrings() {
    binding.placeholderText.text = getString(R.string.sample_placeholder, 1, "RubinTXT")
    binding.redrawTimeText.text = getString(R.string.last_redraw, currentRedrawTimestamp())
  }

  private fun currentRedrawTimestamp(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(Date())
  }
}
