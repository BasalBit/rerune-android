package io.rerune.example.compose

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModelProvider
import io.rerune.example.compose.ui.ChapterApp
import io.rerune.example.compose.ui.theme.ChapterTheme
import rerune.reRune

class MainActivity : AppCompatActivity() {
  override fun attachBaseContext(newBase: Context) {
    super.attachBaseContext(newBase.reRune())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val model = ViewModelProvider(this)[StorybookModel::class.java]
    setContent {
      ChapterTheme {
        ChapterApp(model) { locale ->
          AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale.orEmpty()))
        }
      }
    }
  }
}
