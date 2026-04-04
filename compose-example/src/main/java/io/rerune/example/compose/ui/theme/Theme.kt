package io.rerune.example.compose.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ReRuneDarkColorScheme = darkColorScheme(
  primary = AccentPrimary,
  onPrimary = BgPrimary,
  secondary = TextSecondary,
  tertiary = AccentStrong,
  background = BgPrimary,
  onBackground = TextPrimary,
  surface = BgSecondary,
  onSurface = TextPrimary,
  outline = BorderSubtle,
)

@Composable
fun ReRuneDemoTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = ReRuneDarkColorScheme,
    content = content,
  )
}

@Composable
fun DemoBackground(
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit,
) {
  Box(
    modifier = modifier.background(
      brush = Brush.verticalGradient(
        colors = listOf(BgSecondary, BgPrimary, BgTertiary),
      ),
    ),
  ) {
    Box(
      modifier = Modifier
        .align(Alignment.TopStart)
        .offset(x = (-72).dp, y = (-56).dp)
        .size(280.dp)
        .background(
          brush = Brush.radialGradient(
            colors = listOf(Color(0xFF27406F).copy(alpha = 0.28f), Color.Transparent),
          ),
          shape = CircleShape,
        ),
    )

    Box(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .offset(x = 88.dp, y = (-24).dp)
        .size(300.dp)
        .background(
          brush = Brush.radialGradient(
            colors = listOf(AccentPrimary.copy(alpha = 0.20f), Color.Transparent),
          ),
          shape = CircleShape,
        ),
    )

    content()
  }
}
