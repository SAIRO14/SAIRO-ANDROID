package com.example.sairo14.feature.savedtrip

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.component.SairoBackdropHost
import com.example.sairo14.core.designsystem.component.SairoBackdropState
import com.example.sairo14.core.designsystem.component.SairoButton
import com.example.sairo14.core.designsystem.component.SairoButtonStyle
import com.example.sairo14.core.designsystem.component.SairoHeader
import com.example.sairo14.core.designsystem.component.SairoHeaderVariant
import com.example.sairo14.core.designsystem.component.SairoPlaceFolderCard
import com.example.sairo14.core.designsystem.component.rememberSairoBackdropImagePainter
import com.example.sairo14.core.designsystem.component.rememberSairoBackdropState
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.domain.model.AppError
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

/**
 * 저장 목록 상태와 화면 이동·재시도 행동을 연결한다.
 *
 * 목록 조회 상태는 [SavedTripsViewModel]이 소유하고, 뒤로가기·홈·여행지 탐색 이동은 호출자가
 * 소유한다.
 * @param onBackClick 뒤로가기 헤더 액션을 눌렀을 때 호출할 동작
 * @param onHomeClick 홈 헤더 액션을 눌렀을 때 호출할 동작
 * @param onFindTripClick 빈 상태의 여행지 탐색 CTA를 눌렀을 때 호출할 동작
 * @param onTripClick 폴더 카드의 코스를 눌렀을 때 호출할 동작
 * @param modifier 화면 컨테이너에 적용할 Modifier
 * @param viewModel 저장 목록 조회 상태를 소유하는 ViewModel
 */
@Composable
fun SavedTripsRoute(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onFindTripClick: () -> Unit,
    onTripClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavedTripsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SavedTripsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onFindTripClick = onFindTripClick,
        onRetryClick = viewModel::retry,
        onLoadMore = viewModel::loadMore,
        onBookmarkClick = viewModel::removeSavedTrip,
        onTripClick = onTripClick,
        modifier = modifier,
    )
}

/**
 * 저장된 여행지의 폴더 카드 목록 또는 빈·로딩·오류 상태를 표시한다.
 *
 * 헤더 높이와 시스템 inset을 기준으로 본문을 배치한다. 카드의 저장 상태와 북마크 해제 진행 상태는
 * 호출자가 전달한 [uiState]로 결정하며, 카드 본문 이동과 북마크 해제는 별도 콜백으로 전달한다.
 * @param uiState 화면에 표시할 목록 조회 상태
 * @param onBackClick 뒤로가기 헤더 액션을 눌렀을 때 호출할 동작
 * @param onHomeClick 홈 헤더 액션을 눌렀을 때 호출할 동작
 * @param onFindTripClick 빈 상태의 여행지 탐색 CTA를 눌렀을 때 호출할 동작
 * @param onRetryClick 오류 상태의 재시도 CTA를 눌렀을 때 호출할 동작
 * @param onLoadMore 목록 끝에 도달했을 때 다음 페이지를 조회할 동작
 * @param onBookmarkClick 카드 북마커를 눌렀을 때 호출할 동작
 * @param onTripClick 폴더 카드의 코스를 눌렀을 때 호출할 동작
 * @param modifier 화면 컨테이너에 적용할 Modifier
 */
