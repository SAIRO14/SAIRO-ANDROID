package com.example.sairo14.feature.sharedcourse

import androidx.compose.runtime.Immutable
import com.example.sairo14.domain.model.AppError

/** 읽기 전용 공유 코스 화면이 표시할 조회 상태를 나타낸다. */
sealed interface SharedCourseUiState {

    /** 공유 코스 스냅샷을 읽는 중이다. */
    data object Loading : SharedCourseUiState

    /** 일차 선택, 지도 핀, 장소 목록을 표시할 수 있는 상태다. */
    @Immutable
    data class Content(
        val course: SharedCourseUiModel,
        val selectedDayNumber: Int,
        val selectedPlaceId: String? = null,
        val cameraFocusRequestId: Long = 0L,
    ) : SharedCourseUiState {
        val selectedDay: SharedCourseDayUiModel?
            get() = course.days.firstOrNull { it.dayNumber == selectedDayNumber }

        val selectedPlace: SharedCoursePlaceUiModel?
            get() = selectedDay?.places?.firstOrNull { it.placeId == selectedPlaceId }
    }

    /** 공유 코스를 읽지 못해 오류 안내와 재시도가 필요한 상태다. */
    data class Error(
        val error: AppError,
    ) : SharedCourseUiState
}

/** 공유 링크 수신 화면에만 필요한 읽기 전용 코스 정보다. */
@Immutable
data class SharedCourseUiModel(
    val shareId: String,
    val regionName: String,
    val days: List<SharedCourseDayUiModel>,
)

/** 공유 코스에서 선택 가능한 한 일차의 UI 정보다. */
@Immutable
data class SharedCourseDayUiModel(
    val dayNumber: Int,
    val places: List<SharedCoursePlaceUiModel>,
)

/** 공유 코스의 지도 핀과 장소 목록에 함께 전달할 UI 전용 장소 정보다. */
@Immutable
data class SharedCoursePlaceUiModel(
    val placeId: String,
    val name: String,
    val imageUrl: String?,
    val tags: List<String>,
    val latitude: Double?,
    val longitude: Double?,
)
