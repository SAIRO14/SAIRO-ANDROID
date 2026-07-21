package com.example.sairo14.core.designsystem.token

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/** One layer within a shadow style exported from the Sairo Figma design system. */
@Immutable
internal data class SairoShadowLayer(
    val offset: DpOffset,
    val blurRadius: Dp,
    val spread: Dp = 0.dp,
    val color: Color,
)

/** A named Figma shadow style that can contain one or more shadow layers. */
@Immutable
class SairoShadowStyle internal constructor(
    internal val layers: List<SairoShadowLayer>,
)

/** Figma shadow styles. Select a style by its Figma name until a UI usage guide is established. */
object SairoShadowStyles {
    val mediumRight = SairoShadowStyle(listOf(
        SairoShadowLayer(
            offset = DpOffset.Zero,
            blurRadius = 40.dp,
            color = SairoColor.Gray600.copy(alpha = 0.20f),
        ),
        SairoShadowLayer(
            offset = DpOffset(x = 16.dp, y = 16.dp),
            blurRadius = 16.dp,
            color = SairoColor.Gray700.copy(alpha = 0.20f),
        ),
    ))

    val deepRight = SairoShadowStyle(listOf(
        SairoShadowLayer(
            offset = DpOffset.Zero,
            blurRadius = 40.dp,
            color = SairoColor.Gray700.copy(alpha = 0.50f),
        ),
        SairoShadowLayer(
            offset = DpOffset(x = 32.dp, y = 24.dp),
            blurRadius = 16.dp,
            spread = 16.dp,
            color = SairoColor.Gray700.copy(alpha = 0.30f),
        ),
    ))

    val glowSubtle = SairoShadowStyle(listOf(
        SairoShadowLayer(
            offset = DpOffset.Zero,
            blurRadius = 12.dp,
            color = SairoColor.Gray500.copy(alpha = 0.50f),
        ),
    ))

    val glowDefault = SairoShadowStyle(listOf(
        SairoShadowLayer(
            offset = DpOffset.Zero,
            blurRadius = 40.dp,
            color = SairoColor.Gray600.copy(alpha = 0.30f),
        ),
        SairoShadowLayer(
            offset = DpOffset(x = 0.dp, y = 8.dp),
            blurRadius = 16.dp,
            color = SairoColor.Gray500.copy(alpha = 0.30f),
        ),
    ))

    val glowDeep = SairoShadowStyle(listOf(
        SairoShadowLayer(
            offset = DpOffset.Zero,
            blurRadius = 40.dp,
            color = SairoColor.Gray600.copy(alpha = 0.50f),
        ),
    ))
}
