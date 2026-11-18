package com.example.data

import com.example.model.CanvasBackground
import com.example.model.CanvasDocument
import com.example.model.CanvasPreset
import com.example.model.LayerTextAlign
import com.example.model.ShapeType
import com.example.model.StudioFont
import com.example.model.StudioLayer

data class ShayariTemplate(
    val id: String,
    val category: String,
    val title: String,
    val text: String,
    val font: StudioFont,
    val defaultColorArgb: Long,
    val fontSize: Float = 58f
)

object StudioPresets {

    val solidSwatches: List<Long> = listOf(
        0xFF0F172A, // Deep Slate
        0xFF020617, // Obsidian
        0xFF064E3B, // Deep Emerald
        0xFF10B981, // Vivid Emerald
        0xFF1E3A8A, // Royal Blue
        0xFF311042, // Royal Plum
        0xFF7C2D12, // Terracotta
        0xFF991B1B, // Crimson
        0xFFF59E0B, // Amber Gold
        0xFFFDE68A, // Champagne Gold
        0xFFF8FAFC, // Pure Pearl
        0xFFFEF3C7, // Warm Parchment
        0xFF000000  // Jet Black
    )

    val highlightSwatches: List<Long> = listOf(
        0x00000000, // Transparent
        0xCC0F172A,
        0xCC065F46,
        0xCC991B1B,
        0xCCF59E0B,
        0xCCFFFFFF,
        0xCC000000
    )

    val luxuryGradients: List<CanvasBackground.Gradient> = listOf(
        CanvasBackground.Gradient(
            id = "emerald_royal",
            name = "Emerald Royal",
            colorsArgb = listOf(0xFF022C22, 0xFF065F46, 0xFF0F172A),
            angleDegrees = 135f
        ),
        CanvasBackground.Gradient(
            id = "midnight_gold",
            name = "Midnight Gold",
            colorsArgb = listOf(0xFF090D16, 0xFF1E293B, 0xFF78350F),
            angleDegrees = 145f
        ),
        CanvasBackground.Gradient(
            id = "obsidian_slate",
            name = "Obsidian Slate",
            colorsArgb = listOf(0xFF020617, 0xFF0F172A, 0xFF1E293B),
            angleDegrees = 120f
        ),
        CanvasBackground.Gradient(
            id = "masjid_teal",
            name = "Masjid Teal",
            colorsArgb = listOf(0xFF042F2E, 0xFF0F766E, 0xFF134E4A),
            angleDegrees = 135f
        ),
        CanvasBackground.Gradient(
            id = "royal_velvet",
            name = "Royal Velvet",
            colorsArgb = listOf(0xFF1E1B4B, 0xFF4C1D95, 0xFF311042),
            angleDegrees = 135f
        ),
        CanvasBackground.Gradient(
            id = "sunset_saffron",
            name = "Sunset Saffron",
            colorsArgb = listOf(0xFF450A0A, 0xFF9A3412, 0xFFD97706),
            angleDegrees = 135f
        ),
        CanvasBackground.Gradient(
            id = "deep_sapphire",
            name = "Deep Sapphire",
            colorsArgb = listOf(0xFF082F49, 0xFF1E3A8A, 0xFF0F172A),
            angleDegrees = 150f
        ),
        CanvasBackground.Gradient(
            id = "damascus_copper",
            name = "Damascus Copper",
            colorsArgb = listOf(0xFF1C1917, 0xFF44403C, 0xFFB45309),
            angleDegrees = 135f
        ),
        CanvasBackground.Gradient(
            id = "northern_aurora",
            name = "Northern Aurora",
            colorsArgb = listOf(0xFF064E3B, 0xFF0284C7, 0xFF1E1B4B),
            angleDegrees = 125f
        ),
        CanvasBackground.Gradient(
            id = "pearl_cream",
            name = "Pearl Cream",
            colorsArgb = listOf(0xFFFFFBEB, 0xFFFEF3C7, 0xFFFDE68A),
            angleDegrees = 135f
        ),
        CanvasBackground.Gradient(
            id = "desert_rose",
            name = "Desert Rose",
            colorsArgb = listOf(0xFF4C0519, 0xFF9F1239, 0xFFFB7185),
            angleDegrees = 135f
        ),
        CanvasBackground.Gradient(
            id = "charcoal_bronze",
            name = "Charcoal Bronze",
            colorsArgb = listOf(0xFF111827, 0xFF374151, 0xFF92400E),
            angleDegrees = 135f
        )
    )

