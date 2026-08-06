package com.example.sairo14.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.RectangleShape
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
import com.example.sairo14.core.designsystem.token.SairoShadowStyles
import com.example.sairo14.core.extension.sairoDropShadow
import com.example.sairo14.domain.model.OnboardingRecommendation

/**
 * 온보딩 결과의 상태와 내비게이션 행동을 화면에 연결한다.
 *
 * 선택 사진 ID는 Route가 소유하고, 결과 조회·재시도·북마크 표시 상태는 [OnboardingResultViewModel]이
 * 관리한다. 홈 이동과 사진 재선택 이동은 호출자가 소유한다.
 */
@Composable
fun OnboardingResultRoute(
    selectedPhotoIds: List<String>,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onRequestAgainClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingResultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedPhotoIds) {
        viewModel.load(selectedPhotoIds)
    }

    OnboardingResultScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onRequestAgainClick = onRequestAgainClick,
        onRetryClick = viewModel::retry,
        onBookmarkClick = viewModel::toggleSaved,
        modifier = modifier,
    )
}

/**
 * 온보딩 분석 뒤 추천 수에 맞는 지역 목록 또는 재추천 안내를 표시한다.
 *
 * 추천이 2개 이상이면 카드 목록만 스크롤하고, 0개 또는 1개면 재추천 영역을 화면 하단에 고정한다.
 * 카드와 버튼의 실제 동작은 호출자가 전달한 콜백으로 처리한다.
 */
@Composable
fun OnboardingResultScreen(
    uiState: OnboardingResultUiState,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onRequestAgainClick: () -> Unit,
    onRetryClick: () -> Unit,
    onBookmarkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingResultContainer(
        modifier = modifier,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
    ) { backdropState, headerHeight ->
        when (uiState) {
            OnboardingResultUiState.Loading -> ResultPending(
                headerHeight = headerHeight,
                modifier = Modifier.fillMaxSize(),
            )

            OnboardingResultUiState.Error -> ResultError(
                headerHeight = headerHeight,
                onRetryClick = onRetryClick,
                modifier = Modifier.fillMaxSize(),
            )

            is OnboardingResultUiState.Content -> ResultContent(
                recommendations = uiState.recommendations,
                backdropState = backdropState,
                headerHeight = headerHeight,
                onRequestAgainClick = onRequestAgainClick,
                onBookmarkClick = onBookmarkClick,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun OnboardingResultContainer(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier,
    content: @Composable BoxScope.(SairoBackdropState, Dp) -> Unit,
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
                title = stringResource(R.string.onboarding_result_header_title),
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
private fun ResultContent(
    recommendations: List<OnboardingRecommendation>,
    backdropState: SairoBackdropState,
    headerHeight: Dp,
    onRequestAgainClick: () -> Unit,
    onBookmarkClick: (String) -> Unit,
    modifier: Modifier,
) {
    val isInsufficient = recommendations.size <= InsufficientRecommendationCount
    val density = LocalDensity.current
    var footerHeightPx by remember { mutableIntStateOf(0) }
    val footerHeight = with(density) { footerHeightPx.toDp() }
    val navigationBarPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()
    val listBottomPadding = if (isInsufficient) {
        footerHeight + ResultContentBottomSpacing
    } else {
        navigationBarPadding + ResultContentBottomSpacing
    }

    Column(modifier = modifier) {
        ResultTitleSection(headerHeight = headerHeight)
        ResultTitleShadow()

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = listBottomPadding),
            ) {
                item {
                    Spacer(modifier = Modifier.height(TitleToCardsSpacing))
                }
                itemsIndexed(
                    items = recommendations,
                    key = { _, recommendation -> recommendation.id },
                ) { index, recommendation ->
                    RecommendationCard(
                        recommendation = recommendation,
                        backdropState = backdropState,
                        onBookmarkClick = onBookmarkClick,
                    )
                    if (index < recommendations.lastIndex) {
                        Spacer(modifier = Modifier.height(CardSpacing))
                    }
                }
            }

            if (isInsufficient) {
                InsufficientRecommendationFooter(
                    onRequestAgainClick = onRequestAgainClick,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onSizeChanged { size -> footerHeightPx = size.height },
                )
            }
        }
    }
}

@Composable
private fun ResultTitleSection(
    headerHeight: Dp,
) {
    val colors = SairoTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = ResultHorizontalPadding,
                top = headerHeight + TitleTopSpacing,
                end = ResultHorizontalPadding,
                bottom = TitleBottomSpacing,
            ),
        verticalArrangement = Arrangement.spacedBy(TitleTextSpacing),
    ) {
        Text(
            text = stringResource(R.string.onboarding_result_title),
            color = colors.textPrimary,
            style = SairoTextStyles.displayLight28,
        )
        Text(
            text = stringResource(R.string.onboarding_result_description),
            color = colors.textMuted,
            style = SairoTextStyles.bodyLight16,
        )
    }
}

