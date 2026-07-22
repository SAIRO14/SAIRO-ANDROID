package com.example.sairo14.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.theme.SairoTheme

/** 폴더 프레임의 Figma 크기 변형을 정의한다. */
enum class SairoFolderSize {
    Large,
    Medium,
    Small,
}

/**
 * 다른 콘텐츠 뒤에 배치할 폴더 형태의 배경 프레임을 표시한다.
 *
 * 이 컴포넌트는 폴더의 실루엣과 크기만 책임지며 클릭·콘텐츠·상태를 소유하지 않는다.
 * 이미지 카드, 버튼, 여행지 정보는 호출하는 화면의 `Box`에서 이 프레임 위에 배치한다.
 * @param size Figma에 정의된 폴더 프레임 크기
 * @param modifier 프레임에 적용할 Modifier
 */
@Composable
fun SairoFolderFrame(
    size: SairoFolderSize,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(size.specification.drawableRes),
        contentDescription = null,
        modifier = modifier
            .size(size.specification.size),
        contentScale = ContentScale.FillBounds,
    )
}

private val SairoFolderSize.specification: SairoFolderSpecification
    get() = when (this) {
        SairoFolderSize.Large -> SairoFolderSpecification(
            size = DpSize(375.dp, 230.dp),
            drawableRes = R.drawable.img_folder_large,
        )

        SairoFolderSize.Medium -> SairoFolderSpecification(
            size = DpSize(300.dp, 170.dp),
            drawableRes = R.drawable.img_folder_medium,
        )

        SairoFolderSize.Small -> SairoFolderSpecification(
            size = DpSize(300.dp, 150.dp),
            drawableRes = R.drawable.img_folder_small,
        )
    }

private data class SairoFolderSpecification(
    val size: DpSize,
    val drawableRes: Int,
)

@Preview(name = "Sairo Folder Frames", showBackground = false, widthDp = 400, heightDp = 620)
@Composable
private fun SairoFolderFramePreview() {
    SairoTheme {
        androidx.compose.foundation.layout.Column(
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
        ) {
            SairoFolderFrame(size = SairoFolderSize.Large)
            SairoFolderFrame(size = SairoFolderSize.Medium)
            SairoFolderFrame(size = SairoFolderSize.Small)
        }
    }
}
