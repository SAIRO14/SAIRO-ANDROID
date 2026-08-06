package com.example.sairo14.feature.traveldetail

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.component.SairoBookmarker
import com.example.sairo14.core.designsystem.component.SairoTag
import com.example.sairo14.core.designsystem.component.SairoTagVariant
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.designsystem.token.SairoShadowStyles
import com.example.sairo14.core.extension.sairoDropShadow
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.distinctUntilChanged

/** 여행 상세 시트가 멈출 수 있는 세 위치다. */
internal enum class TravelDetailSheetValue {
    Expanded,
    Default,
    Collapsed,
}

/**
 * 여행 코스와 장소 목록을 세 단계로 펼칠 수 있는 상세 시트를 표시한다.
 *
 * 드래그는 상단 손잡이와 액션 영역에서만 처리하며, [content]는 별도로 스크롤된다. 시트가
 * 지도 위를 가리는 높이는 [onVisibleHeightChanged]로 외부에 전달해 지도 뷰포트에 반영할 수 있다.
 * @param regionName 여행 지역 태그에 표시할 문구
 * @param isSaved 현재 여행 저장 여부
 * @param onShareClick 공유 아이콘을 눌렀을 때 호출할 동작
 * @param onSaveClick 저장 아이콘을 눌렀을 때 호출할 동작
 * @param expandedTopInset 시트가 완전히 펼쳐졌을 때 화면 위에서 남길 영역
 * @param modifier 시트 컨테이너에 적용할 Modifier
 * @param initialValue 처음 표시할 시트 위치
 * @param onSheetValueChanged 시트가 새 위치에 멈췄을 때 호출할 콜백
 * @param onVisibleHeightChanged 현재 화면에 보이는 시트 높이를 전달하는 콜백
 * @param content 시트 본문에 표시할 코스 목록 콘텐츠
 */
@Composable
internal fun TravelDetailSheet(
    regionName: String,
    isSaved: Boolean,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit,
    expandedTopInset: Dp,
    modifier: Modifier = Modifier,
    initialValue: TravelDetailSheetValue = TravelDetailSheetValue.Default,
    onSheetValueChanged: (TravelDetailSheetValue) -> Unit = {},
    onVisibleHeightChanged: (Dp) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = remember { AnchoredDraggableState(initialValue) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds(),
    ) {
        val density = LocalDensity.current
        val containerHeight = maxHeight
        val expandedOffset = expandedTopInset.coerceIn(0.dp, containerHeight)
        val collapsedOffset = (containerHeight - CollapsedSheetVisibleHeight)
            .coerceAtLeast(expandedOffset)
        val defaultOffset = expandedOffset +
            (collapsedOffset - expandedOffset) * DefaultSheetAnchorFraction
        val anchors = remember(expandedOffset, defaultOffset, collapsedOffset, density) {
            DraggableAnchors {
                TravelDetailSheetValue.Expanded at with(density) { expandedOffset.toPx() }
                TravelDetailSheetValue.Default at with(density) { defaultOffset.toPx() }
                TravelDetailSheetValue.Collapsed at with(density) { collapsedOffset.toPx() }
            }
        }

        SideEffect {
            sheetState.updateAnchors(anchors)
        }

        LaunchedEffect(sheetState) {
            snapshotFlow { sheetState.settledValue }
                .distinctUntilChanged()
                .collect(onSheetValueChanged)
        }
        LaunchedEffect(sheetState, containerHeight) {
            snapshotFlow {
                val offset = sheetState.offset
                if (offset.isNaN()) {
                    0.dp
                } else {
                    with(density) {
                        (containerHeight.toPx() - offset).coerceAtLeast(0f).toDp()
                    }
                }
            }
                .distinctUntilChanged()
                .collect(onVisibleHeightChanged)
        }

        val sheetOffset = sheetState.offset.takeUnless(Float::isNaN)
            ?: with(density) { defaultOffset.toPx() }
        val sheetShape = RoundedCornerShape(
            topStart = SheetCornerRadius,
            topEnd = SheetCornerRadius,
        )

        Column(
            modifier = Modifier
                .offset { IntOffset(x = 0, y = sheetOffset.roundToInt()) }
                .fillMaxWidth()
                .height(containerHeight)
                .sairoDropShadow(
                    shape = sheetShape,
                    shadowStyle = SairoShadowStyles.glowDeep,
                )
                .clip(sheetShape)
                .background(SairoTheme.colors.surfaceRaised),
        ) {
            SheetHeader(
                regionName = regionName,
                isSaved = isSaved,
                onShareClick = onShareClick,
                onSaveClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .anchoredDraggable(
                        state = sheetState,
                        orientation = Orientation.Vertical,
                    ),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = SheetHorizontalPadding,
                        end = SheetHorizontalPadding,
                        bottom = SheetBottomPadding,
                    ),
                content = content,
            )
        }
    }
}

@Composable
private fun SheetHeader(
    regionName: String,
    isSaved: Boolean,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SheetHandleAreaHeight),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(SheetHandleWidth)
                    .height(SheetHandleHeight)
                    .clip(SheetHandleShape)
                    .background(SairoTheme.colors.borderDefault),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SheetHorizontalPadding)
                .padding(bottom = SheetHeaderBottomPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SheetActionGap),
        ) {
            SairoTag(
                text = regionName,
                variant = SairoTagVariant.MediumLemon,
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onShareClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_share),
                    contentDescription = stringResource(R.string.travel_detail_share),
                    modifier = Modifier.size(SheetActionIconSize),
                    tint = Color.Unspecified,
                )
            }
            SairoBookmarker(
                saved = isSaved,
                onClick = onSaveClick,
                size = SheetActionIconSize,
                touchTargetSize = SheetActionTouchTargetSize,
            )
        }
    }
}

private const val DefaultSheetAnchorFraction = 0.39f
private val CollapsedSheetVisibleHeight = 160.dp
private val SheetCornerRadius = 20.dp
private val SheetHandleAreaHeight = 24.dp
private val SheetHandleWidth = 36.dp
private val SheetHandleHeight = 4.dp
private val SheetHandleShape = RoundedCornerShape(999.dp)
private val SheetHorizontalPadding = 16.dp
private val SheetHeaderBottomPadding = 8.dp
private val SheetActionGap = 4.dp
private val SheetActionIconSize = 24.dp
private val SheetActionTouchTargetSize = 48.dp
private val SheetBottomPadding = 24.dp
