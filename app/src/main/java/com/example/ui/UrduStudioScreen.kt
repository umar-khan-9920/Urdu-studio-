package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AssetTab
import com.example.model.InspectorTab
import com.example.model.StudioLayer
import com.example.ui.components.AssetDrawerPanel
import com.example.ui.components.InteractiveCanvasStage
import com.example.ui.components.PropertiesAndLayersPanel
import com.example.ui.components.StudioTopBar
import com.example.ui.components.UrduTextEditorDialog
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioEmeraldDark
import com.example.ui.theme.StudioEmeraldLight
import com.example.ui.theme.StudioPanel
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.viewmodel.StudioViewModel

private enum class MobileSheetMode {
    NONE, ASSETS, INSPECTOR
}

@Composable
fun UrduStudioScreen(
    viewModel: StudioViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var mobileSheetMode by remember { mutableStateOf(MobileSheetMode.ASSETS) }

    // Android Photo Picker for Background Image
    val bgImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.importBackgroundImage(uri)
    }

    // Android Photo Picker for Sticker / Logo Image
    val stickerImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.importStickerImage(uri)
    }

    // Save Project JSON (SAF)
    val saveJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) viewModel.saveProjectToUri(uri)
    }

    // Open Project JSON (SAF)
    val loadJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.loadProjectFromUri(uri)
    }

    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Back collapses mobile bottom sheet before exiting
    BackHandler(enabled = mobileSheetMode != MobileSheetMode.NONE) {
        mobileSheetMode = MobileSheetMode.NONE
    }

    Scaffold(
        containerColor = StudioBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StudioTopBar(
                currentPreset = state.document.preset,
                canUndo = state.canUndo,
                canRedo = state.canRedo,
                isExporting = state.isExporting,
                onSelectPreset = viewModel::setPreset,
                onUndo = viewModel::undo,
                onRedo = viewModel::redo,
                onClearCanvas = viewModel::clearCanvas,
                onSaveJson = { saveJsonLauncher.launch("UrduStudio_Project.json") },
                onLoadJson = { loadJsonLauncher.launch(arrayOf("application/json", "*/*")) },
                onExportPng = viewModel::exportHighResPng
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isExpandedLayout = maxWidth >= 840.dp

            if (isExpandedLayout) {
                // Tablet / Wide Studio 3-Pane Layout
                Row(modifier = Modifier.fillMaxSize()) {
                    AssetDrawerPanel(
                        activeTab = state.activeAssetTab,
                        onSelectTab = viewModel::setAssetTab,
                        onAddUrduText = { viewModel.addUrduText() },
                        onAddEnglishText = { viewModel.addEnglishText() },
                        onOpenUrduKeyboard = viewModel::openUrduKeyboardModal,
                        onAddShayari = viewModel::addShayariTemplate,
                        onSelectSolidBg = viewModel::setSolidBackground,
                        onSelectGradientBg = viewModel::setGradientBackground,
                        onPickBackgroundImage = {
                            bgImagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onAddShape = viewModel::addShape,
                        onPickStickerImage = {
                            stickerImagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .width(290.dp)
                            .fillMaxHeight()
                            .border(width = 1.dp, color = StudioBorder)
                    )

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        InteractiveCanvasStage(
                            document = state.document,
                            selectedLayerId = state.selectedLayerId,
                            isSnappedX = state.isSnappedX,
                            isSnappedY = state.isSnappedY,
                            getBitmap = viewModel::getCachedBitmap,
                            onSelectLayer = viewModel::selectLayer,
                            onBeginTransform = viewModel::beginTransformGesture,
                            onMoveLayer = viewModel::updateLayerPosition,
                            onScaleRotateLayer = viewModel::updateLayerScaleAndRotation,
                            onEndTransform = viewModel::endTransformGesture,
                            onDoubleTapText = viewModel::openTextEditor
                        )
                    }

                    PropertiesAndLayersPanel(
                        layers = state.document.layers,
                        selectedLayer = state.selectedLayer,
                        activeTab = state.activeInspectorTab,
                        onSelectTab = viewModel::setInspectorTab,
                        onOpenTextEditor = viewModel::openTextEditor,
                        onUpdateTextLayer = viewModel::updateSelectedTextLayer,
                        onUpdateShapeLayer = viewModel::updateSelectedShapeLayer,
                        onUpdateScaleRotation = viewModel::updateLayerScaleAndRotation,
                        onUpdateOpacity = viewModel::updateSelectedOpacity,
                        onAlignCenterH = viewModel::alignSelectedCenterHorizontally,
                        onAlignCenterV = viewModel::alignSelectedCenterVertically,
                        onFlipH = viewModel::flipSelectedHorizontal,
                        onFlipV = viewModel::flipSelectedVertical,
                        onDuplicate = viewModel::duplicateSelected,
                        onDeleteSelected = {
                            state.selectedLayerId?.let(viewModel::deleteLayer)
                        },
                        onSelectLayer = viewModel::selectLayer,
                        onBringForward = viewModel::bringForward,
                        onSendBackward = viewModel::sendBackward,
                        onToggleLock = viewModel::toggleLock,
                        onToggleVisibility = viewModel::toggleVisibility,
                        onDeleteLayer = viewModel::deleteLayer,
                        modifier = Modifier
                            .width(310.dp)
                            .fillMaxHeight()
                            .border(width = 1.dp, color = StudioBorder)
                    )
                }
            } else {
                // Compact Mobile Studio Layout: Center Canvas + Floating Quick Action Pill + Collapsible Dock
                Column(modifier = Modifier.fillMaxSize()) {
                    // Export Success Quick Share Banner
                    val exportResult = state.lastExportResult
                    AnimatedVisibility(visible = exportResult != null) {
                        if (exportResult != null) {
                            Surface(
                                color = StudioEmeraldDark,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Exported ${exportResult.widthPx}×${exportResult.heightPx} PNG",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StudioEmeraldLight,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (exportResult.uri != null) {
                                        TextButton(
                                            onClick = {
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "image/png"
                                                    putExtra(Intent.EXTRA_STREAM, exportResult.uri)
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(
                                                    Intent.createChooser(shareIntent, "Share Urdu Studio PNG")
                                                )
                                            }
                                        ) {
                                            Icon(
                                                Icons.Filled.Share,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text("Share", color = Color.White)
                                        }
                                    }
                                    IconButton(
                                        onClick = viewModel::dismissExportBanner,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Dismiss",
                                            tint = StudioEmeraldLight,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Interactive Canvas
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        InteractiveCanvasStage(
                            document = state.document,
                            selectedLayerId = state.selectedLayerId,
                            isSnappedX = state.isSnappedX,
                            isSnappedY = state.isSnappedY,
                            getBitmap = viewModel::getCachedBitmap,
                            onSelectLayer = { id ->
                                viewModel.selectLayer(id)
                            },
                            onBeginTransform = viewModel::beginTransformGesture,
                            onMoveLayer = viewModel::updateLayerPosition,
                            onScaleRotateLayer = viewModel::updateLayerScaleAndRotation,
                            onEndTransform = viewModel::endTransformGesture,
                            onDoubleTapText = viewModel::openTextEditor
                        )

                        // Quick Edit Floating Pill when Text Layer is Selected
                        val sel = state.selectedLayer
                        if (sel is StudioLayer.TextLayer) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = StudioPanel.copy(alpha = 0.94f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StudioEmerald),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp)
                                    .clickable { viewModel.openTextEditor(sel.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = null,
                                        tint = StudioEmeraldLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Edit Text / Urdu Keyboard",
                                        fontSize = 11.sp,
                                        color = StudioTextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Expandable Bottom Drawer Panel
                    AnimatedVisibility(visible = mobileSheetMode != MobileSheetMode.NONE) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 210.dp, max = 300.dp)
                                .border(width = 1.dp, color = StudioBorder)
                        ) {
                            when (mobileSheetMode) {
                                MobileSheetMode.ASSETS -> AssetDrawerPanel(
                                    activeTab = state.activeAssetTab,
                                    onSelectTab = viewModel::setAssetTab,
                                    onAddUrduText = { viewModel.addUrduText() },
                                    onAddEnglishText = { viewModel.addEnglishText() },
                                    onOpenUrduKeyboard = viewModel::openUrduKeyboardModal,
                                    onAddShayari = viewModel::addShayariTemplate,
                                    onSelectSolidBg = viewModel::setSolidBackground,
                                    onSelectGradientBg = viewModel::setGradientBackground,
                                    onPickBackgroundImage = {
                                        bgImagePicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onAddShape = viewModel::addShape,
                                    onPickStickerImage = {
                                        stickerImagePicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                )
                                MobileSheetMode.INSPECTOR -> PropertiesAndLayersPanel(
                                    layers = state.document.layers,
                                    selectedLayer = state.selectedLayer,
                                    activeTab = state.activeInspectorTab,
                                    onSelectTab = viewModel::setInspectorTab,
                                    onOpenTextEditor = viewModel::openTextEditor,
                                    onUpdateTextLayer = viewModel::updateSelectedTextLayer,
                                    onUpdateShapeLayer = viewModel::updateSelectedShapeLayer,
                                    onUpdateScaleRotation = viewModel::updateLayerScaleAndRotation,
                                    onUpdateOpacity = viewModel::updateSelectedOpacity,
                                    onAlignCenterH = viewModel::alignSelectedCenterHorizontally,
                                    onAlignCenterV = viewModel::alignSelectedCenterVertically,
                                    onFlipH = viewModel::flipSelectedHorizontal,
                                    onFlipV = viewModel::flipSelectedVertical,
                                    onDuplicate = viewModel::duplicateSelected,
                                    onDeleteSelected = {
                                        state.selectedLayerId?.let(viewModel::deleteLayer)
                                    },
                                    onSelectLayer = viewModel::selectLayer,
                                    onBringForward = viewModel::bringForward,
                                    onSendBackward = viewModel::sendBackward,
                                    onToggleLock = viewModel::toggleLock,
                                    onToggleVisibility = viewModel::toggleVisibility,
                                    onDeleteLayer = viewModel::deleteLayer
                                )
                                MobileSheetMode.NONE -> {}
                            }
                        }
                    }

                    // Bottom Studio Dock Bar (respecting navigationBarsPadding)
                    MobileStudioDock(
                        activeAssetTab = state.activeAssetTab,
                        activeInspectorTab = state.activeInspectorTab,
                        sheetMode = mobileSheetMode,
                        layerCount = state.document.layers.size,
                        onSelectAsset = { tab ->
                            if (mobileSheetMode == MobileSheetMode.ASSETS && state.activeAssetTab == tab) {
                                mobileSheetMode = MobileSheetMode.NONE
                            } else {
                                viewModel.setAssetTab(tab)
                                mobileSheetMode = MobileSheetMode.ASSETS
                            }
                        },
                        onSelectInspector = { tab ->
                            if (mobileSheetMode == MobileSheetMode.INSPECTOR && state.activeInspectorTab == tab) {
                                mobileSheetMode = MobileSheetMode.NONE
                            } else {
                                viewModel.setInspectorTab(tab)
                                mobileSheetMode = MobileSheetMode.INSPECTOR
                            }
                        },
                        onToggleCollapse = {
                            mobileSheetMode = if (mobileSheetMode == MobileSheetMode.NONE) {
                                MobileSheetMode.ASSETS
                            } else {
                                MobileSheetMode.NONE
                            }
                        }
                    )
                }
            }
        }
    }

    // Text Editor + Built-In Urdu Phonetic Keyboard Modal
    val editingId = state.editingTextLayerId
    val editingLayer = state.document.layers.firstOrNull { it.id == editingId } as? StudioLayer.TextLayer
    if (editingLayer != null) {
        UrduTextEditorDialog(
            layer = editingLayer,
            initialShowKeyboard = state.showUrduKeyboardDialog || editingLayer.isRtl,
            onDismiss = viewModel::closeTextEditor,
            onSave = { newText, selectedFont ->
                viewModel.updateTextLayerById(editingLayer.id, newText, selectedFont)
                viewModel.closeTextEditor()
            }
        )
    }
}

@Composable
private fun MobileStudioDock(
    activeAssetTab: AssetTab,
    activeInspectorTab: InspectorTab,
    sheetMode: MobileSheetMode,
    layerCount: Int,
    onSelectAsset: (AssetTab) -> Unit,
    onSelectInspector: (InspectorTab) -> Unit,
    onToggleCollapse: () -> Unit
) {
    Surface(
        color = StudioPanel,
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(StudioBorder)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockButton(
                    label = "Text",
                    icon = Icons.Filled.Title,
                    selected = sheetMode == MobileSheetMode.ASSETS && activeAssetTab == AssetTab.TEXT,
                    onClick = { onSelectAsset(AssetTab.TEXT) },
                    modifier = Modifier.weight(1f)
                )
                DockButton(
                    label = "Shayari",
                    icon = Icons.Filled.FormatQuote,
                    selected = sheetMode == MobileSheetMode.ASSETS && activeAssetTab == AssetTab.SHAYARI,
                    onClick = { onSelectAsset(AssetTab.SHAYARI) },
                    modifier = Modifier.weight(1f)
                )
                DockButton(
                    label = "BG",
                    icon = Icons.Filled.Palette,
                    selected = sheetMode == MobileSheetMode.ASSETS && activeAssetTab == AssetTab.BACKGROUNDS,
                    onClick = { onSelectAsset(AssetTab.BACKGROUNDS) },
                    modifier = Modifier.weight(1f)
                )
                DockButton(
                    label = "Shapes",
                    icon = Icons.Filled.AutoAwesome,
                    selected = sheetMode == MobileSheetMode.ASSETS && activeAssetTab == AssetTab.SHAPES,
                    onClick = { onSelectAsset(AssetTab.SHAPES) },
                    modifier = Modifier.weight(1f)
                )
                DockButton(
                    label = "Style",
                    icon = Icons.Filled.Tune,
                    selected = sheetMode == MobileSheetMode.INSPECTOR && activeInspectorTab != InspectorTab.LAYERS,
                    onClick = { onSelectInspector(InspectorTab.STYLE) },
                    modifier = Modifier.weight(1f)
                )
                DockButton(
                    label = "Layers($layerCount)",
                    icon = Icons.Filled.Layers,
                    selected = sheetMode == MobileSheetMode.INSPECTOR && activeInspectorTab == InspectorTab.LAYERS,
                    onClick = { onSelectInspector(InspectorTab.LAYERS) },
                    modifier = Modifier.weight(1.1f)
                )
                Box(
                    modifier = Modifier
                        .size(width = 30.dp, height = 46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioSurfaceElevated)
                        .clickable(onClick = onToggleCollapse),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (sheetMode == MobileSheetMode.NONE) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = "Toggle drawer",
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DockButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (selected) StudioEmeraldDark else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) StudioEmerald else Color.Transparent,
                shape = RoundedCornerShape(9.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) StudioEmeraldLight else StudioTextSecondary,
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) StudioEmeraldLight else StudioTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
