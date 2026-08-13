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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.designsystem.token.SairoShadowStyles
import com.example.sairo14.core.extension.sairoDropShadow

/** 태그에 적용할 Figma 변형 조합이다. */
enum class SairoTagVariant {
    MediumLemon,
    SmallLemon,
    SmallGray,
    SmallWhite,
}

/**
 * 분위기 키워드나 장소 정보를 표시하는 비상호작용 태그를 그린다.
 *
 * Figma는 Medium 크기에 [SairoTagVariant.MediumLemon]만 정의하므로, 지원하지 않는 크기와
 * 색상 조합을 막기 위해 [variant]가 네 가지 조합을 함께 관리한다. 문구와 변형은 호출자가
 * 소유하며 태그는 상태를 변경하지 않는다.
 *
 * @param text 태그에 표시할 문구
 * @param modifier 태그에 적용할 Modifier
 * @param variant Figma에서 지원하는 크기와 색상 조합
 * @param maxLines 태그 문구에 허용할 최대 줄 수
 * @param overflow 최대 줄 수를 넘는 문구의 처리 방식
 */
@Composable
fun SairoTag(
    text: String,
    modifier: Modifier = Modifier,
    variant: SairoTagVariant = SairoTagVariant.MediumLemon,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
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
            maxLines = maxLines,
            overflow = overflow,
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