    val shayariTemplates: List<ShayariTemplate> = listOf(
        ShayariTemplate(
            id = "iqbal_khudi",
            category = "Shayari",
            title = "Allama Iqbal — Khudi",
            text = "خودی کو کر بلند اتنا کہ ہر تقدیر سے پہلے\nخدا بندے سے خود پوچھے بتا تیری رضا کیا ہے",
            font = StudioFont.NOTO_NASTALIQ_URDU,
            defaultColorArgb = 0xFFFDE68A,
            fontSize = 52f
        ),
        ShayariTemplate(
            id = "iqbal_sitaron",
            category = "Shayari",
            title = "Allama Iqbal — Sitaron Se Aage",
            text = "ستاروں سے آگے جہاں اور بھی ہیں\nابھی عشق کے امتحاں اور بھی ہیں",
            font = StudioFont.NOTO_NASTALIQ_URDU,
            defaultColorArgb = 0xFFFFFFFF,
            fontSize = 56f
        ),
        ShayariTemplate(
            id = "ghalib_khwahish",
            category = "Shayari",
            title = "Mirza Ghalib",
            text = "ہزاروں خواہشیں ایسی کہ ہر خواہش پہ دم نکلے\nبہت نکلے مرے ارمان لیکن پھر بھی کم نکلے",
            font = StudioFont.GULZAR,
            defaultColorArgb = 0xFFF8FAFC,
            fontSize = 54f
        ),
        ShayariTemplate(
            id = "faiz_dil",
            category = "Shayari",
            title = "Faiz Ahmed Faiz",
            text = "دل ناامید تو نہیں ناکام ہی تو ہے\nلمبی ہے غم کی شام مگر شام ہی تو ہے",
            font = StudioFont.NOTO_NASTALIQ_URDU,
            defaultColorArgb = 0xFFA7F3D0,
            fontSize = 54f
        ),
        ShayariTemplate(
            id = "bismillah",
            category = "Calligraphy",
            title = "Bismillah Calligraphy",
            text = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ",
            font = StudioFont.AMIRI,
            defaultColorArgb = 0xFFF59E0B,
            fontSize = 72f
        ),
        ShayariTemplate(
            id = "mashallah",
            category = "Calligraphy",
            title = "MashaAllah",
            text = "مَاشَاءَ اللّٰهُ لَا قُوَّةَ إِلَّا بِاللّٰهِ",
            font = StudioFont.AREF_RUQAA,
            defaultColorArgb = 0xFFFDE68A,
            fontSize = 68f
        ),
        ShayariTemplate(
            id = "alhamdulillah",
            category = "Calligraphy",
            title = "Alhamdulillah",
            text = "اَلْحَمْدُ لِلّٰهِ رَبِّ الْعَالَمِيْنَ",
            font = StudioFont.AMIRI,
            defaultColorArgb = 0xFFFFFFFF,
            fontSize = 68f
        ),
        ShayariTemplate(
            id = "durood",
            category = "Calligraphy",
            title = "Durood Sharif",
            text = "صَلَّى اللّٰهُ عَلَيْهِ وَسَلَّمَ",
            font = StudioFont.AREF_RUQAA,
            defaultColorArgb = 0xFF34D399,
            fontSize = 70f
        ),
        ShayariTemplate(
            id = "jummah",
            category = "Greetings",
            title = "Jummah Mubarak",
            text = "جمعہ مبارک\nاللہ آپ کی زندگی میں برکت عطا فرمائے",
            font = StudioFont.NOTO_NASTALIQ_URDU,
            defaultColorArgb = 0xFFFDE68A,
            fontSize = 56f
        ),
        ShayariTemplate(
            id = "ramadan",
            category = "Greetings",
            title = "Ramadan Kareem",
            text = "رمضان کریم\nرحمتوں اور برکتوں کا مہینہ",
            font = StudioFont.GULZAR,
            defaultColorArgb = 0xFFFFFFFF,
            fontSize = 60f
        ),
        ShayariTemplate(
            id = "promo_sale",
            category = "Promo",
            title = "Mega Sale Heading",
            text = "شاندار میگا سیل\n۵۰٪ تک کی خصوصی رعایت!",
            font = StudioFont.NOTO_NASTALIQ_URDU,
            defaultColorArgb = 0xFFFBBF24,
            fontSize = 64f
        ),
        ShayariTemplate(
            id = "promo_admission",
            category = "Promo",
            title = "Admissions Open Banner",
            text = "فوری داخلے جاری ہیں — آج ہی رابطہ کریں",
            font = StudioFont.LATEEF,
            defaultColorArgb = 0xFFFFFFFF,
            fontSize = 64f
        ),
        ShayariTemplate(
            id = "yt_hook",
            category = "Promo",
            title = "YouTube Thumbnail Hook",
            text = "بڑی خبر! مکمل تفصیل اس ویڈیو میں",
            font = StudioFont.NOTO_NASTALIQ_URDU,
            defaultColorArgb = 0xFFFDE047,
            fontSize = 66f
        )
    )

