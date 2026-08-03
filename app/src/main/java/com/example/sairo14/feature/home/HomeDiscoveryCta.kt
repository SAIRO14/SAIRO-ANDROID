package com.example.sairo14.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.component.SairoButton
import com.example.sairo14.core.designsystem.component.SairoFolderFrame
import com.example.sairo14.core.designsystem.component.SairoFolderVariant
import com.example.sairo14.core.designsystem.component.SairoImageCard
import com.example.sairo14.core.designsystem.component.SairoImageCardSize
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.designsystem.token.SairoShadowStyles
import com.example.sairo14.core.extension.sairoDropShadow

/**
 * 홈 중앙에서 두 사진과 폴더 프레임, 여행지 찾기 버튼을 겹쳐 표시한다.
 *
 * Figma 기준 묶음 너비를 화면의 가용 너비에 맞춰 축소하고, 카드·프레임·버튼의 상대 비율은
 * 함께 유지한다. 카드 이미지는 호출자가 소유하며, 이 컴포넌트는 클릭 동작만 전달한다.
 * @param backPainter 뒤쪽 사진 카드에 표시할 이미지
 * @param frontPainter 앞쪽 사진 카드에 표시할 이미지
 * @param onClick 여행지 찾기 버튼을 눌렀을 때 호출할 동작
 * @param modifier CTA 묶음에 적용할 Modifier
 */
@Composable
fun HomeDiscoveryCta(
    backPainter: Painter?,
    frontPainter: Painter?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        val scale = (maxWidth / DesignWidth).coerceAtMost(1f)
        val cardWidth = CardWidth * scale
        val buttonWidth = ButtonWidth * scale
        val buttonHeight = ButtonHeight * scale

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CtaHeight * scale),
        ) {
            SairoImageCard(
                painter = backPainter,
                selected = false,
                size = SairoImageCardSize.Medium,
                cardWidth = cardWidth,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .rotate(BackCardRotation)
                    .sairoDropShadow(
                        shape = CardShape,
                        shadowStyle = SairoShadowStyles.deepRight,
                    ),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(FrontCardRotation),
            ) {
                SairoImageCard(
                    painter = frontPainter,
                    selected = false,
                    size = SairoImageCardSize.Medium,
                    cardWidth = cardWidth,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .sairoDropShadow(
                            shape = CardShape,
                            shadowStyle = SairoShadowStyles.deepRight,
                        ),
                )

                SairoFolderFrame(
                    variant = SairoFolderVariant.Medium,
                    frameWidth = FolderWidth * scale,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = ButtonBottomPadding * scale)
                        .size(buttonWidth, buttonHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    SairoButton(
                        text = stringResource(R.string.home_find_trip),
                        onClick = onClick,
                        modifier = Modifier
                            .size(ButtonWidth, ButtonHeight)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            },
                    )
                }
            }
        }
    }
}

private val DesignWidth = 294.dp
private val CtaHeight = 375.dp
private val CardWidth = 260.dp
private val FolderWidth = 300.dp
private val ButtonWidth = 268.dp
private val ButtonHeight = 56.dp
private val ButtonBottomPadding = 16.dp
private val CardShape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
private const val BackCardRotation = 1f
private const val FrontCardRotation = -3f

@Preview(name = "Home Discovery CTA", showBackground = true, widthDp = 360, heightDp = 420)
@Composable
private fun HomeDiscoveryCtaPreview() {
    SairoTheme {
        HomeDiscoveryCta(
            backPainter = androidx.compose.ui.res.painterResource(R.drawable.img_dummy_view),
            frontPainter = androidx.compose.ui.res.painterResource(R.drawable.img_dummy_view),
            onClick = {},
        )
    }
}