@Composable
fun SavedTripsScreen(
    uiState: SavedTripsUiState,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onFindTripClick: () -> Unit,
    onRetryClick: () -> Unit,
    onLoadMore: () -> Unit,
    onBookmarkClick: (String) -> Unit,
    onTripClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    SavedTripsContainer(
        modifier = modifier,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
    ) { backdropState, headerHeight ->
        LaunchedEffect(uiState) {
            backdropState.invalidate()
        }

        when (uiState) {
            SavedTripsUiState.Loading -> SavedTripsLoading(
                headerHeight = headerHeight,
                modifier = Modifier.fillMaxSize(),
            )

            is SavedTripsUiState.Content -> SavedTripsList(
                trips = uiState.trips,
                canLoadMore = uiState.nextCursor != null,
                isLoadingMore = uiState.isLoadingMore,
                loadMoreError = uiState.loadMoreError,
                removingSavedTripIds = uiState.removingSavedTripIds,
                backdropState = backdropState,
                headerHeight = headerHeight,
                onBookmarkClick = onBookmarkClick,
                onTripClick = onTripClick,
                onLoadMore = onLoadMore,
                modifier = Modifier.fillMaxSize(),
            )

            SavedTripsUiState.Empty -> SavedTripsEmpty(
                headerHeight = headerHeight,
                onFindTripClick = onFindTripClick,
                modifier = Modifier.fillMaxSize(),
            )

            SavedTripsUiState.Error -> SavedTripsError(
                headerHeight = headerHeight,
                onRetryClick = onRetryClick,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun SavedTripsContainer(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier,
    content: @Composable (SairoBackdropState, Dp) -> Unit,
) {
    val colors = SairoTheme.colors
    val backdropState = rememberSairoBackdropState(cpuBlurEnabled = true)
    val density = LocalDensity.current
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeight = with(density) { headerHeightPx.toDp() }

    SairoBackdropHost(
        state = backdropState,
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundCanvas)
            .clipToBounds(),
    ) {
        Image(
            painter = painterResource(R.drawable.img_bg_shadow_top),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )

        Box(modifier = Modifier.fillMaxSize()) {
            content(backdropState, headerHeight)

            SairoHeader(
                variant = SairoHeaderVariant.Sub,
                title = stringResource(R.string.saved_trips_header_title),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .onSizeChanged { size -> headerHeightPx = size.height },
                onBackClick = onBackClick,
                actionIcon = painterResource(R.drawable.ic_home),
                actionContentDescription = stringResource(R.string.sairo_header_home),
                onActionClick = onHomeClick,
                backdropState = backdropState,
            )
        }
    }
}

@Composable
private fun SavedTripsList(
    trips: List<SavedTripUiModel>,
    canLoadMore: Boolean,
    isLoadingMore: Boolean,
    loadMoreError: AppError?,
    removingSavedTripIds: Set<String>,
    backdropState: SairoBackdropState,
    headerHeight: Dp,
    onBookmarkClick: (String) -> Unit,
    onTripClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val navigationBarPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    LaunchedEffect(listState, trips.size, canLoadMore, isLoadingMore, loadMoreError) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        }.map { lastVisibleItemIndex ->
            lastVisibleItemIndex >= trips.lastIndex - LoadMoreThreshold
        }.distinctUntilChanged().filter { isNearEnd ->
            isNearEnd && canLoadMore && !isLoadingMore && loadMoreError == null
        }.collect {
            onLoadMore()
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(
            start = SavedTripsContentPadding,
            top = headerHeight + SavedTripsTopSpacing,
            end = SavedTripsContentPadding,
            bottom = navigationBarPadding + SavedTripsBottomSpacing,
        ),
        verticalArrangement = Arrangement.spacedBy(SavedTripsItemSpacing),
    ) {
        items(
            items = trips,
            key = { trip -> trip.savedTripId },
        ) { trip ->
            SavedTripCard(
                trip = trip,
                backdropState = backdropState,
                isBookmarkRemoving = trip.savedTripId in removingSavedTripIds,
                onBookmarkClick = { onBookmarkClick(trip.savedTripId) },
                onClick = { onTripClick(trip.courseId) },
            )
        }

        if (isLoadingMore) {
            item(key = "saved-trips-loading-more") {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = SairoTheme.colors.accentBase)
                }
            }
        }

        if (loadMoreError != null) {
            item(key = "saved-trips-load-more-error") {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    SairoButton(
                        text = stringResource(R.string.saved_trips_retry),
                        onClick = onLoadMore,
                        style = SairoButtonStyle.Outline,
                    )
                }
            }
        }
    }
}

