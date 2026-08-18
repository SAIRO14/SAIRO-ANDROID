package com.example.sairo14.feature.error

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.component.SairoButton
import com.example.sairo14.core.designsystem.component.SairoButtonStyle
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme

/**
 * 네트워크 오류 화면의 사용자 행동을 상태 소유 화면과 연결한다.
 *
 * 재시도 진행 상태와 실패한 요청은 호출한 Feature의 ViewModel이 소유한다. 이 Route는 그 상태에
 * 따라 [NetworkErrorScreen]을 표시하고 재시도·홈 이동 이벤트만 전달한다.
 * @param onRetryClick 실패한 요청을 다시 실행할 때 호출할 콜백
 * @param onHomeClick 홈으로 이동할 때 호출할 콜백
 * @param modifier 화면 컨테이너에 적용할 Modifier
 * @param showHomeAction 홈 이동 버튼을 표시할지 여부
 * @param retryEnabled 호출자가 직접 지정할 재시도 버튼 활성화 상태. 지정하지 않으면 시스템 연결 상태를 사용한다
 * @param viewModel 시스템 연결 상태를 관찰하는 ViewModel
 */
@Composable
fun NetworkErrorRoute(
    onRetryClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    showHomeAction: Boolean = true,
    retryEnabled: Boolean? = null,
    viewModel: NetworkErrorViewModel = hiltViewModel(),
) {
    val isSystemRetryEnabled by viewModel.isRetryEnabled.collectAsStateWithLifecycle()

    NetworkErrorScreen(
        onRetryClick = onRetryClick,
        onHomeClick = onHomeClick,
        modifier = modifier,
        showHomeAction = showHomeAction,
        retryEnabled = retryEnabled ?: isSystemRetryEnabled,
    )
}

/**
 * 네트워크 요청을 완료하지 못했을 때 재시도와 홈 이동 행동을 표시한다.
 *
 * 오류 종류와 재시도 진행 상태는 호출자가 소유하며, 이 화면은 상태를 변경하지 않는다. 일반 화면은
 * 시스템 안전 영역 안에서 콘텐츠를 중앙과 하단에 배치하고, 작은 높이에서는 스크롤 가능한 흐름으로
 * 전환해 버튼과 오류 문구가 겹치지 않게 한다.
 * @param onRetryClick 재시도 버튼을 눌렀을 때 호출할 콜백
 * @param onHomeClick 홈으로 이동 버튼을 눌렀을 때 호출할 콜백
 * @param modifier 화면 컨테이너에 적용할 Modifier
 * @param showHomeAction 홈 이동 버튼을 표시할지 여부
 * @param retryEnabled 재시도 버튼을 활성화할지 여부
 */
@Composable
fun NetworkErrorScreen(
    onRetryClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    showHomeAction: Boolean = true,
    retryEnabled: Boolean = true,
) {
    val colors = SairoTheme.colors

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundCanvas)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        if (maxHeight >= NetworkErrorCompactHeight) {
            NetworkErrorMessage(
                modifier = Modifier.align(Alignment.Center),
            )
            NetworkErrorActions(
                onRetryClick = onRetryClick,
                onHomeClick = onHomeClick,
                showHomeAction = showHomeAction,
                retryEnabled = retryEnabled,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .widthIn(max = NetworkErrorActionsMaxWidth)
                    .padding(horizontal = NetworkErrorHorizontalPadding),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = NetworkErrorHorizontalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(NetworkErrorCompactTopPadding))
                NetworkErrorMessage()
                Spacer(modifier = Modifier.height(NetworkErrorCompactContentActionsGap))
                NetworkErrorActions(
                    onRetryClick = onRetryClick,
                    onHomeClick = onHomeClick,
                    showHomeAction = showHomeAction,
                    retryEnabled = retryEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = NetworkErrorActionsMaxWidth),
                )
                Spacer(modifier = Modifier.height(NetworkErrorCompactBottomPadding))
            }
        }
    }
}

@Composable
private fun NetworkErrorMessage(
    modifier: Modifier = Modifier,
) {
    val colors = SairoTheme.colors

    Column(
        modifier = modifier.widthIn(max = NetworkErrorMessageMaxWidth),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(NetworkErrorImageTextGap),
    ) {
        Image(
            painter = painterResource(R.drawable.img_folder_error),
            contentDescription = null,
            modifier = Modifier.size(
                width = NetworkErrorImageWidth,
                height = NetworkErrorImageHeight,
            ),
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(NetworkErrorTitleDescriptionGap),
        ) {
            Text(
                text = stringResource(R.string.network_error_title),
                color = colors.textPrimary,
                style = SairoTextStyles.displayLight24,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.network_error_description),
                color = colors.textMuted,
                style = SairoTextStyles.bodyLight16,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun NetworkErrorActions(
    onRetryClick: () -> Unit,
    onHomeClick: () -> Unit,
    showHomeAction: Boolean,
    retryEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(NetworkErrorActionsGap),
    ) {
        SairoButton(
            text = stringResource(R.string.network_error_retry),
            onClick = onRetryClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = retryEnabled,
        )
        if (showHomeAction) {
            SairoButton(
                text = stringResource(R.string.network_error_go_home),
                onClick = onHomeClick,
                modifier = Modifier.fillMaxWidth(),
                style = SairoButtonStyle.Outline,
            )
        }
    }
}

private val NetworkErrorCompactHeight = 560.dp
private val NetworkErrorActionsMaxWidth = 360.dp
private val NetworkErrorMessageMaxWidth = 280.dp
private val NetworkErrorHorizontalPadding = 16.dp
private val NetworkErrorImageWidth = 100.dp
private val NetworkErrorImageHeight = 56.dp
private val NetworkErrorImageTextGap = 24.dp
private val NetworkErrorTitleDescriptionGap = 8.dp
private val NetworkErrorActionsGap = 10.dp
private val NetworkErrorCompactTopPadding = 24.dp
private val NetworkErrorCompactContentActionsGap = 24.dp
private val NetworkErrorCompactBottomPadding = 8.dp

@Preview(name = "Network Error", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun NetworkErrorScreenPreview() {
    SairoTheme {
        NetworkErrorScreen(
            onRetryClick = {},
            onHomeClick = {},
        )
    }
}

@Preview(name = "Network Error / Compact", showBackground = true, widthDp = 360, heightDp = 480)
@Composable
private fun NetworkErrorScreenCompactPreview() {
    SairoTheme {
        NetworkErrorScreen(
            onRetryClick = {},
            onHomeClick = {},
        )
    }
}
