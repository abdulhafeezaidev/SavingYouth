package com.behavioralfirewall.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// Figma Meditation App UI Design (Community) Theme
private val CalmingDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8E97FD),       // Meditative Lavender Blue
    secondary = Color(0xFFF99E44),     // Sunset Peach / Warm Orange
    tertiary = Color(0xFFFFC97F),      // Sunset Gold / Light Honey Amber
    background = Color(0xFF03174C),    // Celestial Deep Midnight Indigo
    surface = Color(0x3D1F2C46),       // Glassmorphic translucent twilight slate card
    surfaceVariant = Color(0x61293452),// Glassmorphic deep twilight indigo card
    onSurfaceVariant = Color(0xFFB3A9BA), // Soft grey-lavender text
    outline = Color(0x26FFFFFF),       // Soft white frosted glass borders (15% opacity)
    primaryContainer = Color(0xFF03174C),
    secondaryContainer = Color(0xFF1F2C46),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF03174C),   
    onBackground = Color(0xFFF8F2ED),  // Soothing linen off-white text
    onSurface = Color(0xFFFFFFFF),
    error = Color(0xFFFF6B6B),         // Soothing coral red (reduces user stress)
    onError = Color(0xFFFFFFFF)
)

// Reusable vertical gradient representing the Celestial Midnight sky (Twilight Slate to Celestial Navy to Midnight)
val PremiumBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1F2C46),
        Color(0xFF03174C),
        Color(0xFF07091B)
    )
)

// Reusable glassmorphic card background gradient matching the twilight ambiance
val GlassCardBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF).copy(alpha = 0.08f),
        Color(0xFF1F2C46).copy(alpha = 0.25f)
    )
)

// Reusable glassmorphic card border gradient (frosted glass edge glow)
val GlassBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF).copy(alpha = 0.20f),
        Color(0xFFFFFFFF).copy(alpha = 0.02f)
    )
)

// Vibrant calming horizontal gradient for highlights, stats, and primary interactive elements
val NeonGlowGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF8E97FD), // Meditative Lavender Blue
        Color(0xFFF99E44)  // Sunset Peach
    )
)

@Composable
fun BehavioralFirewallTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalRadius provides Radius(),
        LocalTiming provides Timing()
    ) {
        MaterialTheme(
            colorScheme = CalmingDarkColorScheme,
            content = content
        )
    }
}
