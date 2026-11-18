package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CanvasBackground
import com.example.model.CanvasDocument
import com.example.model.LayerTextAlign
import com.example.model.StudioLayer
import com.example.render.CanvasRenderer
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioEmeraldLight
import com.example.ui.theme.StudioGold
import com.example.ui.theme.StudioSnapGuide
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun InteractiveCanvasStage(
    document: CanvasDocument,
    selectedLayerId: String?,
    isSnappedX: Boolean,
    isSnappedY: Boolean,
    getBitmap: (String) -> Bitmap?,
    onSelectLayer: (String?) -> Unit,
    onBeginTransform: () -> Unit,
    onMoveLayer: (String, Float, Float) -> Unit,
    onScaleRotateLayer: (String, Float?, Float?) -> Unit,
    onEndTransform: () -> Unit,
    onDoubleTapText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBg)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onSelectLayer(null) })
            }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val availW = with(density) { maxWidth.toPx() }.coerceAtLeast(1f)
        val availH = with(density) { maxHeight.toPx() }.coerceAtLeast(1f)

        val docW = document.preset.width.toFloat()
        val docH = document.preset.height.toFloat()

        // Reserve subtle headroom for rotation handle
        val viewportScale = min(availW / docW, availH / docH).coerceAtLeast(0.05f)
        val canvasWidthPx = docW * viewportScale
        val canvasHeightPx = docH * viewportScale
        val canvasWidthDp = with(density) { canvasWidthPx.toDp() }
        val canvasHeightDp = with(density) { canvasHeightPx.toDp() }

        Box(
            modifier = Modifier
                .size(canvasWidthDp, canvasHeightDp)
                .shadow(16.dp, RoundedCornerShape(4.dp))
                .border(1.dp, StudioBorder, RoundedCornerShape(4.dp))
                .testTag("interactive_canvas_stage")
        ) {
            // Clipped Artboard Layer Stack
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
            ) {
                CanvasBackgroundSurface(
                    background = document.background,
                    getBitmap = getBitmap,
                    modifier = Modifier.fillMaxSize()
                )

                document.layers.filter { it.isVisible }.forEach { layer ->
                    val isSelected = layer.id == selectedLayerId
                    CanvasLayerItem(
                        layer = layer,
                        isSelected = isSelected,
                        viewportScale = viewportScale,
                        getBitmap = getBitmap,
                        onSelect = { onSelectLayer(layer.id) },
                        onBeginTransform = onBeginTransform,
                        onMove = { dxCanvas, dyCanvas -> onMoveLayer(layer.id, dxCanvas, dyCanvas) },
                        onScaleRotate = { sc, rot -> onScaleRotateLayer(layer.id, sc, rot) },
                        onEndTransform = onEndTransform,
                        onDoubleTap = {
                            if (layer is StudioLayer.TextLayer) {
                                onDoubleTapText(layer.id)
                            }
                        }
                    )
                }

                // Center-Snapping Magnetic Guidelines
                if (isSnappedX || isSnappedY) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        if (isSnappedX) {
                            drawLine(
                                color = StudioSnapGuide,
                                start = Offset(size.width / 2f, 0f),
                                end = Offset(size.width / 2f, size.height),
                                strokeWidth = 2.5f,
                                pathEffect = dash
                            )
                        }
                        if (isSnappedY) {
                            drawLine(
                                color = StudioSnapGuide,
                                start = Offset(0f, size.height / 2f),
                                end = Offset(size.width, size.height / 2f),
                                strokeWidth = 2.5f,
                                pathEffect = dash
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CanvasBackgroundSurface(
    background: CanvasBackground,
    getBitmap: (String) -> Bitmap?,
    modifier: Modifier = Modifier
) {
    when (background) {
        is CanvasBackground.Solid -> {
            Box(modifier = modifier.background(Color(background.colorArgb)))
        }
        is CanvasBackground.Gradient -> {
            Canvas(modifier = modifier) {
                val rad = Math.toRadians(background.angleDegrees.toDouble())
                val dx = cos(rad).toFloat() * size.width * 0.5f
                val dy = sin(rad).toFloat() * size.height * 0.5f
                val cx = size.width / 2f
                val cy = size.height / 2f
                val colors = background.colorsArgb.map { Color(it) }
                drawRect(
                    brush = Brush.linearGradient(
                        colors = if (colors.size >= 2) colors else listOf(colors.first(), colors.first()),
                        start = Offset(cx - dx, cy - dy),
                        end = Offset(cx + dx, cy + dy)
                    )
                )
            }
        }
        is CanvasBackground.Image -> {
            val bmp = remember(background.filePath) { getBitmap(background.filePath) }
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Canvas background",
                    contentScale = ContentScale.Crop,
                    modifier = modifier
                )
            } else {
                Box(modifier = modifier.background(StudioBg))
            }
        }
    }
}

@Composable
private fun CanvasLayerItem(
    layer: StudioLayer,
    isSelected: Boolean,
    viewportScale: Float,
    getBitmap: (String) -> Bitmap?,
    onSelect: () -> Unit,
    onBeginTransform: () -> Unit,
    onMove: (Float, Float) -> Unit,
    onScaleRotate: (Float?, Float?) -> Unit,
    onEndTransform: () -> Unit,
    onDoubleTap: () -> Unit
) {
    val density = LocalDensity.current
    val currentLayer by rememberUpdatedState(layer)
    val currentScale by rememberUpdatedState(viewportScale)

    var measuredWidthPx by remember { mutableFloatStateOf(200f) }
    var measuredHeightPx by remember { mutableFloatStateOf(120f) }

    val centerXScreenPx = layer.x * viewportScale
    val centerYScreenPx = layer.y * viewportScale

    val sx = layer.scale * if (layer.flipX) -1f else 1f
    val sy = layer.scale * if (layer.flipY) -1f else 1f

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (centerXScreenPx - measuredWidthPx / 2f).roundToInt(),
                    y = (centerYScreenPx - measuredHeightPx / 2f).roundToInt()
                )
            }
            .onSizeChanged { sz ->
                measuredWidthPx = sz.width.toFloat()
                measuredHeightPx = sz.height.toFloat()
            }
            .graphicsLayer {
                rotationZ = layer.rotation
                scaleX = sx
                scaleY = sy
                alpha = layer.opacity
            }
            .pointerInput(layer.id) {
                detectTapGestures(
                    onTap = { onSelect() },
                    onDoubleTap = {
                        onSelect()
                        onDoubleTap()
                    }
                )
            }
            .pointerInput(layer.id, layer.isLocked) {
                if (!layer.isLocked) {
                    detectTransformGestures { _, pan, zoom, rotation ->
                        onSelect()
                        onBeginTransform()
                        val sc = currentScale.coerceAtLeast(0.05f)
                        onMove(pan.x / sc, pan.y / sc)
                        if (zoom != 1f || rotation != 0f) {
                            onScaleRotate(
                                currentLayer.scale * zoom,
                                currentLayer.rotation + rotation
                            )
                        }
                    }
                }
            }
            .pointerInput(layer.id, layer.isLocked) {
                if (!layer.isLocked) {
                    detectDragGestures(
                        onDragStart = {
                            onSelect()
                            onBeginTransform()
                        },
                        onDragEnd = { onEndTransform() },
                        onDragCancel = { onEndTransform() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val sc = currentScale.coerceAtLeast(0.05f)
                            onMove(dragAmount.x / sc, dragAmount.y / sc)
                        }
                    )
                }
            }
    ) {
        // Actual Layer Visual Content
        Box(
            modifier = Modifier
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 1.5.dp,
                            color = if (layer.isLocked) StudioGold else StudioEmeraldLight,
                            shape = RoundedCornerShape(4.dp)
                        )
                    } else Modifier
                )
        ) {
            when (layer) {
                is StudioLayer.TextLayer -> TextLayerVisual(layer, viewportScale)
                is StudioLayer.ShapeLayer -> ShapeLayerVisual(layer, viewportScale)
                is StudioLayer.ImageLayer -> ImageLayerVisual(layer, viewportScale, getBitmap)
            }
        }

        // Visible Selection Corner Scale Handles + Top Rotation Handle (Fabric.js style)
        if (isSelected) {
            if (layer.isLocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(StudioGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = Color.Black,
                        modifier = Modifier.size(13.dp)
                    )
                }
            } else {
                // Top Center Rotation Handle
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-28).dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(StudioEmerald)
                        .border(1.5.dp, Color.White, CircleShape)
                        .pointerInput(layer.id) {
                            detectDragGestures(
                                onDragStart = { onBeginTransform() },
                                onDragEnd = { onEndTransform() },
                                onDragCancel = { onEndTransform() },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onScaleRotate(null, currentLayer.rotation + dragAmount.x * 0.6f)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Rotate handle",
                        tint = Color(0xFF022C22),
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Bottom-Right Corner Scale Handle
                ScaleCornerHandle(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp),
                    onBegin = onBeginTransform,
                    onEnd = onEndTransform,
                    onScaleDelta = { deltaPx ->
                        onScaleRotate(currentLayer.scale * (1f + deltaPx / 220f), null)
                    }
                )
                // Top-Left Corner Scale Handle
                ScaleCornerHandle(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-8).dp, y = (-8).dp),
                    onBegin = onBeginTransform,
                    onEnd = onEndTransform,
                    onScaleDelta = { deltaPx ->
                        onScaleRotate(currentLayer.scale * (1f - deltaPx / 220f), null)
                    }
                )
            }
        }
    }
}

