package com.example.sairo14.feature.onboarding.select

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.component.SairoButton
import com.example.sairo14.core.designsystem.component.SairoFolderFrame
import com.example.sairo14.core.designsystem.component.SairoFolderVariant
import com.example.sairo14.core.designsystem.component.SairoImageCard
import com.example.sairo14.core.designsystem.component.SairoImageCardSize
import com.example.sairo14.core.designsystem.component.SairoImageThumbnail
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.designsystem.token.SairoShadowStyles
import com.example.sairo14.core.extension.sairoDropShadow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * 온보딩 사진 선택 화면의 상태와 다음 단계 이동을 연결한다.
 *
 * 사진 선택·해제·재시도는 [OnboardingPhotoSelectViewModel]이 처리하고, 완료 결과를 받은 호출자가
 * 다음 목적지 이동을 결정한다.
 * @param modifier 화면 컨테이너에 적용할 Modifier
 * @param viewModel 사진 후보와 선택 상태를 소유하는 ViewModel
 * @param onSelectionComplete 최소 선택 수를 만족한 사진 ID 목록을 전달받는 콜백
 */
@Composable
fun OnboardingPhotoSelectRoute(
    modifier: Modifier = Modifier,
    viewModel: OnboardingPhotoSelectViewModel = hiltViewModel(),
    onSelectionComplete: (List<String>) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel, onSelectionComplete) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OnboardingPhotoSelectEffect.SelectionCompleted -> {
                    onSelectionComplete(effect.photoIds)
                }
            }
        }
    }

    OnboardingPhotoSelectScreen(
        uiState = uiState,
        onPhotoClick = viewModel::togglePhotoSelection,
        onPhotoRemoveClick = viewModel::removePhotoSelection,
        onRetryClick = viewModel::retry,
        onCompleteClick = viewModel::completeSelection,
        modifier = modifier,
    )
}

/**
 * 서버 사진 후보를 선택하고 선택 목록을 확인하는 온보딩 화면을 표시한다.
 *
 * 사진 후보의 앞 5장을 사전 로딩한 뒤 Pager를 표시한다. 나머지 사진은 Pager가 카드를 구성할 때
 * 지연 로딩한다. Pager는 남은 화면 높이와 가용 너비를 기준으로 카드 크기를 계산하고, 하단 폴더는
 * 화면 폭에 비례해 크기가 정해진다. 개별 이미지 요청이 실패하면 dummy 이미지로 대체하며, 사진 선택
 * 상태와 완료 동작은 호출자가 소유한다.
 * @param uiState 화면에 표시할 로딩, 빈 목록, 오류, 콘텐츠 상태
 * @param onPhotoClick 사진 카드를 눌렀을 때 호출할 콜백
 * @param onPhotoRemoveClick 선택 썸네일의 제거 버튼을 눌렀을 때 호출할 콜백
 * @param onRetryClick 오류 상태에서 재시도를 요청할 때 호출할 콜백
 * @param onCompleteClick 완료 버튼을 눌렀을 때 호출할 콜백
 * @param modifier 화면 컨테이너에 적용할 Modifier
 */
