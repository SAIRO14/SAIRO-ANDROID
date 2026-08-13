package com.example.sairo14.feature.onboarding.loading

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import com.example.sairo14.R
import com.example.sairo14.core.navigation.OnboardingAnimationPhoto
import com.example.sairo14.core.designsystem.component.SairoButton
import com.example.sairo14.core.designsystem.component.SairoImageCard
import com.example.sairo14.core.designsystem.component.SairoImageCardSize
import com.example.sairo14.core.designsystem.component.SairoTag
import com.example.sairo14.core.designsystem.component.SairoTagVariant
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme
import androidx.compose.ui.MotionDurationScale
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.CancellationException

/**
 * 온보딩 로딩 상태와 결과 화면 이동을 연결한다.
 *
 * 선택한 사진 URL은 Route가 소유하며, ViewModel이 변환한 [OnboardingLoadingUiState]만 화면에 전달한다.
 * 카드 모션이 끝나고 임시 분석 대기 시간이 지나면 [onFinished]를 한 번 호출한다.
 * @param animationPhotos 사진 선택 순서의 앞 5장으로 구성한 애니메이션 카드 정보
 * @param onFinished 로딩과 분석 대기가 끝났을 때 결과 화면으로 이동할 콜백
 * @param onBackClick 사진을 다시 선택해야 할 때 이전 화면으로 돌아가는 콜백
 */
@Composable
fun OnboardingLoadingRoute(
    searchSessionId: String,
    selectedPhotoIds: List<String>,
    animationPhotos: List<OnboardingAnimationPhoto>,
    onFinished: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingLoadingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(searchSessionId, selectedPhotoIds, animationPhotos) {
        viewModel.load(searchSessionId, selectedPhotoIds, animationPhotos)
    }

    OnboardingLoadingScreen(
        uiState = uiState,
        onFinished = onFinished,
        onBackClick = onBackClick,
        onRetryClick = viewModel::retry,
        modifier = modifier,
    )
}

/**
 * 선택한 사진 다섯 장을 덱처럼 쌓으며 분석 중 상태를 표시한다.
 *
 * 카드와 태그의 진행률은 하나의 타임라인이 소유한다. 시스템 애니메이션 배율이 0이면 최종 상태를
 * 즉시 표시해 축소 모션 환경에서도 결과 이동이 지연되지 않는다.
 * @param uiState 사진 복원 상태와 카드에 표시할 사진 목록
 * @param onFinished 카드 모션과 분석 대기가 끝난 뒤 호출할 콜백
 * @param onBackClick 사진 복원 오류에서 이전 화면으로 돌아갈 때 호출할 콜백
 * @param modifier 화면 컨테이너에 적용할 Modifier
 */
@Composable
fun OnboardingLoadingScreen(
    uiState: OnboardingLoadingUiState,
    onFinished: () -> Unit,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val loadingPhotos = (uiState as? OnboardingLoadingUiState.Content)
        ?.photos
        ?.take(OnboardingLoadingCardCount)
        .orEmpty()
    val imagePreloadState = rememberLoadingImagePreloadState(loadingPhotos)

    when (uiState) {
        OnboardingLoadingUiState.Loading -> OnboardingLoadingPendingScreen(modifier)
        OnboardingLoadingUiState.Error -> OnboardingLoadingErrorScreen(
            messageResId = R.string.onboarding_loading_error,
            onBackClick = onBackClick,
            onRetryClick = null,
            modifier = modifier,
        )

        OnboardingLoadingUiState.AnalysisError -> OnboardingLoadingErrorScreen(
            messageResId = R.string.onboarding_result_error,
            onBackClick = onBackClick,
            onRetryClick = onRetryClick,
            modifier = modifier,
        )

        is OnboardingLoadingUiState.Content -> {
            if (imagePreloadState == LoadingImagePreloadState.Ready) {
                OnboardingLoadingContent(
                    photos = loadingPhotos,
                    moodTags = uiState.moodTags,
                    onFinished = onFinished,
                    modifier = modifier,
                )
            } else {
                OnboardingLoadingPendingScreen(modifier)
            }
        }
    }
}

