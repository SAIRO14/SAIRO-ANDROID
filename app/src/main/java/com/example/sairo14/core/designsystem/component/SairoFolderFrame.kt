package com.example.sairo14.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.theme.SairoTheme

/** 폴더 프레임에 적용할 Figma 시각 변형을 정의한다. */
enum class SairoFolderVariant {
    Large,
    Medium,
    Small,
}

/**
 * 다른 콘텐츠 뒤에 배치할 폴더 형태의 배경 프레임을 표시한다.
 *
 * 이 컴포넌트는 폴더의 실루엣과 시각 변형만 책임지며 클릭·콘텐츠·상태를 소유하지 않는다.
 * 이미지 카드, 버튼, 여행지 정보는 호출하는 화면의 `Box`에서 이 프레임 위에 배치한다.
 * 실제 크기는 부모의 제약과 [modifier]가 결정한다.
 * @param variant Figma에 정의된 폴더 프레임 시각 변형
 * @param modifier 프레임에 적용할 Modifier
 * @param frameWidth 프레임의 실제 가로 길이. `null`이면 [modifier] 또는 부모의 제약을 사용하며,
 * 높이는 선택한 [variant]의 원본 비율에 맞춰 계산한다.
 */
@Composable
fun SairoFolderFrame(
    variant: SairoFolderVariant,
    modifier: Modifier = Modifier,
    frameWidth: Dp? = null,
) {
    val specification = variant.specification

    Image(
        painter = painterResource(specification.drawableRes),
        contentDescription = null,
        modifier = modifier
            .then(if (frameWidth != null) Modifier.width(frameWidth) else Modifier)
            .aspectRatio(specification.aspectRatio),
        contentScale = ContentScale.FillBounds,
    )
}

private val SairoFolderVariant.specification: SairoFolderSpecification
    get() = when (this) {
        SairoFolderVariant.Large -> SairoFolderSpecification(
            size = DpSize(375.dp, 230.dp),
            drawableRes = R.drawable.img_folder_large,
        )

        SairoFolderVariant.Medium -> SairoFolderSpecification(
            size = DpSize(300.dp, 170.dp),
            drawableRes = R.drawable.img_folder_medium,
        )

        SairoFolderVariant.Small -> SairoFolderSpecification(
            size = DpSize(300.dp, 150.dp),
            drawableRes = R.drawable.img_folder_small,
        )
    }

private data class SairoFolderSpecification(
    val size: DpSize,
    val drawableRes: Int,
)

private val SairoFolderSpecification.aspectRatio: Float
    get() = size.width.value / size.height.value

@Preview(name = "Sairo Folder Frames", showBackground = false, widthDp = 400, heightDp = 620)
@Composable
private fun SairoFolderFramePreview() {
    SairoTheme {
        androidx.compose.foundation.layout.Column(
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
        ) {
            SairoFolderFrame(
                variant = SairoFolderVariant.Large,
                frameWidth = 375.dp,
            )
            SairoFolderFrame(
                variant = SairoFolderVariant.Medium,
                frameWidth = 300.dp,
            )
            SairoFolderFrame(
                variant = SairoFolderVariant.Small,
                frameWidth = 300.dp,
            )
        }
    }
}
