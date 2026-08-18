package com.example.sairo14.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.component.SairoBackdropState
import com.example.sairo14.core.designsystem.component.SairoButton
import com.example.sairo14.core.designsystem.component.SairoHeader
import com.example.sairo14.core.designsystem.component.SairoHeaderVariant
import com.example.sairo14.core.designsystem.component.rememberSairoBackdropImagePainter
import com.example.sairo14.core.designsystem.component.rememberSairoBackdropState
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.skydoves.cloudy.sky
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * 홈 화면의 상태와 사용자 행동을 화면에 연결한다.
 *
 * 이미지 상태는 [HomeUiState]로 전달하고, 여행지 탐색·저장 목록 이동은 앱 내비게이션을
 * 소유한 호출자가 처리한다.
 * @param modifier 화면 컨테이너에 적용할 Modifier
 * @param viewModel 홈 화면의 중앙 이미지 상태를 소유하는 ViewModel
 * @param onFindTripClick 여행지 찾기 CTA를 눌렀을 때 호출할 동작
 * @param onFolderClick 상단 저장 목록 액션을 눌렀을 때 호출할 동작
 * @param onSavedTripClick 저장 여행지 카드를 눌렀을 때 코스 ID와 저장 항목 ID로 호출할 동작
 */
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onFindTripClick: () -> Unit = {},
    onFolderClick: () -> Unit = {},
    onSavedTripClick: (courseId: String, savedTripId: String) -> Unit = { _, _ -> },
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    HomeScreen(
        modifier = modifier,
        uiState = uiState,
        onFindTripClick = onFindTripClick,
        onFolderClick = onFolderClick,
        onSavedTripClick = onSavedTripClick,
        onRetryClick = viewModel::retry,
    )
}

