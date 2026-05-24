package com.behavioralfirewall.presentation.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription

/**
 * Custom Compose Modifiers to support screen readers and large font scales cleanly.
 */
fun Modifier.chartSemantics(
    description: String
): Modifier = this.clearAndSetSemantics {
    contentDescription = description
}
