package com.example.sairo14.feature.sharedcourse

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.rememberAsyncImagePainter
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.component.SairoButton
import com.example.sairo14.core.designsystem.component.SairoChip
import com.example.sairo14.core.designsystem.component.SairoHeader
import com.example.sairo14.core.designsystem.component.SairoHeaderVariant
import com.example.sairo14.core.designsystem.component.SairoPlaceListItem
import com.example.sairo14.core.designsystem.component.SairoPlaceListItemVariant
import com.example.sairo14.core.designsystem.component.SairoTag
import com.example.sairo14.core.designsystem.component.SairoTagVariant
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.designsystem.token.SairoShadowStyles
import com.example.sairo14.core.extension.sairoDropShadow
import com.example.sairo14.core.map.SairoKakaoMap
import com.example.sairo14.core.map.SairoMapCameraTarget
import com.example.sairo14.core.map.SairoMapMarker
import com.example.sairo14.core.map.SairoMapViewportPadding
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.isNetworkError
import com.example.sairo14.feature.error.NetworkErrorRoute
import kotlinx.coroutines.flow.distinctUntilChanged

/** 공유 링크의 스냅샷 ID와 읽기 전용 코스 화면을 연결한다.
 *
 * 조회·일차·장소 선택 상태는 [SharedCourseViewModel]이 소유하고, 뒤로가기와 홈 이동은 호출자가
 * 소유한다. 저장·공유 같은 원본 코스 소유자 전용 행동은 표시하지 않는다.
 * @param shareId 조회할 공유 스냅샷 식별자
 * @param onBackClick 헤더 뒤로가기 동작
 * @param onHomeClick 헤더 홈 이동 동작
 */
@Composable
fun SharedCourseRoute(
    shareId: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SharedCourseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(shareId) {
        viewModel.load(shareId)
    }

    SharedCourseScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onDayClick = viewModel::selectDay,
        onPlaceClick = viewModel::selectPlace,
        onRetryClick = viewModel::retry,
        modifier = modifier,
    )
}

