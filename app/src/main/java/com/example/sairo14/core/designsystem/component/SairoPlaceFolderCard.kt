package com.example.sairo14.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.theme.SairoTextStyles
import com.example.sairo14.core.designsystem.theme.SairoTheme
import com.example.sairo14.core.designsystem.token.SairoShadowStyles
import com.example.sairo14.core.extension.sairoDropShadow
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 저장한 여행지의 지역·분위기·장소를 폴더 형태로 표시하는 카드를 그린다.
 *
 * 카드 이동과 북마크 상태는 호출자가 소유한다. 카드 클릭은 [onClick]으로, 북마크 클릭은
 * [onBookmarkClick]으로 각각 전달한다. Figma의 겹친 사진 레이아웃에는 [imagePainters]의
 * 첫 두 이미지만 표시하며, 사진 크기는 카드가 배치된 부모의 가로 너비에 비례해 계산한다.
 * @param imagePainters 겹쳐진 여행지 이미지에 표시할 Painter 목록
 * @param regionLabel 지역을 나타내는 태그 문구
 * @param description 여행지의 분위기 또는 요약 문구
 * @param placeNames 카드 하단에 표시할 장소 이름 목록
 * @param saved 현재 저장 여부
 * @param onClick 카드를 클릭했을 때 호출할 동작
 * @param onBookmarkClick 북마크 버튼을 클릭했을 때 호출할 동작
 * @param modifier 카드에 적용할 Modifier
 * @param imageContentDescription 여행지 이미지의 접근성 설명
 * @param enabled `false`이면 카드와 북마크 클릭을 모두 전달하지 않는지 여부
 * @param cardEnabled 카드 본문 클릭을 전달할지 여부. 상세 화면이 아직 없는 목록에서 카드의 시각 상태는
 * 유지하면서 클릭만 막을 때 사용한다.
 * @param bookmarkEnabled 북마크 클릭을 전달할지 여부
 */
@Composable
fun SairoPlaceFolderCard(
    imagePainters: List<Painter>,
    regionLabel: String,
    description: String,
    placeNames: List<String>,
    saved: Boolean,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageContentDescription: String? = null,
    enabled: Boolean = true,
    cardEnabled: Boolean = enabled,
    bookmarkEnabled: Boolean = enabled,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        val scaleFactor = maxWidth.value / FolderBaseWidth.value
        val imageSize = maxWidth * ImageWidthRatio
        val imageBottomPadding = maxWidth * ImageBottomPaddingRatio

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(FolderCardAspectRatio)
                .clickable(
                    enabled = cardEnabled,
                    interactionSource = null,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                ),
        ) {
            StackedPlaceImages(
                painters = imagePainters,
                imageSize = imageSize,
                bottomPadding = imageBottomPadding,
                scaleFactor = scaleFactor,
                contentDescription = imageContentDescription,
                modifier = Modifier.align(Alignment.TopCenter),
            )
            FolderInformation(
                regionLabel = regionLabel,
                description = description,
                placeNames = placeNames,
                saved = saved,
                enabled = bookmarkEnabled,
                onBookmarkClick = onBookmarkClick,
                scaleFactor = scaleFactor,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun StackedPlaceImages(
    painters: List<Painter>,
    imageSize: Dp,
    bottomPadding: Dp,
    scaleFactor: Float,
    contentDescription: String?,
    modifier: Modifier,
) {
    val frontImageBounds = imageSize.rotatedSquareBounds(FrontImageRotation)
    val backImageBounds = imageSize.rotatedSquareBounds(BackImageRotation)
    val imageStackSize = maxOf(frontImageBounds, backImageBounds)
    val reservedBottomPadding = (imageSize + bottomPadding - imageStackSize).coerceAtLeast(0.dp)

    Box(
        modifier = modifier
            .padding(bottom = reservedBottomPadding)
            .size(imageStackSize),
    ) {
        painters.getOrNull(1)?.let { backPainter ->
            PlaceFolderImage(
                painter = backPainter,
                imageSize = imageSize,
                wrapperSize = backImageBounds,
                scaleFactor = scaleFactor,
                contentDescription = null,
                rotation = BackImageRotation,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = 1.dp * scaleFactor, y = 4.dp * scaleFactor),
            )
        }
        painters.firstOrNull()?.let { frontPainter ->
            PlaceFolderImage(
                painter = frontPainter,
                imageSize = imageSize,
                wrapperSize = frontImageBounds,
                scaleFactor = scaleFactor,
                contentDescription = contentDescription,
                rotation = FrontImageRotation,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = (-2).dp * scaleFactor),
            )
        }
    }
}

