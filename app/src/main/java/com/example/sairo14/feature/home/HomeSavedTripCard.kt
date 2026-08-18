package com.example.sairo14.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.designsystem.token.SairoShadowStyles
import com.example.sairo14.core.extension.noRippleClickable
import com.example.sairo14.core.extension.sairoDropShadow
import kotlin.random.Random

/**
 * 이동 가능한 Home 캔버스에서 저장한 여행지 하나를 사진 카드로 표시한다.
 *
 * 카드는 고정 슬롯에 배치되며, 회전값만 Composition마다 한 번 무작위로 결정된다. 사진과 지역명,
 * 선택 동작은 호출자가 소유하고 이 컴포넌트는 Figma의 카드·캡션·그림자 표현을 담당한다.
 * @param savedTrip 카드에 표시할 저장 여행지 요약 정보
 * @param painter 카드 사진에 표시할 이미지
 * @param onClick 카드를 눌렀을 때 호출할 동작
 * @param modifier 카드 위치와 크기에 적용할 Modifier
 */
@Composable
fun HomeSavedTripCard(
    savedTrip: HomeSavedTripUiModel,
    painter: Painter?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation = remember(savedTrip.savedTripId) {
        Random.nextDouble(
            from = -MaxSubtleCardRotationDegrees.toDouble(),
            until = MaxSubtleCardRotationDegrees.toDouble(),
        ).toFloat()
    }
    val colors = SairoTheme.colors

    Box(
        modifier = modifier
            .width(HomeSavedTripCardWidth)
            .height(HomeSavedTripCardHeight)
            .rotate(rotation)
            .sairoDropShadow(
                shape = SavedTripCardShape,
                shadowStyle = SairoShadowStyles.mediumRight,
            )
            .clip(SavedTripCardShape)
            .background(colors.surfaceSunken)
            .noRippleClickable(
                onClick = onClick,
                role = Role.Button,
            ),
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(HomeSavedTripCardWidth)
                .height(CaptionHeight)
                .background(colors.overlayScrim)
                .padding(horizontal = CaptionHorizontalPadding),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = savedTrip.regionName,
                color = colors.highlightBase,
                style = SairoTextStyles.headRegular16,
            )
        }
    }
}

/** 홈 캔버스 배치 정책과 사진 카드가 공유하는 카드의 고정 너비다. */
internal val HomeSavedTripCardWidth = 150.dp

/** 홈 캔버스 배치 정책과 사진 카드가 공유하는 카드의 고정 높이다. */
internal val HomeSavedTripCardHeight = 195.dp

private val CaptionHeight = 40.dp
private val CaptionHorizontalPadding = 12.dp
private val SavedTripCardShape = RoundedCornerShape(4.dp)
private const val MaxSubtleCardRotationDegrees = 7f
