package io.rerune.example.compose.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.rerune.example.compose.ui.theme.AccentPrimary

@Composable
fun BadgeChip(text: String) {
  Surface(
    color = AccentPrimary.copy(alpha = 0.14f),
    contentColor = AccentPrimary,
    shape = RoundedCornerShape(999.dp),
    border = BorderStroke(1.dp, AccentPrimary.copy(alpha = 0.24f)),
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.3.sp),
      modifier = Modifier.padding(
        horizontal = 14.dp,
        vertical = 8.dp,
      ),
    )
  }
}
