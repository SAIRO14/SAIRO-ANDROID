package com.example.sairo14.feature.traveldetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.ClosedDaysSummary
import com.example.sairo14.domain.model.ContactSummary
import com.example.sairo14.domain.model.Course
import com.example.sairo14.domain.model.CoursePlace
import com.example.sairo14.domain.model.OperatingHoursSummary
import com.example.sairo14.domain.model.ParkingSummary
import com.example.sairo14.domain.model.PlaceInfoSummary
import com.example.sairo14.domain.usecase.DeleteSavedTripUseCase
import com.example.sairo14.domain.usecase.GetCourseDetailUseCase
import com.example.sairo14.domain.usecase.SaveTripUseCase
import com.example.sairo14.domain.usecase.SummarizePlaceInfoUseCase
import com.example.sairo14.feature.bookmark.BookmarkUiState
import com.example.sairo14.feature.bookmark.BookmarkChange
import com.example.sairo14.feature.bookmark.BookmarkChangeNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 여행 상세 화면의 코스·일차·저장 표시 상태를 관리한다.
 *
 * 코스 조회 결과는 [TravelDetailUiState]의 UI 모델로 변환하며, 일차·장소 선택에 따라 지도와
 * 타임라인이 같은 장소 목록을 사용하도록 한다. 북마크는 서버 성공 후에만 표시 상태를 변경하며, 공유와
 * 화면 이동은 호출자가 소유한다.
 */
