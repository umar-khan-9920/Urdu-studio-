package com.example.model

import androidx.annotation.FontRes
import androidx.compose.ui.text.font.FontFamily
import com.example.R
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.ArefRuqaaFontFamily
import com.example.ui.theme.GulzarFontFamily
import com.example.ui.theme.LateefFontFamily
import com.example.ui.theme.NotoNastaliqUrduFontFamily
import com.example.ui.theme.PoppinsFontFamily
import java.util.UUID

enum class CanvasPreset(
    val displayName: String,
    val shortLabel: String,
    val width: Int,
    val height: Int,
    val aspectLabel: String
) {
    INSTAGRAM_POST("Instagram Post", "1080×1080", 1080, 1080, "1:1"),
    YOUTUBE_THUMBNAIL("YouTube Thumbnail", "1280×720", 1280, 720, "16:9"),
    WHATSAPP_STATUS("WhatsApp Status", "1080×1920", 1080, 1920, "9:16"),
    PANAFLEX_BANNER("Panaflex Banner", "1200×600", 1200, 600, "2:1")
}

enum class StudioFont(
    val displayName: String,
    @FontRes val fontRes: Int,
    val fontFamily: FontFamily,
    val isUrdu: Boolean,
    val sampleText: String
) {
    NOTO_NASTALIQ_URDU(
        "Noto Nastaliq Urdu",
        R.font.noto_nastaliq_urdu,
        NotoNastaliqUrduFontFamily,
        true,
        "نستعلیق اردو"
    ),
    GULZAR(
        "Gulzar",
        R.font.gulzar,
        GulzarFontFamily,
        true,
        "گلزار خط"
    ),
    AMIRI(
        "Amiri",
        R.font.amiri,
        AmiriFontFamily,
        true,
        "امیری عربی"
    ),
    LATEEF(
        "Lateef",
        R.font.lateef,
        LateefFontFamily,
        true,
        "لطیف خط"
    ),
    AREF_RUQAA(
        "Aref Ruqaa",
        R.font.aref_ruqaa,
        ArefRuqaaFontFamily,
        true,
        "عارف رقعہ"
    ),
    POPPINS(
        "Poppins",
        R.font.poppins,
        PoppinsFontFamily,
        false,
        "Modern Sans"
    )
}

enum class LayerTextAlign(val label: String) {
    RIGHT("Right"),
    CENTER("Center"),
    LEFT("Left")
}

enum class ShapeType(val displayName: String) {
    RIBBON_BANNER("Ribbon Banner"),
    STARBURST_BADGE("Promo Badge"),
    ISLAMIC_EIGHT_STAR("Islamic Star"),
    FIVE_STAR("5-Point Star"),
    CIRCLE("Circle"),
    ROUNDED_RECT("Rounded Card"),
    ORNATE_BORDER("Ornate Border"),
    CRESCENT_MOON("Crescent Moon")
}

sealed class CanvasBackground {
    data class Solid(val colorArgb: Long) : CanvasBackground()
    data class Gradient(
        val id: String,
        val name: String,
        val colorsArgb: List<Long>,
        val angleDegrees: Float = 135f
    ) : CanvasBackground()
    data class Image(val filePath: String) : CanvasBackground()
}

sealed class StudioLayer {
    abstract val id: String
    abstract val name: String
    abstract val x: Float
    abstract val y: Float
    abstract val scale: Float
    abstract val rotation: Float
    abstract val opacity: Float
    abstract val flipX: Boolean
    abstract val flipY: Boolean
    abstract val isLocked: Boolean
    abstract val isVisible: Boolean

    data class TextLayer(
        override val id: String = UUID.randomUUID().toString(),
        override val name: String = "Urdu Text",
        override val x: Float,
        override val y: Float,
        override val scale: Float = 1f,
        override val rotation: Float = 0f,
        override val opacity: Float = 1f,
        override val flipX: Boolean = false,
        override val flipY: Boolean = false,
        override val isLocked: Boolean = false,
        override val isVisible: Boolean = true,
        val text: String = "اردو ڈیزائنر اسٹوڈیو",
        val font: StudioFont = StudioFont.NOTO_NASTALIQ_URDU,
        val fontSize: Float = 68f,
        val lineHeightMultiplier: Float = 1.6f,
        val colorArgb: Long = 0xFFFFFFFF,
        val backgroundColorArgb: Long = 0x00000000,
        val strokeColorArgb: Long = 0xFF0F172A,
        val strokeWidth: Float = 0f,
        val shadowColorArgb: Long = 0xAA000000,
        val shadowBlur: Float = 10f,
        val shadowOffsetX: Float = 0f,
        val shadowOffsetY: Float = 4f,
        val isRtl: Boolean = true,
        val textAlign: LayerTextAlign = LayerTextAlign.RIGHT,
        val boxWidth: Float = 760f
    ) : StudioLayer()

    data class ShapeLayer(
        override val id: String = UUID.randomUUID().toString(),
        override val name: String = "Shape",
        override val x: Float,
        override val y: Float,
        override val scale: Float = 1f,
        override val rotation: Float = 0f,
        override val opacity: Float = 1f,
        override val flipX: Boolean = false,
        override val flipY: Boolean = false,
        override val isLocked: Boolean = false,
        override val isVisible: Boolean = true,
        val shapeType: ShapeType,
        val baseWidth: Float = 360f,
        val baseHeight: Float = 240f,
        val fillColorArgb: Long = 0xFF10B981,
        val strokeColorArgb: Long = 0xFFF59E0B,
        val strokeWidth: Float = 6f,
        val shadowColorArgb: Long = 0x80000000,
        val shadowBlur: Float = 12f,
        val shadowOffsetX: Float = 0f,
        val shadowOffsetY: Float = 6f,
        val cornerRadius: Float = 36f
    ) : StudioLayer()

    data class ImageLayer(
        override val id: String = UUID.randomUUID().toString(),
        override val name: String = "Image / Logo",
        override val x: Float,
        override val y: Float,
        override val scale: Float = 1f,
        override val rotation: Float = 0f,
        override val opacity: Float = 1f,
        override val flipX: Boolean = false,
        override val flipY: Boolean = false,
        override val isLocked: Boolean = false,
        override val isVisible: Boolean = true,
        val filePath: String,
        val baseWidth: Float = 420f,
        val baseHeight: Float = 420f
    ) : StudioLayer()
}

data class CanvasDocument(
    val preset: CanvasPreset = CanvasPreset.INSTAGRAM_POST,
    val background: CanvasBackground = CanvasBackground.Gradient(
        id = "emerald_royal",
        name = "Emerald Royal",
        colorsArgb = listOf(0xFF022C22, 0xFF064E3B, 0xFF0F172A),
        angleDegrees = 135f
    ),
    val layers: List<StudioLayer> = emptyList()
)

enum class AssetTab(val title: String) {
    TEXT("Text"),
    SHAYARI("Shayari"),
    BACKGROUNDS("Canvas BG"),
    SHAPES("Shapes")
}

enum class InspectorTab(val title: String) {
    STYLE("Style"),
    ARRANGE("Arrange"),
    LAYERS("Layers")
}
