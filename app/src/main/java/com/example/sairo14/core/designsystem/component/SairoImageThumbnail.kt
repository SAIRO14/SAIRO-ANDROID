package com.example.sairo14.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.theme.SairoTheme

/**
 * 삭제 동작을 제공하는 작은 이미지 썸네일을 표시한다.
 *
 * 이미지와 삭제 결과는 호출자가 소유하며, 이 컴포넌트는 우측 상단 X 버튼을 눌렀을 때
 * [onRemoveClick]만 전달한다. X 버튼은 이미지 영역 밖으로 일부 돌출되므로, 인접 요소와
 * 겹치지 않도록 호출하는 레이아웃에서 오른쪽·위쪽 여백을 확보해야 한다. 테두리는 이미지
 * 영역 크기를 줄이지 않도록 컴포넌트 측정 범위에 포함해 바깥쪽에 그린다.
 * @param painter 썸네일에 표시할 이미지
 * @param onRemoveClick X 버튼을 클릭했을 때 호출할 동작
 * @param modifier 썸네일에 적용할 Modifier
 * @param contentDescription 썸네일 이미지의 접근성 설명
 * @param enabled `false`이면 X 버튼 클릭을 전달하지 않는지 여부
 */
@Composable
fun SairoImageThumbnail(
    painter: Painter,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
) {
    val colors = SairoTheme.colors
    val imageShape = RoundedCornerShape(4.dp)
    val outerBorderShape = RoundedCornerShape(5.dp)

    Box(
        modifier = modifier.size(ImageThumbnailLayoutSize),
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .align(Alignment.Center)
                .size(ImageThumbnailSize)
                .clip(imageShape)
                .background(colors.surfaceSunken),
            contentScale = ContentScale.Crop,
        )

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
                .clickable(
                    enabled = enabled,
                    interactionSource = null,
                    indication = null,
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
                painter = painterResource(R.drawable.img_dummy_view),
                onRemoveClick = {},
                contentDescription = stringResource(R.string.sairo_image_thumbnail_preview),
            )
        }
    }
}
