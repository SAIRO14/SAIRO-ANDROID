package com.example.sairo14.feature.traveldetail

import androidx.compose.runtime.Immutable

/** 여행 상세 화면이 표시할 코스 조회 상태를 나타낸다. */
sealed interface TravelDetailUiState {

    /** 코스 상세 정보를 읽는 중이다. */
    data object Loading : TravelDetailUiState

    /** 일차 선택, 지도 핀, 장소 목록을 표시할 수 있는 상태다. */
    @Immutable
    data class Content(
        val course: TravelDetailCourseUiModel,
        val selectedDayNumber: Int,
        val isSaved: Boolean = false,
    ) : TravelDetailUiState {
        val selectedDay: TravelDetailDayUiModel?
            get() = course.days.firstOrNull { day -> day.dayNumber == selectedDayNumber }
    }

    /** 코스 상세 정보를 읽지 못해 재시도가 필요한 상태다. */
    data object Error : TravelDetailUiState
}

/** 여행 상세 화면이 domain 모델에 의존하지 않고 표시할 코스 정보다. */
@Immutable
data class TravelDetailCourseUiModel(
    val courseId: String,
    val regionName: String,
    val days: List<TravelDetailDayUiModel>,
)

/** 여행 코스에서 선택 가능한 한 일차의 UI 정보다. */
@Immutable
data class TravelDetailDayUiModel(
    val dayNumber: Int,
    val places: List<TravelDetailPlaceUiModel>,
)

/** 장소 타임라인과 지도 핀에 함께 전달할 UI 전용 장소 정보다. */
@Immutable
data class TravelDetailPlaceUiModel(
    val placeId: String,
    val name: String,
    val imageUrl: String?,
    val tags: List<String>,
    val latitude: Double,
    val longitude: Double,
)
