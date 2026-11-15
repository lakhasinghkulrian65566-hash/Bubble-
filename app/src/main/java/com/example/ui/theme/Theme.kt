package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val ArcadeDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = CosmicMidnight,
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = Color(0xFFB5F5FF),
    secondary = SunlitGold,
    onSecondary = CosmicMidnight,
    secondaryContainer = Color(0xFF543C00),
    onSecondaryContainer = Color(0xFFFFE7A3),
    tertiary = CoralPop,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF6B1028),
    onTertiaryContainer = Color(0xFFFFD9E0),
    background = CosmicMidnight,
    onBackground = Color(0xFFF6F4FF),
    surface = CosmicIndigo,
    onSurface = Color(0xFFF6F4FF),
    surfaceVariant = CosmicSurface,
    onSurfaceVariant = SoftLavender,
    surfaceContainerHigh = CosmicSurfaceElevated,
    outline = Color(0xFF4C3D99),
    error = RubyHeart,
    onError = Color.White
)

val BubbleQuestShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun BubbleQuestTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ArcadeDarkColorScheme,
        typography = BubbleQuestTypography,
        shapes = BubbleQuestShapes,
        content = content
    )
}
