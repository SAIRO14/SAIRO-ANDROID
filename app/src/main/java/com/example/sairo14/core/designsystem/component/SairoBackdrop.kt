package com.example.sairo14.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import coil3.compose.rememberAsyncImagePainter
import com.skydoves.cloudy.Sky
import com.skydoves.cloudy.rememberSky
import com.skydoves.cloudy.sky

/**
 * 화면의 backdrop 캡처 상태와 구형 Android blur 정책을 관리한다.
 *
 * 이 상태는 Compose 그래픽 레이어와 연결되므로 ViewModel이 아니라 화면의 Composition에서
 * [rememberSairoBackdropState]로 생성한다.
 */
@Stable
class SairoBackdropState internal constructor(
    internal val sky: Sky,
    internal val cpuBlurEnabled: Boolean,
) {
    /** 현재 backdrop 콘텐츠를 다시 캡처하도록 요청한다. */
    fun invalidate() {
        sky.invalidate()
    }

    /**
     * 지정한 애니메이션 시간 동안 backdrop 캡처를 계속 갱신한다.
     *
     * @param durationMillis 캡처 갱신을 유지할 시간
     */
    fun invalidate(durationMillis: Long) {
        sky.invalidate(durationMillis)
    }
}

/**
 * 화면의 Composition 수명주기에 맞는 [SairoBackdropState]를 생성해 기억한다.
 *
 * @param cpuBlurEnabled Android 30 이하에서 fallback scrim 대신 CPU blur를 사용할지 여부
 */
@Composable
fun rememberSairoBackdropState(
    cpuBlurEnabled: Boolean = false,
): SairoBackdropState {
    val sky = rememberSky()

    return remember(sky, cpuBlurEnabled) {
        SairoBackdropState(
            sky = sky,
            cpuBlurEnabled = cpuBlurEnabled,
        )
    }
}

/**
 * 배경 콘텐츠와 blur 대상이 같은 [SairoBackdropState] 캡처 계층에 놓이도록 구성한다.
 *
 * [content] 안에 배경과 [SairoHeader] 같은 blur 대상을 함께 배치한다.
 * @param state 이 영역의 backdrop 캡처 상태
 * @param modifier 캡처 컨테이너에 적용할 Modifier
 * @param content backdrop 배경과 blur 대상을 배치할 콘텐츠
 */
@Composable
fun SairoBackdropHost(
    state: SairoBackdropState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.sky(state.sky),
        content = content,
    )
}

/**
 * 비동기 이미지 상태가 바뀔 때 backdrop 캡처를 갱신하는 Painter를 생성한다.
 *
 * Coil의 로딩·성공·실패 상태가 변경될 때 [backdropState]를 갱신하여 CPU blur가 오래된
 * 이미지 캐시를 계속 표시하지 않도록 한다.
 * @param model Coil이 불러올 이미지 모델
 * @param backdropState 이미지가 포함된 backdrop 캡처 상태
 * @return [model]을 표시하고 backdrop 갱신을 연결한 Painter
 */
@Composable
fun rememberSairoBackdropImagePainter(
    model: Any?,
    backdropState: SairoBackdropState,
): Painter {
    val painter = rememberAsyncImagePainter(model = model)
    val painterState by painter.state.collectAsState()

    LaunchedEffect(painterState) {
        backdropState.invalidate()
    }

    return painter
}
