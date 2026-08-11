package com.example.sairo14.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.theme.SairoTheme

/**
 * 원격 이미지를 현재 레이아웃 제약에 맞춰 표시하고 실패하면 로컬 대체 이미지를 표시한다.
 *
 * URL의 유효성과 네트워크 응답은 Coil이 판단한다. 이미지의 크기와 배치는 호출자가 [modifier]로
 * 소유하므로, 이 컴포넌트는 화면별 고정 크기를 정의하지 않는다.
 * @param imageUrl 표시할 원격 이미지 주소. `null` 또는 빈 문자열이면 [fallbackRes]를 표시한다.
 * @param contentDescription 이미지의 접근성 설명
 * @param modifier 이미지 크기와 배치를 결정하는 Modifier
 * @param fallbackRes URL이 없거나 이미지 요청·디코딩에 실패했을 때 표시할 로컬 리소스
 * @param contentScale 표시 영역에서 이미지를 조정하는 방식
 */
@Composable
fun SairoRemoteImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    @DrawableRes fallbackRes: Int = R.drawable.img_dummy_view,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val fallbackPainter = painterResource(fallbackRes)

    AsyncImage(
        model = imageUrl?.takeIf(String::isNotBlank),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = ColorPainter(SairoTheme.colors.surfaceSunken),
        error = fallbackPainter,
        fallback = fallbackPainter,
    )
}
