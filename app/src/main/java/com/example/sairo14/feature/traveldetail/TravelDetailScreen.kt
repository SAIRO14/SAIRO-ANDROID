package com.example.sairo14.feature.traveldetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
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
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.map.SairoKakaoMap
import com.example.sairo14.core.map.SairoMapCameraTarget
import com.example.sairo14.core.map.SairoMapMarker
import com.example.sairo14.core.map.SairoMapViewportPadding

/**
 * Route의 코스 ID와 여행 상세 화면의 상태·행동을 연결한다.
 *
 * 코스 조회와 일차·저장 표시 상태는 [TravelDetailViewModel]이 소유하고, 뒤로가기·홈·공유 같은
 * 앱 이동 또는 외부 동작은 호출자가 소유한다.
 * @param courseId 표시할 코스의 안정적인 ID
 * @param onboardingSessionId 온보딩 분석 결과를 보관한 세션 ID. 있으면 해당 세션의 코스를 우선 표시한다
 * @param initialSaved 이전 화면에서 전달한 최신 저장 표시 상태. 없으면 상세 코스 응답을 사용한다
 * @param savedTripId 저장 해제 API에 사용할 저장 항목 ID. 없으면 `null`
 * @param onBackClick 헤더 뒤로가기 동작
 * @param onHomeClick 헤더 홈 이동 동작
 * @param onShareClick 공유 아이콘을 눌렀을 때 호출할 동작
 * @param modifier 화면 컨테이너에 적용할 Modifier
 * @param viewModel 코스 상세와 선택 상태를 소유하는 ViewModel
 */
@Composable
fun TravelDetailRoute(
    courseId: String,
    onboardingSessionId: String?,
    initialSaved: Boolean?,
    savedTripId: String?,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TravelDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(courseId, onboardingSessionId, initialSaved, savedTripId) {
        viewModel.load(
            courseId = courseId,
            onboardingSessionId = onboardingSessionId,
            initialSaved = initialSaved,
            savedTripId = savedTripId,
        )
    }

    TravelDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onShareClick = onShareClick,
        onDayClick = viewModel::selectDay,
        onPlaceClick = viewModel::selectPlace,
        onSaveClick = viewModel::onBookmarkClick,
        onRetryClick = viewModel::retry,
        modifier = modifier,
    )
}

/**
 * 여행 코스의 일차별 지도 핀과 장소 타임라인을 표시한다.
 *
 * 선택한 일차의 장소 목록을 지도와 시트에 함께 전달하며, 시트와 헤더가 차지하는 실제 높이는 지도
 * 뷰포트 여백으로 반영한다. 데이터 조회와 사용자 행동의 결과는 호출자가 [uiState]와 콜백으로 소유한다.
 * @param uiState 화면에 표시할 로딩·콘텐츠·오류 상태
 * @param onBackClick 헤더 뒤로가기 동작
 * @param onHomeClick 헤더 홈 이동 동작
 * @param onShareClick 공유 아이콘 동작
 * @param onDayClick 일차 칩을 선택했을 때 호출할 동작
 * @param onPlaceClick 장소 행을 선택했을 때 호출할 동작
 * @param onSaveClick 저장 아이콘을 눌렀을 때 호출할 동작
 * @param onRetryClick 오류 상태의 재시도 동작
 * @param modifier 화면 컨테이너에 적용할 Modifier
 */
@Composable
fun TravelDetailScreen(
    uiState: TravelDetailUiState,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onShareClick: () -> Unit,
    onDayClick: (Int) -> Unit,
    onPlaceClick: (String) -> Unit,
    onSaveClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenModifier = modifier.navigationBarsPadding()

    when (uiState) {
        TravelDetailUiState.Loading -> TravelDetailLoading(
            onBackClick = onBackClick,
            onHomeClick = onHomeClick,
            modifier = screenModifier,
        )

        is TravelDetailUiState.Content -> TravelDetailContent(
            content = uiState,
            onBackClick = onBackClick,
            onHomeClick = onHomeClick,
            onShareClick = onShareClick,
            onDayClick = onDayClick,
            onPlaceClick = onPlaceClick,
            onSaveClick = onSaveClick,
            modifier = screenModifier,
        )

        TravelDetailUiState.Error -> TravelDetailError(
            onBackClick = onBackClick,
            onHomeClick = onHomeClick,
            onRetryClick = onRetryClick,
            modifier = screenModifier,
        )
    }
}

