package io.rerune.example.compose.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.rerune.example.compose.ui.theme.BgSecondary
import io.rerune.example.compose.ui.theme.BorderSubtle
import io.rerune.example.compose.ui.theme.TextSecondary

data class StatusItem(
  @param:StringRes val labelResId: Int,
  @param:StringRes val valueResId: Int? = null,
  val value: String? = null,
)

@Composable
fun StatusCard(items: List<StatusItem>) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = BgSecondary.copy(alpha = 0.86f)),
    border = BorderStroke(1.dp, BorderSubtle),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      items.forEachIndexed { index, item ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = stringResource(item.labelResId),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
          )
          Text(
            text = item.value ?: stringResource(checkNotNull(item.valueResId)),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
          )
        }

        if (index != items.lastIndex) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(BorderSubtle)
          )
        }
      }
    }
  }
}
