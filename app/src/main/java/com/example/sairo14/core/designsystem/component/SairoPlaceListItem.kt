package com.example.sairo14.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme

/** 여행 상세 바텀시트 장소 행의 Figma 정보 배치를 정의한다. */
enum class SairoPlaceListItemVariant {
    Simple,
    Detailed,
}

/**
 * 여행 상세 바텀시트에 표시할 장소 정보 행을 그린다.
 *
 * [SairoPlaceListItemVariant.Simple]은 56dp 썸네일과 정보를 가로로 배치하고,
 * [SairoPlaceListItemVariant.Detailed]는 제목·태그·이미지를 세로로 배치한다. 장소 정보와
 * 이미지의 소유·변경은 호출자에게 있으며, 이 컴포넌트는 정보를 표시만 한다.
 * @param title 장소 순서와 이름을 포함한 제목
 * @param tags 운영 시간·휴무일처럼 장소에 표시할 태그 문구 목록
 * @param painter 장소 이미지를 표시할 Painter
 * @param modifier 장소 행에 적용할 Modifier
 * @param variant Figma의 Simple 또는 Detailed 정보 배치
 * @param imageContentDescription 장소 이미지의 접근성 설명
 */
@Composable
fun SairoPlaceListItem(
    title: String,
    tags: List<String>,
    painter: Painter,
    modifier: Modifier = Modifier,
    variant: SairoPlaceListItemVariant = SairoPlaceListItemVariant.Simple,
    imageContentDescription: String? = null,
) {
    when (variant) {
        SairoPlaceListItemVariant.Simple -> SairoSimplePlaceListItem(
            title = title,
            tags = tags,
            painter = painter,
            modifier = modifier,
            imageContentDescription = imageContentDescription,
        )

        SairoPlaceListItemVariant.Detailed -> SairoDetailedPlaceListItem(
            title = title,
            tags = tags,
            painter = painter,
            modifier = modifier,
            imageContentDescription = imageContentDescription,
        )
    }
}

@Composable
private fun SairoSimplePlaceListItem(
    title: String,
    tags: List<String>,
    painter: Painter,
    modifier: Modifier,
    imageContentDescription: String?,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaceImage(
            painter = painter,
            contentDescription = imageContentDescription,
            modifier = Modifier.size(56.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PlaceTitle(title = title)
            PlaceTags(tags = tags)
        }
    }
}

@Composable
private fun SairoDetailedPlaceListItem(
    title: String,
    tags: List<String>,
    painter: Painter,
    modifier: Modifier,
    imageContentDescription: String?,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PlaceTitle(title = title)
        PlaceTags(tags = tags)
        PlaceImage(
            painter = painter,
            contentDescription = imageContentDescription,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        )
    }
}

@Composable
private fun PlaceTitle(title: String) {
    Text(
        text = title,
        color = SairoTheme.colors.textPrimary,
        style = SairoTextStyles.headRegular16,
    )
}

@Composable
private fun PlaceTags(tags: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        tags.forEach { tag ->
            SairoTag(
                text = tag,
                variant = SairoTagVariant.SmallGray,
            )
        }
    }
}

@Composable
private fun PlaceImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier,
) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop,
    )
}

@Preview(name = "Sairo Place List Item / Simple", showBackground = true, widthDp = 375)
@Composable
private fun SairoPlaceListItemSimplePreview() {
    SairoTheme {
        SairoPlaceListItemPreview(variant = SairoPlaceListItemVariant.Simple)
    }
}

@Preview(name = "Sairo Place List Item / Detailed", showBackground = true, widthDp = 375)
@Composable
private fun SairoPlaceListItemDetailedPreview() {
    SairoTheme {
        SairoPlaceListItemPreview(variant = SairoPlaceListItemVariant.Detailed)
    }
}

@Composable
private fun SairoPlaceListItemPreview(variant: SairoPlaceListItemVariant) {
    SairoPlaceListItem(
        title = stringResource(R.string.sairo_place_list_item_preview_title),
        tags = listOf(
            stringResource(R.string.sairo_place_list_item_preview_hours),
            stringResource(R.string.sairo_place_list_item_preview_closed_day),
            stringResource(R.string.sairo_place_list_item_preview_parking),
        ),
        painter = painterResource(R.drawable.img_dummy_view),
        variant = variant,
        imageContentDescription = stringResource(R.string.sairo_place_list_item_preview_image),
        modifier = Modifier.width(343.dp),
    )
}
