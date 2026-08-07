package com.example.sairo14.feature.traveldetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sairo14.R
import com.example.sairo14.core.designsystem.theme.SairoTheme

/**
 * 여행 코스 장소를 순서 핀과 연결선으로 묶어 표시한다.
 *
 * 장소의 표시 데이터와 클릭 상태는 [itemContent]를 제공하는 화면이 소유하며, 이 컴포넌트는
 * 항목 수에 맞춰 순서선만 배치한다.
 * @param placeCount 코스에 표시할 장소 수
 * @param modifier 타임라인 전체에 적용할 Modifier
 * @param itemContent 각 순서 위치에 표시할 장소 콘텐츠
 */
@Composable
internal fun TravelCourseTimeline(
    placeCount: Int,
    modifier: Modifier = Modifier,
    itemContent: @Composable (index: Int) -> Unit,
) {
    if (placeCount == 0) return

    val lineColor = SairoTheme.colors.borderDefault

    Column(modifier = modifier.fillMaxWidth()) {
        repeat(placeCount) { index ->
            val isLastItem = index == placeCount - 1

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        if (!isLastItem) {
                            val markerCenterX = TimelineMarkerSize.toPx() / 2f
                            val lineEndY = size.height
                            val dashLength = TimelineDashLength.toPx()
                            val dashGap = TimelineDashGap.toPx()
                            var dashStartY = TimelineMarkerSize.toPx()

                            while (dashStartY < lineEndY) {
                                val dashEndY = (dashStartY + dashLength).coerceAtMost(lineEndY)
                                drawLine(
                                    color = lineColor,
                                    start = Offset(x = markerCenterX, y = dashStartY),
                                    end = Offset(x = markerCenterX, y = dashEndY),
                                    strokeWidth = TimelineLineWidth.toPx(),
                                )
                                dashStartY += dashLength + dashGap
                            }
                        }
                    },
            ) {
                Box(
                    modifier = Modifier.width(TimelineMarkerSize),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    androidx.compose.material3.Icon(
                        painter = painterResource(R.drawable.ic_location_small),
                        contentDescription = null,
                        modifier = Modifier.size(TimelineMarkerSize),
                        tint = androidx.compose.ui.graphics.Color.Unspecified,
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = TimelineContentGap),
                ) {
                    itemContent(index)
                    if (!isLastItem) {
                        Spacer(modifier = Modifier.height(TimelineItemGap))
                    }
                }
            }
        }
    }
}

private val TimelineMarkerSize = 20.dp
private val TimelineLineWidth = 1.dp
private val TimelineDashLength = 4.dp
private val TimelineDashGap = 4.dp
private val TimelineContentGap = 8.dp
private val TimelineItemGap = 24.dp