@HiltViewModel
class TravelDetailViewModel @Inject constructor(
    private val getCourseDetail: GetCourseDetailUseCase,
    private val summarizePlaceInfo: SummarizePlaceInfoUseCase,
    private val saveTripUseCase: SaveTripUseCase,
    private val deleteSavedTripUseCase: DeleteSavedTripUseCase,
    private val bookmarkChangeNotifier: BookmarkChangeNotifier,
) : ViewModel() {
    private val _uiState = MutableStateFlow<TravelDetailUiState>(TravelDetailUiState.Loading)

    val uiState: StateFlow<TravelDetailUiState> = _uiState.asStateFlow()

    private var courseId: String? = null
    private var onboardingSessionId: String? = null
    private var initialSaved: Boolean? = null
    private var initialSavedTripId: String? = null
    private var loadJob: Job? = null
    private var loadRequestId = 0L

    /** Route가 전달한 코스와 북마크 초기 상태를 조회하고 최신 요청만 표시한다. */
    fun load(
        courseId: String,
        onboardingSessionId: String? = null,
        initialSaved: Boolean? = null,
        savedTripId: String? = null,
        force: Boolean = false,
    ) {
        if (!force &&
            this.courseId == courseId &&
            this.onboardingSessionId == onboardingSessionId &&
            this.initialSaved == initialSaved &&
            this.initialSavedTripId == savedTripId
        ) {
            return
        }

        this.courseId = courseId
        this.onboardingSessionId = onboardingSessionId
        this.initialSaved = initialSaved
        this.initialSavedTripId = savedTripId
        val requestId = ++loadRequestId
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = TravelDetailUiState.Loading

            val nextState = when (val result = getCourseDetail(courseId, onboardingSessionId)) {
                is AppResult.Success -> result.value.toUiState(
                    initialSaved = initialSaved,
                    savedTripId = savedTripId,
                    summarizePlaceInfo = summarizePlaceInfo,
                )
                is AppResult.Failure -> TravelDetailUiState.Error(result.error)
            }
            if (requestId == loadRequestId) {
                _uiState.value = nextState
            }
        }
    }

    /** 실패한 마지막 코스 조회를 다시 시도한다. */
    fun retry() {
        courseId?.let { currentCourseId ->
            load(
                courseId = currentCourseId,
                onboardingSessionId = onboardingSessionId,
                initialSaved = initialSaved,
                savedTripId = initialSavedTripId,
                force = true,
            )
        }
    }

    /** 지도와 목록에 표시할 일차를 변경한다. */
    fun selectDay(dayNumber: Int) {
        _uiState.update { state ->
            val content = state as? TravelDetailUiState.Content ?: return@update state

            val selectedDay = content.course.days.firstOrNull { day -> day.dayNumber == dayNumber }
                ?: return@update content
            content.copy(
                selectedDayNumber = dayNumber,
                selectedPlaceId = selectedDay.places.firstOrNull()?.placeId,
                cameraFocusRequestId = content.cameraFocusRequestId + 1,
            )
        }
    }

    /** 선택한 일차에 포함된 장소를 지도 카메라의 중심으로 지정한다. */
    fun selectPlace(placeId: String) {
        _uiState.update { state ->
            val content = state as? TravelDetailUiState.Content ?: return@update state

            if (content.selectedDay?.places?.any { place -> place.placeId == placeId } == true) {
                content.copy(
                    selectedPlaceId = placeId,
                    cameraFocusRequestId = content.cameraFocusRequestId + 1,
                )
            } else {
                content
            }
        }
    }

    /** 현재 코스의 북마크를 서버에 저장하거나 저장 해제한다.
     *
     * 요청 중에는 추가 클릭을 무시한다. 체크 상태에 [BookmarkUiState.savedTripId]가 없으면 삭제 API를
     * 호출하지 않고 현재 상태를 유지한다.
     */
    fun onBookmarkClick() {
        val content = _uiState.value as? TravelDetailUiState.Content ?: return
        val bookmark = content.bookmark
        if (bookmark.isRequesting) return

        if (bookmark.isSaved) {
            bookmark.savedTripId?.let { savedTripId ->
                deleteSavedTrip(content.course.courseId, savedTripId)
            }
        } else {
            saveTrip(content.course.courseId)
        }
    }

    private fun saveTrip(courseId: String) {
        setBookmarkRequesting(isRequesting = true)
        viewModelScope.launch {
            when (val result = saveTripUseCase(courseId)) {
                is AppResult.Success -> {
                    updateBookmark {
                        it.copy(
                            isSaved = true,
                            savedTripId = result.value.savedTripId,
                            isRequesting = false,
                        )
                    }
                    bookmarkChangeNotifier.notify(
                        BookmarkChange(
                            courseId = courseId,
                            isSaved = true,
                            savedTripId = result.value.savedTripId,
                        ),
                    )
                }

                is AppResult.Failure -> setBookmarkRequesting(isRequesting = false)
            }
        }
    }

    private fun deleteSavedTrip(courseId: String, savedTripId: String) {
        setBookmarkRequesting(isRequesting = true)
        viewModelScope.launch {
            when (deleteSavedTripUseCase(savedTripId)) {
                is AppResult.Success -> {
                    updateBookmark {
                        it.copy(
                            isSaved = false,
                            savedTripId = null,
                            isRequesting = false,
                        )
                    }
                    bookmarkChangeNotifier.notify(
                        BookmarkChange(
                            courseId = courseId,
                            isSaved = false,
                            savedTripId = null,
                        ),
                    )
                }

                is AppResult.Failure -> setBookmarkRequesting(isRequesting = false)
            }
        }
    }

    private fun setBookmarkRequesting(isRequesting: Boolean) {
        updateBookmark { bookmark -> bookmark.copy(isRequesting = isRequesting) }
    }

    private inline fun updateBookmark(
        transform: (BookmarkUiState) -> BookmarkUiState,
    ) {
        _uiState.update { state ->
            val content = state as? TravelDetailUiState.Content ?: return@update state
            content.copy(bookmark = transform(content.bookmark))
        }
    }
}

private fun Course.toUiState(
    initialSaved: Boolean?,
    savedTripId: String?,
    summarizePlaceInfo: SummarizePlaceInfoUseCase,
): TravelDetailUiState {
    val saved = initialSaved ?: this.isSaved

    return TravelDetailUiState.Content(
        course = TravelDetailCourseUiModel(
            courseId = courseId,
            regionName = regionName,
            days = days.map { day ->
                TravelDetailDayUiModel(
                    dayNumber = day.dayNumber,
                    places = day.places.map { place ->
                        TravelDetailPlaceUiModel(
                            placeId = place.placeId,
                            name = place.name,
                            imageUrl = place.imageUrl,
                            tags = place.toDisplayTags(summarizePlaceInfo),
                            latitude = place.coordinate?.latitude,
                            longitude = place.coordinate?.longitude,
                        )
                    },
                )
            },
        ),
        selectedDayNumber = days.firstOrNull()?.dayNumber ?: 1,
        selectedPlaceId = days.firstOrNull()?.places?.firstOrNull()?.placeId,
        bookmark = BookmarkUiState(
            isSaved = saved,
            savedTripId = savedTripId?.takeIf { saved },
        ),
    )
}

