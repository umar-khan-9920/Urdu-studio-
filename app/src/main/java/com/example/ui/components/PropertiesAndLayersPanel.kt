package com.example.ui.components

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
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.AlignHorizontalCenter
import androidx.compose.material.icons.filled.AlignVerticalCenter
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StudioPresets
import com.example.model.InspectorTab
import com.example.model.LayerTextAlign
import com.example.model.ShapeType
import com.example.model.StudioFont
import com.example.model.StudioLayer
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioDanger
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioEmeraldDark
import com.example.ui.theme.StudioEmeraldLight
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioPanel
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import kotlin.math.roundToInt

@Composable
fun PropertiesAndLayersPanel(
    layers: List<StudioLayer>,
    selectedLayer: StudioLayer?,
    activeTab: InspectorTab,
    onSelectTab: (InspectorTab) -> Unit,
    onOpenTextEditor: (String) -> Unit,
    onUpdateTextLayer: ((StudioLayer.TextLayer) -> StudioLayer.TextLayer) -> Unit,
    onUpdateShapeLayer: ((StudioLayer.ShapeLayer) -> StudioLayer.ShapeLayer) -> Unit,
    onUpdateScaleRotation: (String, Float?, Float?) -> Unit,
    onUpdateOpacity: (Float) -> Unit,
    onAlignCenterH: () -> Unit,
    onAlignCenterV: () -> Unit,
    onFlipH: () -> Unit,
    onFlipV: () -> Unit,
    onDuplicate: () -> Unit,
    onDeleteSelected: () -> Unit,
    onSelectLayer: (String) -> Unit,
    onBringForward: (String) -> Unit,
    onSendBackward: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onDeleteLayer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = StudioPanel
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Inspector Tabs: Style / Arrange / Layers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioBg)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                InspectorTab.entries.forEach { tab ->
                    val selected = tab == activeTab
                    val label = if (tab == InspectorTab.LAYERS) "Layers (${layers.size})" else tab.title
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (selected) StudioEmeraldDark else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (selected) StudioEmerald else Color.Transparent,
                                shape = RoundedCornerShape(9.dp)
                            )
                            .clickable { onSelectTab(tab) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) StudioEmeraldLight else StudioTextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (activeTab) {
                    InspectorTab.STYLE -> {
                        if (selectedLayer == null) {
                            EmptySelectionHint()
                        } else {
                            when (selectedLayer) {
                                is StudioLayer.TextLayer -> TextPropertiesSection(
                                    layer = selectedLayer,
                                    onEditText = { onOpenTextEditor(selectedLayer.id) },
                                    onUpdate = onUpdateTextLayer
                                )
                                is StudioLayer.ShapeLayer -> ShapePropertiesSection(
                                    layer = selectedLayer,
                                    onUpdate = onUpdateShapeLayer
                                )
                                is StudioLayer.ImageLayer -> ImagePropertiesSection(selectedLayer)
                            }
                        }
                    }
                    InspectorTab.ARRANGE -> {
                        if (selectedLayer == null) {
                            EmptySelectionHint()
                        } else {
                            ObjectControlsSection(
                                layer = selectedLayer,
                                onUpdateScaleRotation = { sc, rot ->
                                    onUpdateScaleRotation(selectedLayer.id, sc, rot)
                                },
                                onUpdateOpacity = onUpdateOpacity,
                                onAlignCenterH = onAlignCenterH,
                                onAlignCenterV = onAlignCenterV,
                                onFlipH = onFlipH,
                                onFlipV = onFlipV,
                                onDuplicate = onDuplicate,
                                onDelete = onDeleteSelected
                            )
                        }
                    }
                    InspectorTab.LAYERS -> {
                        LayersListSection(
                            layers = layers,
                            selectedLayerId = selectedLayer?.id,
                            onSelectLayer = onSelectLayer,
                            onBringForward = onBringForward,
                            onSendBackward = onSendBackward,
                            onToggleLock = onToggleLock,
                            onToggleVisibility = onToggleVisibility,
                            onDeleteLayer = onDeleteLayer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySelectionHint() {
    Surface(
        color = StudioBg,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Tune,
                contentDescription = null,
                tint = StudioEmeraldLight,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Tap any layer on the canvas to edit its font, colors, shadow, stroke, or alignment.",
                style = MaterialTheme.typography.bodySmall,
                color = StudioTextSecondary
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TextPropertiesSection(
    layer: StudioLayer.TextLayer,
    onEditText: () -> Unit,
    onUpdate: ((StudioLayer.TextLayer) -> StudioLayer.TextLayer) -> Unit
) {
    var fontDropdownOpen by remember { mutableStateOf(false) }

    Button(
        onClick = onEditText,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = StudioEmerald,
            contentColor = Color(0xFF022C22)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_selected_text_button")
    ) {
        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Edit Text & Urdu Keyboard", fontWeight = FontWeight.Bold)
    }

    // Font Family Dropdown
    Column {
        Text("Font Family", style = MaterialTheme.typography.labelMedium, color = StudioTextSecondary)
        Spacer(Modifier.height(4.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudioSurfaceElevated)
                    .border(1.dp, StudioBorder, RoundedCornerShape(10.dp))
                    .clickable { fontDropdownOpen = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("font_family_dropdown"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(layer.font.displayName, color = StudioTextPrimary, fontWeight = FontWeight.Medium)
                    Text(
                        text = layer.font.sampleText,
                        fontFamily = layer.font.fontFamily,
                        fontSize = 14.sp,
                        color = StudioEmeraldLight
                    )
                }
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = StudioTextSecondary)
            }
            DropdownMenu(
                expanded = fontDropdownOpen,
                onDismissRequest = { fontDropdownOpen = false },
                containerColor = StudioPanel
            ) {
                StudioFont.entries.forEach { font ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(font.displayName, color = StudioTextPrimary)
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = font.sampleText,
                                    fontFamily = font.fontFamily,
                                    fontSize = 15.sp,
                                    color = StudioEmeraldLight
                                )
                            }
                        },
                        onClick = {
                            fontDropdownOpen = false
                            onUpdate { it.copy(font = font, isRtl = font.isUrdu) }
                        }
                    )
                }
            }
        }
    }

    // Alignment + RTL
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LayerTextAlign.entries.forEach { align ->
            val icon = when (align) {
                LayerTextAlign.RIGHT -> Icons.AutoMirrored.Filled.FormatAlignRight
                LayerTextAlign.CENTER -> Icons.Filled.FormatAlignCenter
                LayerTextAlign.LEFT -> Icons.AutoMirrored.Filled.FormatAlignLeft
            }
            FilterChip(
                selected = layer.textAlign == align,
                onClick = { onUpdate { it.copy(textAlign = align) } },
                label = { Icon(icon, contentDescription = align.label, modifier = Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StudioEmeraldDark,
                    selectedLabelColor = StudioEmeraldLight,
                    containerColor = StudioSurfaceElevated,
                    labelColor = StudioTextSecondary
                )
            )
        }
        Spacer(Modifier.weight(1f))
        FilterChip(
            selected = layer.isRtl,
            onClick = { onUpdate { it.copy(isRtl = !it.isRtl) } },
            label = { Text(if (layer.isRtl) "RTL" else "LTR", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = StudioEmeraldDark,
                selectedLabelColor = StudioEmeraldLight,
                containerColor = StudioSurfaceElevated,
                labelColor = StudioTextSecondary
            )
        )
    }

    // Sliders: Font Size, Line Height, Text Width
    LabeledSlider(
        label = "Font Size",
        valueLabel = "${layer.fontSize.roundToInt()} px",
        value = layer.fontSize,
        range = 20f..180f,
        onValueChange = { v -> onUpdate { it.copy(fontSize = v) } }
    )
    LabeledSlider(
        label = "Line Height (Nastaliq Spacing)",
        valueLabel = "${"%.2f".format(layer.lineHeightMultiplier)}x",
        value = layer.lineHeightMultiplier,
        range = 1.0f..2.8f,
        onValueChange = { v -> onUpdate { it.copy(lineHeightMultiplier = v) } }
    )
    LabeledSlider(
        label = "Text Box Width",
        valueLabel = "${layer.boxWidth.roundToInt()} px",
        value = layer.boxWidth,
        range = 220f..1100f,
        onValueChange = { v -> onUpdate { it.copy(boxWidth = v) } }
    )

    // Text Color Swatches
    ColorSwatchRow(
        title = "Text Color",
        colors = StudioPresets.solidSwatches,
        selectedColor = layer.colorArgb,
        onSelect = { argb -> onUpdate { it.copy(colorArgb = argb) } }
    )

    // Text Highlight / Background Color
    ColorSwatchRow(
        title = "Highlight Background",
        colors = StudioPresets.highlightSwatches,
        selectedColor = layer.backgroundColorArgb,
        onSelect = { argb -> onUpdate { it.copy(backgroundColorArgb = argb) } }
    )

    // Outline Stroke
    ColorSwatchRow(
        title = "Stroke (Outline) Color",
        colors = StudioPresets.solidSwatches,
        selectedColor = layer.strokeColorArgb,
        onSelect = { argb -> onUpdate { it.copy(strokeColorArgb = argb) } }
    )
    LabeledSlider(
        label = "Stroke Width",
        valueLabel = "${"%.1f".format(layer.strokeWidth)} px",
        value = layer.strokeWidth,
        range = 0f..14f,
        onValueChange = { v -> onUpdate { it.copy(strokeWidth = v) } }
    )

    // Drop Shadow
    ColorSwatchRow(
        title = "Drop Shadow Color",
        colors = listOf(0xCC000000, 0xAA0F172A, 0xAA065F46, 0xAAF59E0B, 0xAA991B1B, 0xAAFFFFFF),
        selectedColor = layer.shadowColorArgb,
        onSelect = { argb -> onUpdate { it.copy(shadowColorArgb = argb) } }
    )
    LabeledSlider(
        label = "Shadow Blur",
        valueLabel = "${layer.shadowBlur.roundToInt()} px",
        value = layer.shadowBlur,
        range = 0f..32f,
        onValueChange = { v -> onUpdate { it.copy(shadowBlur = v) } }
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            LabeledSlider(
                label = "Shadow X",
                valueLabel = "${layer.shadowOffsetX.roundToInt()}",
                value = layer.shadowOffsetX,
                range = -24f..24f,
                onValueChange = { v -> onUpdate { it.copy(shadowOffsetX = v) } }
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            LabeledSlider(
                label = "Shadow Y",
                valueLabel = "${layer.shadowOffsetY.roundToInt()}",
                value = layer.shadowOffsetY,
                range = -24f..24f,
                onValueChange = { v -> onUpdate { it.copy(shadowOffsetY = v) } }
            )
        }
    }
}

@Composable
private fun ShapePropertiesSection(
    layer: StudioLayer.ShapeLayer,
    onUpdate: ((StudioLayer.ShapeLayer) -> StudioLayer.ShapeLayer) -> Unit
) {
    ColorSwatchRow(
        title = "Shape Fill Color",
        colors = listOf(0x00000000L) + StudioPresets.solidSwatches,
        selectedColor = layer.fillColorArgb,
        onSelect = { argb -> onUpdate { it.copy(fillColorArgb = argb) } }
    )
    ColorSwatchRow(
        title = "Shape Border Color",
        colors = StudioPresets.solidSwatches,
        selectedColor = layer.strokeColorArgb,
        onSelect = { argb -> onUpdate { it.copy(strokeColorArgb = argb) } }
    )
    LabeledSlider(
        label = "Border Width",
        valueLabel = "${layer.strokeWidth.roundToInt()} px",
        value = layer.strokeWidth,
        range = 0f..24f,
        onValueChange = { v -> onUpdate { it.copy(strokeWidth = v) } }
    )
    if (layer.shapeType == ShapeType.ROUNDED_RECT) {
        LabeledSlider(
            label = "Corner Radius",
            valueLabel = "${layer.cornerRadius.roundToInt()} px",
            value = layer.cornerRadius,
            range = 0f..120f,
            onValueChange = { v -> onUpdate { it.copy(cornerRadius = v) } }
        )
    }
}

@Composable
private fun ImagePropertiesSection(layer: StudioLayer.ImageLayer) {
    Surface(
        color = StudioBg,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(layer.name, style = MaterialTheme.typography.titleSmall, color = StudioTextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Drag handles on the canvas or switch to the Arrange tab to adjust Opacity, Scale, Rotation, Flip, or Alignment.",
                style = MaterialTheme.typography.bodySmall,
                color = StudioTextSecondary
            )
        }
    }
}

