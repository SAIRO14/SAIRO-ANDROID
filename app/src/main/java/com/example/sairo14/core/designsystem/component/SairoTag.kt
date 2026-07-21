package com.example.sairo14.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.designsystem.token.SairoShadowStyles
import com.example.sairo14.core.extension.sairoDropShadow

/** The four tag variants defined in the Sairo Figma design system. */
enum class SairoTagVariant {
    MediumLemon,
    SmallLemon,
    SmallGray,
    SmallWhite,
}

/**
 * Displays a non-interactive keyword or place-information tag.
 *
 * Figma defines only [SairoTagVariant.MediumLemon] for the medium size. The remaining
 * colors are available in the small size, so the variant is represented as one enum to
 * prevent unsupported size and color combinations.
 */
@Composable
fun SairoTag(
    text: String,
    modifier: Modifier = Modifier,
    variant: SairoTagVariant = SairoTagVariant.MediumLemon,
) {
    val specification = variant.specification

    Box(
        modifier = modifier
            .then(
                if (specification.hasShadow) {
                    Modifier.sairoDropShadow(
                        shape = RectangleShape,
                        shadowStyle = SairoShadowStyles.glowSubtle,
                    )
                } else {
                    Modifier
                },
            )
            .background(specification.backgroundColor())
            .padding(horizontal = specification.horizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = specification.contentColor(),
            style = specification.textStyle,
        )
    }
}

private val SairoTagVariant.specification: SairoTagSpecification
    get() = when (this) {
        SairoTagVariant.MediumLemon -> SairoTagSpecification(
            horizontalPadding = 10.dp,
            textStyle = SairoTextStyles.bodyLight18,
            backgroundColor = { SairoTheme.colors.chipLimeBackground },
            contentColor = { SairoTheme.colors.chipLimeText },
            hasShadow = true,
        )
        SairoTagVariant.SmallLemon -> SairoTagSpecification(
            horizontalPadding = 6.dp,
            textStyle = SairoTextStyles.bodyLight14,
            backgroundColor = { SairoTheme.colors.chipLimeBackground },
            contentColor = { SairoTheme.colors.chipLimeText },
            hasShadow = true,
        )
        SairoTagVariant.SmallGray -> SairoTagSpecification(
            horizontalPadding = 6.dp,
            textStyle = SairoTextStyles.bodyLight14,
            backgroundColor = { SairoTheme.colors.surfaceSunken },
            contentColor = { SairoTheme.colors.textMuted },
            hasShadow = false,
        )
        SairoTagVariant.SmallWhite -> SairoTagSpecification(
            horizontalPadding = 6.dp,
            textStyle = SairoTextStyles.bodyLight14,
            backgroundColor = { SairoTheme.colors.surfaceRaised },
            contentColor = { SairoTheme.colors.textMuted },
            hasShadow = true,
        )
    }

private data class SairoTagSpecification(
    val horizontalPadding: androidx.compose.ui.unit.Dp,
    val textStyle: androidx.compose.ui.text.TextStyle,
    val backgroundColor: @Composable () -> androidx.compose.ui.graphics.Color,
    val contentColor: @Composable () -> androidx.compose.ui.graphics.Color,
    val hasShadow: Boolean,
)

@Preview(name = "Sairo Tag", showBackground = true, widthDp = 360)
@Composable
private fun SairoTagPreview() {
    SairoTheme {
        val tagText = stringResource(R.string.sairo_tag_preview_label)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SairoTheme.colors.backgroundCanvas)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SairoTagPreviewItem(
                label = stringResource(R.string.sairo_tag_preview_medium_lemon),
                text = tagText,
                variant = SairoTagVariant.MediumLemon,
            )
            SairoTagPreviewItem(
                label = stringResource(R.string.sairo_tag_preview_small_lemon),
                text = tagText,
                variant = SairoTagVariant.SmallLemon,
            )
            SairoTagPreviewItem(
                label = stringResource(R.string.sairo_tag_preview_small_gray),
                text = tagText,
                variant = SairoTagVariant.SmallGray,
            )
            SairoTagPreviewItem(
                label = stringResource(R.string.sairo_tag_preview_small_white),
                text = tagText,
                variant = SairoTagVariant.SmallWhite,
            )
        }
    }
}

@Composable
private fun SairoTagPreviewItem(
    label: String,
    text: String,
    variant: SairoTagVariant,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            color = SairoTheme.colors.textSubtle,
            style = SairoTextStyles.headRegular14,
        )
        SairoTag(text = text, variant = variant)
    }
}
