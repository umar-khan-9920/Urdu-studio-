package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ShayariTemplate
import com.example.data.StudioPresets
import com.example.model.AssetTab
import com.example.model.CanvasBackground
import com.example.model.ShapeType
import com.example.render.CanvasRenderer
import com.example.ui.theme.NotoNastaliqUrduFontFamily
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioEmeraldDark
import com.example.ui.theme.StudioEmeraldLight
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioPanel
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AssetDrawerPanel(
    activeTab: AssetTab,
    onSelectTab: (AssetTab) -> Unit,
    onAddUrduText: () -> Unit,
    onAddEnglishText: () -> Unit,
    onOpenUrduKeyboard: () -> Unit,
    onAddShayari: (ShayariTemplate) -> Unit,
    onSelectSolidBg: (Long) -> Unit,
    onSelectGradientBg: (CanvasBackground.Gradient) -> Unit,
    onPickBackgroundImage: () -> Unit,
    onAddShape: (ShapeType) -> Unit,
    onPickStickerImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = StudioPanel
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Asset Tab Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioBg)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AssetTabItem(
                    label = "Text",
                    icon = Icons.Filled.Title,
                    selected = activeTab == AssetTab.TEXT,
                    onClick = { onSelectTab(AssetTab.TEXT) },
                    modifier = Modifier.weight(1f).testTag("asset_tab_text")
                )
                AssetTabItem(
                    label = "Shayari",
                    icon = Icons.Filled.FormatQuote,
                    selected = activeTab == AssetTab.SHAYARI,
                    onClick = { onSelectTab(AssetTab.SHAYARI) },
                    modifier = Modifier.weight(1f).testTag("asset_tab_shayari")
                )
                AssetTabItem(
                    label = "Canvas BG",
                    icon = Icons.Filled.Palette,
                    selected = activeTab == AssetTab.BACKGROUNDS,
                    onClick = { onSelectTab(AssetTab.BACKGROUNDS) },
                    modifier = Modifier.weight(1f).testTag("asset_tab_bg")
                )
                AssetTabItem(
                    label = "Shapes",
                    icon = Icons.Filled.AutoAwesome,
                    selected = activeTab == AssetTab.SHAPES,
                    onClick = { onSelectTab(AssetTab.SHAPES) },
                    modifier = Modifier.weight(1f).testTag("asset_tab_shapes")
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (activeTab) {
                    AssetTab.TEXT -> TextAssetSection(
                        onAddUrduText = onAddUrduText,
                        onAddEnglishText = onAddEnglishText,
                        onOpenUrduKeyboard = onOpenUrduKeyboard
                    )
                    AssetTab.SHAYARI -> ShayariAssetSection(
                        onAddShayari = onAddShayari
                    )
                    AssetTab.BACKGROUNDS -> BackgroundsAssetSection(
                        onSelectSolid = onSelectSolidBg,
                        onSelectGradient = onSelectGradientBg,
                        onPickBackgroundImage = onPickBackgroundImage
                    )
                    AssetTab.SHAPES -> ShapesAssetSection(
                        onAddShape = onAddShape,
                        onPickStickerImage = onPickStickerImage
                    )
                }
            }
        }
    }
}

