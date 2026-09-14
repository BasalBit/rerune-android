package io.rerune.example.compose.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import io.rerune.example.compose.StoryId

@Composable
internal fun StoryCover(id: StoryId, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit = {}) {
  Box(modifier.clip(RoundedCornerShape(22.dp))) {
    Canvas(Modifier.matchParentSize()) {
      val aspect = (size.width / size.height) / 1.5f
      withTransform({ scale(size.width / 600f, size.height / 400f, Offset.Zero) }) {
        when (id) {
          StoryId.Atlas -> atlas(aspect)
          StoryId.Lantern -> lantern(aspect)
          StoryId.Garden -> garden(aspect)
        }
      }
    }
    content()
  }
}

private fun DrawScope.fill(color: Long) = drawRect(Color(color), size = Size(600f, 400f))
private fun DrawScope.circle(x: Float, y: Float, radius: Float, color: Long, aspect: Float) {
  drawOval(Color(color), Offset(x - radius / aspect, y - radius), Size(radius * 2 / aspect, radius * 2))
}
private fun DrawScope.land(color: Long, geometry: Path.() -> Unit) = drawPath(Path().apply(geometry), Color(color))

private fun DrawScope.atlas(aspect: Float) {
  fill(0xFFD5DCC6)
  repeat(15) {
    val radius = 94f + it * 13
    drawOval(Color(0xFF708B79).copy(alpha = .23f),
      Offset(484 - radius / aspect, 345 - radius), Size(radius * 2 / aspect, radius * 2), style = Stroke(1.3f))
  }
  circle(457f, 157f, 39f, 0xFFD58C4E, aspect)
  land(0xFF547D79) {
    moveTo(0f, 290f); cubicTo(140f, 340f, 207f, 345f, 348f, 278f)
    cubicTo(440f, 231f, 500f, 254f, 600f, 267f); lineTo(600f, 400f); lineTo(0f, 400f); close()
  }
  land(0xFF244E52) {
    moveTo(0f, 341f); cubicTo(166f, 379f, 304f, 265f, 600f, 319f)
    lineTo(600f, 400f); lineTo(0f, 400f); close()
  }
  land(0xFF163A41) {
    moveTo(0f, 386f); cubicTo(163f, 315f, 321f, 397f, 600f, 354f)
    lineTo(600f, 400f); lineTo(0f, 400f); close()
  }
}

private fun DrawScope.lantern(aspect: Float) {
  fill(0xFF243E59)
  repeat(28) {
    circle((it * 97f + 37) % 600, (it * 43f + 27) % 260, if (it % 3 == 0) 2f else 1f, 0xFFB4CBC9, aspect)
  }
  circle(463f, 113f, 41f, 0xFFF0D6A0, aspect)
  circle(449f, 102f, 37f, 0xFF243E59, aspect)
  land(0xFF3B5A69) {
    moveTo(0f, 319f); lineTo(141f, 147f); lineTo(311f, 339f); lineTo(463f, 206f)
    lineTo(600f, 328f); lineTo(600f, 400f); lineTo(0f, 400f); close()
  }
  land(0xFF182C42) {
    moveTo(0f, 368f); lineTo(207f, 222f); lineTo(361f, 373f); lineTo(505f, 285f)
    lineTo(600f, 355f); lineTo(600f, 400f); lineTo(0f, 400f); close()
  }
  drawCircle(Brush.radialGradient(listOf(Color(0x99F4B35A), Color(0x00F4B35A)), Offset(316f, 316f), 50f),
    radius = 50f, center = Offset(316f, 316f))
  drawRoundRect(Color(0xFFF4BE69), Offset(307f, 299f), Size(18f, 30f), androidx.compose.ui.geometry.CornerRadius(4f))
  drawArc(Color(0xFFF4BE69), 180f, 180f, false, Offset(308f, 291f), Size(16f, 16f), style = Stroke(2f))
}

private fun DrawScope.garden(aspect: Float) {
  fill(0xFFE8CBAC)
  circle(468f, 128f, 69f, 0xFFD99D7E, aspect)
  land(0xFF698475) {
    moveTo(0f, 325f); quadraticBezierTo(277f, 244f, 600f, 318f)
    lineTo(600f, 400f); lineTo(0f, 400f); close()
  }
  repeat(6) { i ->
    val x = 70f + i * 99
    val y = 240f + (i % 3) * 37
    drawPath(Path().apply { moveTo(x, 410f); quadraticBezierTo(x - 18, y + 50, x, y) }, Color(0xFF34584E), style = Stroke(3f))
    repeat(3) { j ->
      val leafY = y + 35 + j * 33
      drawOval(Color(0xFF34584E), Offset(x - 34.5f, leafY - 7), Size(35f, 14f))
      drawOval(Color(0xFF43695A), Offset(x - 3, leafY + 8.5f), Size(30f, 13f))
    }
    circle(x, y, 11f, 0xFFF2DFB6, aspect)
    circle(x, y, 4f, 0xFFBB7456, aspect)
  }
}