private enum class LoadingImagePreloadState {
    Loading,
    Ready,
}

@Composable
private fun rememberLoadingImagePreloadState(
    photos: List<OnboardingLoadingPhotoUiModel>,
): LoadingImagePreloadState {
    val context = LocalContext.current
    val density = LocalDensity.current
    val imageLoader = remember(context) { SingletonImageLoader.get(context) }
    val imageUrls = remember(photos) { photos.map(OnboardingLoadingPhotoUiModel::imageUrl) }
    val targetWidthPx = remember(density) { with(density) { LoadingCardWidth.toPx().toInt() } }
    val targetHeightPx = remember(density) {
        with(density) { (LoadingCardWidth / LoadingCardAspectRatio).toPx().toInt() }
    }
    var state by remember(imageUrls, targetWidthPx, targetHeightPx) {
        mutableStateOf(LoadingImagePreloadState.Loading)
    }

    LaunchedEffect(imageUrls, targetWidthPx, targetHeightPx) {
        state = LoadingImagePreloadState.Loading
        imageUrls
            .asSequence()
            .filter(String::isNotBlank)
            .distinct()
            .map { imageUrl ->
                async {
                    preloadLoadingImage(
                        context = context,
                        imageLoader = imageLoader,
                        imageUrl = imageUrl,
                        targetWidthPx = targetWidthPx,
                        targetHeightPx = targetHeightPx,
                    )
                }
            }
            .toList()
            .awaitAll()
        state = LoadingImagePreloadState.Ready
    }

    return state
}

private suspend fun preloadLoadingImage(
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
        // 실패한 URL은 애니메이션 카드에서 SairoRemoteImage의 fallback으로 표시한다.
    }
}

@Composable
private fun OnboardingLoadingContent(
    photos: List<OnboardingLoadingPhotoUiModel>,
    moodTags: List<String>?,
    onFinished: () -> Unit,
    modifier: Modifier,
) {
    val timeline = remember { Animatable(0f) }
    val tagTimeline = remember { Animatable(0f) }
    var isCardAnimationFinished by remember(photos) { mutableStateOf(false) }
    var isTagAnimationFinished by remember(moodTags) { mutableStateOf(false) }

    LaunchedEffect(photos) {
        timeline.snapTo(0f)
        val motionScale = currentCoroutineContext()[MotionDurationScale]?.scaleFactor ?: 1f
        if (motionScale == 0f) {
            timeline.snapTo(LoadingTimelineEndMillis.toFloat())
        } else {
            timeline.animateTo(
                targetValue = LoadingTimelineEndMillis.toFloat(),
                animationSpec = tween(LoadingTimelineEndMillis, easing = LinearEasing),
            )
        }
        isCardAnimationFinished = true
    }

    LaunchedEffect(moodTags) {
        if (moodTags == null) return@LaunchedEffect
        tagTimeline.snapTo(0f)
        val endMillis = moodTags.lastIndex.coerceAtLeast(0) * TagAppearIntervalMillis + TagEnterDurationMillis
        tagTimeline.animateTo(endMillis.toFloat(), tween(endMillis, easing = LinearEasing))
        isTagAnimationFinished = true
    }

    LaunchedEffect(isCardAnimationFinished, isTagAnimationFinished, moodTags) {
        if (moodTags != null && isCardAnimationFinished && isTagAnimationFinished) onFinished()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(SairoTheme.colors.backgroundCanvas),
        contentAlignment = Alignment.TopCenter,
    ) {
        val compactHeight = maxHeight < CompactHeightThreshold
        val topPadding = if (compactHeight) CompactContentTopPadding else ContentTopPadding

        Column(
            modifier = Modifier.padding(top = topPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LoadingCardDeck(
                photos = photos,
                elapsedMillis = timeline.value,
            )

            Spacer(modifier = Modifier.height(InfoTopSpacing))

            LoadingInformation(
                moodTags = moodTags,
                elapsedMillis = tagTimeline.value,
            )
        }
    }
}

@Composable
private fun LoadingCardDeck(
    photos: List<OnboardingLoadingPhotoUiModel>,
    elapsedMillis: Float,
) {
    val density = LocalDensity.current

    Box(
        modifier = Modifier.size(DeckWidth, DeckHeight),
        contentAlignment = Alignment.Center,
    ) {
        photos.forEachIndexed { index, photo ->
            val spec = LoadingCardMotionSpecs[index]
            val cardProgress = timedProgress(
                elapsedMillis = elapsedMillis,
                delayMillis = spec.delayMillis,
                durationMillis = CardEnterDurationMillis,
            )
            val easedProgress = CardEnterEasing.transform(cardProgress)
            val landingScale = landingScale(
                elapsedMillis = elapsedMillis,
                landingMillis = spec.delayMillis + CardEnterDurationMillis,
            )
            val translationX = with(density) {
                (spec.initialTranslationX * (1f - easedProgress)).toPx()
            }
            val translationY = with(density) {
                (spec.initialTranslationY * (1f - easedProgress)).toPx()
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        this.translationX = translationX
                        this.translationY = translationY
                        rotationZ = lerp(spec.initialRotation, spec.restingRotation, easedProgress)
                        scaleX = landingScale
                        scaleY = landingScale
                        alpha = easedProgress
                        transformOrigin = TransformOrigin.Center
                    },
            ) {
                SairoImageCard(
                    imageUrl = photo.imageUrl,
                    selected = false,
                    showShadow = true,
                    size = SairoImageCardSize.Medium,
                    cardWidth = LoadingCardWidth,
                    contentDescription = photo.contentDescription,
                )
            }
        }
    }
}

