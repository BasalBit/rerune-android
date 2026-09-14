package io.rerune.example.compose.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.rerune.example.compose.R

internal val InterfaceFont = FontFamily(
  Font(R.font.instrument_sans_regular), Font(R.font.instrument_sans_bold, FontWeight.Bold),
)
internal val ReaderFont = FontFamily(Font(R.font.lora_regular))
private fun type(size: Int, height: Int, bold: Boolean = false) = TextStyle(
  fontFamily = InterfaceFont, fontSize = size.sp, lineHeight = height.sp,
  fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
)

@Composable
internal fun ChapterTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = darkColorScheme(
      primary = AccentPrimary, onPrimary = BgPrimary, secondary = TextSecondary,
      background = BgPrimary, onBackground = TextPrimary,
      surface = BgSecondary, onSurface = TextPrimary, onSurfaceVariant = TextSecondary,
      surfaceTint = BgSecondary,
      outline = BorderSubtle, secondaryContainer = AccentPrimary.copy(alpha = .12f),
      onSecondaryContainer = AccentPrimary,
    ),
    typography = Typography(
      displaySmall = type(34, 38, true).copy(letterSpacing = (-1.2).sp),
      headlineMedium = type(26, 32, true).copy(letterSpacing = (-.6).sp),
      titleLarge = type(22, 28, true), titleMedium = type(17, 23, true),
      bodyLarge = type(16, 24), bodyMedium = type(14, 21), bodySmall = type(12, 18),
      labelLarge = type(16, 20, true), labelMedium = type(13, 18), labelSmall = type(12, 16, true),
    ),
    content = content,
  )
}