@Composable
fun OnboardingPhotoSelectScreen(
    uiState: OnboardingPhotoSelectUiState,
    onPhotoClick: (String) -> Unit,
    onPhotoRemoveClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onCompleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SairoTheme.colors
    val content = uiState as? OnboardingPhotoSelectUiState.Content

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundCanvas)
            .statusBarsPadding(),
    ) {
        val isCompactHeight = maxHeight < PhotoSelectCompactHeightThreshold
        val candidateCardWidth = minOf(
            maxWidth * PhotoCardWidthRatio,
            PhotoCardMaximumWidth,
        )
        val density = LocalDensity.current
        val photoPreloadState = content?.let { loadedContent ->
            rememberPhotoPreloadState(
                imageUrls = loadedContent.photos
                    .take(InitialPhotoPreloadCount)
                    .map(OnboardingPhotoUiModel::imageUrl),
                targetWidthPx = with(density) { candidateCardWidth.toPx().toInt() },
                targetHeightPx = with(density) {
                    (candidateCardWidth / PhotoCardAspectRatio).toPx().toInt()
                },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isCompactHeight) {
                        Modifier.verticalScroll(rememberScrollState())
                    } else {
                        Modifier
                    },
                ),
        ) {
                val pagerContainerModifier = if (isCompactHeight) {
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = CompactPagerMinimumHeight)
                } else {
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                }

                Spacer(modifier = Modifier.height(HeaderTopSpacing))

                PhotoSelectHeader(
                    modifier = Modifier.padding(horizontal = ScreenHorizontalPadding),
                )

                Spacer(modifier = Modifier.height(HeaderToPagerSpacing))

                Box(
                    modifier = pagerContainerModifier.zIndex(PhotoCandidatePagerLayer),
                ) {
                    when (uiState) {
                        OnboardingPhotoSelectUiState.Loading -> PhotoSelectLoadingContent()
                        OnboardingPhotoSelectUiState.Empty -> PhotoSelectMessageContent(
                            text = stringResource(R.string.onboarding_photo_select_empty),
                        )

                        OnboardingPhotoSelectUiState.Error -> PhotoSelectErrorContent(
                            onRetryClick = onRetryClick,
                        )

                        is OnboardingPhotoSelectUiState.Content -> {
                            if (photoPreloadState == PhotoPreloadState.Loading) {
                                PhotoSelectLoadingContent()
                            } else {
                                PhotoCandidatePager(
                                photos = uiState.photos,
                                selectedPhotoIds = uiState.selectedPhotoIds,
                                hasReachedMaximumSelection = uiState.hasReachedMaximumSelection,
                                onPhotoClick = onPhotoClick,
                            )
                            }
                        }
                    }
                }

                PhotoSelectionTray(
                    selectedPhotos = content?.selectedPhotos.orEmpty(),
                    maximumSelectionCount = content?.maximumSelectionCount ?: MaximumSelectionCount,
                    canComplete = content?.canComplete == true,
                    onPhotoRemoveClick = onPhotoRemoveClick,
                    onCompleteClick = onCompleteClick,
                )
            }
    }
}

private enum class PhotoPreloadState {
    Loading,
    Ready,
}

@Composable
private fun rememberPhotoPreloadState(
    imageUrls: List<String>,
    targetWidthPx: Int,
    targetHeightPx: Int,
): PhotoPreloadState {
    val context = LocalContext.current
    val imageLoader = remember(context) { SingletonImageLoader.get(context) }
    var state by remember(imageUrls, targetWidthPx, targetHeightPx) {
        mutableStateOf(PhotoPreloadState.Loading)
    }

    LaunchedEffect(imageUrls, targetWidthPx, targetHeightPx) {
        state = PhotoPreloadState.Loading
        imageUrls
            .asSequence()
            .filter(String::isNotBlank)
            .distinct()
            .chunked(PhotoPreloadConcurrentRequestCount)
            .forEach { urls ->
                coroutineScope {
                    urls.map { imageUrl ->
                        async {
                            preloadPhoto(
                                context = context,
                                imageLoader = imageLoader,
                                imageUrl = imageUrl,
                                targetWidthPx = targetWidthPx,
                                targetHeightPx = targetHeightPx,
                            )
                        }
                    }.awaitAll()
                }
            }
        state = PhotoPreloadState.Ready
    }

    return state
}

private suspend fun preloadPhoto(
    context: Context,
    imageLoader: ImageLoader,
    imageUrl: String,
    targetWidthPx: Int,
    targetHeightPx: Int,
) {
    try {
        imageLoader.execute(
            ImageRequest.Builder(context)
                .data(imageUrl)
                .size(targetWidthPx.coerceAtLeast(1), targetHeightPx.coerceAtLeast(1))
                .build(),
        )
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        // 이미지 요청 실패는 SairoRemoteImage의 fallback으로 표시한다.
    }
}

@Composable
private fun PhotoSelectHeader(
    modifier: Modifier = Modifier,
) {
    val colors = SairoTheme.colors

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = TitleHighlightTopPadding)
                .width(maxWidth * TitleHighlightWidthRatio)
                .height(TitleHighlightHeight)
                .background(colors.highlightTint),
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(TitleToDescriptionSpacing),
        ) {
            Text(
                text = stringResource(R.string.onboarding_photo_select_title),
                color = colors.textPrimary,
                style = SairoTextStyles.displayLight28,
            )
            Text(
                text = stringResource(R.string.onboarding_photo_select_description),
                color = colors.textMuted,
                style = SairoTextStyles.bodyLight18,
            )
        }
    }
}

