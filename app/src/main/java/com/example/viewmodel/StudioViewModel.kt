package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProjectStorage
import com.example.data.ShayariTemplate
import com.example.data.StudioPresets
import com.example.model.AssetTab
import com.example.model.CanvasBackground
import com.example.model.CanvasDocument
import com.example.model.CanvasPreset
import com.example.model.InspectorTab
import com.example.model.LayerTextAlign
import com.example.model.ShapeType
import com.example.model.StudioFont
import com.example.model.StudioLayer
import com.example.render.CanvasRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.math.abs

data class StudioUiState(
    val document: CanvasDocument = StudioPresets.createStarterDocument(),
    val selectedLayerId: String? = null,
    val activeAssetTab: AssetTab = AssetTab.TEXT,
    val activeInspectorTab: InspectorTab = InspectorTab.STYLE,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isSnappedX: Boolean = false,
    val isSnappedY: Boolean = false,
    val isExporting: Boolean = false,
    val editingTextLayerId: String? = null,
    val showUrduKeyboardDialog: Boolean = false,
    val statusMessage: String? = null,
    val lastExportResult: CanvasRenderer.ExportResult? = null
) {
    val selectedLayer: StudioLayer?
        get() = document.layers.firstOrNull { it.id == selectedLayerId }
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    private val undoStack = ArrayDeque<CanvasDocument>()
    private val redoStack = ArrayDeque<CanvasDocument>()
    private var gestureSnapshot: CanvasDocument? = null
    private val bitmapCache = mutableMapOf<String, Bitmap>()

    init {
        viewModelScope.launch {
            val saved = withContext(Dispatchers.IO) {
                ProjectStorage.loadAutosave(getApplication())
            }
            if (saved != null) {
                _uiState.update {
                    it.copy(
                        document = saved,
                        selectedLayerId = saved.layers.lastOrNull()?.id
                    )
                }
            } else {
                _uiState.update { state ->
                    state.copy(selectedLayerId = state.document.layers.firstOrNull { it is StudioLayer.TextLayer }?.id)
                }
            }
        }
    }

    fun getCachedBitmap(filePath: String): Bitmap? {
        bitmapCache[filePath]?.let { if (!it.isRecycled) return it }
        val bmp = ProjectStorage.decodeBitmap(filePath, maxPreviewDim = 1600)
        if (bmp != null) {
            bitmapCache[filePath] = bmp
        }
        return bmp
    }

    private fun commitDocument(
        newDoc: CanvasDocument,
        newSelectedId: String? = _uiState.value.selectedLayerId,
        recordHistory: Boolean = true
    ) {
        val current = _uiState.value.document
        if (recordHistory && current != newDoc) {
            if (undoStack.size >= 40) undoStack.removeFirst()
            undoStack.addLast(current)
            redoStack.clear()
        }
        _uiState.update {
            it.copy(
                document = newDoc,
                selectedLayerId = newSelectedId,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        persistAutosave(newDoc)
    }

    private fun persistAutosave(doc: CanvasDocument) {
        viewModelScope.launch(Dispatchers.IO) {
            ProjectStorage.saveAutosave(getApplication(), doc)
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _uiState.value.document
        redoStack.addLast(current)
        val prev = undoStack.removeLast()
        _uiState.update {
            it.copy(
                document = prev,
                selectedLayerId = prev.layers.lastOrNull()?.id,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        persistAutosave(prev)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _uiState.value.document
        undoStack.addLast(current)
        val next = redoStack.removeLast()
        _uiState.update {
            it.copy(
                document = next,
                selectedLayerId = next.layers.lastOrNull()?.id,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        }
        persistAutosave(next)
    }

    fun setAssetTab(tab: AssetTab) {
        _uiState.update { it.copy(activeAssetTab = tab) }
    }

    fun setInspectorTab(tab: InspectorTab) {
        _uiState.update { it.copy(activeInspectorTab = tab) }
    }

    fun selectLayer(layerId: String?) {
        _uiState.update { it.copy(selectedLayerId = layerId) }
    }

    fun setPreset(preset: CanvasPreset) {
        val doc = _uiState.value.document
        if (doc.preset == preset) return
        val oldW = doc.preset.width.toFloat()
        val oldH = doc.preset.height.toFloat()
        val newW = preset.width.toFloat()
        val newH = preset.height.toFloat()
        val rx = newW / oldW
        val ry = newH / oldH

        val adjusted = doc.layers.map { layer ->
            when (layer) {
                is StudioLayer.TextLayer -> layer.copy(
                    x = layer.x * rx,
                    y = layer.y * ry,
                    boxWidth = (layer.boxWidth * rx).coerceIn(220f, newW * 0.92f)
                )
                is StudioLayer.ShapeLayer -> layer.copy(
                    x = layer.x * rx,
                    y = layer.y * ry
                )
                is StudioLayer.ImageLayer -> layer.copy(
                    x = layer.x * rx,
                    y = layer.y * ry
                )
            }
        }
        commitDocument(doc.copy(preset = preset, layers = adjusted))
        showStatus("Canvas set to ${preset.displayName} (${preset.shortLabel})")
    }

    fun addUrduText(initialText: String = "اردو ڈیزائنر اسٹوڈیو") {
        val doc = _uiState.value.document
        val w = doc.preset.width.toFloat()
        val h = doc.preset.height.toFloat()
        val layer = StudioLayer.TextLayer(
            name = "Urdu Text",
            x = w / 2f,
            y = h / 2f,
            text = initialText,
            font = StudioFont.NOTO_NASTALIQ_URDU,
            fontSize = 70f,
            lineHeightMultiplier = 1.6f,
            colorArgb = 0xFFFFFFFF,
            isRtl = true,
            textAlign = LayerTextAlign.RIGHT,
            boxWidth = (w * 0.78f).coerceAtLeast(320f)
        )
        commitDocument(doc.copy(layers = doc.layers + layer), newSelectedId = layer.id)
        _uiState.update { it.copy(activeInspectorTab = InspectorTab.STYLE) }
    }

    fun addEnglishText() {
        val doc = _uiState.value.document
        val w = doc.preset.width.toFloat()
        val h = doc.preset.height.toFloat()
        val layer = StudioLayer.TextLayer(
            name = "English Text",
            x = w / 2f,
            y = h / 2f,
            text = "URDU STUDIO PRO",
            font = StudioFont.POPPINS,
            fontSize = 54f,
            lineHeightMultiplier = 1.35f,
            colorArgb = 0xFFFFFFFF,
            isRtl = false,
            textAlign = LayerTextAlign.CENTER,
            boxWidth = (w * 0.75f).coerceAtLeast(300f)
        )
        commitDocument(doc.copy(layers = doc.layers + layer), newSelectedId = layer.id)
        _uiState.update { it.copy(activeInspectorTab = InspectorTab.STYLE) }
    }

    fun addShayariTemplate(template: ShayariTemplate) {
        val doc = _uiState.value.document
        val w = doc.preset.width.toFloat()
        val h = doc.preset.height.toFloat()
        val layer = StudioLayer.TextLayer(
            name = template.title,
            x = w / 2f,
            y = h / 2f,
            text = template.text,
            font = template.font,
            fontSize = template.fontSize,
            lineHeightMultiplier = 1.65f,
            colorArgb = template.defaultColorArgb,
            shadowColorArgb = 0xCC000000,
            shadowBlur = 12f,
            shadowOffsetY = 6f,
            isRtl = true,
            textAlign = LayerTextAlign.CENTER,
            boxWidth = (w * 0.84f).coerceAtLeast(360f)
        )
        commitDocument(doc.copy(layers = doc.layers + layer), newSelectedId = layer.id)
        _uiState.update { it.copy(activeInspectorTab = InspectorTab.STYLE) }
    }

    fun setSolidBackground(colorArgb: Long) {
        val doc = _uiState.value.document
        commitDocument(doc.copy(background = CanvasBackground.Solid(colorArgb)))
    }

    fun setGradientBackground(gradient: CanvasBackground.Gradient) {
        val doc = _uiState.value.document
        commitDocument(doc.copy(background = gradient))
    }

    fun importBackgroundImage(uri: Uri) {
        viewModelScope.launch {
            val imported = withContext(Dispatchers.IO) {
                ProjectStorage.importImageToLocalMedia(getApplication(), uri, maxDimension = 2048)
            }
            imported.onSuccess { media ->
                val doc = _uiState.value.document
                commitDocument(doc.copy(background = CanvasBackground.Image(media.filePath)))
                showStatus("Background image applied")
            }.onFailure {
                showStatus("Could not load background image")
            }
        }
    }

    fun addShape(shapeType: ShapeType) {
        val doc = _uiState.value.document
        val w = doc.preset.width.toFloat()
        val h = doc.preset.height.toFloat()
        val (bw, bh) = when (shapeType) {
            ShapeType.RIBBON_BANNER -> 580f to 160f
            ShapeType.ORNATE_BORDER -> (w * 0.88f) to (h * 0.88f)
            ShapeType.ROUNDED_RECT -> 480f to 260f
            else -> 320f to 320f
        }
        val fill = if (shapeType == ShapeType.ORNATE_BORDER) 0x00000000L else 0xFF10B981L
        val stroke = if (shapeType == ShapeType.ORNATE_BORDER) 0xFFF59E0BL else 0xFFFDE68AL
        val layer = StudioLayer.ShapeLayer(
            name = shapeType.displayName,
            x = w / 2f,
            y = h / 2f,
            shapeType = shapeType,
            baseWidth = bw,
            baseHeight = bh,
            fillColorArgb = fill,
            strokeColorArgb = stroke,
            strokeWidth = 6f
        )
        commitDocument(doc.copy(layers = doc.layers + layer), newSelectedId = layer.id)
        _uiState.update { it.copy(activeInspectorTab = InspectorTab.STYLE) }
    }

    fun importStickerImage(uri: Uri) {
        viewModelScope.launch {
            val imported = withContext(Dispatchers.IO) {
                ProjectStorage.importImageToLocalMedia(getApplication(), uri, maxDimension = 1600)
            }
            imported.onSuccess { media ->
                val doc = _uiState.value.document
                val targetW = (doc.preset.width * 0.45f).coerceAtMost(540f)
                val aspect = media.height.toFloat() / media.width.coerceAtLeast(1)
                val layer = StudioLayer.ImageLayer(
                    name = "Uploaded Image",
                    x = doc.preset.width / 2f,
                    y = doc.preset.height / 2f,
                    filePath = media.filePath,
                    baseWidth = targetW,
                    baseHeight = (targetW * aspect).coerceIn(80f, doc.preset.height * 0.8f)
                )
                commitDocument(doc.copy(layers = doc.layers + layer), newSelectedId = layer.id)
                _uiState.update { it.copy(activeInspectorTab = InspectorTab.ARRANGE) }
                showStatus("Image added to canvas")
            }.onFailure {
                showStatus("Could not import image")
            }
        }
    }

    fun beginTransformGesture() {
        if (gestureSnapshot == null) {
            gestureSnapshot = _uiState.value.document
        }
    }

    fun updateLayerPosition(
        layerId: String,
        deltaCanvasX: Float,
        deltaCanvasY: Float
    ) {
        val doc = _uiState.value.document
        val layer = doc.layers.firstOrNull { it.id == layerId } ?: return
        if (layer.isLocked) return

        val centerX = doc.preset.width / 2f
        val centerY = doc.preset.height / 2f
        val rawX = layer.x + deltaCanvasX
        val rawY = layer.y + deltaCanvasY

        val snapThreshold = 14f
        val snappedX = abs(rawX - centerX) <= snapThreshold
        val snappedY = abs(rawY - centerY) <= snapThreshold

        val finalX = if (snappedX) centerX else rawX.coerceIn(-400f, doc.preset.width + 400f)
        val finalY = if (snappedY) centerY else rawY.coerceIn(-400f, doc.preset.height + 400f)

        val updated = updateLayerInList(doc.layers, layerId) {
            when (it) {
                is StudioLayer.TextLayer -> it.copy(x = finalX, y = finalY)
                is StudioLayer.ShapeLayer -> it.copy(x = finalX, y = finalY)
                is StudioLayer.ImageLayer -> it.copy(x = finalX, y = finalY)
            }
        }
        _uiState.update {
            it.copy(
                document = doc.copy(layers = updated),
                isSnappedX = snappedX,
                isSnappedY = snappedY
            )
        }
    }

    fun updateLayerScaleAndRotation(
        layerId: String,
        newScale: Float? = null,
        newRotation: Float? = null
    ) {
        val doc = _uiState.value.document
        val layer = doc.layers.firstOrNull { it.id == layerId } ?: return
        if (layer.isLocked) return

        val clampedScale = (newScale ?: layer.scale).coerceIn(0.2f, 4.5f)
        val normalizedRot = ((newRotation ?: layer.rotation) % 360f).let {
            if (abs(it) < 2.5f) 0f else it
        }
        val updated = updateLayerInList(doc.layers, layerId) {
            when (it) {
                is StudioLayer.TextLayer -> it.copy(scale = clampedScale, rotation = normalizedRot)
                is StudioLayer.ShapeLayer -> it.copy(scale = clampedScale, rotation = normalizedRot)
                is StudioLayer.ImageLayer -> it.copy(scale = clampedScale, rotation = normalizedRot)
            }
        }
        _uiState.update { it.copy(document = doc.copy(layers = updated)) }
    }

    fun endTransformGesture() {
        val snapshot = gestureSnapshot
        gestureSnapshot = null
        val current = _uiState.value.document
        _uiState.update { it.copy(isSnappedX = false, isSnappedY = false) }
        if (snapshot != null && snapshot != current) {
            if (undoStack.size >= 40) undoStack.removeFirst()
            undoStack.addLast(snapshot)
            redoStack.clear()
            _uiState.update {
                it.copy(
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = redoStack.isNotEmpty()
                )
            }
            persistAutosave(current)
        }
    }

    fun updateSelectedTextLayer(transform: (StudioLayer.TextLayer) -> StudioLayer.TextLayer) {
        val id = _uiState.value.selectedLayerId ?: return
        val doc = _uiState.value.document
        val updated = updateLayerInList(doc.layers, id) { layer ->
            if (layer is StudioLayer.TextLayer) transform(layer) else layer
        }
        commitDocument(doc.copy(layers = updated))
    }

    fun updateTextLayerById(layerId: String, newText: String, font: StudioFont? = null) {
        val doc = _uiState.value.document
        val updated = updateLayerInList(doc.layers, layerId) { layer ->
            if (layer is StudioLayer.TextLayer) {
                layer.copy(
                    text = newText,
                    font = font ?: layer.font,
                    name = newText.lineSequence().firstOrNull()?.take(18)?.ifBlank { "Text" } ?: "Text"
                )
            } else layer
        }
        commitDocument(doc.copy(layers = updated))
    }

    fun updateSelectedShapeLayer(transform: (StudioLayer.ShapeLayer) -> StudioLayer.ShapeLayer) {
        val id = _uiState.value.selectedLayerId ?: return
        val doc = _uiState.value.document
        val updated = updateLayerInList(doc.layers, id) { layer ->
            if (layer is StudioLayer.ShapeLayer) transform(layer) else layer
        }
        commitDocument(doc.copy(layers = updated))
    }

    fun updateSelectedOpacity(opacity: Float) {
        val id = _uiState.value.selectedLayerId ?: return
        val doc = _uiState.value.document
        val clamped = opacity.coerceIn(0.05f, 1f)
        val updated = updateLayerInList(doc.layers, id) {
            when (it) {
                is StudioLayer.TextLayer -> it.copy(opacity = clamped)
                is StudioLayer.ShapeLayer -> it.copy(opacity = clamped)
                is StudioLayer.ImageLayer -> it.copy(opacity = clamped)
            }
        }
        commitDocument(doc.copy(layers = updated))
    }

    fun alignSelectedCenterHorizontally() {
        val id = _uiState.value.selectedLayerId ?: return
        val doc = _uiState.value.document
        val cx = doc.preset.width / 2f
        val updated = updateLayerInList(doc.layers, id) {
            if (it.isLocked) it else when (it) {
                is StudioLayer.TextLayer -> it.copy(x = cx)
                is StudioLayer.ShapeLayer -> it.copy(x = cx)
                is StudioLayer.ImageLayer -> it.copy(x = cx)
            }
        }
        commitDocument(doc.copy(layers = updated))
    }

    fun alignSelectedCenterVertically() {
        val id = _uiState.value.selectedLayerId ?: return
        val doc = _uiState.value.document
        val cy = doc.preset.height / 2f
        val updated = updateLayerInList(doc.layers, id) {
            if (it.isLocked) it else when (it) {
                is StudioLayer.TextLayer -> it.copy(y = cy)
                is StudioLayer.ShapeLayer -> it.copy(y = cy)
                is StudioLayer.ImageLayer -> it.copy(y = cy)
            }
        }
        commitDocument(doc.copy(layers = updated))
    }

    fun flipSelectedHorizontal() {
        val id = _uiState.value.selectedLayerId ?: return
        val doc = _uiState.value.document
        val updated = updateLayerInList(doc.layers, id) {
            if (it.isLocked) it else when (it) {
                is StudioLayer.TextLayer -> it.copy(flipX = !it.flipX)
                is StudioLayer.ShapeLayer -> it.copy(flipX = !it.flipX)
                is StudioLayer.ImageLayer -> it.copy(flipX = !it.flipX)
            }
        }
        commitDocument(doc.copy(layers = updated))
    }

    fun flipSelectedVertical() {
        val id = _uiState.value.selectedLayerId ?: return
        val doc = _uiState.value.document
        val updated = updateLayerInList(doc.layers, id) {
            if (it.isLocked) it else when (it) {
                is StudioLayer.TextLayer -> it.copy(flipY = !it.flipY)
                is StudioLayer.ShapeLayer -> it.copy(flipY = !it.flipY)
                is StudioLayer.ImageLayer -> it.copy(flipY = !it.flipY)
            }
        }
        commitDocument(doc.copy(layers = updated))
    }

    fun duplicateSelected() {
        val doc = _uiState.value.document
        val layer = doc.layers.firstOrNull { it.id == _uiState.value.selectedLayerId } ?: return
        val newId = UUID.randomUUID().toString()
        val copy = when (layer) {
            is StudioLayer.TextLayer -> layer.copy(
                id = newId,
                name = "${layer.name} Copy",
                x = layer.x + 36f,
                y = layer.y + 36f,
                isLocked = false
            )
            is StudioLayer.ShapeLayer -> layer.copy(
                id = newId,
                name = "${layer.name} Copy",
                x = layer.x + 36f,
                y = layer.y + 36f,
                isLocked = false
            )
            is StudioLayer.ImageLayer -> layer.copy(
                id = newId,
                name = "${layer.name} Copy",
                x = layer.x + 36f,
                y = layer.y + 36f,
                isLocked = false
            )
        }
        commitDocument(doc.copy(layers = doc.layers + copy), newSelectedId = newId)
    }

    fun deleteLayer(layerId: String) {
        val doc = _uiState.value.document
        val remaining = doc.layers.filterNot { it.id == layerId }
        val nextSel = if (_uiState.value.selectedLayerId == layerId) remaining.lastOrNull()?.id else _uiState.value.selectedLayerId
        commitDocument(doc.copy(layers = remaining), newSelectedId = nextSel)
    }

    fun bringForward(layerId: String) {
        val doc = _uiState.value.document
        val idx = doc.layers.indexOfFirst { it.id == layerId }
        if (idx == -1 || idx >= doc.layers.lastIndex) return
        val mutable = doc.layers.toMutableList()
        val item = mutable.removeAt(idx)
        mutable.add(idx + 1, item)
        commitDocument(doc.copy(layers = mutable))
    }

    fun sendBackward(layerId: String) {
        val doc = _uiState.value.document
        val idx = doc.layers.indexOfFirst { it.id == layerId }
        if (idx <= 0) return
        val mutable = doc.layers.toMutableList()
        val item = mutable.removeAt(idx)
        mutable.add(idx - 1, item)
        commitDocument(doc.copy(layers = mutable))
    }

    fun toggleLock(layerId: String) {
        val doc = _uiState.value.document
        val updated = updateLayerInList(doc.layers, layerId) {
            when (it) {
                is StudioLayer.TextLayer -> it.copy(isLocked = !it.isLocked)
                is StudioLayer.ShapeLayer -> it.copy(isLocked = !it.isLocked)
                is StudioLayer.ImageLayer -> it.copy(isLocked = !it.isLocked)
            }
        }
        commitDocument(doc.copy(layers = updated))
    }

    fun toggleVisibility(layerId: String) {
        val doc = _uiState.value.document
        val updated = updateLayerInList(doc.layers, layerId) {
            when (it) {
                is StudioLayer.TextLayer -> it.copy(isVisible = !it.isVisible)
                is StudioLayer.ShapeLayer -> it.copy(isVisible = !it.isVisible)
                is StudioLayer.ImageLayer -> it.copy(isVisible = !it.isVisible)
            }
        }
        commitDocument(doc.copy(layers = updated))
    }

    fun clearCanvas() {
        val doc = _uiState.value.document
        commitDocument(doc.copy(layers = emptyList()), newSelectedId = null)
        showStatus("Canvas cleared")
    }

    fun openTextEditor(layerId: String) {
        _uiState.update { it.copy(editingTextLayerId = layerId, selectedLayerId = layerId) }
    }

    fun closeTextEditor() {
        _uiState.update { it.copy(editingTextLayerId = null) }
    }

    fun openUrduKeyboardModal() {
        val current = _uiState.value.selectedLayer
        if (current is StudioLayer.TextLayer) {
            _uiState.update { it.copy(editingTextLayerId = current.id, showUrduKeyboardDialog = true) }
        } else {
            val doc = _uiState.value.document
            val w = doc.preset.width.toFloat()
            val h = doc.preset.height.toFloat()
            val newLayer = StudioLayer.TextLayer(
                name = "Urdu Text",
                x = w / 2f,
                y = h / 2f,
                text = "",
                font = StudioFont.NOTO_NASTALIQ_URDU,
                fontSize = 68f,
                lineHeightMultiplier = 1.6f,
                colorArgb = 0xFFFFFFFF,
                isRtl = true,
                textAlign = LayerTextAlign.RIGHT,
                boxWidth = (w * 0.78f).coerceAtLeast(320f)
            )
            commitDocument(doc.copy(layers = doc.layers + newLayer), newSelectedId = newLayer.id)
            _uiState.update {
                it.copy(editingTextLayerId = newLayer.id, showUrduKeyboardDialog = true)
            }
        }
    }

    fun saveProjectToUri(uri: Uri) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                ProjectStorage.saveToUri(getApplication(), uri, _uiState.value.document)
            }
            res.onSuccess { showStatus("Project JSON saved") }
                .onFailure { showStatus("Failed to save JSON project") }
        }
    }

    fun loadProjectFromUri(uri: Uri) {
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                ProjectStorage.loadFromUri(getApplication(), uri)
            }
            res.onSuccess { loaded ->
                commitDocument(loaded, newSelectedId = loaded.layers.lastOrNull()?.id)
                showStatus("Project loaded")
            }.onFailure {
                showStatus("Invalid Urdu Studio JSON file")
            }
        }
    }

    fun exportHighResPng() {
        if (_uiState.value.isExporting) return
        _uiState.update { it.copy(isExporting = true) }
        viewModelScope.launch {
            val res = withContext(Dispatchers.IO) {
                CanvasRenderer.exportHighResPng(getApplication(), _uiState.value.document, multiplier = 2)
            }
            res.onSuccess { exported ->
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        lastExportResult = exported,
                        statusMessage = if (exported.savedToGallery) {
                            "Saved ${exported.widthPx}×${exported.heightPx} PNG to Pictures/Urdu Studio"
                        } else {
                            "Exported ${exported.widthPx}×${exported.heightPx} PNG"
                        }
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        statusMessage = "Export failed: ${err.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun dismissExportBanner() {
        _uiState.update { it.copy(lastExportResult = null) }
    }

    private fun showStatus(msg: String) {
        _uiState.update { it.copy(statusMessage = msg) }
    }

    private fun updateLayerInList(
        layers: List<StudioLayer>,
        layerId: String,
        transform: (StudioLayer) -> StudioLayer
    ): List<StudioLayer> = layers.map { if (it.id == layerId) transform(it) else it }

    override fun onCleared() {
        super.onCleared()
        bitmapCache.values.forEach { if (!it.isRecycled) it.recycle() }
        bitmapCache.clear()
    }
}
