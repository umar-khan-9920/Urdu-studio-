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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardCapslock
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.SubdirectoryArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.StudioFont
import com.example.model.StudioLayer
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
import com.example.urdu.PhoneticUrduKeyboard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UrduTextEditorDialog(
    layer: StudioLayer.TextLayer,
    initialShowKeyboard: Boolean,
    onDismiss: () -> Unit,
    onSave: (newText: String, selectedFont: StudioFont) -> Unit
) {
    var textValue by remember(layer.id) { mutableStateOf(layer.text) }
    var selectedFont by remember(layer.id) { mutableStateOf(layer.font) }
    var phoneticTypingEnabled by remember { mutableStateOf(layer.isRtl) }
    var showOnScreenKeyboard by remember { mutableStateOf(initialShowKeyboard || layer.isRtl) }
    var shiftActive by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            shape = RoundedCornerShape(18.dp),
            color = StudioPanel,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Edit Text & Urdu Keyboard",
                        style = MaterialTheme.typography.titleMedium,
                        color = StudioTextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close editor",
                            tint = StudioTextSecondary
                        )
                    }
                }

                // Font quick chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StudioFont.entries.forEach { font ->
                        val active = font == selectedFont
                        FilterChip(
                            selected = active,
                            onClick = { selectedFont = font },
                            label = {
                                Text(
                                    text = font.displayName,
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StudioEmeraldDark,
                                selectedLabelColor = StudioEmeraldLight,
                                containerColor = StudioSurfaceElevated,
                                labelColor = StudioTextSecondary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Text Input Box
                CompositionLocalProvider(
                    LocalLayoutDirection provides if (selectedFont.isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr
                ) {
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { updated ->
                            textValue = if (phoneticTypingEnabled) {
                                PhoneticUrduKeyboard.transliterateDelta(textValue, updated)
                            } else {
                                updated
                            }
                        },
                        placeholder = {
                            Text(
                                text = if (selectedFont.isUrdu) "یہاں اردو لکھیں..." else "Type your text...",
                                color = StudioTextSecondary
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = selectedFont.fontFamily,
                            fontSize = 22.sp,
                            lineHeight = 36.sp,
                            color = StudioTextPrimary,
                            textAlign = if (selectedFont.isUrdu) TextAlign.Right else TextAlign.Left
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StudioEmerald,
                            unfocusedBorderColor = StudioBorder,
                            focusedContainerColor = StudioBg,
                            unfocusedContainerColor = StudioBg,
                            cursorColor = StudioEmeraldLight
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 160.dp)
                            .testTag("text_editor_input")
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Toggles: Phonetic Auto-Convert & On-Screen Keyboard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = phoneticTypingEnabled,
                            onCheckedChange = { phoneticTypingEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF022C22),
                                checkedTrackColor = StudioEmerald
                            )
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Phonetic (a→ا)",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioTextSecondary
                        )
                    }

                    TextButton(
                        onClick = { showOnScreenKeyboard = !showOnScreenKeyboard },
                        modifier = Modifier.testTag("toggle_urdu_keyboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Keyboard,
                            contentDescription = null,
                            tint = StudioEmeraldLight,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (showOnScreenKeyboard) "Hide Pad" else "Urdu Pad",
                            color = StudioEmeraldLight
                        )
                    }
                }

                // Built-In Phonetic Urdu On-Screen Keyboard
                if (showOnScreenKeyboard) {
                    Spacer(Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StudioBg)
                            .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
                            .padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        // Islamic Ligatures Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            PhoneticUrduKeyboard.quickLigatures.forEach { symbol ->
                                UrduKeyButton(
                                    primary = symbol,
                                    hint = "",
                                    accentColor = StudioGold,
                                    modifier = Modifier.weight(1f),
                                    onClick = { textValue += symbol }
                                )
                            }
                        }

                        val r1 = if (shiftActive) PhoneticUrduKeyboard.shiftRow1 else PhoneticUrduKeyboard.row1
                        val r2 = if (shiftActive) PhoneticUrduKeyboard.shiftRow2 else PhoneticUrduKeyboard.row2

                        KeyboardKeyRow(r1) { textValue += it }
                        KeyboardKeyRow(r2) { textValue += it }

                        if (!shiftActive) {
                            KeyboardKeyRow(PhoneticUrduKeyboard.row3) { textValue += it }
                        }

                        // Control Row: Shift, Space, Newline, Backspace
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (shiftActive) StudioEmerald else StudioSurfaceElevated)
                                    .clickable { shiftActive = !shiftActive }
                                    .testTag("urdu_kb_shift"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.KeyboardCapslock,
                                    contentDescription = "Shift / Diacritics",
                                    tint = if (shiftActive) Color(0xFF022C22) else StudioTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(3f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StudioSurfaceElevated)
                                    .clickable { textValue += " " }
                                    .testTag("urdu_kb_space"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.SpaceBar,
                                        contentDescription = "Space",
                                        tint = StudioTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Space", fontSize = 11.sp, color = StudioTextSecondary)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StudioSurfaceElevated)
                                    .clickable { textValue += "\n" },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SubdirectoryArrowLeft,
                                    contentDescription = "New line",
                                    tint = StudioTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StudioSurfaceElevated)
                                    .clickable {
                                        if (textValue.isNotEmpty()) {
                                            textValue = textValue.dropLast(1)
                                        }
                                    }
                                    .testTag("urdu_kb_backspace"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = StudioTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Apply / Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = StudioTextSecondary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(textValue, selectedFont) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StudioEmerald,
                            contentColor = Color(0xFF022C22)
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("apply_text_edit_button")
                    ) {
                        Text("Apply to Canvas", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyboardKeyRow(
    keys: List<PhoneticUrduKeyboard.KeyCap>,
    onKey: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        keys.forEach { key ->
            UrduKeyButton(
                primary = key.urdu,
                hint = key.hint,
                modifier = Modifier.weight(1f),
                onClick = { onKey(key.urdu) }
            )
        }
    }
}

@Composable
private fun UrduKeyButton(
    primary: String,
    hint: String,
    modifier: Modifier = Modifier,
    accentColor: Color = StudioTextPrimary,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(StudioSurfaceElevated)
            .border(0.5.dp, StudioBorder, RoundedCornerShape(7.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = primary,
                fontSize = 15.sp,
                color = accentColor,
                lineHeight = 18.sp
            )
            if (hint.isNotEmpty()) {
                Text(
                    text = hint,
                    fontSize = 8.sp,
                    color = StudioTextSecondary,
                    lineHeight = 9.sp
                )
            }
        }
    }
}
