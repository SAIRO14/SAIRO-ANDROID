package com.example.sairo14.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.extension.noRippleClickable

/**
 * 원격 이미지와 삭제 동작을 제공하는 작은 이미지 썸네일을 표시한다.
 *
 * 이미지 요청과 실패 처리는 [SairoRemoteImage]가 담당한다. 썸네일의 배치와 주변 여백은 호출자가
 * [modifier]로 소유하며, 이 컴포넌트는 X 버튼 클릭만 [onRemoveClick]으로 전달한다.
 * @param imageUrl 썸네일에 표시할 원격 이미지 주소
 * @param onRemoveClick X 버튼을 클릭했을 때 호출할 동작
 * @param modifier 썸네일에 적용할 Modifier
 * @param contentDescription 썸네일 이미지의 접근성 설명
 * @param fallbackRes URL이 없거나 이미지 요청에 실패했을 때 표시할 로컬 리소스
 * @param enabled `false`이면 X 버튼 클릭을 전달하지 않는지 여부
 */
@Composable
fun SairoImageThumbnail(
    imageUrl: String?,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    @DrawableRes fallbackRes: Int = R.drawable.img_dummy_view,
    enabled: Boolean = true,
) {
    val colors = SairoTheme.colors
    val imageShape = RoundedCornerShape(4.dp)
    val outerBorderShape = RoundedCornerShape(5.dp)

    Box(
        modifier = modifier.size(ImageThumbnailLayoutSize),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(ImageThumbnailSize)
                .clip(imageShape)
                .background(colors.surfaceSunken),
        ) {
            SairoRemoteImage(
                imageUrl = imageUrl,
                contentDescription = contentDescription,
                fallbackRes = fallbackRes,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Box(
            modifier = Modifier
                .size(ImageThumbnailLayoutSize)
                .border(
                    width = OutstrokeWidth,
                    color = colors.borderDefault,
                    shape = outerBorderShape,
                ),
        )

        Icon(
            painter = painterResource(R.drawable.ic_close_circle_fill),
            contentDescription = stringResource(R.string.sairo_image_thumbnail_remove),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 7.dp, y = (-7).dp)
                .size(RemoveButtonTouchSize)
                .noRippleClickable(
                    isEnabled = enabled,
                    role = Role.Button,
                    onClick = onRemoveClick,
                ),
            tint = Color.Unspecified,
        )
    }
}

private val ImageThumbnailSize = 50.dp
private val RemoveButtonTouchSize = 24.dp
private val OutstrokeWidth = 1.dp
private val ImageThumbnailLayoutSize = ImageThumbnailSize + OutstrokeWidth * 2

@Preview(name = "Sairo Image Thumbnail", showBackground = true)
@Composable
private fun SairoImageThumbnailPreview() {
    SairoTheme {
        Box(
            modifier = Modifier.size(74.dp),
            contentAlignment = Alignment.Center,
        ) {
            SairoImageThumbnail(
                imageUrl = null,
                onRemoveClick = {},
                contentDescription = stringResource(R.string.sairo_image_thumbnail_preview),
            )
        }
    }
}