@Composable
private fun SavedTripCard(
    trip: SavedTripUiModel,
    backdropState: SairoBackdropState,
    isBookmarkRemoving: Boolean,
    onBookmarkClick: () -> Unit,
    onClick: () -> Unit,
) {
    val remoteImagePainter = rememberSairoBackdropImagePainter(
        model = trip.imageUrl,
        backdropState = backdropState,
    )
    val imagePainters = listOf(
        if (trip.imageUrl.isNullOrBlank()) {
            painterResource(R.drawable.img_dummy_view)
        } else {
            remoteImagePainter
        },
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        SairoPlaceFolderCard(
            imagePainters = imagePainters,
            regionLabel = trip.regionName,
            description = trip.reason.orEmpty(),
            placeNames = listOfNotNull(trip.regionArea),
            saved = true,
            onClick = onClick,
            onBookmarkClick = onBookmarkClick,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = SavedTripCardMaxWidth),
            imageContentDescription = trip.regionName,
            bookmarkEnabled = !isBookmarkRemoving,
        )
    }
}

@Composable
private fun SavedTripsEmpty(
    headerHeight: Dp,
    onFindTripClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(top = headerHeight)
            .navigationBarsPadding()
            .padding(horizontal = SavedTripsContentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = EmptyContentMaxWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(EmptySectionSpacing),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(EmptyIllustrationSpacing),
            ) {
                Image(
                    painter = painterResource(R.drawable.img_folder_empty),
                    contentDescription = null,
                    modifier = Modifier.size(EmptyFolderWidth, EmptyFolderHeight),
                )
                Text(
                    text = stringResource(R.string.saved_trips_empty_description),
                    color = SairoTheme.colors.textMuted,
                    style = SairoTextStyles.bodyLight16,
                    textAlign = TextAlign.Center,
                )
            }
            SairoButton(
                text = stringResource(R.string.onboarding_intro_start),
                onClick = onFindTripClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SavedTripsLoading(
    headerHeight: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(top = headerHeight)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = SairoTheme.colors.accentBase)
    }
}

@Composable
private fun SavedTripsError(
    headerHeight: Dp,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(top = headerHeight)
            .navigationBarsPadding()
            .padding(horizontal = SavedTripsContentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ErrorSectionSpacing),
        ) {
            Text(
                text = stringResource(R.string.saved_trips_load_error),
                color = SairoTheme.colors.textPrimary,
                style = SairoTextStyles.bodyLight18,
                textAlign = TextAlign.Center,
            )
            SairoButton(
                text = stringResource(R.string.saved_trips_retry),
                onClick = onRetryClick,
                style = SairoButtonStyle.Outline,
            )
        }
    }
}

private val SavedTripsContentPadding = 16.dp
private val SavedTripsTopSpacing = 24.dp
private val SavedTripsBottomSpacing = 40.dp
private val SavedTripsItemSpacing = 24.dp
private val SavedTripCardMaxWidth = 300.dp
private val EmptyContentMaxWidth = 224.dp
private val EmptyFolderWidth = 100.dp
private val EmptyFolderHeight = 56.dp
private val EmptyIllustrationSpacing = 24.dp
private val EmptySectionSpacing = 32.dp
private val ErrorSectionSpacing = 16.dp
private const val LoadMoreThreshold = 1

@Preview(name = "Saved Trips / Content", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SavedTripsContentPreview() {
    SairoTheme {
        SavedTripsScreen(
            uiState = SavedTripsUiState.Content(previewSavedTrips, nextCursor = null),
            onBackClick = {},
            onHomeClick = {},
            onFindTripClick = {},
            onRetryClick = {},
            onLoadMore = {},
            onBookmarkClick = {},
            onTripClick = {},
        )
    }
}

@Preview(name = "Saved Trips / Empty", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SavedTripsEmptyPreview() {
    SairoTheme {
        SavedTripsScreen(
            uiState = SavedTripsUiState.Empty,
            onBackClick = {},
            onHomeClick = {},
            onFindTripClick = {},
            onRetryClick = {},
            onLoadMore = {},
            onBookmarkClick = {},
            onTripClick = {},
        )
    }
}

private val previewSavedTrips = listOf(
    SavedTripUiModel(
        savedTripId = "preview-boeun",
        courseId = "course-boeun",
        regionName = "충북 보은권",
        regionArea = "보은군",
        imageUrl = "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=900&q=85",
        reason = "고요한 자연과 전통의 분위기",
    ),
)