/** 투명한 결과 제목 아래에 Figma의 은은한 경계 그림자만 표시한다. */
@Composable
private fun ResultTitleShadow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .sairoDropShadow(
                shape = RectangleShape,
                shadowStyle = SairoShadowStyles.glowSubtle,
            ),
    )
}

@Composable
private fun RecommendationCard(
    recommendation: OnboardingRecommendation,
    backdropState: SairoBackdropState,
    onBookmarkClick: (String) -> Unit,
) {
    val imagePainters = recommendation.imageUrls.take(MaxCardImageCount).map { imageUrl ->
        rememberSairoBackdropImagePainter(
            model = imageUrl,
            backdropState = backdropState,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ResultHorizontalPadding),
        contentAlignment = Alignment.TopCenter,
    ) {
        SairoPlaceFolderCard(
            imagePainters = imagePainters,
            regionLabel = recommendation.regionName,
            description = recommendation.description,
            placeNames = recommendation.placeNames,
            saved = recommendation.isSaved,
            onClick = {},
            onBookmarkClick = { onBookmarkClick(recommendation.id) },
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = ResultCardMaxWidth),
            imageContentDescription = recommendation.regionName,
            cardEnabled = false,
        )
    }
}

@Composable
private fun InsufficientRecommendationFooter(
    onRequestAgainClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SairoTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ResultHorizontalPadding)
            .navigationBarsPadding()
            .padding(bottom = FooterBottomSpacing),
        verticalArrangement = Arrangement.spacedBy(FooterSectionSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FooterInfoSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalDivider(color = colors.borderDefault)
            Text(
                text = stringResource(R.string.onboarding_result_insufficient_description),
                color = colors.textMuted,
                style = SairoTextStyles.bodyLight16,
                textAlign = TextAlign.Center,
            )
        }
        SairoButton(
            text = stringResource(R.string.onboarding_result_request_again),
            onClick = onRequestAgainClick,
            modifier = Modifier.fillMaxWidth(),
            style = SairoButtonStyle.Outline,
        )
    }
}

@Composable
private fun ResultPending(
    headerHeight: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(top = headerHeight),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = SairoTheme.colors.accentBase)
    }
}

@Composable
private fun ResultError(
    headerHeight: Dp,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(
            start = ResultHorizontalPadding,
            top = headerHeight,
            end = ResultHorizontalPadding,
        ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.onboarding_result_error),
            color = SairoTheme.colors.textPrimary,
            style = SairoTextStyles.bodyLight18,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(ErrorButtonSpacing))
        SairoButton(
            text = stringResource(R.string.onboarding_result_retry),
            onClick = onRetryClick,
            style = SairoButtonStyle.Outline,
        )
    }
}

private const val InsufficientRecommendationCount = 1
private const val MaxCardImageCount = 2
private val ResultHorizontalPadding = 16.dp
private val ResultCardMaxWidth = 300.dp
private val TitleTopSpacing = 18.dp
private val TitleBottomSpacing = 13.dp
private val TitleTextSpacing = 8.dp
private val TitleToCardsSpacing = 20.dp
private val CardSpacing = 24.dp
private val ResultContentBottomSpacing = 40.dp
private val FooterBottomSpacing = 8.dp
private val FooterSectionSpacing = 16.dp
private val FooterInfoSpacing = 24.dp
private val ErrorButtonSpacing = 16.dp

@Preview(name = "Onboarding Result / Multiple", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OnboardingResultMultiplePreview() {
    SairoTheme {
        OnboardingResultScreen(
            uiState = OnboardingResultUiState.Content(previewRecommendations),
            onBackClick = {},
            onHomeClick = {},
            onRequestAgainClick = {},
            onRetryClick = {},
            onBookmarkClick = {},
        )
    }
}

@Preview(name = "Onboarding Result / Insufficient", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OnboardingResultInsufficientPreview() {
    SairoTheme {
        OnboardingResultScreen(
            uiState = OnboardingResultUiState.Content(previewRecommendations.take(1)),
            onBackClick = {},
            onHomeClick = {},
            onRequestAgainClick = {},
            onRetryClick = {},
            onBookmarkClick = {},
        )
    }
}

@Preview(name = "Onboarding Result / Empty", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OnboardingResultEmptyPreview() {
    SairoTheme {
        OnboardingResultScreen(
            uiState = OnboardingResultUiState.Content(emptyList()),
            onBackClick = {},
            onHomeClick = {},
            onRequestAgainClick = {},
            onRetryClick = {},
            onBookmarkClick = {},
        )
    }
}

private val previewRecommendations = listOf(
    OnboardingRecommendation(
        id = "preview-boeun",
        courseId = "course-boeun",
        regionName = "충북 보은권",
        description = "고요한 자연과 전통의 분위기",
        imageUrls = emptyList(),
        placeNames = listOf("말티재 전망대", "세조길 숲 산책"),
    ),
    OnboardingRecommendation(
        id = "preview-gangneung",
        courseId = "course-gangneung",
        regionName = "강원 강릉권",
        description = "바다와 골목이 어우러진 느긋한 풍경",
        imageUrls = emptyList(),
        placeNames = listOf("안목해변", "명주동 골목"),
    ),
)
