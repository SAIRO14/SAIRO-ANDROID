package com.example.sairo14.feature.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 여행지 탐색 전 사용자가 도착하는 홈의 빈 상태 화면을 표시한다.
 *
 * 실제 빈 상태 안내와 여행지 찾기 재진입 행동은 호출자가 추가할 화면 상태와 이벤트로 확장한다.
 * @param modifier 홈 화면 컨테이너에 적용할 Modifier
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
    ) {}
}
