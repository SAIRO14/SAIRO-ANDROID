package com.example.sairo14.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.designsystem.token.SairoShadowStyles
import com.example.sairo14.core.extension.sairoDropShadow
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 두 장의 사진 카드를 회전해 겹친 장식 이미지를 표시한다.
 *
 * 두 이미지와 실제 카드 너비는 호출자가 소유한다. 이 컴포넌트는 Medium 카드의 비율과 Figma의
 * 회전·그림자만 적용하므로, 화면은 [cardWidth]와 [modifier]로 위치 및 화면 크기 대응을 결정한다.
 * 그림자는 카드 좌표를 기준으로 적용된 뒤 회전하므로, 회전된 화면에서는 카드의 왼쪽·아래 방향으로 보일 수 있다.
 * @param backPainter 뒤에 표시할 이미지
 * @param frontPainter 앞에 표시할 이미지
 * @param modifier 겹친 카드 묶음에 적용할 Modifier
 * @param cardWidth 각 카드의 실제 가로 길이
 */
@Composable
fun SairoOverlappingImageCards(
    backPainter: Painter?,
    frontPainter: Painter?,
    modifier: Modifier = Modifier,
    cardWidth: Dp = DefaultCardWidth,
) {
    val cardHeight = cardWidth / MediumCardAspectRatio
    val backBounds = rotatedBounds(
        width = cardWidth,
        height = cardHeight,
        rotation = BackCardRotation,
    )
    val frontBounds = rotatedBounds(
        width = cardWidth,
        height = cardHeight,
        rotation = FrontCardRotation,
    )
    val cardGroupSize = DpSize(
        width = maxOf(backBounds.width, frontBounds.width),
        height = maxOf(backBounds.height, frontBounds.height),
    )
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier.size(cardGroupSize),
        contentAlignment = Alignment.Center,
    ) {
        SairoImageCard(
            painter = backPainter,
            selected = false,
            size = SairoImageCardSize.Medium,
            cardWidth = cardWidth,
            modifier = Modifier
                .align(Alignment.Center)
                .rotate(BackCardRotation)
                .sairoDropShadow(
                    shape = shape,
                    shadowStyle = SairoShadowStyles.mediumTopRight,
                ),
        )
        SairoImageCard(
            painter = frontPainter,
            selected = false,
            size = SairoImageCardSize.Medium,
            cardWidth = cardWidth,
            modifier = Modifier
                .align(Alignment.Center)
                .rotate(FrontCardRotation)
                .sairoDropShadow(
                    shape = shape,
                    shadowStyle = SairoShadowStyles.mediumTopRight,
                ),
        )
    }
}

private const val MediumCardAspectRatio = 260f / 347f
private val DefaultCardWidth = 260.dp
private const val BackCardRotation = 85f
private const val FrontCardRotation = 90f

private fun rotatedBounds(
    width: Dp,
    height: Dp,
    rotation: Float,
): DpSize {
    val radians = Math.toRadians(rotation.toDouble())
    val absoluteCosine = abs(cos(radians)).toFloat()
    val absoluteSine = abs(sin(radians)).toFloat()

    return DpSize(
        width = width * absoluteCosine + height * absoluteSine,
        height = width * absoluteSine + height * absoluteCosine,
    )
}

@Preview(name = "Sairo Overlapping Image Cards", showBackground = true, widthDp = 400, heightDp = 340)
@Composable
private fun SairoOverlappingImageCardsPreview() {
    SairoTheme {
        Box(
            modifier = Modifier.background(SairoTheme.colors.backgroundCanvas),
            contentAlignment = Alignment.Center,
        ) {
            SairoOverlappingImageCards(
                backPainter = androidx.compose.ui.res.painterResource(R.drawable.img_dummy_view),
                frontPainter = androidx.compose.ui.res.painterResource(R.drawable.img_dummy_view),
            )
        }
    }
}
