package com.example.sairo14.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sairo14.core.designsystem.theme.SairoTheme

/**
 * 온보딩 분석 완료 뒤 추천 결과가 채워질 화면의 빈 컨테이너를 표시한다.
 *
 * 이번 단계에서는 로딩 흐름의 도착지만 제공하며, 실제 추천 결과 데이터와 콘텐츠는 이후 기능에서
 * 이 화면에 추가한다.
 * @param modifier 결과 화면 컨테이너에 적용할 Modifier
 */
@Composable
fun OnboardingResultScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SairoTheme.colors.backgroundCanvas),
    )
}

@Preview(name = "Onboarding Result / Empty", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OnboardingResultScreenPreview() {
    SairoTheme {
        OnboardingResultScreen()
    }
}