@Composable
private fun AssetTabItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) StudioEmeraldDark else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) StudioEmerald else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) StudioEmeraldLight else StudioTextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) StudioEmeraldLight else StudioTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TextAssetSection(
    onAddUrduText: () -> Unit,
    onAddEnglishText: () -> Unit,
    onOpenUrduKeyboard: () -> Unit
) {
    Button(
        onClick = onAddUrduText,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = StudioEmerald,
            contentColor = Color(0xFF022C22)
        ),
        contentPadding = PaddingValues(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("add_urdu_text_button")
    ) {
        Icon(Icons.Filled.TextFields, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Add Urdu Text (RTL)", fontWeight = FontWeight.Bold)
            Text(
                text = "اردو ڈیزائنر اسٹوڈیو",
                fontFamily = NotoNastaliqUrduFontFamily,
                fontSize = 13.sp
            )
        }
    }

    OutlinedButton(
        onClick = onAddEnglishText,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
        contentPadding = PaddingValues(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("add_english_text_button")
    ) {
        Icon(Icons.Filled.Title, contentDescription = null, tint = StudioTextPrimary)
        Spacer(Modifier.width(8.dp))
        Text("Add English Text (LTR)", color = StudioTextPrimary)
    }

    OutlinedButton(
        onClick = onOpenUrduKeyboard,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioEmerald.copy(alpha = 0.6f)),
        contentPadding = PaddingValues(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("open_urdu_keyboard_button")
    ) {
        Icon(Icons.Filled.Keyboard, contentDescription = null, tint = StudioEmeraldLight)
        Spacer(Modifier.width(8.dp))
        Text("Open Phonetic Urdu Keyboard", color = StudioEmeraldLight)
    }

    Surface(
        color = StudioBg,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
    ) {
        Text(
            text = "Tip: Double-tap any text on the canvas anytime to edit with the built-in Phonetic Urdu Keyboard. Nastaliq lineHeight defaults to 1.6x with generous padding to prevent ascender/descender clipping.",
            style = MaterialTheme.typography.bodySmall,
            color = StudioTextSecondary,
            modifier = Modifier.padding(10.dp)
        )
    }
}

@Composable
private fun ShayariAssetSection(
    onAddShayari: (ShayariTemplate) -> Unit
) {
    Text(
        text = "Tap any Shayari, Calligraphy, or Banner Heading to add:",
        style = MaterialTheme.typography.bodySmall,
        color = StudioTextSecondary
    )
    StudioPresets.shayariTemplates.forEach { item ->
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onAddShayari(item) },
            color = StudioSurfaceElevated,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = StudioEmeraldLight
                    )
                    Surface(
                        color = StudioBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.category,
                            fontSize = 10.sp,
                            color = StudioGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.text,
                    fontFamily = item.font.fontFamily,
                    fontSize = 16.sp,
                    lineHeight = 28.sp,
                    color = StudioTextPrimary,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BackgroundsAssetSection(
    onSelectSolid: (Long) -> Unit,
    onSelectGradient: (CanvasBackground.Gradient) -> Unit,
    onPickBackgroundImage: () -> Unit
) {
    Button(
        onClick = onPickBackgroundImage,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = StudioSurfaceElevated,
            contentColor = StudioEmeraldLight
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioEmerald.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("upload_bg_image_button")
    ) {
        Icon(Icons.Filled.Wallpaper, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Upload Custom Background Image")
    }

    Text(
        text = "Luxury Gradients (12)",
        style = MaterialTheme.typography.titleSmall,
        color = StudioTextPrimary
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 3,
        modifier = Modifier.fillMaxWidth()
    ) {
        StudioPresets.luxuryGradients.forEach { grad ->
            Column(
                modifier = Modifier
                    .width(92.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudioBg)
                    .border(1.dp, StudioBorder, RoundedCornerShape(10.dp))
                    .clickable { onSelectGradient(grad) }
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(7.dp))
                ) {
                    val rad = Math.toRadians(grad.angleDegrees.toDouble())
                    val dx = cos(rad).toFloat() * size.width * 0.5f
                    val dy = sin(rad).toFloat() * size.height * 0.5f
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = grad.colorsArgb.map { Color(it) },
                            start = Offset(cx - dx, cy - dy),
                            end = Offset(cx + dx, cy + dy)
                        )
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = grad.name,
                    fontSize = 10.sp,
                    color = StudioTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    Text(
        text = "Solid Colors",
        style = MaterialTheme.typography.titleSmall,
        color = StudioTextPrimary
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StudioPresets.solidSwatches.forEach { argb ->
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(argb))
                    .border(1.5.dp, StudioBorder, CircleShape)
                    .clickable { onSelectSolid(argb) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShapesAssetSection(
    onAddShape: (ShapeType) -> Unit,
    onPickStickerImage: () -> Unit
) {
    Button(
        onClick = onPickStickerImage,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = StudioSurfaceElevated,
            contentColor = StudioEmeraldLight
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioEmerald.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("upload_sticker_image_button")
    ) {
        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Upload Image / Logo (PNG/JPG)")
    }

    Text(
        text = "Vector Shapes & Badges",
        style = MaterialTheme.typography.titleSmall,
        color = StudioTextPrimary
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = 2,
        modifier = Modifier.fillMaxWidth()
    ) {
        ShapeType.entries.forEach { shape ->
            Surface(
                modifier = Modifier
                    .width(132.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onAddShape(shape) },
                color = StudioSurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(30.dp)) {
                        val path = CanvasRenderer.buildComposeShapePath(
                            shapeType = shape,
                            width = size.width * 0.85f,
                            height = size.height * 0.72f,
                            cornerRadius = 6f
                        )
                        path.translate(Offset(size.width / 2f, size.height / 2f))
                        if (shape != ShapeType.ORNATE_BORDER) {
                            drawPath(path, color = StudioEmerald)
                        }
                        drawPath(path, color = StudioGold, style = Stroke(width = 2.5f))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = shape.displayName,
                        fontSize = 11.sp,
                        color = StudioTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
