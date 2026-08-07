package com.example.sairo14.core.map

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 카카오 지도에 표시할 순서 번호와 좌표를 가진 마커를 표현한다.
 *
 * [id]는 현재 지도 안에서 고유해야 하며, [order]는 지도 핀과 장소 목록에 공통으로 표시할 1부터
 * 시작하는 방문 순서다.
 * @param id 마커를 식별하는 고유 ID
 * @param order 사용자에게 표시할 1부터 시작하는 방문 순서
 * @param latitude WGS84 북위 좌표
 * @param longitude WGS84 동경 좌표
 */
@Immutable
data class SairoMapMarker(
    val id: String,
    val order: Int,
    val latitude: Double,
    val longitude: Double,
)

/** 카카오 지도의 카메라가 중심으로 둘 좌표를 표현한다.
 *
 * 화면은 선택된 장소의 UI 모델을 이 값으로 변환하고, 지도 어댑터는 변경된 값에만 카메라 이동을
 * 적용한다.
 * @param latitude WGS84 북위 좌표
 * @param longitude WGS84 동경 좌표
 */
@Immutable
data class SairoMapCameraTarget(
    val latitude: Double,
    val longitude: Double,
)

/** 지도 위에 겹치는 UI를 피해 카메라 중심과 지도 요소를 배치할 뷰포트 여백을 표현한다.
 *
 * 여백은 호출자가 헤더와 드래그 시트의 실제 측정값으로 계산한다. 카카오 지도 SDK가 요구하는 px
 * 변환은 [SairoKakaoMap]이 담당한다.
 * @param left 지도 왼쪽에 확보할 여백
 * @param top 지도 위쪽에 확보할 여백
 * @param right 지도 오른쪽에 확보할 여백
 * @param bottom 지도 아래쪽에 확보할 여백
 */
@Immutable
data class SairoMapViewportPadding(
    val left: Dp = 0.dp,
    val top: Dp = 0.dp,
    val right: Dp = 0.dp,
    val bottom: Dp = 0.dp,
)