@Composable
private fun PhotoCandidatePager(
    photos: List<OnboardingPhotoUiModel>,
    selectedPhotoIds: List<String>,
    hasReachedMaximumSelection: Boolean,
    onPhotoClick: (String) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        val availableCardHeight = (
            maxHeight - PhotoCardTopPadding - PhotoCardBottomShadowClearance
        ).coerceAtLeast(0.dp)
        val cardWidth = minOf(
            maxWidth * PhotoCardWidthRatio,
            availableCardHeight * PhotoCardAspectRatio,
            PhotoCardMaximumWidth,
        )
        val pagerState = rememberPagerState(pageCount = { photos.size })

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = ScreenHorizontalPadding,
                top = PhotoCardTopPadding,
                end = ScreenHorizontalPadding,
                bottom = PhotoCardBottomShadowClearance,
            ),
            pageSpacing = PhotoCardPageSpacing,
            pageSize = PageSize.Fixed(cardWidth),
            verticalAlignment = Alignment.CenterVertically,
        ) { page ->
            val photo = photos[page]

            SairoImageCard(
                imageUrl = photo.imageUrl,
                selected = photo.id in selectedPhotoIds,
                size = SairoImageCardSize.Large,
                cardWidth = cardWidth,
                contentDescription = photo.contentDescription,
                modifier = Modifier.rotate(photoCardRotation(page)),
                enabled = photo.id in selectedPhotoIds || !hasReachedMaximumSelection,
                onClick = { onPhotoClick(photo.id) },
            )
        }
    }
}

@Composable
private fun PhotoSelectionTray(
    selectedPhotos: List<OnboardingPhotoUiModel>,
    maximumSelectionCount: Int,
    canComplete: Boolean,
    onPhotoRemoveClick: (String) -> Unit,
    onCompleteClick: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(SairoTheme.colors.surfaceRaised)
            .navigationBarsPadding(),
    ) {
        val frameWidth = maxWidth * FolderWidthRatio
        val frameHeight = frameWidth / FolderAspectRatio
        val trayHeight = frameHeight * FolderVisibleHeightRatio
        val folderBodyTopPadding = frameWidth * FolderBodyTopPaddingRatio
        val trayContentTopPadding = frameWidth * FolderContentTopPaddingRatio
        val countTopPadding = frameWidth * FolderCountTopPaddingRatio

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trayHeight),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(folderBodyTopPadding)
                    .background(SairoTheme.colors.backgroundCanvas),
            )

            // 폴더 그림자는 탭 상단의 캔버스와 본문 흰색 표면 위에 자연스럽게 이어서 표시한다.
            SairoFolderFrame(
                variant = SairoFolderVariant.Large,
                frameWidth = frameWidth,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .sairoDropShadow(
                        shape = FolderShadowShape,
                        shadowStyle = SairoShadowStyles.glowDefault,
                    ),
            )

            SairoFolderFrame(
                variant = SairoFolderVariant.Large,
                frameWidth = frameWidth,
                modifier = Modifier.align(Alignment.TopCenter),
            )

            Text(
                text = stringResource(
                    R.string.onboarding_photo_select_count,
                    selectedPhotos.size,
                    maximumSelectionCount,
                ),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = ScreenHorizontalPadding,
                        top = countTopPadding,
                    )
                    .background(SairoTheme.colors.chipLimeBackground)
                    .padding(horizontal = CountHorizontalPadding),
                color = SairoTheme.colors.chipLimeText,
                style = SairoTextStyles.bodyLight18,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = ScreenHorizontalPadding,
                        top = trayContentTopPadding,
                        end = ScreenHorizontalPadding,
                    ),
                verticalArrangement = Arrangement.spacedBy(TrayContentSpacing),
            ) {
                PhotoProgressIndicator(
                    selectedCount = selectedPhotos.size,
                    maximumSelectionCount = maximumSelectionCount,
                )

                SelectedPhotoThumbnails(
                    selectedPhotos = selectedPhotos,
                    onPhotoRemoveClick = onPhotoRemoveClick,
                )

                SairoButton(
                    text = stringResource(R.string.onboarding_photo_select_complete),
                    onClick = onCompleteClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canComplete,
                )
            }
        }
    }
}

@Composable
private fun PhotoProgressIndicator(
    selectedCount: Int,
    maximumSelectionCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ProgressIndicatorSpacing),
    ) {
        repeat(maximumSelectionCount) { index ->
            val color = if (index < selectedCount) {
                SairoTheme.colors.indicatorActive
            } else {
                SairoTheme.colors.indicatorEmpty
            }

            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .height(ProgressIndicatorHeight)
                    .background(color),
            )
        }
    }
}

