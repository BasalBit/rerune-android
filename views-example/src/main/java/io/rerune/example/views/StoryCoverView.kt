package io.rerune.example.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.widget.FrameLayout

/** The reference's cover geometry, painted by the native Android Canvas. */
internal class StoryCoverView(context: Context, private val story: StoryId) : FrameLayout(context) {
  private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val clip = Path()
  init {
    setWillNotDraw(false)
    background = GradientDrawable().apply { setColor(0xFFD5DCC6.toInt()); cornerRadius = context.dp(22).toFloat() }
    clipToOutline = true
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    clip.reset()
    val radius = context.dp(22).toFloat()
    clip.addRoundRect(0f, 0f, w.toFloat(), h.toFloat(), radius, radius, Path.Direction.CW)
  }

  override fun draw(canvas: Canvas) {
    // Canvas clipping also preserves the cover shape when rendered in software.
    val checkpoint = canvas.save()
    canvas.clipPath(clip)
    super.draw(canvas)
    canvas.restoreToCount(checkpoint)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (width == 0 || height == 0) return
    val aspect = (width.toFloat() / height) / 1.5f
    canvas.save()
    canvas.scale(width / 600f, height / 400f)
    when (story) {
      StoryId.Atlas -> atlas(canvas, aspect)
      StoryId.Lantern -> lantern(canvas, aspect)
      StoryId.Garden -> garden(canvas, aspect)
    }
    canvas.restore()
  }

  private fun ink(color: Long, stroke: Float = 0f): Paint = paint.apply {
    this.color = color.toInt(); shader = null
    style = if (stroke == 0f) Paint.Style.FILL else Paint.Style.STROKE
    strokeWidth = stroke
  }
  private fun fill(c: Canvas, color: Long) = c.drawRect(0f, 0f, 600f, 400f, ink(color))
  private fun circle(c: Canvas, x: Float, y: Float, r: Float, color: Long, aspect: Float) =
    c.drawOval(x - r / aspect, y - r, x + r / aspect, y + r, ink(color))
  private fun land(c: Canvas, color: Long, geometry: Path.() -> Unit) = c.drawPath(Path().apply(geometry), ink(color))

  private fun atlas(c: Canvas, aspect: Float) {
    fill(c, 0xFFD5DCC6)
    repeat(15) {
      val r = 94f + it * 13
      c.drawOval(484 - r / aspect, 345 - r, 484 + r / aspect, 345 + r, ink(0x3B708B79, 1.3f))
    }
    circle(c, 457f, 157f, 39f, 0xFFD58C4E, aspect)
    land(c, 0xFF547D79) {
      moveTo(0f, 290f); cubicTo(140f, 340f, 207f, 345f, 348f, 278f)
      cubicTo(440f, 231f, 500f, 254f, 600f, 267f); lineTo(600f, 400f); lineTo(0f, 400f); close()
    }
    land(c, 0xFF244E52) {
      moveTo(0f, 341f); cubicTo(166f, 379f, 304f, 265f, 600f, 319f)
      lineTo(600f, 400f); lineTo(0f, 400f); close()
    }
    land(c, 0xFF163A41) {
      moveTo(0f, 386f); cubicTo(163f, 315f, 321f, 397f, 600f, 354f)
      lineTo(600f, 400f); lineTo(0f, 400f); close()
    }
  }

  private fun lantern(c: Canvas, aspect: Float) {
    fill(c, 0xFF243E59)
    repeat(28) { circle(c, (it * 97f + 37) % 600, (it * 43f + 27) % 260, if (it % 3 == 0) 2f else 1f, 0xFFB4CBC9, aspect) }
    circle(c, 463f, 113f, 41f, 0xFFF0D6A0, aspect)
    circle(c, 449f, 102f, 37f, 0xFF243E59, aspect)
    land(c, 0xFF3B5A69) {
      moveTo(0f, 319f); lineTo(141f, 147f); lineTo(311f, 339f); lineTo(463f, 206f)
      lineTo(600f, 328f); lineTo(600f, 400f); lineTo(0f, 400f); close()
    }
    land(c, 0xFF182C42) {
      moveTo(0f, 368f); lineTo(207f, 222f); lineTo(361f, 373f); lineTo(505f, 285f)
      lineTo(600f, 355f); lineTo(600f, 400f); lineTo(0f, 400f); close()
    }
    ink(0xFFF4B35A).shader = RadialGradient(316f, 316f, 50f, 0x99F4B35A.toInt(), 0x00F4B35A, Shader.TileMode.CLAMP)
    c.drawCircle(316f, 316f, 50f, paint)
    c.drawRoundRect(307f, 299f, 325f, 329f, 4f, 4f, ink(0xFFF4BE69))
    c.drawArc(308f, 291f, 324f, 307f, 180f, 180f, false, ink(0xFFF4BE69, 2f))
  }

  private fun garden(c: Canvas, aspect: Float) {
    fill(c, 0xFFE8CBAC)
    circle(c, 468f, 128f, 69f, 0xFFD99D7E, aspect)
    land(c, 0xFF698475) {
      moveTo(0f, 325f); quadTo(277f, 244f, 600f, 318f)
      lineTo(600f, 400f); lineTo(0f, 400f); close()
    }
    repeat(6) { i ->
      val x = 70f + i * 99
      val y = 240f + (i % 3) * 37
      c.drawPath(Path().apply { moveTo(x, 410f); quadTo(x - 18, y + 50, x, y) }, ink(0xFF34584E, 3f))
      repeat(3) { j ->
        val leafY = y + 35 + j * 33
        c.drawOval(x - 34.5f, leafY - 7, x + .5f, leafY + 7, ink(0xFF34584E))
        c.drawOval(x - 3, leafY + 8.5f, x + 27, leafY + 21.5f, ink(0xFF43695A))
      }
      circle(c, x, y, 11f, 0xFFF2DFB6, aspect)
      circle(c, x, y, 4f, 0xFFBB7456, aspect)
    }
  }
}