    fun createStarterDocument(): CanvasDocument {
        val w = CanvasPreset.INSTAGRAM_POST.width.toFloat()
        val h = CanvasPreset.INSTAGRAM_POST.height.toFloat()
        return CanvasDocument(
            preset = CanvasPreset.INSTAGRAM_POST,
            background = luxuryGradients.first(),
            layers = listOf(
                StudioLayer.ShapeLayer(
                    name = "Ornate Frame",
                    x = w / 2f,
                    y = h / 2f,
                    shapeType = ShapeType.ORNATE_BORDER,
                    baseWidth = 960f,
                    baseHeight = 960f,
                    fillColorArgb = 0x00000000,
                    strokeColorArgb = 0xFFF59E0B,
                    strokeWidth = 6f,
                    shadowBlur = 0f,
                    shadowOffsetY = 0f
                ),
                StudioLayer.TextLayer(
                    name = "Calligraphy Header",
                    x = w / 2f,
                    y = 260f,
                    text = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ",
                    font = StudioFont.AMIRI,
                    fontSize = 54f,
                    lineHeightMultiplier = 1.6f,
                    colorArgb = 0xFFFDE68A,
                    isRtl = true,
                    textAlign = LayerTextAlign.CENTER,
                    boxWidth = 820f
                ),
                StudioLayer.TextLayer(
                    name = "Main Urdu Title",
                    x = w / 2f,
                    y = 540f,
                    text = "اردو ڈیزائنر اسٹوڈیو",
                    font = StudioFont.NOTO_NASTALIQ_URDU,
                    fontSize = 84f,
                    lineHeightMultiplier = 1.65f,
                    colorArgb = 0xFFFFFFFF,
                    strokeColorArgb = 0xFF022C22,
                    strokeWidth = 3f,
                    shadowColorArgb = 0xBB000000,
                    shadowBlur = 16f,
                    shadowOffsetY = 8f,
                    isRtl = true,
                    textAlign = LayerTextAlign.CENTER,
                    boxWidth = 860f
                ),
                StudioLayer.TextLayer(
                    name = "Subtitle",
                    x = w / 2f,
                    y = 770f,
                    text = "Create Bold Social Posts & Thumbnails",
                    font = StudioFont.POPPINS,
                    fontSize = 30f,
                    lineHeightMultiplier = 1.4f,
                    colorArgb = 0xFF34D399,
                    isRtl = false,
                    textAlign = LayerTextAlign.CENTER,
                    boxWidth = 760f
                )
            )
        )
    }
}