private fun CoursePlace.toDisplayTags(
    summarizePlaceInfo: SummarizePlaceInfoUseCase,
): List<TravelDetailPlaceTagUiModel> {
    val hasStructuredPlaceInfo = operatingHours != null ||
        closedDays != null ||
        parking != null ||
        contact != null
    if (!hasStructuredPlaceInfo) return tags.map(TravelDetailPlaceTagUiModel::Text)

    return summarizePlaceInfo(this).toUiTags()
}

private fun PlaceInfoSummary.toUiTags(): List<TravelDetailPlaceTagUiModel> {
    val hasPlaceInfoTag = operatingHours != null || closedDays.isNotEmpty() || parking != null

    return buildList {
        operatingHours?.toUiTags()?.forEach(::add)
        closedDays.map(ClosedDaysSummary::toUiTag).forEach(::add)
        parking?.toUiTag()?.let(::add)
        contact?.toUiTags(showPhoneInquiry = !hasPlaceInfoTag)?.forEach(::add)
    }.distinct()
}

private fun OperatingHoursSummary.toUiTags(): List<TravelDetailPlaceTagUiModel> = when (this) {
    OperatingHoursSummary.AlwaysOpen -> listOf(TravelDetailPlaceTagUiModel.AlwaysOpen)
    OperatingHoursSummary.PhoneInquiry -> listOf(TravelDetailPlaceTagUiModel.PhoneInquiry)
    is OperatingHoursSummary.TimeRange -> listOf(TravelDetailPlaceTagUiModel.Text(value))
    is OperatingHoursSummary.Periods -> values.map { period ->
        TravelDetailPlaceTagUiModel.PeriodHours(label = period.label, hours = period.hours)
    }
    is OperatingHoursSummary.WeekdayWeekend -> listOfNotNull(
        weekday?.let(TravelDetailPlaceTagUiModel::WeekdayHours),
        weekend?.let(TravelDetailPlaceTagUiModel::WeekendHours),
    )
}

private fun ClosedDaysSummary.toUiTag(): TravelDetailPlaceTagUiModel = when (this) {
    ClosedDaysSummary.OpenAllYear -> TravelDetailPlaceTagUiModel.OpenAllYear
    ClosedDaysSummary.PublicHoliday -> TravelDetailPlaceTagUiModel.PublicHolidayClosed
    ClosedDaysSummary.BadWeather -> TravelDetailPlaceTagUiModel.BadWeatherClosed
    ClosedDaysSummary.PhoneInquiry -> TravelDetailPlaceTagUiModel.PhoneInquiry
    is ClosedDaysSummary.Weekly -> TravelDetailPlaceTagUiModel.WeeklyClosed(dayOfWeek)
    is ClosedDaysSummary.Literal -> TravelDetailPlaceTagUiModel.Text(value)
}

private fun ParkingSummary.toUiTag(): TravelDetailPlaceTagUiModel = when (this) {
    ParkingSummary.Available -> TravelDetailPlaceTagUiModel.ParkingAvailable
    ParkingSummary.Unavailable -> TravelDetailPlaceTagUiModel.ParkingUnavailable
    ParkingSummary.PhoneInquiry -> TravelDetailPlaceTagUiModel.PhoneInquiry
}

private fun ContactSummary.toUiTags(
    showPhoneInquiry: Boolean,
): List<TravelDetailPlaceTagUiModel> = when (this) {
    is ContactSummary.PhoneNumber -> buildList {
        if (showPhoneInquiry) add(TravelDetailPlaceTagUiModel.PhoneInquiry)
        add(TravelDetailPlaceTagUiModel.Text(value))
    }
    ContactSummary.PhoneInquiry -> listOf(TravelDetailPlaceTagUiModel.PhoneInquiry)
}