@Composable
private fun LoadingInformation(
    moodTags: List<String>?,
    elapsedMillis: Float,
) {
    val density = LocalDensity.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.onboarding_loading_title),
            color = SairoTheme.colors.textPrimary,
            style = SairoTextStyles.displayLight24,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )

        //불필요한 로딩 dot
        /*
        if (showDots) {
            LoadingDots()
            Spacer(modifier = Modifier.height(DotsToTagsSpacing))
        } else {
            Spacer(modifier = Modifier.height(TitleToTagsSpacing))
        }*/
        Spacer(modifier = Modifier.height(18.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(TagSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            moodTags.orEmpty().forEachIndexed { index, tag ->
                val tagProgress = TagEnterEasing.transform(
                    timedProgress(
                        elapsedMillis = elapsedMillis,
                        delayMillis = index * TagAppearIntervalMillis,
                        durationMillis = TagEnterDurationMillis,
                    ),
                )

                SairoTag(
                    text = tag,
                    variant = SairoTagVariant.MediumLemon,
                    modifier = Modifier.graphicsLayer {
                        translationY = with(density) {
                            (TagInitialTranslationY * (1f - tagProgress)).toPx()
                        }
                        alpha = tagProgress
                        transformOrigin = TransformOrigin.Center
                    },
                )
            }
        }
    }
}

@Composable
private fun LoadingDots() {
    val transition = rememberInfiniteTransition(label = "onboarding_loading_dots")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(DotsCycleDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "onboarding_loading_dots_progress",
    )
    val visibleDotCount = (progress * DotCount).toInt().coerceIn(0, DotCount)

    Row(
        modifier = Modifier
            .height(DotsHeight)
            .clearAndSetSemantics { },
    ) {
        repeat(DotCount) { index ->
            Text(
                text = stringResource(R.string.onboarding_loading_dot),
                color = SairoTheme.colors.textMuted,
                style = SairoTextStyles.bodyLight18,
                modifier = Modifier.graphicsLayer {
                    alpha = if (index < visibleDotCount) 1f else InactiveDotAlpha
                },
            )
        }
    }
}

@Composable
private fun OnboardingLoadingPendingScreen(
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SairoTheme.colors.backgroundCanvas),
    )
}

