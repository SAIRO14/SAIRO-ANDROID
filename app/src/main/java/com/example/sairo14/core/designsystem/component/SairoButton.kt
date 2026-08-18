package com.example.sairo14.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.extension.noRippleClickable

/** 주요 CTA 버튼에 적용할 Figma 크기 규격이다. */
enum class SairoButtonSize {
    Large,
    Medium,
    Small,
}

/** 주요 CTA 버튼에 적용할 시각 스타일이다. */
enum class SairoButtonStyle {
    Primary,
    Outline,
}

/**
 * 주요 CTA의 문구와 상호작용 상태를 Figma 규격으로 표시한다.
 *
 * 비활성 상태와 클릭 동작은 호출자가 [enabled], [onClick]으로 관리한다. 누름 ripple이나 별도
 * 시각 효과는 표시하지 않는다. 전체 너비 버튼이 필요하면 호출자가 [modifier]에
 * [Modifier.fillMaxWidth]를 적용한다.
 *
 * @param text 버튼에 표시할 문구
 * @param onClick 버튼 클릭 시 호출할 동작
 * @param modifier 버튼에 적용할 Modifier
 * @param size Figma의 Large, Medium 또는 Small 크기 규격
 * @param style Primary 또는 Outline 시각 스타일
 * @param enabled `false`이면 비활성 표현을 적용하고 클릭 이벤트를 전달하지 않는지 여부
 */
@Composable
fun SairoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: SairoButtonSize = SairoButtonSize.Large,
    style: SairoButtonStyle = SairoButtonStyle.Primary,
    enabled: Boolean = true,
) {
    SairoButtonContent(
        text = text,
        onClick = onClick,
        modifier = modifier,
        size = size,
        style = style,
        enabled = enabled,
    )
}

@Composable
private fun SairoButtonContent(
    text: String,
    onClick: (() -> Unit)?,
    modifier: Modifier,
    size: SairoButtonSize,
    style: SairoButtonStyle,
    enabled: Boolean,
) {
    val specification = size.specification
    val colors = SairoTheme.colors
    val outline = style == SairoButtonStyle.Outline

    val backgroundColor = when {
        !enabled -> colors.actionDisabled
        outline -> colors.actionOutlineBackground
        else -> colors.actionDefault
    }
    val contentColor = when {
        !enabled -> colors.actionTextDisabled
        outline -> colors.actionOutlineText
        else -> colors.actionText
    }
    val borderColor = if (enabled && outline) colors.actionOutlineBorder else Color.Transparent

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = specification.height)
            .height(specification.height)
            .clip(specification.shape)
            .background(backgroundColor)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(
                        width = 1.dp,
                        color = borderColor,
                        shape = specification.shape,
                    )
                } else {
                    Modifier
                },
            )
            .then(
                if (onClick != null) {
                    Modifier.noRippleClickable(
                        isEnabled = enabled,
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .padding(specification.contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = contentColor,
            style = specification.textStyle,
        )
    }
}

@Preview(name = "Sairo Button / Large", showBackground = true, widthDp = 360)
@Composable
private fun SairoButtonLargePreview() {
    SairoTheme {
        SairoButtonVariantsPreview(size = SairoButtonSize.Large)
    }
}

@Preview(name = "Sairo Button / Medium", showBackground = true, widthDp = 360)
@Composable
private fun SairoButtonMediumPreview() {
    SairoTheme {
        SairoButtonVariantsPreview(size = SairoButtonSize.Medium)
    }
}

@Preview(name = "Sairo Button / Small", showBackground = true, widthDp = 360)
@Composable
private fun SairoButtonSmallPreview() {
    SairoTheme {
        SairoButtonVariantsPreview(size = SairoButtonSize.Small)
    }
}

@Composable
private fun SairoButtonVariantsPreview(size: SairoButtonSize) {
    val colors = SairoTheme.colors
    val buttonLabel = stringResource(R.string.sairo_button_preview_label)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.backgroundCanvas)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PreviewHeader(
                text = stringResource(R.string.sairo_button_preview_primary),
                modifier = Modifier.weight(1f),
            )
            PreviewHeader(
                text = stringResource(R.string.sairo_button_preview_outline),
                modifier = Modifier.weight(1f),
            )
        }
        SairoButtonPreviewRow(
            label = stringResource(R.string.sairo_button_preview_default),
            buttonLabel = buttonLabel,
            size = size,
        )
        SairoButtonPreviewRow(
            label = stringResource(R.string.sairo_button_preview_disabled),
            buttonLabel = buttonLabel,
            size = size,
            enabled = false,
        )
    }
}

@Composable
private fun PreviewHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = SairoTheme.colors.textSubtle,
        style = SairoTextStyles.headRegular14,
    )
}

@Composable
private fun SairoButtonPreviewRow(
    label: String,
    buttonLabel: String,
    size: SairoButtonSize,
    enabled: Boolean = true,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            color = SairoTheme.colors.textSubtle,
            style = SairoTextStyles.headRegular14,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SairoButtonContent(
                text = buttonLabel,
                onClick = null,
                modifier = Modifier.weight(1f),
                size = size,
                style = SairoButtonStyle.Primary,
                enabled = enabled,
            )
            SairoButtonContent(
                text = buttonLabel,
                onClick = null,
                modifier = Modifier.weight(1f),
                size = size,
                style = SairoButtonStyle.Outline,
                enabled = enabled,
            )
        }
    }
}

private val SairoButtonSize.specification: SairoButtonSpecification
    get() = when (this) {
        SairoButtonSize.Large -> SairoButtonSpecification(
            height = 56.dp,
            horizontalPadding = 32.dp,
            cornerRadius = 14.dp,
            textStyle = SairoTextStyles.headRegular20,
        )
        SairoButtonSize.Medium -> SairoButtonSpecification(
            height = 48.dp,
            horizontalPadding = 26.dp,
            cornerRadius = 12.dp,
            textStyle = SairoTextStyles.headRegular20,
        )
        SairoButtonSize.Small -> SairoButtonSpecification(
            height = 40.dp,
            horizontalPadding = 20.dp,
            cornerRadius = 10.dp,
            textStyle = SairoTextStyles.headRegular18,
        )
    }

private data class SairoButtonSpecification(
    val height: Dp,
    val horizontalPadding: Dp,
    val cornerRadius: Dp,
    val textStyle: androidx.compose.ui.text.TextStyle,
) {
    val shape get() = RoundedCornerShape(cornerRadius)
    val contentPadding get() = PaddingValues(horizontal = horizontalPadding)
}
