package com.example.sairo14.feature.traveldetail

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.component.SairoBookmarker
import com.example.sairo14.core.designsystem.component.SairoTag
import com.example.sairo14.core.designsystem.component.SairoTagVariant
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.designsystem.token.SairoShadowStyles
import com.example.sairo14.core.extension.sairoDropShadow
import com.example.sairo14.core.extension.noRippleClickable
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * 여행 코스와 장소 목록을 자유롭게 펼칠 수 있는 상세 시트를 표시한다.
 *
 * 드래그는 상단 손잡이와 액션 영역에서만 처리하며, 손을 놓으면 현재 위치에 그대로 머문다.
 * [content]는 별도로 스크롤되며, 시트가 지도 위를 가리는 높이는 [onVisibleHeightChanged]로 외부에
 * 전달해 지도 뷰포트에 반영할 수 있다.
 * @param regionName 여행 지역 태그에 표시할 문구
 * @param isSaved 현재 여행 저장 여부
 * @param onShareClick 공유 아이콘을 눌렀을 때 호출할 동작
 * @param onSaveClick 저장 아이콘을 눌렀을 때 호출할 동작
 * @param expandedTopInset 시트가 완전히 펼쳐졌을 때 화면 위에서 남길 영역
 * @param modifier 시트 컨테이너에 적용할 Modifier
 * @param onVisibleHeightChanged 현재 화면에 보이는 시트 높이를 전달하는 콜백
 * @param contentKey 본문 데이터가 바뀌었을 때 목록을 처음으로 되돌릴 식별자
 * @param content 시트 본문에 지연 구성할 코스 목록 콘텐츠
 */
@Composable
internal fun TravelDetailSheet(
    regionName: String,
    isSaved: Boolean,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit,
    expandedTopInset: Dp,
    modifier: Modifier = Modifier,
    onVisibleHeightChanged: (Dp) -> Unit = {},
    contentKey: Any? = null,
    content: LazyListScope.() -> Unit,
) {
    var sheetOffsetPx by remember { mutableFloatStateOf(Float.NaN) }
    val listState = rememberLazyListState()

    LaunchedEffect(contentKey) {
        listState.scrollToItem(0)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds(),
    ) {
        val density = LocalDensity.current
        val containerHeight = maxHeight
        val expandedOffset = expandedTopInset.coerceIn(0.dp, containerHeight)
        val collapsedOffset = (containerHeight - SheetHeaderHeight)
            .coerceAtLeast(expandedOffset)
        val defaultOffset = expandedOffset +
            (collapsedOffset - expandedOffset) * DefaultSheetOffsetFraction
        val expandedOffsetPx = with(density) { expandedOffset.toPx() }
        val collapsedOffsetPx = with(density) { collapsedOffset.toPx() }
        val defaultOffsetPx = with(density) { defaultOffset.toPx() }

        LaunchedEffect(expandedOffsetPx, collapsedOffsetPx, defaultOffsetPx) {
            sheetOffsetPx = sheetOffsetPx
                .takeUnless(Float::isNaN)
                ?.coerceIn(expandedOffsetPx, collapsedOffsetPx)
                ?: defaultOffsetPx
        }

        val dragState = rememberDraggableState { delta ->
            val currentOffset = sheetOffsetPx.takeUnless(Float::isNaN) ?: defaultOffsetPx
            sheetOffsetPx = (currentOffset + delta).coerceIn(expandedOffsetPx, collapsedOffsetPx)
        }

        LaunchedEffect(containerHeight, defaultOffsetPx) {
            snapshotFlow {
                val offset = sheetOffsetPx.takeUnless(Float::isNaN) ?: defaultOffsetPx
                with(density) {
                    (containerHeight.toPx() - offset).coerceAtLeast(0f).toDp()
                }
            }
                .distinctUntilChanged()
                .collect(onVisibleHeightChanged)
        }

        val sheetOffset = sheetOffsetPx.takeUnless(Float::isNaN) ?: defaultOffsetPx
        val visibleSheetHeight = with(density) {
            (containerHeight.toPx() - sheetOffset)
                .coerceAtLeast(SheetHeaderHeight.toPx())
                .toDp()
        }
        val sheetShape = RoundedCornerShape(
            topStart = SheetCornerRadius,
            topEnd = SheetCornerRadius,
        )

        Column(
            modifier = Modifier
                .offset(y = with(density) { sheetOffset.toDp() })
                .fillMaxWidth()
                .height(visibleSheetHeight)
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
                    .draggable(
                        state = dragState,
                        orientation = Orientation.Vertical,
                    ),
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(
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
    Box(
        modifier = modifier.height(SheetHeaderHeight),
    ) {
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
                .align(Alignment.TopStart)
                .padding(horizontal = SheetHorizontalPadding)
                .padding(top = SheetActionTopPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SheetActionGap),
        ) {
            SairoTag(
                text = regionName,
                variant = SairoTagVariant.MediumLemon,
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(SheetActionTouchTargetSize)
                    .noRippleClickable(
                        onClick = onShareClick,
                        role = Role.Button,
                    ),
                contentAlignment = Alignment.Center,
            ) {
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

private const val DefaultSheetOffsetFraction = 0.39f
private val SheetCornerRadius = 20.dp
private val SheetHeaderHeight = 63.dp
private val SheetHandleAreaHeight = 24.dp
private val SheetHandleWidth = 36.dp
private val SheetHandleHeight = 4.dp
private val SheetHandleShape = RoundedCornerShape(999.dp)
private val SheetHorizontalPadding = 16.dp
private val SheetActionTopPadding = 13.dp
private val SheetActionGap = 4.dp
private val SheetActionIconSize = 24.dp
private val SheetActionTouchTargetSize = 48.dp
private val SheetBottomPadding = 24.dp
