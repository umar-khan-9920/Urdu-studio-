package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.model.CanvasPreset
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioDanger
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioEmeraldDark
import com.example.ui.theme.StudioEmeraldLight
import com.example.ui.theme.StudioPanel
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

@Composable
fun StudioTopBar(
    currentPreset: CanvasPreset,
    canUndo: Boolean,
    canRedo: Boolean,
    isExporting: Boolean,
    onSelectPreset: (CanvasPreset) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClearCanvas: () -> Unit,
    onSaveJson: () -> Unit,
    onLoadJson: () -> Unit,
    onExportPng: () -> Unit,
    modifier: Modifier = Modifier
) {
    var presetMenuOpen by remember { mutableStateOf(false) }
    var moreMenuOpen by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = StudioPanel,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand + Preset Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(StudioEmeraldDark)
                            .border(1.dp, StudioEmerald, RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Urdu Studio",
                            tint = StudioEmeraldLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(StudioSurfaceElevated)
                                .border(1.dp, StudioBorder, RoundedCornerShape(10.dp))
                                .clickable { presetMenuOpen = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("preset_selector_button"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AspectRatio,
                                contentDescription = "Canvas size",
                                tint = StudioEmeraldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = currentPreset.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = StudioTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${currentPreset.shortLabel} (${currentPreset.aspectLabel})",
                                    fontSize = 10.sp,
                                    color = StudioTextSecondary
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Select preset",
                                tint = StudioTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = presetMenuOpen,
                            onDismissRequest = { presetMenuOpen = false },
                            containerColor = StudioPanel
                        ) {
                            CanvasPreset.entries.forEach { preset ->
                                val selected = preset == currentPreset
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = preset.displayName,
                                                    color = if (selected) StudioEmeraldLight else StudioTextPrimary,
                                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                                Text(
                                                    text = "${preset.shortLabel} • ${preset.aspectLabel}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = StudioTextSecondary
                                                )
                                            }
                                            if (selected) {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = StudioEmeraldLight,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        presetMenuOpen = false
                                        onSelectPreset(preset)
                                    }
                                )
                            }
                        }
                    }
                }

                // Undo / Redo + More + Export
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onUndo,
                        enabled = canUndo,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (canUndo) StudioTextPrimary else StudioTextSecondary.copy(alpha = 0.35f)
                        )
                    }
                    IconButton(
                        onClick = onRedo,
                        enabled = canRedo,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("redo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (canRedo) StudioTextPrimary else StudioTextSecondary.copy(alpha = 0.35f)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { moreMenuOpen = true },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("more_actions_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Project actions",
                                tint = StudioTextPrimary
                            )
                        }
                        DropdownMenu(
                            expanded = moreMenuOpen,
                            onDismissRequest = { moreMenuOpen = false },
                            containerColor = StudioPanel
                        ) {
                            DropdownMenuItem(
                                text = { Text("Save Project (JSON)", color = StudioTextPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Save, contentDescription = null, tint = StudioEmeraldLight)
                                },
                                onClick = {
                                    moreMenuOpen = false
                                    onSaveJson()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Open Project (JSON)", color = StudioTextPrimary) },
                                leadingIcon = {
                                    Icon(Icons.Filled.FolderOpen, contentDescription = null, tint = StudioEmeraldLight)
                                },
                                onClick = {
                                    moreMenuOpen = false
                                    onLoadJson()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear Canvas", color = StudioDanger) },
                                leadingIcon = {
                                    Icon(Icons.Filled.DeleteSweep, contentDescription = null, tint = StudioDanger)
                                },
                                onClick = {
                                    moreMenuOpen = false
                                    showClearConfirm = true
                                }
                            )
                        }
                    }

                    Spacer(Modifier.width(4.dp))

                    Button(
                        onClick = onExportPng,
                        enabled = !isExporting,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioEmerald,
                            contentColor = Color(0xFF022C22)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("export_png_button")
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF022C22)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "Export 2x",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(StudioBorder)
            )
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = StudioPanel,
            title = { Text("Clear Canvas?", color = StudioTextPrimary) },
            text = {
                Text(
                    "Remove all layers from the current canvas? You can undo this immediately if needed.",
                    color = StudioTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        onClearCanvas()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudioDanger)
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = StudioTextSecondary)
                }
            }
        )
    }
}