/**
 * 홈 데이터의 로딩·콘텐츠·오류 상태에 맞는 화면을 표시한다.
 *
 * 콘텐츠 상태의 중앙 카드 묶음은 화면의 가용 가로폭을 기준으로 계산한다. 저장 여행지가 있으면
 * [HomeUiState.Content.savedTrips]를 네 개의 고정 슬롯을 가진 이동 가능한 캔버스로 표시한다.
 * @param modifier 화면 컨테이너에 적용할 Modifier
 * @param uiState 화면에 표시할 로딩·콘텐츠·오류 상태
 * @param onFindTripClick 여행지 찾기 CTA를 눌렀을 때 호출할 동작
 * @param onFolderClick 상단 저장 목록 액션을 눌렀을 때 호출할 동작
 * @param onSavedTripClick 저장 여행지 카드를 눌렀을 때 코스 ID와 저장 항목 ID로 호출할 동작
 * @param onRetryClick 오류 화면의 재시도 버튼을 눌렀을 때 호출할 동작
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState = HomeUiState.Content(),
    onFindTripClick: () -> Unit = {},
    onFolderClick: () -> Unit = {},
    onSavedTripClick: (courseId: String, savedTripId: String) -> Unit = { _, _ -> },
    onRetryClick: () -> Unit = {},
) {
    when (uiState) {
        HomeUiState.Loading -> HomeLoadingScreen(
            modifier = modifier,
            onFolderClick = onFolderClick,
        )

        is HomeUiState.Content -> HomeContentScreen(
            modifier = modifier,
            uiState = uiState,
            onFindTripClick = onFindTripClick,
            onFolderClick = onFolderClick,
            onSavedTripClick = onSavedTripClick,
        )

        HomeUiState.Error -> HomeErrorScreen(
            modifier = modifier,
            onFolderClick = onFolderClick,
            onRetryClick = onRetryClick,
        )
    }
}

@Composable
private fun HomeContentScreen(
    modifier: Modifier,
    uiState: HomeUiState.Content,
    onFindTripClick: () -> Unit,
    onFolderClick: () -> Unit,
    onSavedTripClick: (courseId: String, savedTripId: String) -> Unit,
) {
    HomeContainer(
        modifier = modifier,
        onFolderClick = onFolderClick,
    ) { backdropState, headerHeight ->
        val backImagePainter = rememberSairoBackdropImagePainter(
            model = uiState.discoveryImages.backImageUrl ?: R.drawable.img_dummy_view,
            backdropState = backdropState,
        )
        val frontImagePainter = rememberSairoBackdropImagePainter(
            model = uiState.discoveryImages.frontImageUrl ?: R.drawable.img_dummy_view,
            backdropState = backdropState,
        )

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
        ) {
            if (uiState.savedTrips.isNotEmpty()) {
                val density = LocalDensity.current
                val canvasSize = remember(maxWidth, maxHeight, density) {
                    with(density) {
                        HomeCanvasSize(
                            width = maxWidth.toPx(),
                            height = maxHeight.toPx(),
                        )
                    }
                }
                val cardSize = remember(density) {
                    with(density) {
                        HomeCanvasSize(
                            width = HomeSavedTripCardWidth.toPx(),
                            height = HomeSavedTripCardHeight.toPx(),
                        )
                    }
                }
                val revealPadding = with(density) { HomeCanvasRevealPadding.toPx() }
                val visualOverflow = with(density) { HomeSavedTripVisualOverflow.toPx() }
                val savedTripLayout = remember(
                    canvasSize,
                    cardSize,
                    density,
                    headerHeight,
                    uiState.savedTrips.size,
                    revealPadding,
                    visualOverflow,
                ) {
                    val visibleTop = with(density) { headerHeight.toPx() }
                        .coerceIn(0f, canvasSize.height)
                    HomeCanvasLayoutPolicy.calculate(
                        canvasSize = canvasSize,
                        visibleViewport = HomeCanvasRect(
                            left = 0f,
                            top = visibleTop,
                            right = canvasSize.width,
                            bottom = canvasSize.height,
                        ),
                        cardSize = cardSize,
                        placements = HomeSavedTripPlacements.create(cardSize),
                        cardCount = uiState.savedTrips.size,
                        revealPadding = revealPadding,
                        visualOverflow = visualOverflow,
                    )
                }

                HomePannableCanvas(
                    backdropState = backdropState,
                    panBounds = savedTripLayout.panBounds,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    HomeSavedTripsLayer(
                        savedTrips = uiState.savedTrips,
                        cardPlacements = savedTripLayout.cardPlacements,
                        backdropState = backdropState,
                        onSavedTripClick = onSavedTripClick,
                        modifier = Modifier.fillMaxSize(),
                    )
                    HomeDiscoveryContent(
                        hasSavedTrips = true,
                        headerHeight = headerHeight,
                        backImagePainter = backImagePainter,
                        frontImagePainter = frontImagePainter,
                        onFindTripClick = onFindTripClick,
                    )
                }
            } else {
                HomeDiscoveryContent(
                    hasSavedTrips = false,
                    headerHeight = headerHeight,
                    backImagePainter = backImagePainter,
                    frontImagePainter = frontImagePainter,
                    onFindTripClick = onFindTripClick,
                )
            }
        }
    }
}

@Composable
private fun HomeDiscoveryContent(
    hasSavedTrips: Boolean,
    headerHeight: Dp,
    backImagePainter: Painter,
    frontImagePainter: Painter,
    onFindTripClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = headerHeight + HomeContentTopSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HomeHorizontalPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = HomeContentMaxWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(
                        if (hasSavedTrips) R.string.home_populated_title else R.string.home_empty_title,
                    ),
                    color = SairoTheme.colors.textPrimary,
                    style = SairoTextStyles.displayLight24,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(HomeContentGap))

                HomeDiscoveryCta(
                    backPainter = backImagePainter,
                    frontPainter = frontImagePainter,
                    onClick = onFindTripClick,
                    buttonText = stringResource(
                        if (hasSavedTrips) R.string.home_find_another_trip else R.string.home_find_trip,
                    ),
                )
            }
        }
    }
}

@Composable
private fun HomeLoadingScreen(
    modifier: Modifier,
    onFolderClick: () -> Unit,
) {
    HomeContainer(
        modifier = modifier,
        onFolderClick = onFolderClick,
    ) { _, headerHeight ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = headerHeight),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = SairoTheme.colors.accentBase)
        }
    }
}

@Composable
private fun HomeErrorScreen(
    modifier: Modifier,
    onFolderClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    HomeContainer(
        modifier = modifier,
        onFolderClick = onFolderClick,
    ) { _, headerHeight ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = headerHeight),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_load_error),
                    color = SairoTheme.colors.textPrimary,
                    style = SairoTextStyles.bodyLight18,
                )
                SairoButton(
                    text = stringResource(R.string.home_retry),
                    onClick = onRetryClick,
                )
            }
        }
    }
}

@Composable
private fun HomeContainer(
    modifier: Modifier,
    onFolderClick: () -> Unit,
    content: @Composable BoxScope.(SairoBackdropState, Dp) -> Unit,
) {
    val colors = SairoTheme.colors
    val backdropState = rememberSairoBackdropState(cpuBlurEnabled = true)
    val density = LocalDensity.current
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeight = with(density) { headerHeightPx.toDp() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundCanvas)
            .clipToBounds(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .sky(backdropState.sky),
        ) {
            Image(
                painter = painterResource(R.drawable.img_bg_shadow_default),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )

            content(backdropState, headerHeight)
        }

        SairoHeader(
            variant = SairoHeaderVariant.Home,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { size -> headerHeightPx = size.height },
            actionIcon = painterResource(R.drawable.ic_folder_outline),
            actionContentDescription = stringResource(R.string.home_saved_places),
            onActionClick = onFolderClick,
            backdropState = backdropState,
        )
    }
}

@Composable
private fun HomePannableCanvas(
    backdropState: SairoBackdropState,
    panBounds: HomeCanvasPanBounds,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var canvasOffset by remember { mutableStateOf(Offset.Zero) }
    val backdropInvalidationPending = remember(backdropState) { AtomicBoolean(false) }
    val scheduleBackdropInvalidation = remember(
        backdropState,
        coroutineScope,
        backdropInvalidationPending,
    ) {
        {
            if (backdropInvalidationPending.compareAndSet(false, true)) {
                coroutineScope.launch {
                    withFrameNanos { }
                    backdropState.invalidate()
                    backdropInvalidationPending.set(false)
                }
            }
        }
    }

    LaunchedEffect(panBounds) {
        canvasOffset = Offset(
            x = canvasOffset.x.coerceIn(panBounds.minX, panBounds.maxX),
            y = canvasOffset.y.coerceIn(panBounds.minY, panBounds.maxY),
        )
    }

    Box(
        modifier = modifier
            .clipToBounds()
            .pointerInput(panBounds) {
                detectDragGestures(
                    onDragStart = { backdropState.invalidate() },
                ) { change, dragAmount ->
                    change.consume()
                    canvasOffset = Offset(
                        x = (canvasOffset.x + dragAmount.x)
                            .coerceIn(panBounds.minX, panBounds.maxX),
                        y = (canvasOffset.y + dragAmount.y)
                            .coerceIn(panBounds.minY, panBounds.maxY),
                    )
                    scheduleBackdropInvalidation()
                }
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = canvasOffset.x.roundToInt(),
                        y = canvasOffset.y.roundToInt(),
                    )
                },
            content = content,
        )
    }
}

@Composable
private fun HomeSavedTripsLayer(
    savedTrips: List<HomeSavedTripUiModel>,
    cardPlacements: List<HomeResolvedSavedTripPlacement>,
    backdropState: SairoBackdropState,
    onSavedTripClick: (courseId: String, savedTripId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        savedTrips.zip(cardPlacements).forEach { (savedTrip, resolvedPlacement) ->
            key(savedTrip.savedTripId) {
                val painter = rememberSairoBackdropImagePainter(
                    model = savedTrip.thumbnailImageUrl ?: R.drawable.img_dummy_view,
                    backdropState = backdropState,
                )

                HomeSavedTripCard(
                    savedTrip = savedTrip,
                    painter = painter,
                    onClick = {
                        onSavedTripClick(savedTrip.courseId, savedTrip.savedTripId)
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset {
                            IntOffset(
                                x = resolvedPlacement.cardBounds.left.roundToInt(),
                                y = resolvedPlacement.cardBounds.top.roundToInt(),
                            )
                        },
                )
            }
        }
    }
}

private val HomeHorizontalPadding = 24.dp
private val HomeContentMaxWidth = 294.dp
private val HomeContentTopSpacing = 55.dp
private val HomeContentGap = 20.dp
private val HomeCanvasRevealPadding = 24.dp
private val HomeSavedTripVisualOverflow = 32.dp

@Preview(name = "Home Empty", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HomeScreenPreview() {
    SairoTheme {
        HomeScreen()
    }
}

@Preview(name = "Home One Saved Trip", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HomeScreenOneSavedTripPreview() {
    HomeScreenSavedTripsPreview(cardCount = 1)
}

@Preview(name = "Home Four Saved Trips", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HomeScreenFourSavedTripsPreview() {
    HomeScreenSavedTripsPreview(cardCount = 4)
}

@Preview(name = "Home Eight Saved Trips - Small", showBackground = true, widthDp = 320, heightDp = 568)
@Composable
private fun HomeScreenEightSavedTripsSmallPreview() {
    HomeScreenSavedTripsPreview(cardCount = 8)
}

@Composable
private fun HomeScreenSavedTripsPreview(cardCount: Int) {
    SairoTheme {
        HomeScreen(
            uiState = HomeUiState.Content(
                savedTrips = List(cardCount) { index ->
                    HomeSavedTripUiModel(
                        savedTripId = "saved-trip-$index",
                        courseId = "course-$index",
                        regionName = if (index % 2 == 0) "전남 담양권" else "충북 보은권",
                    )
                },
            ),
        )
    }
}
