package com.example.sairo14.feature.traveldetail

import androidx.compose.runtime.Immutable
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.feature.bookmark.BookmarkUiState
import java.time.DayOfWeek

/** 여행 상세 화면이 표시할 코스 조회 상태를 나타낸다. */
sealed interface TravelDetailUiState {

    /** 코스 상세 정보를 읽는 중이다. */
    data object Loading : TravelDetailUiState

    /** 일차 선택, 지도 핀, 장소 목록을 표시할 수 있는 상태다. */
    @Immutable
    data class Content(
        val course: TravelDetailCourseUiModel,
        val selectedDayNumber: Int,
        val selectedPlaceId: String? = null,
        val cameraFocusRequestId: Long = 0L,
        val bookmark: BookmarkUiState = BookmarkUiState(),
        val isShareRequesting: Boolean = false,
    ) : TravelDetailUiState {
        val selectedDay: TravelDetailDayUiModel?
            get() = course.days.firstOrNull { day -> day.dayNumber == selectedDayNumber }

        val selectedPlace: TravelDetailPlaceUiModel?
            get() = selectedDay?.places?.firstOrNull { place -> place.placeId == selectedPlaceId }
    }

    /** 코스 상세 정보를 읽지 못해 오류 종류에 맞는 안내가 필요한 상태다. */
    data class Error(
        val error: AppError,
    ) : TravelDetailUiState
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
    val tags: List<TravelDetailPlaceTagUiModel>,
    val latitude: Double?,
    val longitude: Double?,
)

/** 상세 화면의 장소 태그를 리소스 문구와 동적 값으로 구분해 표현한다. */
sealed interface TravelDetailPlaceTagUiModel {
    data object AlwaysOpen : TravelDetailPlaceTagUiModel

    data object OpenAllYear : TravelDetailPlaceTagUiModel

    data object PublicHolidayClosed : TravelDetailPlaceTagUiModel

    data object BadWeatherClosed : TravelDetailPlaceTagUiModel

    data object ParkingAvailable : TravelDetailPlaceTagUiModel

    data object ParkingUnavailable : TravelDetailPlaceTagUiModel

    data object PhoneInquiry : TravelDetailPlaceTagUiModel

    data class Text(val value: String) : TravelDetailPlaceTagUiModel

    data class PeriodHours(
        val label: String,
        val hours: String,
    ) : TravelDetailPlaceTagUiModel

    data class WeekdayHours(val value: String) : TravelDetailPlaceTagUiModel

    data class WeekendHours(val value: String) : TravelDetailPlaceTagUiModel

    data class WeeklyClosed(val dayOfWeek: DayOfWeek) : TravelDetailPlaceTagUiModel
}