@Composable
private fun TravelDetailContent(
    content: TravelDetailUiState.Content,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onShareClick: () -> Unit,
    onDayClick: (Int) -> Unit,
    onPlaceClick: (String) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier,
) {
    val density = LocalDensity.current
    var headerHeightPx by remember { mutableIntStateOf(0) }
    var daySelectorHeightPx by remember { mutableIntStateOf(0) }
    var sheetVisibleHeightPx by remember { mutableIntStateOf(0) }
    val headerHeight = with(density) { headerHeightPx.toDp() }
    val daySelectorHeight = with(density) { daySelectorHeightPx.toDp() }
    val sheetVisibleHeight = with(density) { sheetVisibleHeightPx.toDp() }
    val selectedDay = content.selectedDay
    val markers = selectedDay.orEmptyMarkers()
    val cameraTarget = content.selectedPlace?.toMapCameraTarget()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SairoTheme.colors.backgroundCanvas),
    ) {
        SairoKakaoMap(
            markers = markers,
            cameraTarget = cameraTarget,
            cameraRequestId = content.cameraFocusRequestId,
            viewportPadding = SairoMapViewportPadding(
                top = headerHeight + daySelectorHeight,
                bottom = sheetVisibleHeight,
            ),
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = headerHeight)
                .onSizeChanged { size -> daySelectorHeightPx = size.height },
        ) {
            Spacer(modifier = Modifier.height(DaySelectorTopSpacing))
            TravelDaySelector(
                days = content.course.days,
                selectedDayNumber = content.selectedDayNumber,
                onDayClick = onDayClick,
                modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
            )
            Spacer(modifier = Modifier.height(DaySelectorBottomSpacing))
        }

        TravelDetailSheet(
            regionName = content.course.regionName,
            isSaved = content.bookmark.isSaved,
            isBookmarkRequesting = content.bookmark.isRequesting,
            onShareClick = onShareClick,
            onSaveClick = onSaveClick,
            expandedTopInset = headerHeight,
            onVisibleHeightChanged = { visibleHeight ->
                sheetVisibleHeightPx = with(density) { visibleHeight.roundToPx() }
            },
            contentKey = content.selectedDayNumber,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (selectedDay == null || selectedDay.places.isEmpty()) {
                item(key = "empty-day") {
                    Text(
                        text = stringResource(R.string.travel_detail_empty_day),
                        color = SairoTheme.colors.textMuted,
                        style = SairoTextStyles.bodyLight16,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                itemsIndexed(
                    items = selectedDay.places,
                    key = { _, place -> place.placeId },
                ) { index, place ->
                    TravelCourseTimelineItem(
                        isLastItem = index == selectedDay.places.lastIndex,
                    ) {
                        SairoPlaceListItem(
                            title = stringResource(
                                R.string.travel_detail_place_title,
                                index + 1,
                                place.name,
                            ),
                            tags = place.tags,
                            painter = rememberAsyncImagePainter(
                                model = place.imageUrl ?: R.drawable.img_dummy_view,
                            ),
                            variant = SairoPlaceListItemVariant.Detailed,
                            imageContentDescription = stringResource(
                                R.string.travel_detail_place_image,
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
                .onSizeChanged { size -> headerHeightPx = size.height },
            onBackClick = onBackClick,
            actionIcon = androidx.compose.ui.res.painterResource(R.drawable.ic_home),
            actionContentDescription = stringResource(R.string.sairo_header_home),
            onActionClick = onHomeClick,
        )
    }
}

@Composable
private fun TravelDaySelector(
    days: List<TravelDetailDayUiModel>,
    selectedDayNumber: Int,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DayChipGap),
    ) {
        days.forEach { day ->
            SairoChip(
                text = stringResource(R.string.travel_detail_day_label, day.dayNumber),
                selected = day.dayNumber == selectedDayNumber,
                onClick = { onDayClick(day.dayNumber) },
            )
        }
    }
}

@Composable
private fun TravelDetailLoading(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier,
) {
    TravelDetailMessageLayout(
        headerTitle = stringResource(R.string.travel_detail_header_title),
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        modifier = modifier,
    ) {
        CircularProgressIndicator(color = SairoTheme.colors.accentBase)
        Spacer(modifier = Modifier.height(MessageContentGap))
        Text(
            text = stringResource(R.string.travel_detail_loading),
            color = SairoTheme.colors.textMuted,
            style = SairoTextStyles.bodyLight16,
        )
    }
}

@Composable
private fun TravelDetailError(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier,
) {
    TravelDetailMessageLayout(
        headerTitle = stringResource(R.string.travel_detail_header_title),
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.travel_detail_load_error),
            color = SairoTheme.colors.textPrimary,
            style = SairoTextStyles.bodyLight18,
        )
        Spacer(modifier = Modifier.height(MessageContentGap))
        SairoButton(
            text = stringResource(R.string.travel_detail_retry),
            onClick = onRetryClick,
        )
    }
}

@Composable
private fun TravelDetailMessageLayout(
    headerTitle: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SairoTheme.colors.backgroundCanvas),
    ) {
        SairoHeader(
            variant = SairoHeaderVariant.SubFilled,
            title = headerTitle,
            onBackClick = onBackClick,
            actionIcon = androidx.compose.ui.res.painterResource(R.drawable.ic_home),
            actionContentDescription = stringResource(R.string.sairo_header_home),
            onActionClick = onHomeClick,
        )
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )
        }
    }
}

private fun TravelDetailDayUiModel?.orEmptyMarkers(): List<SairoMapMarker> =
    this?.places?.mapIndexedNotNull { index, place ->
        val latitude = place.latitude ?: return@mapIndexedNotNull null
        val longitude = place.longitude ?: return@mapIndexedNotNull null
        SairoMapMarker(
            id = place.placeId,
            order = index + 1,
            latitude = latitude,
            longitude = longitude,
        )
    }.orEmpty()

private fun TravelDetailPlaceUiModel.toMapCameraTarget(): SairoMapCameraTarget? {
    val latitude = latitude ?: return null
    val longitude = longitude ?: return null
    return SairoMapCameraTarget(latitude = latitude, longitude = longitude)
}

private val ScreenHorizontalPadding = 16.dp
private val DaySelectorTopSpacing = 12.dp
private val DaySelectorBottomSpacing = 12.dp
private val DayChipGap = 8.dp
private val MessageContentGap = 16.dp