/** 공유받은 코스의 지도와 일차별 장소 목록을 읽기 전용으로 표시한다. */
@Composable
fun SharedCourseScreen(
    uiState: SharedCourseUiState,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onDayClick: (Int) -> Unit,
    onPlaceClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState is SharedCourseUiState.Error && uiState.error.isNetworkError()) {
        NetworkErrorRoute(
            onRetryClick = onRetryClick,
            onHomeClick = onHomeClick,
            modifier = modifier,
        )
        return
    }

    when (uiState) {
        SharedCourseUiState.Loading -> SharedCourseMessageLayout(
            title = stringResource(R.string.shared_course_header_title),
            onBackClick = onBackClick,
            onHomeClick = onHomeClick,
            modifier = modifier,
        ) {
            CircularProgressIndicator(color = SairoTheme.colors.accentBase)
            Spacer(modifier = Modifier.height(MessageContentGap))
            Text(
                text = stringResource(R.string.shared_course_loading),
                color = SairoTheme.colors.textMuted,
                style = SairoTextStyles.bodyLight16,
            )
        }

        is SharedCourseUiState.Content -> SharedCourseContent(
            content = uiState,
            onBackClick = onBackClick,
            onHomeClick = onHomeClick,
            onDayClick = onDayClick,
            onPlaceClick = onPlaceClick,
            modifier = modifier,
        )

        is SharedCourseUiState.Error -> SharedCourseError(
            error = uiState.error,
            onBackClick = onBackClick,
            onHomeClick = onHomeClick,
            onRetryClick = onRetryClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun SharedCourseError(
    error: AppError,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier,
) {
    val isNotFound = error == AppError.ResourceNotFound

    SharedCourseMessageLayout(
        title = stringResource(R.string.shared_course_header_title),
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(
                if (isNotFound) R.string.shared_course_not_found else R.string.shared_course_load_error,
            ),
            color = SairoTheme.colors.textPrimary,
            style = SairoTextStyles.bodyLight18,
        )
        Spacer(modifier = Modifier.height(MessageContentGap))
        SairoButton(
            text = stringResource(
                if (isNotFound) R.string.shared_course_go_home else R.string.shared_course_retry,
            ),
            onClick = if (isNotFound) onHomeClick else onRetryClick,
        )
    }
}

@Composable
private fun SharedCourseContent(
    content: SharedCourseUiState.Content,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onDayClick: (Int) -> Unit,
    onPlaceClick: (String) -> Unit,
    modifier: Modifier,
) {
    val density = LocalDensity.current
    var headerHeightPx by remember { mutableIntStateOf(0) }
    var selectorHeightPx by remember { mutableIntStateOf(0) }
    var sheetHeightPx by remember { mutableIntStateOf(0) }
    val headerHeight = with(density) { headerHeightPx.toDp() }
    val selectorHeight = with(density) { selectorHeightPx.toDp() }
    val sheetHeight = with(density) { sheetHeightPx.toDp() }
    val selectedDay = content.selectedDay

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SairoTheme.colors.backgroundCanvas),
    ) {
        SairoKakaoMap(
            markers = selectedDay.toMarkers(),
            cameraTarget = content.selectedPlace.toCameraTarget(),
            cameraRequestId = content.cameraFocusRequestId,
            viewportPadding = SairoMapViewportPadding(
                top = headerHeight + selectorHeight,
                bottom = sheetHeight,
            ),
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = headerHeight)
                .onSizeChanged { selectorHeightPx = it.height },
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                content.course.days.forEach { day ->
                    SairoChip(
                        text = stringResource(R.string.shared_course_day_label, day.dayNumber),
                        selected = day.dayNumber == content.selectedDayNumber,
                        onClick = { onDayClick(day.dayNumber) },
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        SharedCourseSheet(
            regionName = content.course.regionName,
            expandedTopInset = headerHeight,
            contentKey = content.selectedDayNumber,
            onVisibleHeightChanged = { visibleHeight ->
                sheetHeightPx = with(density) { visibleHeight.roundToPx() }
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            if (selectedDay == null || selectedDay.places.isEmpty()) {
                item(key = "empty-day") {
                    Text(
                        text = stringResource(R.string.shared_course_empty_day),
                        color = SairoTheme.colors.textMuted,
                        style = SairoTextStyles.bodyLight16,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                itemsIndexed(selectedDay.places, key = { _, place -> place.placeId }) { index, place ->
                    SharedCourseTimelineItem(isLastItem = index == selectedDay.places.lastIndex) {
                        SairoPlaceListItem(
                            title = stringResource(
                                R.string.shared_course_place_title,
                                index + 1,
                                place.name,
                            ),
                            tags = place.tags,
                            painter = rememberAsyncImagePainter(
                                model = place.imageUrl ?: R.drawable.img_dummy_view,
                            ),
                            variant = SairoPlaceListItemVariant.Detailed,
                            imageContentDescription = stringResource(
                                R.string.shared_course_place_image,
                                place.name,
                            ),
                            onClick = { onPlaceClick(place.placeId) },
                        )
                    }
                }
            }
        }

        SairoHeader(
            variant = SairoHeaderVariant.SubFilled,
            title = content.course.regionName,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { headerHeightPx = it.height },
            onBackClick = onBackClick,
            actionIcon = painterResource(R.drawable.ic_home),
            actionContentDescription = stringResource(R.string.sairo_header_home),
            onActionClick = onHomeClick,
        )
    }
}

@Composable
private fun SharedCourseSheet(
    regionName: String,
    expandedTopInset: Dp,
    onVisibleHeightChanged: (Dp) -> Unit,
    contentKey: Any?,
    modifier: Modifier = Modifier,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit,
) {
    var sheetOffsetPx by remember { mutableFloatStateOf(Float.NaN) }
    val listState = rememberLazyListState()
    LaunchedEffect(contentKey) { listState.scrollToItem(0) }

    BoxWithConstraints(modifier = modifier.fillMaxSize().clipToBounds()) {
        val density = LocalDensity.current
        val expandedOffset = expandedTopInset.coerceIn(0.dp, maxHeight)
        val collapsedOffset = (maxHeight - SheetHeaderHeight).coerceAtLeast(expandedOffset)
        val defaultOffset = expandedOffset + (collapsedOffset - expandedOffset) * DefaultSheetOffsetFraction
        val expandedOffsetPx = with(density) { expandedOffset.toPx() }
        val collapsedOffsetPx = with(density) { collapsedOffset.toPx() }
        val defaultOffsetPx = with(density) { defaultOffset.toPx() }

        LaunchedEffect(expandedOffsetPx, collapsedOffsetPx, defaultOffsetPx) {
            sheetOffsetPx = sheetOffsetPx.takeUnless(Float::isNaN)
                ?.coerceIn(expandedOffsetPx, collapsedOffsetPx) ?: defaultOffsetPx
        }
        val dragState = rememberDraggableState { delta ->
            val currentOffset = sheetOffsetPx.takeUnless(Float::isNaN) ?: defaultOffsetPx
            sheetOffsetPx = (currentOffset + delta).coerceIn(expandedOffsetPx, collapsedOffsetPx)
        }
        LaunchedEffect(maxHeight, defaultOffsetPx) {
            snapshotFlow {
                with(density) {
                    (maxHeight.toPx() - (sheetOffsetPx.takeUnless(Float::isNaN) ?: defaultOffsetPx))
                        .coerceAtLeast(0f)
                        .toDp()
                }
            }.distinctUntilChanged().collect(onVisibleHeightChanged)
        }

        val visibleHeight = with(density) {
            (maxHeight.toPx() - (sheetOffsetPx.takeUnless(Float::isNaN) ?: defaultOffsetPx))
                .coerceAtLeast(SheetHeaderHeight.toPx())
                .toDp()
        }
        val shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        Column(
            modifier = Modifier
                .offset(y = with(density) { (sheetOffsetPx.takeUnless(Float::isNaN) ?: defaultOffsetPx).toDp() })
                .fillMaxWidth()
                .height(visibleHeight)
                .sairoDropShadow(shape, SairoShadowStyles.glowDeep)
                .clip(shape)
                .background(SairoTheme.colors.surfaceRaised),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SheetHeaderHeight)
                    .draggable(dragState, Orientation.Vertical),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(SairoTheme.colors.borderDefault),
                )
                SairoTag(
                    text = regionName,
                    variant = SairoTagVariant.MediumLemon,
                    modifier = Modifier.padding(start = 16.dp, top = 37.dp),
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun SharedCourseMessageLayout(
    title: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().background(SairoTheme.colors.backgroundCanvas),
    ) {
        SairoHeader(
            variant = SairoHeaderVariant.SubFilled,
            title = title,
            onBackClick = onBackClick,
            actionIcon = painterResource(R.drawable.ic_home),
            actionContentDescription = stringResource(R.string.sairo_header_home),
            onActionClick = onHomeClick,
        )
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, content = content)
        }
    }
}

private fun SharedCourseDayUiModel?.toMarkers(): List<SairoMapMarker> =
    this?.places?.mapIndexedNotNull { index, place ->
        val latitude = place.latitude ?: return@mapIndexedNotNull null
        val longitude = place.longitude ?: return@mapIndexedNotNull null
        SairoMapMarker(place.placeId, index + 1, latitude, longitude)
    }.orEmpty()

private fun SharedCoursePlaceUiModel?.toCameraTarget(): SairoMapCameraTarget? {
    val latitude = this?.latitude ?: return null
    val longitude = longitude ?: return null
    return SairoMapCameraTarget(latitude, longitude)
}

@Composable
private fun SharedCourseTimelineItem(
    isLastItem: Boolean,
    content: @Composable () -> Unit,
) {
    val lineColor = SairoTheme.colors.borderDefault

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (!isLastItem) {
                    val markerCenterX = TimelineMarkerSize.toPx() / 2f
                    var dashStartY = TimelineMarkerSize.toPx()
                    while (dashStartY < size.height) {
                        val dashEndY = (dashStartY + TimelineDashLength.toPx()).coerceAtMost(size.height)
                        drawLine(
                            color = lineColor,
                            start = Offset(markerCenterX, dashStartY),
                            end = Offset(markerCenterX, dashEndY),
                            strokeWidth = TimelineLineWidth.toPx(),
                        )
                        dashStartY += TimelineDashLength.toPx() + TimelineDashGap.toPx()
                    }
                }
            },
    ) {
        Box(
            modifier = Modifier.width(TimelineMarkerSize),
            contentAlignment = Alignment.TopCenter,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_location_small),
                contentDescription = null,
                modifier = Modifier.size(TimelineMarkerSize),
                tint = androidx.compose.ui.graphics.Color.Unspecified,
            )
        }
        Column(
            modifier = Modifier.weight(1f).padding(start = 8.dp),
        ) {
            content()
            if (!isLastItem) Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private const val DefaultSheetOffsetFraction = 0.39f
private val SheetHeaderHeight = 63.dp
private val MessageContentGap = 16.dp
private val TimelineMarkerSize = 20.dp
private val TimelineLineWidth = 1.dp
private val TimelineDashLength = 4.dp
private val TimelineDashGap = 4.dp
