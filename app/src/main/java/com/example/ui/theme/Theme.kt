package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val UrduStudioColorScheme = darkColorScheme(
    primary = StudioEmerald,
    onPrimary = Color(0xFF022C22),
    primaryContainer = StudioEmeraldDark,
    onPrimaryContainer = StudioEmeraldLight,
    secondary = StudioGold,
    onSecondary = Color(0xFF271700),
    secondaryContainer = Color(0xFF422006),
    onSecondaryContainer = Color(0xFFFDE68A),
    background = StudioBg,
    onBackground = StudioTextPrimary,
    surface = StudioPanel,
    onSurface = StudioTextPrimary,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = StudioTextSecondary,
    outline = StudioBorder,
    error = StudioDanger,
    onError = Color.White
)

private val StudioShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp)
)

@Composable
fun UrduStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = UrduStudioColorScheme,
        typography = StudioTypography,
        shapes = StudioShapes,
        content = content
    )
}
