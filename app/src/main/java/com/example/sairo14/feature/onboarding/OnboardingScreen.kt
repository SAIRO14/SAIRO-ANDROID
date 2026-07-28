package com.example.sairo14.feature.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 여행지 찾기 서비스의 온보딩 소개 화면을 표시한다.
 *
 * 소개 문구와 CTA, 홈 이동 동작은 온보딩 Route와 ViewModel이 준비되는 다음 단계에서 연결한다.
 * @param modifier 온보딩 화면 컨테이너에 적용할 Modifier
 */
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
    ) {}
}
