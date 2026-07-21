package com.example.sairo14.core.extension

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import com.example.sairo14.core.designsystem.token.SairoShadowStyle

/** Applies every layer in a Figma shadow style behind the modified content. */
fun Modifier.sairoDropShadow(
    shape: Shape,
    shadowStyle: SairoShadowStyle,
): Modifier = shadowStyle.layers.fold(this) { modifier, layer ->
    modifier.dropShadow(
        shape = shape,
        shadow = Shadow(
            radius = layer.blurRadius,
            spread = layer.spread,
            color = layer.color,
            offset = layer.offset,
        ),
    )
}