@Composable
private fun PlaceFolderImage(
    painter: Painter,
    imageSize: Dp,
    wrapperSize: Dp,
    scaleFactor: Float,
    contentDescription: String?,
    rotation: Float,
    modifier: Modifier,
) {
    val shape = RoundedCornerShape(24.dp * scaleFactor)

    Box(modifier = modifier.size(wrapperSize)) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .align(Alignment.Center)
                .size(imageSize)
                .rotate(rotation)
                .sairoDropShadow(
                    shape = shape,
                    shadowStyle = SairoShadowStyles.glowDefault,
                )
                .clip(shape)
                .background(SairoTheme.colors.surfaceSunken),
            contentScale = ContentScale.Crop,
        )
    }
}

private val FolderBaseWidth = 300.dp
private const val FolderCardAspectRatio = 300f / 286f
private const val ImageWidthRatio = 260f / 300f
private const val ImageBottomPaddingRatio = 26f / 300f
private const val BackImageRotation = 1f
private const val FrontImageRotation = -3f

private fun Dp.rotatedSquareBounds(rotation: Float): Dp {
    val radians = Math.toRadians(rotation.toDouble())
    return this * (abs(cos(radians)) + abs(sin(radians))).toFloat()
}

@Composable
private fun FolderInformation(
    regionLabel: String,
    description: String,
    placeNames: List<String>,
    saved: Boolean,
    enabled: Boolean,
    onBookmarkClick: () -> Unit,
    scaleFactor: Float,
    modifier: Modifier,
) {
    Box(
        modifier = modifier,
    ) {
        SairoFolderFrame(
            variant = SairoFolderVariant.Small,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 12.dp * scaleFactor)
                .padding(horizontal = 12.dp * scaleFactor),
            contentAlignment = Alignment.TopCenter,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SairoTag(
                        text = regionLabel,
                        variant = SairoTagVariant.MediumLemon,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        color = SairoTheme.colors.textPrimary,
                        style = SairoTextStyles.headRegular16,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                SairoBookmarker(
                    saved = saved,
                    enabled = enabled,
                    onClick = onBookmarkClick,
                    size = 24.dp,
                    modifier = Modifier
                        .offset(
                            y = BookmarkVerticalAdjustment * scaleFactor,
                        ),
                )
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(
                    start = 12.dp * scaleFactor,
                    end = 12.dp * scaleFactor,
                    bottom = 16.dp * scaleFactor,
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp * scaleFactor),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            placeNames.take(MaxPlaceCount).forEach { placeName ->
                PlaceLocation(
                    name = placeName,
                    scaleFactor = scaleFactor,
                )
            }
        }
    }
}

@Composable
private fun PlaceLocation(
    name: String,
    scaleFactor: Float,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp * scaleFactor),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_location_small),
            contentDescription = null,
            modifier = Modifier.size(20.dp * scaleFactor),
            tint = androidx.compose.ui.graphics.Color.Unspecified,
        )
        Text(
            text = name,
            color = SairoTheme.colors.textPrimary,
            style = SairoTextStyles.bodyLight14,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val MaxPlaceCount = 2
private val BookmarkVerticalAdjustment = 4.dp

@Preview(name = "Sairo Place Folder Card", showBackground = true, widthDp = 360, heightDp = 340)
@Composable
private fun SairoPlaceFolderCardPreview() {
    SairoTheme {
        Box(
            modifier = Modifier
                .width(300.dp)
                .background(SairoTheme.colors.backgroundCanvas)
                .padding(top = 16.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            SairoPlaceFolderCard(
                imagePainters = listOf(
                    painterResource(R.drawable.img_dummy_view),
                    painterResource(R.drawable.img_dummy_view),
                ),
                regionLabel = stringResource(R.string.sairo_place_folder_card_preview_region),
                description = stringResource(R.string.sairo_place_folder_card_preview_description),
                placeNames = listOf(
                    stringResource(R.string.sairo_place_folder_card_preview_first_place),
                    stringResource(R.string.sairo_place_folder_card_preview_second_place),
                ),
                saved = true,
                onClick = {},
                onBookmarkClick = {},
                imageContentDescription = stringResource(R.string.sairo_place_folder_card_preview_image),
            )
        }
    }
}
