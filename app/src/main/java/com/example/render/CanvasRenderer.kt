package com.example.render

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.asComposePath
import androidx.core.content.res.ResourcesCompat
import com.example.data.ProjectStorage
import com.example.model.CanvasBackground
import com.example.model.CanvasDocument
import com.example.model.LayerTextAlign
import com.example.model.ShapeType
import com.example.model.StudioLayer
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object CanvasRenderer {

    data class ExportResult(
        val uri: Uri?,
        val fileName: String,
        val widthPx: Int,
        val heightPx: Int,
        val savedToGallery: Boolean
    )

    fun buildAndroidShapePath(
        shapeType: ShapeType,
        width: Float,
        height: Float,
        cornerRadius: Float = 36f
    ): Path {
        val path = Path()
        val halfW = width / 2f
        val halfH = height / 2f
        val left = -halfW
        val top = -halfH
        val right = halfW
        val bottom = halfH

        when (shapeType) {
            ShapeType.CIRCLE -> {
                val r = min(halfW, halfH)
                path.addCircle(0f, 0f, r, Path.Direction.CW)
            }
            ShapeType.ROUNDED_RECT -> {
                val r = min(cornerRadius, min(halfW, halfH))
                path.addRoundRect(RectF(left, top, right, bottom), r, r, Path.Direction.CW)
            }
            ShapeType.RIBBON_BANNER -> {
                val notch = min(width * 0.1f, height * 0.45f)
                path.moveTo(left, top)
                path.lineTo(right, top)
                path.lineTo(right - notch, 0f)
                path.lineTo(right, bottom)
                path.lineTo(left, bottom)
                path.lineTo(left + notch, 0f)
                path.close()
            }
            ShapeType.FIVE_STAR -> {
                addStarPath(path, 5, min(halfW, halfH), min(halfW, halfH) * 0.44f)
            }
            ShapeType.STARBURST_BADGE -> {
                addStarPath(path, 16, min(halfW, halfH), min(halfW, halfH) * 0.80f)
            }
            ShapeType.ISLAMIC_EIGHT_STAR -> {
                // Rub el Hizb: two overlapping squares rotated 45 degrees
                val side = min(width, height) * 0.7f
                val halfS = side / 2f
                path.addRect(-halfS, -halfS, halfS, halfS, Path.Direction.CW)
                val rotPath = Path()
                rotPath.addRect(-halfS, -halfS, halfS, halfS, Path.Direction.CW)
                val m = android.graphics.Matrix()
                m.setRotate(45f)
                rotPath.transform(m)
                path.addPath(rotPath)
            }
            ShapeType.ORNATE_BORDER -> {
                val r = min(width, height) * 0.06f
                path.addRoundRect(RectF(left, top, right, bottom), r, r, Path.Direction.CW)
                val inset = min(width, height) * 0.04f
                path.addRoundRect(
                    RectF(left + inset, top + inset, right - inset, bottom - inset),
                    r * 0.6f,
                    r * 0.6f,
                    Path.Direction.CCW
                )
            }
            ShapeType.CRESCENT_MOON -> {
                val outerR = min(halfW, halfH) * 0.92f
                val innerR = outerR * 0.78f
                val outer = Path().apply { addCircle(0f, 0f, outerR, Path.Direction.CW) }
                val inner = Path().apply {
                    addCircle(outerR * 0.34f, -outerR * 0.14f, innerR, Path.Direction.CW)
                }
                outer.op(inner, Path.Op.DIFFERENCE)
                path.addPath(outer)
            }
        }
        return path
    }

    fun buildComposeShapePath(
        shapeType: ShapeType,
        width: Float,
        height: Float,
        cornerRadius: Float = 36f
    ): androidx.compose.ui.graphics.Path {
        return buildAndroidShapePath(shapeType, width, height, cornerRadius).asComposePath()
    }

    private fun addStarPath(path: Path, points: Int, outerR: Float, innerR: Float) {
        val step = PI / points
        for (i in 0 until points * 2) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = i * step - PI / 2.0
            val px = (r * cos(angle)).toFloat()
            val py = (r * sin(angle)).toFloat()
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        path.close()
    }

    fun exportHighResPng(
        context: Context,
        document: CanvasDocument,
        multiplier: Int = 2
    ): Result<ExportResult> = runCatching {
        val outW = document.preset.width * multiplier
        val outH = document.preset.height * multiplier
        val bitmap = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.scale(multiplier.toFloat(), multiplier.toFloat())

        val docW = document.preset.width.toFloat()
        val docH = document.preset.height.toFloat()

        // 1. Draw Background
        drawBackground(canvas, docW, docH, document.background)

        // 2. Draw Layers in z-order
        document.layers.filter { it.isVisible }.forEach { layer ->
            canvas.save()
            canvas.translate(layer.x, layer.y)
            canvas.rotate(layer.rotation)
            val sx = layer.scale * if (layer.flipX) -1f else 1f
            val sy = layer.scale * if (layer.flipY) -1f else 1f
            canvas.scale(sx, sy)

            when (layer) {
                is StudioLayer.TextLayer -> drawTextLayer(context, canvas, layer)
                is StudioLayer.ShapeLayer -> drawShapeLayer(canvas, layer)
                is StudioLayer.ImageLayer -> drawImageLayer(canvas, layer)
            }
            canvas.restore()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "UrduStudio_${timestamp}_${outW}x${outH}.png"

        val result = saveBitmapToMediaStoreOrCache(context, bitmap, fileName, outW, outH)
        bitmap.recycle()
        result
    }

    private fun drawBackground(canvas: Canvas, width: Float, height: Float, bg: CanvasBackground) {
        when (bg) {
            is CanvasBackground.Solid -> {
                canvas.drawColor(bg.colorArgb.toInt())
            }
            is CanvasBackground.Gradient -> {
                val rad = Math.toRadians(bg.angleDegrees.toDouble())
                val dx = cos(rad).toFloat() * width * 0.5f
                val dy = sin(rad).toFloat() * height * 0.5f
                val cx = width / 2f
                val cy = height / 2f
                val colors = bg.colorsArgb.map { it.toInt() }.toIntArray()
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        cx - dx, cy - dy,
                        cx + dx, cy + dy,
                        colors,
                        null,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width, height, paint)
            }
            is CanvasBackground.Image -> {
                val bmp = ProjectStorage.decodeBitmap(bg.filePath, maxPreviewDim = 2400)
                if (bmp != null) {
                    val scale = max(width / bmp.width, height / bmp.height)
                    val dstW = bmp.width * scale
                    val dstH = bmp.height * scale
                    val left = (width - dstW) / 2f
                    val top = (height - dstH) / 2f
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                    canvas.drawBitmap(bmp, null, RectF(left, top, left + dstW, top + dstH), paint)
                    bmp.recycle()
                } else {
                    canvas.drawColor(Color.parseColor("#0F172A"))
                }
            }
        }
    }

    private fun drawTextLayer(context: Context, canvas: Canvas, layer: StudioLayer.TextLayer) {
        val typeface = runCatching { ResourcesCompat.getFont(context, layer.font.fontRes) }.getOrNull()
        val alphaInt = (layer.opacity.coerceIn(0f, 1f) * 255).toInt()
        val boxW = layer.boxWidth.toInt().coerceAtLeast(120)
        // Generous vertical padding so Nastaliq ascenders and deep descenders never clip
        val padX = 28f
        val padY = 36f

        val alignment = when (layer.textAlign) {
            LayerTextAlign.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
            LayerTextAlign.LEFT -> Layout.Alignment.ALIGN_NORMAL
            LayerTextAlign.CENTER -> Layout.Alignment.ALIGN_CENTER
        }

        val fillPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
            textSize = layer.fontSize
            color = applyAlpha(layer.colorArgb.toInt(), alphaInt)
            if (typeface != null) this.typeface = typeface
            if (layer.shadowBlur > 0f || layer.shadowOffsetX != 0f || layer.shadowOffsetY != 0f) {
                setShadowLayer(
                    layer.shadowBlur.coerceAtLeast(0.5f),
                    layer.shadowOffsetX,
                    layer.shadowOffsetY,
                    applyAlpha(layer.shadowColorArgb.toInt(), alphaInt)
                )
            }
        }

        val fillLayout = StaticLayout.Builder.obtain(
            layer.text, 0, layer.text.length, fillPaint, boxW
        )
            .setAlignment(alignment)
            .setLineSpacing(0f, layer.lineHeightMultiplier.coerceAtLeast(1.0f))
            .setIncludePad(true)
            .build()

        val textH = fillLayout.height.toFloat()
        val totalW = boxW + padX * 2f
        val totalH = textH + padY * 2f

        canvas.save()
        canvas.translate(-totalW / 2f, -totalH / 2f)

        // Optional highlight background pill
        val bgAlpha = Color.alpha(layer.backgroundColorArgb.toInt())
        if (bgAlpha > 0) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = applyAlpha(layer.backgroundColorArgb.toInt(), (bgAlpha * layer.opacity).toInt())
            }
            canvas.drawRoundRect(RectF(0f, 0f, totalW, totalH), 24f, 24f, bgPaint)
        }

        canvas.translate(padX, padY)

        // Optional outline stroke behind fill
        if (layer.strokeWidth > 0.2f) {
            val strokePaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
                textSize = layer.fontSize
                style = Paint.Style.STROKE
                strokeWidth = layer.strokeWidth * 2f
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
                color = applyAlpha(layer.strokeColorArgb.toInt(), alphaInt)
                if (typeface != null) this.typeface = typeface
            }
            val strokeLayout = StaticLayout.Builder.obtain(
                layer.text, 0, layer.text.length, strokePaint, boxW
            )
                .setAlignment(alignment)
                .setLineSpacing(0f, layer.lineHeightMultiplier.coerceAtLeast(1.0f))
                .setIncludePad(true)
                .build()
            strokeLayout.draw(canvas)
        }

        fillLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawShapeLayer(canvas: Canvas, layer: StudioLayer.ShapeLayer) {
        val alphaInt = (layer.opacity.coerceIn(0f, 1f) * 255).toInt()
        val path = buildAndroidShapePath(
            layer.shapeType,
            layer.baseWidth,
            layer.baseHeight,
            layer.cornerRadius
        )

        val fillAlpha = Color.alpha(layer.fillColorArgb.toInt())
        if (fillAlpha > 0) {
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = applyAlpha(layer.fillColorArgb.toInt(), (fillAlpha * layer.opacity).toInt())
                if (layer.shadowBlur > 0f || layer.shadowOffsetX != 0f || layer.shadowOffsetY != 0f) {
                    setShadowLayer(
                        layer.shadowBlur.coerceAtLeast(0.5f),
                        layer.shadowOffsetX,
                        layer.shadowOffsetY,
                        applyAlpha(layer.shadowColorArgb.toInt(), alphaInt)
                    )
                }
            }
            canvas.drawPath(path, fillPaint)
        }

        if (layer.strokeWidth > 0.2f) {
            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = layer.strokeWidth
                strokeJoin = Paint.Join.ROUND
                color = applyAlpha(layer.strokeColorArgb.toInt(), alphaInt)
            }
            canvas.drawPath(path, strokePaint)
        }
    }

    private fun drawImageLayer(canvas: Canvas, layer: StudioLayer.ImageLayer) {
        val bmp = ProjectStorage.decodeBitmap(layer.filePath, maxPreviewDim = 2048) ?: return
        val alphaInt = (layer.opacity.coerceIn(0f, 1f) * 255).toInt()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            alpha = alphaInt
        }
        val halfW = layer.baseWidth / 2f
        val halfH = layer.baseHeight / 2f
        canvas.drawBitmap(bmp, null, RectF(-halfW, -halfH, halfW, halfH), paint)
        bmp.recycle()
    }

    private fun applyAlpha(color: Int, alpha: Int): Int {
        val origAlpha = Color.alpha(color)
        val combined = ((origAlpha / 255f) * (alpha / 255f) * 255f).toInt().coerceIn(0, 255)
        return Color.argb(combined, Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun saveBitmapToMediaStoreOrCache(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
        widthPx: Int,
        heightPx: Int
    ): ExportResult {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Urdu Studio")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                return ExportResult(uri, fileName, widthPx, heightPx, savedToGallery = true)
            }
        }
        val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return ExportResult(Uri.fromFile(file), fileName, widthPx, heightPx, savedToGallery = false)
    }
}