@Composable
private fun ScaleCornerHandle(
    modifier: Modifier,
    onBegin: () -> Unit,
    onEnd: () -> Unit,
    onScaleDelta: (Float) -> Unit
) {
    Box(
        modifier = modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, StudioEmerald, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onBegin() },
                    onDragEnd = { onEnd() },
                    onDragCancel = { onEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onScaleDelta(dragAmount.x + dragAmount.y)
                    }
                )
            }
    )
}

@Composable
private fun TextLayerVisual(
    layer: StudioLayer.TextLayer,
    viewportScale: Float
) {
    val density = LocalDensity.current
    val boxWidthDp = with(density) { (layer.boxWidth * viewportScale).toDp() }
    val padXDp = with(density) { (28f * viewportScale).toDp() }
    val padYDp = with(density) { (36f * viewportScale).toDp() }
    val fontSizeSp = with(density) { (layer.fontSize * viewportScale).toSp() }
    val lineHeightSp = with(density) { (layer.fontSize * layer.lineHeightMultiplier * viewportScale).toSp() }

    val layoutDir = if (layer.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val align = when (layer.textAlign) {
        LayerTextAlign.RIGHT -> TextAlign.Right
        LayerTextAlign.CENTER -> TextAlign.Center
        LayerTextAlign.LEFT -> TextAlign.Left
    }

    val bgAlpha = android.graphics.Color.alpha(layer.backgroundColorArgb.toInt())
    val bgModifier = if (bgAlpha > 0) {
        Modifier.background(
            color = Color(layer.backgroundColorArgb),
            shape = RoundedCornerShape(with(density) { (24f * viewportScale).toDp() })
        )
    } else {
        Modifier
    }

    val shadow = if (layer.shadowBlur > 0f || layer.shadowOffsetX != 0f || layer.shadowOffsetY != 0f) {
        Shadow(
            color = Color(layer.shadowColorArgb),
            offset = Offset(layer.shadowOffsetX * viewportScale, layer.shadowOffsetY * viewportScale),
            blurRadius = (layer.shadowBlur * viewportScale).coerceAtLeast(0.5f)
        )
    } else null

    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        Box(
            modifier = Modifier
                .width(boxWidthDp + padXDp * 2)
                .wrapContentHeight()
                .then(bgModifier)
                .padding(horizontal = padXDp, vertical = padYDp)
        ) {
            // Optional Stroke Layer behind Fill
            if (layer.strokeWidth > 0.2f) {
                Text(
                    text = layer.text.ifEmpty { " " },
                    style = TextStyle(
                        fontFamily = layer.font.fontFamily,
                        fontSize = fontSizeSp,
                        lineHeight = lineHeightSp,
                        color = Color(layer.strokeColorArgb),
                        textAlign = align,
                        drawStyle = Stroke(width = layer.strokeWidth * 2f * viewportScale),
                        platformStyle = PlatformTextStyle(includeFontPadding = true)
                    ),
                    modifier = Modifier.width(boxWidthDp)
                )
            }

            // Fill + Shadow Layer
            Text(
                text = layer.text.ifEmpty { " " },
                style = TextStyle(
                    fontFamily = layer.font.fontFamily,
                    fontSize = fontSizeSp,
                    lineHeight = lineHeightSp,
                    color = Color(layer.colorArgb),
                    textAlign = align,
                    shadow = shadow,
                    platformStyle = PlatformTextStyle(includeFontPadding = true)
                ),
                modifier = Modifier.width(boxWidthDp)
            )
        }
    }
}

