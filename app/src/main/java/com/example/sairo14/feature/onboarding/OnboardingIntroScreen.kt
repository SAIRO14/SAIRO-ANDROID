package com.example.sairo14.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.component.SairoButton
import com.example.sairo14.core.designsystem.component.SairoHeader
import com.example.sairo14.core.designsystem.component.SairoHeaderVariant
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.skydoves.cloudy.rememberSky
import com.skydoves.cloudy.sky

/**
 * 온보딩 소개 화면의 상태와 내비게이션 행동을 화면에 연결한다.
 *
 * ViewModel의 UI 상태와 사용자 이벤트는 이 Route에서 [OnboardingIntroScreen]으로 전달한다.
 * @param modifier 화면 컨테이너에 적용할 Modifier
 * @param viewModel 인트로 이미지 상태를 소유하는 ViewModel
 * @param onHomeClick 홈으로 이동해야 할 때 호출할 콜백
 * @param onStartClick 여행지 찾기를 시작해야 할 때 호출할 콜백
 */
@Composable
fun OnboardingIntroRoute(
    modifier: Modifier = Modifier,
    viewModel: OnboardingIntroViewModel = hiltViewModel(),
    onHomeClick: () -> Unit,
    onStartClick: () -> Unit = {},
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    OnboardingIntroScreen(
        modifier = modifier,
        uiState = uiState,
        onHomeClick = onHomeClick,
        onStartClick = onStartClick,
    )
}

/**
 * 여행지 찾기 서비스의 온보딩 소개 화면을 표시한다.
 *
 * 사진 카드 배경은 화면 끝까지 표시하고, 헤더와 CTA는 각각 시스템 상태 표시줄·내비게이션 바
 * inset을 고려한다. 홈 이동과 시작 행동은 호출자가 소유한다.
 * @param modifier 온보딩 화면 컨테이너에 적용할 Modifier
 * @param uiState 서버에서 제공할 인트로 이미지 상태
 * @param onHomeClick 우측 홈 버튼을 눌렀을 때 호출할 콜백
 * @param onStartClick 여행지 찾기 시작 CTA를 눌렀을 때 호출할 콜백
 */
@Composable
fun OnboardingIntroScreen(
    modifier: Modifier = Modifier,
    uiState: OnboardingIntroUiState = OnboardingIntroUiState(),
    onHomeClick: () -> Unit = {},
    onStartClick: () -> Unit = {},
) {
    val colors = SairoTheme.colors
    val backdropSky = rememberSky()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundCanvas)
            .clipToBounds(),
    ) {
        Image(
            painter = painterResource(R.drawable.img_bg_shadow_top),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .sky(backdropSky),
            contentScale = ContentScale.FillBounds,
        )

        Image(
            painter = painterResource(R.drawable.img_shadow_bottom),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(130.dp),
            contentScale = ContentScale.FillBounds,
        )

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            SairoHeader(
                variant = SairoHeaderVariant.ActionOnly,
                actionIcon = painterResource(R.drawable.ic_home),
                actionContentDescription = stringResource(R.string.sairo_header_home),
                iconTint = colors.textWhite,
                onActionClick = onHomeClick,
                backdropSky = backdropSky,
            )

            Spacer(modifier = Modifier.height(37.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_intro_title),
                    color = colors.textPrimary,
                    style = SairoTextStyles.displayLight28,
                )
                Text(
                    text = stringResource(R.string.onboarding_intro_description),
                    color = colors.textMuted,
                    style = SairoTextStyles.bodyLight20,
                )
                Spacer(modifier = Modifier.weight(1f))

                SairoButton(
                    text = stringResource(R.string.onboarding_intro_start),
                    onClick = onStartClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                )
            }
        }
    }
}

@Preview(name = "Onboarding Intro", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OnboardingIntroScreenPreview() {
    SairoTheme {
        OnboardingIntroScreen()
    }
}
