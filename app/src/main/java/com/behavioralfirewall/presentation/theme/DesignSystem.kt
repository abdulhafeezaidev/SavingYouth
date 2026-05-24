package com.behavioralfirewall.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val mediumSmall: Dp = 12.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp
)

data class Radius(
    val small: Dp = 12.dp,
    val medium: Dp = 18.dp,
    val card: Dp = 24.dp,
    val dialog: Dp = 32.dp,
    val outer: Dp = 40.dp
)

data class Timing(
    val fast: Int = 150,
    val medium: Int = 300,
    val slow: Int = 500
)

val LocalSpacing = compositionLocalOf { Spacing() }
val LocalRadius = compositionLocalOf { Radius() }
val LocalTiming = compositionLocalOf { Timing() }
