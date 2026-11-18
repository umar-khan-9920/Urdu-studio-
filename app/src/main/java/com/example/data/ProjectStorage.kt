package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.model.CanvasBackground
import com.example.model.CanvasDocument
import com.example.model.CanvasPreset
import com.example.model.LayerTextAlign
import com.example.model.ShapeType
import com.example.model.StudioFont
import com.example.model.StudioLayer
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

object ProjectStorage {

    private const val AUTOSAVE_FILE = "urdu_studio_autosave.json"
    private const val MEDIA_DIR = "studio_media"

    fun saveAutosave(context: Context, document: CanvasDocument) {
        runCatching {
            val file = File(context.filesDir, AUTOSAVE_FILE)
            file.writeText(toJson(document).toString(2))
        }
    }

    fun loadAutosave(context: Context): CanvasDocument? {
        return runCatching {
            val file = File(context.filesDir, AUTOSAVE_FILE)
            if (!file.exists()) return null
            val raw = file.readText()
            if (raw.isBlank()) return null
            fromJson(JSONObject(raw))
        }.getOrNull()
    }

    fun saveToUri(context: Context, uri: Uri, document: CanvasDocument): Result<Unit> {
        return runCatching {
            val jsonString = toJson(document).toString(2)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(jsonString.toByteArray(Charsets.UTF_8))
                out.flush()
            } ?: error("Unable to open output stream")
        }
    }

    fun loadFromUri(context: Context, uri: Uri): Result<CanvasDocument> {
        return runCatching {
            val raw = context.contentResolver.openInputStream(uri)?.use { input ->
                input.bufferedReader(Charsets.UTF_8).readText()
            } ?: error("Unable to read project file")
            fromJson(JSONObject(raw))
        }
    }

    fun importImageToLocalMedia(context: Context, uri: Uri, maxDimension: Int = 2048): Result<ImportedImage> {
        return runCatching {
            val mediaDir = File(context.filesDir, MEDIA_DIR).apply { mkdirs() }
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOpts)
            }
            val origW = boundsOpts.outWidth.coerceAtLeast(1)
            val origH = boundsOpts.outHeight.coerceAtLeast(1)
            var sampleSize = 1
            while (origW / sampleSize > maxDimension || origH / sampleSize > maxDimension) {
                sampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOpts)
            } ?: error("Could not decode selected image")

            val finalBitmap = if (max(decoded.width, decoded.height) > maxDimension) {
                val ratio = maxDimension.toFloat() / max(decoded.width, decoded.height)
                val targetW = (decoded.width * ratio).roundToInt().coerceAtLeast(1)
                val targetH = (decoded.height * ratio).roundToInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(decoded, targetW, targetH, true).also {
                    if (it !== decoded) decoded.recycle()
                }
            } else {
                decoded
            }

            val outFile = File(mediaDir, "img_${UUID.randomUUID()}.png")
            FileOutputStream(outFile).use { fos ->
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            val width = finalBitmap.width
            val height = finalBitmap.height
            finalBitmap.recycle()
            ImportedImage(outFile.absolutePath, width, height)
        }
    }

    fun decodeBitmap(filePath: String, maxPreviewDim: Int = 1600): Bitmap? {
        return runCatching {
            val file = File(filePath)
            if (!file.exists()) return null
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(filePath, bounds)
            var sample = 1
            while (bounds.outWidth / sample > maxPreviewDim || bounds.outHeight / sample > maxPreviewDim) {
                sample *= 2
            }
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(filePath, opts)
        }.getOrNull()
    }

    data class ImportedImage(val filePath: String, val width: Int, val height: Int)

    fun toJson(doc: CanvasDocument): JSONObject {
        val root = JSONObject()
        root.put("version", 1)
        root.put("preset", doc.preset.name)
        root.put("background", backgroundToJson(doc.background))
        val layersArr = JSONArray()
        doc.layers.forEach { layersArr.put(layerToJson(it)) }
        root.put("layers", layersArr)
        return root
    }

    fun fromJson(root: JSONObject): CanvasDocument {
        val presetName = root.optString("preset", CanvasPreset.INSTAGRAM_POST.name)
        val preset = CanvasPreset.entries.firstOrNull { it.name == presetName }
            ?: CanvasPreset.INSTAGRAM_POST
        val bg = backgroundFromJson(root.optJSONObject("background"))
        val layersArr = root.optJSONArray("layers") ?: JSONArray()
        val layers = buildList {
            for (i in 0 until layersArr.length()) {
                layerFromJson(layersArr.optJSONObject(i))?.let { add(it) }
            }
        }
        return CanvasDocument(preset = preset, background = bg, layers = layers)
    }

    private fun backgroundToJson(bg: CanvasBackground): JSONObject {
        val obj = JSONObject()
        when (bg) {
            is CanvasBackground.Solid -> {
                obj.put("type", "solid")
                obj.put("colorArgb", bg.colorArgb)
            }
            is CanvasBackground.Gradient -> {
                obj.put("type", "gradient")
                obj.put("id", bg.id)
                obj.put("name", bg.name)
                obj.put("angleDegrees", bg.angleDegrees.toDouble())
                val arr = JSONArray()
                bg.colorsArgb.forEach { arr.put(it) }
                obj.put("colorsArgb", arr)
            }
            is CanvasBackground.Image -> {
                obj.put("type", "image")
                obj.put("filePath", bg.filePath)
            }
        }
        return obj
    }

    private fun backgroundFromJson(obj: JSONObject?): CanvasBackground {
        if (obj == null) return StudioPresets.luxuryGradients.first()
        return when (obj.optString("type")) {
            "solid" -> CanvasBackground.Solid(obj.optLong("colorArgb", 0xFF0F172A))
            "gradient" -> {
                val arr = obj.optJSONArray("colorsArgb")
                val colors = buildList {
                    if (arr != null) {
                        for (i in 0 until arr.length()) add(arr.optLong(i, 0xFF0F172A))
                    }
                }.ifEmpty { listOf(0xFF022C22, 0xFF064E3B) }
                CanvasBackground.Gradient(
                    id = obj.optString("id", "custom"),
                    name = obj.optString("name", "Gradient"),
                    colorsArgb = colors,
                    angleDegrees = obj.optDouble("angleDegrees", 135.0).toFloat()
                )
            }
            "image" -> {
                val path = obj.optString("filePath")
                if (path.isNotBlank() && File(path).exists()) {
                    CanvasBackground.Image(path)
                } else {
                    StudioPresets.luxuryGradients.first()
                }
            }
            else -> StudioPresets.luxuryGradients.first()
        }
    }

    private fun layerToJson(layer: StudioLayer): JSONObject {
        val obj = JSONObject()
        obj.put("id", layer.id)
        obj.put("name", layer.name)
        obj.put("x", layer.x.toDouble())
        obj.put("y", layer.y.toDouble())
        obj.put("scale", layer.scale.toDouble())
        obj.put("rotation", layer.rotation.toDouble())
        obj.put("opacity", layer.opacity.toDouble())
        obj.put("flipX", layer.flipX)
        obj.put("flipY", layer.flipY)
        obj.put("isLocked", layer.isLocked)
        obj.put("isVisible", layer.isVisible)

        when (layer) {
            is StudioLayer.TextLayer -> {
                obj.put("kind", "text")
                obj.put("text", layer.text)
                obj.put("font", layer.font.name)
                obj.put("fontSize", layer.fontSize.toDouble())
                obj.put("lineHeightMultiplier", layer.lineHeightMultiplier.toDouble())
                obj.put("colorArgb", layer.colorArgb)
                obj.put("backgroundColorArgb", layer.backgroundColorArgb)
                obj.put("strokeColorArgb", layer.strokeColorArgb)
                obj.put("strokeWidth", layer.strokeWidth.toDouble())
                obj.put("shadowColorArgb", layer.shadowColorArgb)
                obj.put("shadowBlur", layer.shadowBlur.toDouble())
                obj.put("shadowOffsetX", layer.shadowOffsetX.toDouble())
                obj.put("shadowOffsetY", layer.shadowOffsetY.toDouble())
                obj.put("isRtl", layer.isRtl)
                obj.put("textAlign", layer.textAlign.name)
                obj.put("boxWidth", layer.boxWidth.toDouble())
            }
            is StudioLayer.ShapeLayer -> {
                obj.put("kind", "shape")
                obj.put("shapeType", layer.shapeType.name)
                obj.put("baseWidth", layer.baseWidth.toDouble())
                obj.put("baseHeight", layer.baseHeight.toDouble())
                obj.put("fillColorArgb", layer.fillColorArgb)
                obj.put("strokeColorArgb", layer.strokeColorArgb)
                obj.put("strokeWidth", layer.strokeWidth.toDouble())
                obj.put("shadowColorArgb", layer.shadowColorArgb)
                obj.put("shadowBlur", layer.shadowBlur.toDouble())
                obj.put("shadowOffsetX", layer.shadowOffsetX.toDouble())
                obj.put("shadowOffsetY", layer.shadowOffsetY.toDouble())
                obj.put("cornerRadius", layer.cornerRadius.toDouble())
            }
            is StudioLayer.ImageLayer -> {
                obj.put("kind", "image")
                obj.put("filePath", layer.filePath)
                obj.put("baseWidth", layer.baseWidth.toDouble())
                obj.put("baseHeight", layer.baseHeight.toDouble())
            }
        }
        return obj
    }

    private fun layerFromJson(obj: JSONObject?): StudioLayer? {
        if (obj == null) return null
        val id = obj.optString("id", UUID.randomUUID().toString())
        val name = obj.optString("name", "Layer")
        val x = obj.optDouble("x", 540.0).toFloat()
        val y = obj.optDouble("y", 540.0).toFloat()
        val scale = obj.optDouble("scale", 1.0).toFloat()
        val rotation = obj.optDouble("rotation", 0.0).toFloat()
        val opacity = obj.optDouble("opacity", 1.0).toFloat()
        val flipX = obj.optBoolean("flipX", false)
        val flipY = obj.optBoolean("flipY", false)
        val isLocked = obj.optBoolean("isLocked", false)
        val isVisible = obj.optBoolean("isVisible", true)

        return when (obj.optString("kind")) {
            "text" -> {
                val fontName = obj.optString("font", StudioFont.NOTO_NASTALIQ_URDU.name)
                val font = StudioFont.entries.firstOrNull { it.name == fontName }
                    ?: StudioFont.NOTO_NASTALIQ_URDU
                val alignName = obj.optString("textAlign", LayerTextAlign.RIGHT.name)
                val align = LayerTextAlign.entries.firstOrNull { it.name == alignName }
                    ?: LayerTextAlign.RIGHT
                StudioLayer.TextLayer(
                    id = id, name = name, x = x, y = y, scale = scale, rotation = rotation,
                    opacity = opacity, flipX = flipX, flipY = flipY, isLocked = isLocked, isVisible = isVisible,
                    text = obj.optString("text", "اردو ڈیزائنر اسٹوڈیو"),
                    font = font,
                    fontSize = obj.optDouble("fontSize", 64.0).toFloat(),
                    lineHeightMultiplier = obj.optDouble("lineHeightMultiplier", 1.6).toFloat(),
                    colorArgb = obj.optLong("colorArgb", 0xFFFFFFFF),
                    backgroundColorArgb = obj.optLong("backgroundColorArgb", 0x00000000),
                    strokeColorArgb = obj.optLong("strokeColorArgb", 0xFF0F172A),
                    strokeWidth = obj.optDouble("strokeWidth", 0.0).toFloat(),
                    shadowColorArgb = obj.optLong("shadowColorArgb", 0xAA000000),
                    shadowBlur = obj.optDouble("shadowBlur", 10.0).toFloat(),
                    shadowOffsetX = obj.optDouble("shadowOffsetX", 0.0).toFloat(),
                    shadowOffsetY = obj.optDouble("shadowOffsetY", 4.0).toFloat(),
                    isRtl = obj.optBoolean("isRtl", true),
                    textAlign = align,
                    boxWidth = obj.optDouble("boxWidth", 760.0).toFloat()
                )
            }
            "shape" -> {
                val shapeName = obj.optString("shapeType", ShapeType.ROUNDED_RECT.name)
                val shape = ShapeType.entries.firstOrNull { it.name == shapeName }
                    ?: ShapeType.ROUNDED_RECT
                StudioLayer.ShapeLayer(
                    id = id, name = name, x = x, y = y, scale = scale, rotation = rotation,
                    opacity = opacity, flipX = flipX, flipY = flipY, isLocked = isLocked, isVisible = isVisible,
                    shapeType = shape,
                    baseWidth = obj.optDouble("baseWidth", 360.0).toFloat(),
                    baseHeight = obj.optDouble("baseHeight", 240.0).toFloat(),
                    fillColorArgb = obj.optLong("fillColorArgb", 0xFF10B981),
                    strokeColorArgb = obj.optLong("strokeColorArgb", 0xFFF59E0B),
                    strokeWidth = obj.optDouble("strokeWidth", 6.0).toFloat(),
                    shadowColorArgb = obj.optLong("shadowColorArgb", 0x80000000),
                    shadowBlur = obj.optDouble("shadowBlur", 12.0).toFloat(),
                    shadowOffsetX = obj.optDouble("shadowOffsetX", 0.0).toFloat(),
                    shadowOffsetY = obj.optDouble("shadowOffsetY", 6.0).toFloat(),
                    cornerRadius = obj.optDouble("cornerRadius", 36.0).toFloat()
                )
            }
            "image" -> {
                val path = obj.optString("filePath")
                if (path.isBlank() || !File(path).exists()) null
                else StudioLayer.ImageLayer(
                    id = id, name = name, x = x, y = y, scale = scale, rotation = rotation,
                    opacity = opacity, flipX = flipX, flipY = flipY, isLocked = isLocked, isVisible = isVisible,
                    filePath = path,
                    baseWidth = obj.optDouble("baseWidth", 420.0).toFloat(),
                    baseHeight = obj.optDouble("baseHeight", 420.0).toFloat()
                )
            }
            else -> null
        }
    }
}
