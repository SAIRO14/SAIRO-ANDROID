package com.example.sairo14.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.designsystem.token.SairoShadowStyles
import com.example.sairo14.core.extension.sairoDropShadow
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** 이미지 카드에 적용할 Figma 크기 규격이다. */
enum class SairoImageCardSize {
    Large,
    Medium,
}

/**
 * 이미지 기반 콘텐츠 카드와 선택 상태를 표시한다.
 *
 * 선택 상태는 호출자가 [selected]로 소유하며, 이 컴포넌트는 선택 시 테두리·gradient·체크
 * 아이콘만 표시한다. [onClick]이 있을 때만 클릭할 수 있으며, `null` [painter]는 빈 이미지
 * 상태를 표현한다.
 *
 * @param painter 카드에 표시할 이미지. `null`이면 빈 이미지 상태를 표시한다.
 * @param selected 현재 카드의 선택 여부
 * @param modifier 카드에 적용할 Modifier
 * @param size Figma의 Large 또는 Medium 크기 규격
 * @param contentDescription 카드 이미지의 접근성 설명
 * @param enabled `false`이면 클릭 이벤트를 전달하지 않는지 여부
 * @param onClick 카드를 클릭했을 때 호출할 콜백. `null`이면 클릭 기능을 적용하지 않는다.
 */
@Composable
fun SairoImageCard(
    painter: Painter?,
    selected: Boolean,
    modifier: Modifier = Modifier,
    size: SairoImageCardSize = SairoImageCardSize.Large,
    contentDescription: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val specification = size.specification
    val shape = RoundedCornerShape(24.dp)
    val colors = SairoTheme.colors
    val selectionBorderBrush = rememberSelectionBorderBrush(
        size = specification.cardSize,
        startColor = colors.accentBase,
        endColor = colors.highlightBase,
    )

    Box(
        modifier = modifier
            .size(specification.cardSize)
            .then(
                if (selected) {
                    Modifier.sairoDropShadow(
                        shape = shape,
                        shadowStyle = SairoShadowStyles.glowDefault,
                    )
                } else {
                    Modifier
                },
            )
            .clip(shape)
            .background(colors.surfaceSunken)
            .then(
                if (selected) {
                    Modifier.border(
                        width = 3.dp,
                        brush = selectionBorderBrush,
                        shape = shape,
                    )
                } else {
                    Modifier
                },
            )
            .semantics { this.selected = selected }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        enabled = enabled,
                        interactionSource = null,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 24.dp)
                    .size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_circle_2),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = colors.accentBase,
                )
            }
        }
    }
}

private val SairoImageCardSize.specification: SairoImageCardSpecification
    get() = when (this) {
        SairoImageCardSize.Large -> SairoImageCardSpecification(DpSize(300.dp, 400.dp))
        SairoImageCardSize.Medium -> SairoImageCardSpecification(DpSize(260.dp, 347.dp))
    }

private data class SairoImageCardSpecification(
    val cardSize: DpSize,
)

@Composable
private fun rememberSelectionBorderBrush(
    size: DpSize,
    startColor: Color,
    endColor: Color,
): Brush {
    val density = LocalDensity.current

    return remember(size, startColor, endColor, density) {
        val width = with(density) { size.width.toPx() }
        val height = with(density) { size.height.toPx() }
        val directionRadians = Math.toRadians(40.0)
        val direction = Offset(
            x = cos(directionRadians).toFloat(),
            y = sin(directionRadians).toFloat(),
        )
        val center = Offset(x = width / 2f, y = height / 2f)
        // The projection length reaches every card corner, preventing an unfilled edge.
        val halfLength = abs(direction.x) * width / 2f + abs(direction.y) * height / 2f

        Brush.linearGradient(
            colors = listOf(startColor, endColor),
            start = center - direction * halfLength,
            end = center + direction * halfLength,
        )
    }
}

@Preview(name = "Sairo Image Card / Large", showBackground = true)
@Composable
private fun SairoImageCardLargePreview() {
    SairoTheme {
        SairoImageCardPreview(size = SairoImageCardSize.Large, selected = false)
    }
}

@Preview(name = "Sairo Image Card / Medium", showBackground = true)
@Composable
private fun SairoImageCardMediumPreview() {
    SairoTheme {
        SairoImageCardPreview(size = SairoImageCardSize.Medium, selected = false)
    }
}

@Preview(name = "Sairo Image Card / Large Selected", showBackground = true)
@Composable
private fun SairoImageCardLargeSelectedPreview() {
    SairoTheme {
        SairoImageCardPreview(size = SairoImageCardSize.Large, selected = true)
    }
}

@Preview(name = "Sairo Image Card / Large Selected Empty", showBackground = true)
@Composable
private fun SairoImageCardSelectedEmptyPreview() {
    SairoTheme {
        SairoImageCardPreview(
            size = SairoImageCardSize.Large,
            selected = true,
            hasImage = false,
        )
    }
}

@Composable
private fun SairoImageCardPreview(
    size: SairoImageCardSize,
    selected: Boolean,
    hasImage: Boolean = true,
) {
    Column(
        modifier = Modifier
            .background(SairoTheme.colors.backgroundCanvas)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(
                when {
                    selected && !hasImage -> R.string.sairo_image_card_preview_selected_empty
                    selected -> R.string.sairo_image_card_preview_selected
                    else -> R.string.sairo_image_card_preview_default
                },
            ),
            color = SairoTheme.colors.textSubtle,
            style = SairoTextStyles.headRegular14,
        )
        SairoImageCard(
            painter = if (hasImage) painterResource(R.drawable.img_dummy_view) else null,
            selected = selected,
            size = size,
        )
    }
}
