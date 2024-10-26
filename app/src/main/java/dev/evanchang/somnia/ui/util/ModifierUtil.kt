package dev.evanchang.somnia.ui.util

import androidx.compose.ui.Modifier

fun Modifier.thenIf(
    condition: Boolean,
    other: Modifier.() -> Modifier,
) = if (condition) other() else this