@Composable
private fun OnboardingLoadingErrorScreen(
    messageResId: Int,
    onBackClick: () -> Unit,
    onRetryClick: (() -> Unit)?,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SairoTheme.colors.backgroundCanvas)
            .padding(horizontal = ScreenHorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ErrorContentSpacing),
        ) {
            Text(
                text = stringResource(messageResId),
                color = SairoTheme.colors.textPrimary,
                style = SairoTextStyles.bodyLight18,
                textAlign = TextAlign.Center,
            )
            SairoButton(
                text = stringResource(
                    if (onRetryClick == null) R.string.onboarding_loading_reselect else R.string.onboarding_result_retry,
                ),
                onClick = onRetryClick ?: onBackClick,
            )
        }
    }
}

private fun timedProgress(
    elapsedMillis: Float,
    delayMillis: Int,
    durationMillis: Int,
): Float = ((elapsedMillis - delayMillis) / durationMillis).coerceIn(0f, 1f)

private fun landingScale(
    elapsedMillis: Float,
    landingMillis: Int,
): Float {
    val progress = timedProgress(
        elapsedMillis = elapsedMillis,
        delayMillis = landingMillis,
        durationMillis = CardLandingDurationMillis,
    )
    return if (progress < 0.5f) {
        lerp(
            start = 1f,
            end = LandingPressedScale,
            progress = CardLandingEasing.transform(progress / 0.5f),
        )
    } else {
        lerp(
            start = LandingPressedScale,
            end = 1f,
            progress = CardLandingEasing.transform((progress - 0.5f) / 0.5f),
        )
    }
}

private fun lerp(start: Float, end: Float, progress: Float): Float =
    start + (end - start) * progress

private data class LoadingCardMotionSpec(
    val delayMillis: Int,
    val initialTranslationX: Dp,
    val initialTranslationY: Dp,
    val initialRotation: Float,
    val restingRotation: Float,
)

private val LoadingCardMotionSpecs = listOf(
    LoadingCardMotionSpec(0, (-90).dp, (-18).dp, 22f, 7f),
    LoadingCardMotionSpec(650, 90.dp, 18.dp, -18f, -5f),
    LoadingCardMotionSpec(1300, (-90).dp, 12.dp, 14f, 2f),
    LoadingCardMotionSpec(1950, 90.dp, (-12).dp, -20f, -8f),
    LoadingCardMotionSpec(2600, (-90).dp, (-18).dp, 18f, 4f),
)

private const val CardEnterDurationMillis = 600
private const val CardLandingDurationMillis = 180
private const val LoadingTimelineEndMillis = 3380
private const val AnalysisMinimumDurationMillis = 2000L
private const val TagEnterDurationMillis = 450
private const val TagAppearIntervalMillis = 650
private const val DotsCycleDurationMillis = 1800
private const val DotCount = 3
private const val InactiveDotAlpha = 0.2f
private const val LandingPressedScale = 0.98f
private val CardEnterEasing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
private val CardLandingEasing = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
private val TagEnterEasing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
private val DeckWidth = 274.dp
private val DeckHeight = 370.dp
private val LoadingCardWidth = 260.dp
private const val LoadingCardAspectRatio = 260f / 347f
private val ContentTopPadding = 168.dp
private val CompactContentTopPadding = 96.dp
private val CompactHeightThreshold = 700.dp
private val InfoTopSpacing = 32.dp
private val TitleToTagsSpacing = 18.dp
private val DotsToTagsSpacing = 2.dp
private val DotsHeight = 20.dp
private val TagSpacing = 8.dp
private val TagInitialTranslationY = 12.dp
private val ScreenHorizontalPadding = 24.dp
private val ErrorContentSpacing = 16.dp

@Preview(name = "Onboarding Loading / Final", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OnboardingLoadingScreenPreview() {
    SairoTheme {
        OnboardingLoadingScreen(
            uiState = OnboardingLoadingUiState.Content(
                photos = List(OnboardingLoadingCardCount) { index ->
                    OnboardingLoadingPhotoUiModel(
                        id = "preview-$index",
                        imageUrl = "",
                        contentDescription = null,
                    )
                },
            ),
            onFinished = {},
            onBackClick = {},
            onRetryClick = {},
        )
    }
}