@Composable
private fun ShapeLayerVisual(
    layer: StudioLayer.ShapeLayer,
    viewportScale: Float
) {
    val density = LocalDensity.current
    val wDp = with(density) { (layer.baseWidth * viewportScale).toDp() }
    val hDp = with(density) { (layer.baseHeight * viewportScale).toDp() }

    Canvas(modifier = Modifier.size(wDp, hDp)) {
        val wPx = size.width
        val hPx = size.height
        val path = CanvasRenderer.buildComposeShapePath(
            shapeType = layer.shapeType,
            width = wPx,
            height = hPx,
            cornerRadius = layer.cornerRadius * viewportScale
        )
        path.translate(Offset(wPx / 2f, hPx / 2f))

        val fillAlpha = android.graphics.Color.alpha(layer.fillColorArgb.toInt())
        if (fillAlpha > 0) {
            drawPath(path = path, color = Color(layer.fillColorArgb))
        }
        if (layer.strokeWidth > 0.2f) {
            drawPath(
                path = path,
                color = Color(layer.strokeColorArgb),
                style = Stroke(width = layer.strokeWidth * viewportScale)
            )
        }
    }
}

@Composable
private fun ImageLayerVisual(
    layer: StudioLayer.ImageLayer,
    viewportScale: Float,
    getBitmap: (String) -> Bitmap?
) {
    val density = LocalDensity.current
    val wDp = with(density) { (layer.baseWidth * viewportScale).toDp() }
    val hDp = with(density) { (layer.baseHeight * viewportScale).toDp() }
    val bmp = remember(layer.filePath) { getBitmap(layer.filePath) }

    if (bmp != null) {
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = layer.name,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.size(wDp, hDp)
        )
    } else {
        Surface(
            modifier = Modifier.size(wDp, hDp),
            color = StudioEmerald.copy(alpha = 0.2f)
        ) {}
    }
}
