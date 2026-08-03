package com.example.sairo14.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.component.SairoBackdropHost
import com.example.sairo14.core.designsystem.component.SairoBackdropState
import com.example.sairo14.core.designsystem.component.SairoButton
import com.example.sairo14.core.designsystem.component.SairoHeader
import com.example.sairo14.core.designsystem.component.SairoHeaderVariant
import com.example.sairo14.core.designsystem.component.rememberSairoBackdropImagePainter
import com.example.sairo14.core.designsystem.component.rememberSairoBackdropState
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme

/**
 * 홈 화면의 상태와 사용자 행동을 화면에 연결한다.
 *
 * 이미지 상태는 [HomeUiState]로 전달하고, 여행지 탐색·저장 목록 이동은 앱 내비게이션을
 * 소유한 호출자가 처리한다.
 * @param modifier 화면 컨테이너에 적용할 Modifier
 * @param viewModel 홈 화면의 중앙 이미지 상태를 소유하는 ViewModel
 * @param onFindTripClick 여행지 찾기 CTA를 눌렀을 때 호출할 동작
 * @param onFolderClick 상단 저장 목록 액션을 눌렀을 때 호출할 동작
 */
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onFindTripClick: () -> Unit = {},
    onFolderClick: () -> Unit = {},
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    HomeScreen(
        modifier = modifier,
        uiState = uiState,
        onFindTripClick = onFindTripClick,
        onFolderClick = onFolderClick,
        onRetryClick = viewModel::retry,
    )
}

/**
 * 홈 데이터의 로딩·콘텐츠·오류 상태에 맞는 화면을 표시한다.
 *
 * 콘텐츠 상태의 중앙 카드 묶음은 화면의 가용 가로폭을 기준으로 계산한다. 저장 여행지 목록의
 * 캔버스 표현은 후속 단계에서 [HomeUiState.Content.savedTrips]를 이용해 추가한다.
 * @param modifier 화면 컨테이너에 적용할 Modifier
 * @param uiState 화면에 표시할 로딩·콘텐츠·오류 상태
 * @param onFindTripClick 여행지 찾기 CTA를 눌렀을 때 호출할 동작
 * @param onFolderClick 상단 저장 목록 액션을 눌렀을 때 호출할 동작
 * @param onRetryClick 오류 화면의 재시도 버튼을 눌렀을 때 호출할 동작
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState = HomeUiState.Content(),
    onFindTripClick: () -> Unit = {},
    onFolderClick: () -> Unit = {},
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
) {
    HomeContainer(
        modifier = modifier,
        onFolderClick = onFolderClick,
    ) { backdropState ->
        val backImagePainter = rememberSairoBackdropImagePainter(
            model = uiState.discoveryImages.backImageUrl ?: R.drawable.img_dummy_view,
            backdropState = backdropState,
        )
        val frontImagePainter = rememberSairoBackdropImagePainter(
            model = uiState.discoveryImages.frontImageUrl ?: R.drawable.img_dummy_view,
            backdropState = backdropState,
        )

        Spacer(modifier = Modifier.height(HomeContentTopSpacing))

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
                    text = stringResource(R.string.home_empty_title),
                    color = SairoTheme.colors.textPrimary,
                    style = SairoTextStyles.displayLight24,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(HomeContentGap))

                HomeDiscoveryCta(
                    backPainter = backImagePainter,
                    frontPainter = frontImagePainter,
                    onClick = onFindTripClick,
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
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
    content: @Composable ColumnScope.(SairoBackdropState) -> Unit,
) {
    val colors = SairoTheme.colors
    val backdropState = rememberSairoBackdropState(cpuBlurEnabled = true)

    SairoBackdropHost(
        state = backdropState,
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundCanvas)
            .clipToBounds(),
    ) {
        Image(
            painter = painterResource(R.drawable.img_bg_shadow_default),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            SairoHeader(
                variant = SairoHeaderVariant.Home,
                actionIcon = painterResource(R.drawable.ic_folder_outline),
                actionContentDescription = stringResource(R.string.home_saved_places),
                onActionClick = onFolderClick,
                backdropState = backdropState,
            )
            content(backdropState)
        }
    }
}

private val HomeHorizontalPadding = 24.dp
private val HomeContentMaxWidth = 294.dp
private val HomeContentTopSpacing = 55.dp
private val HomeContentGap = 20.dp

@Preview(name = "Home Empty", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HomeScreenPreview() {
    SairoTheme {
        HomeScreen()
    }
}