@Composable
private fun SelectedPhotoThumbnails(
    selectedPhotos: List<OnboardingPhotoUiModel>,
    onPhotoRemoveClick: (String) -> Unit,
) {
    if (selectedPhotos.isEmpty()) {
        Spacer(modifier = Modifier.height(ThumbnailRowHeight))
        return
    }

    LazyRow(
        modifier = Modifier.heightIn(min = ThumbnailRowHeight),
        horizontalArrangement = Arrangement.spacedBy(ThumbnailSpacing),
        contentPadding = PaddingValues(end = ThumbnailRemoveOverflowPadding),
    ) {
        items(
            items = selectedPhotos,
            key = OnboardingPhotoUiModel::id,
        ) { photo ->
            SairoImageThumbnail(
                imageUrl = photo.imageUrl,
                onRemoveClick = { onPhotoRemoveClick(photo.id) },
                contentDescription = photo.contentDescription,
            )
        }
    }
}

@Composable
private fun PhotoSelectLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = SairoTheme.colors.accentBase)
    }
}

@Composable
private fun PhotoSelectMessageContent(
    text: String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenHorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = SairoTheme.colors.textMuted,
            style = SairoTextStyles.bodyLight18,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PhotoSelectErrorContent(
    onRetryClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = ScreenHorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ErrorContentSpacing),
        ) {
            Text(
                text = stringResource(R.string.onboarding_photo_select_error),
                color = SairoTheme.colors.textPrimary,
                style = SairoTextStyles.bodyLight18,
                textAlign = TextAlign.Center,
            )
            SairoButton(
                text = stringResource(R.string.onboarding_photo_select_retry),
                onClick = onRetryClick,
            )
        }
    }
}

private fun photoCardRotation(page: Int): Float = when (page % 3) {
    1 -> -3f
    2 -> 2f
    else -> 0f
}

private val ScreenHorizontalPadding = 16.dp
private val HeaderTopSpacing = 24.dp
private val HeaderToPagerSpacing = 12.dp
private val TitleHighlightTopPadding = 7.dp
private val TitleHighlightHeight = 21.dp
private const val TitleHighlightWidthRatio = 159f / 328f
private val TitleToDescriptionSpacing = 2.dp
private const val PhotoCardWidthRatio = 300f / 360f
private const val PhotoCardAspectRatio = 3f / 4f
private val PhotoCardMaximumWidth = 300.dp
private val PhotoCardTopPadding = 8.dp
private val PhotoCardBottomShadowClearance = 20.dp
private val PhotoCardPageSpacing = 16.dp
private const val PhotoCandidatePagerLayer = 1f
private const val InitialPhotoPreloadCount = 3
private const val PhotoPreloadConcurrentRequestCount = InitialPhotoPreloadCount
private val PhotoSelectCompactHeightThreshold = 520.dp
private val CompactPagerMinimumHeight = 200.dp
private const val FolderWidthRatio = 375f / 360f
private const val FolderAspectRatio = 375f / 230f
private const val FolderVisibleHeightRatio = 219f / 230f
private const val FolderBodyTopPaddingRatio = 37.75f / 375f
private const val FolderContentTopPaddingRatio = 55f / 375f
private const val FolderCountTopPaddingRatio = 16f / 375f
private val FolderShadowShape = RectangleShape
private val CountHorizontalPadding = 10.dp
private val TrayContentSpacing = 16.dp
private val ProgressIndicatorHeight = 4.dp
private val ProgressIndicatorSpacing = 4.dp
private val ThumbnailRowHeight = 52.dp
private val ThumbnailSpacing = 12.dp
private val ThumbnailRemoveOverflowPadding = 7.dp
private val ErrorContentSpacing = 16.dp

@Preview(name = "Onboarding Photo Select / Default", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OnboardingPhotoSelectScreenDefaultPreview() {
    SairoTheme {
        OnboardingPhotoSelectScreen(
            uiState = previewPhotoContent(),
            onPhotoClick = {},
            onPhotoRemoveClick = {},
            onRetryClick = {},
            onCompleteClick = {},
        )
    }
}

@Preview(name = "Onboarding Photo Select / Selected", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OnboardingPhotoSelectScreenSelectedPreview() {
    val content = previewPhotoContent()
    SairoTheme {
        OnboardingPhotoSelectScreen(
            uiState = content.copy(
                selectedPhotoIds = content.photos.take(6).map { photo -> photo.id },
            ),
            onPhotoClick = {},
            onPhotoRemoveClick = {},
            onRetryClick = {},
            onCompleteClick = {},
        )
    }
}

private fun previewPhotoContent(): OnboardingPhotoSelectUiState.Content =
    OnboardingPhotoSelectUiState.Content(
        photos = List(10) { index ->
            OnboardingPhotoUiModel(
                id = "preview-$index",
                imageUrl = "",
                contentDescription = null,
            )
        },
    )