@Composable
private fun ObjectControlsSection(
    layer: StudioLayer,
    onUpdateScaleRotation: (Float?, Float?) -> Unit,
    onUpdateOpacity: (Float) -> Unit,
    onAlignCenterH: () -> Unit,
    onAlignCenterV: () -> Unit,
    onFlipH: () -> Unit,
    onFlipV: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    LabeledSlider(
        label = "Opacity",
        valueLabel = "${(layer.opacity * 100).roundToInt()}%",
        value = layer.opacity,
        range = 0.05f..1f,
        onValueChange = onUpdateOpacity
    )
    LabeledSlider(
        label = "Scale",
        valueLabel = "${(layer.scale * 100).roundToInt()}%",
        value = layer.scale,
        range = 0.25f..3.0f,
        onValueChange = { onUpdateScaleRotation(it, null) }
    )
    LabeledSlider(
        label = "Rotation",
        valueLabel = "${layer.rotation.roundToInt()}°",
        value = layer.rotation,
        range = -180f..180f,
        onValueChange = { onUpdateScaleRotation(null, it) }
    )

    Text("Alignment", style = MaterialTheme.typography.labelMedium, color = StudioTextSecondary)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onAlignCenterH,
            modifier = Modifier.weight(1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            Icon(Icons.Filled.AlignHorizontalCenter, contentDescription = null, tint = StudioEmeraldLight, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Center H", fontSize = 11.sp, color = StudioTextPrimary)
        }
        OutlinedButton(
            onClick = onAlignCenterV,
            modifier = Modifier.weight(1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            Icon(Icons.Filled.AlignVerticalCenter, contentDescription = null, tint = StudioEmeraldLight, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Center V", fontSize = 11.sp, color = StudioTextPrimary)
        }
    }

    Text("Transform & Actions", style = MaterialTheme.typography.labelMedium, color = StudioTextSecondary)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onFlipH,
            modifier = Modifier.weight(1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            Icon(Icons.Filled.Flip, contentDescription = null, tint = StudioTextPrimary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Flip H", fontSize = 11.sp, color = StudioTextPrimary)
        }
        OutlinedButton(
            onClick = onFlipV,
            modifier = Modifier.weight(1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            Icon(Icons.Filled.Flip, contentDescription = null, tint = StudioTextPrimary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Flip V", fontSize = 11.sp, color = StudioTextPrimary)
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onDuplicate,
            modifier = Modifier.weight(1f).testTag("duplicate_layer_button"),
            colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceElevated)
        ) {
            Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = StudioEmeraldLight, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(5.dp))
            Text("Duplicate", fontSize = 12.sp, color = StudioTextPrimary)
        }
        Button(
            onClick = onDelete,
            modifier = Modifier.weight(1f).testTag("delete_layer_button"),
            colors = ButtonDefaults.buttonColors(containerColor = StudioDanger.copy(alpha = 0.2f))
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null, tint = StudioDanger, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(5.dp))
            Text("Delete", fontSize = 12.sp, color = StudioDanger)
        }
    }
}

@Composable
private fun LayersListSection(
    layers: List<StudioLayer>,
    selectedLayerId: String?,
    onSelectLayer: (String) -> Unit,
    onBringForward: (String) -> Unit,
    onSendBackward: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onDeleteLayer: (String) -> Unit
) {
    if (layers.isEmpty()) {
        Surface(
            color = StudioBg,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.Layers, contentDescription = null, tint = StudioTextSecondary)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "No layers yet. Add Urdu text, Shayari, shapes, or images.",
                    style = MaterialTheme.typography.bodySmall,
                    color = StudioTextSecondary
                )
            }
        }
        return
    }

    // Topmost layer shown first
    layers.asReversed().forEach { layer ->
        val selected = layer.id == selectedLayerId
        val icon = when (layer) {
            is StudioLayer.TextLayer -> Icons.Filled.Title
            is StudioLayer.ShapeLayer -> Icons.Filled.Category
            is StudioLayer.ImageLayer -> Icons.Filled.Image
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { onSelectLayer(layer.id) },
            color = if (selected) StudioEmeraldDark.copy(alpha = 0.55f) else StudioSurfaceElevated,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (selected) StudioEmerald else StudioBorder
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) StudioEmeraldLight else StudioTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = layer.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (layer.isVisible) StudioTextPrimary else StudioTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onBringForward(layer.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowUpward,
                            contentDescription = "Bring Forward",
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { onSendBackward(layer.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDownward,
                            contentDescription = "Send Backward",
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { onToggleLock(layer.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (layer.isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                            contentDescription = if (layer.isLocked) "Unlock" else "Lock",
                            tint = if (layer.isLocked) StudioGold else StudioTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { onToggleVisibility(layer.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (layer.isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (layer.isVisible) "Hide" else "Show",
                            tint = if (layer.isVisible) StudioEmeraldLight else StudioTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { onDeleteLayer(layer.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete layer",
                            tint = StudioDanger.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    valueLabel: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = StudioTextSecondary)
            Text(valueLabel, style = MaterialTheme.typography.bodySmall, color = StudioEmeraldLight)
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = StudioEmerald,
                activeTrackColor = StudioEmerald,
                inactiveTrackColor = StudioSurfaceElevated
            ),
            modifier = Modifier.height(30.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorSwatchRow(
    title: String,
    colors: List<Long>,
    selectedColor: Long,
    onSelect: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.bodySmall, color = StudioTextSecondary)
        Spacer(Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            colors.forEach { argb ->
                val isTransparent = android.graphics.Color.alpha(argb.toInt()) == 0
                val isSelected = argb == selectedColor
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isTransparent) StudioBg else Color(argb))
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) StudioEmeraldLight else StudioBorder,
                            shape = CircleShape
                        )
                        .clickable { onSelect(argb) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isTransparent) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "None",
                            tint = StudioDanger,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
