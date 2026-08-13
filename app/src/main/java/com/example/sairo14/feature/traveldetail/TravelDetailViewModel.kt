package com.example.sairo14.feature.traveldetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.Course
import com.example.sairo14.domain.usecase.GetCourseDetailUseCase
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
 * 타임라인이 같은 장소 목록을 사용하도록 한다. 공유와 화면 이동은 호출자가 소유한다.
 */
@HiltViewModel
class TravelDetailViewModel @Inject constructor(
    private val getCourseDetail: GetCourseDetailUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<TravelDetailUiState>(TravelDetailUiState.Loading)

    val uiState: StateFlow<TravelDetailUiState> = _uiState.asStateFlow()

    private var courseId: String? = null
    private var onboardingSessionId: String? = null
    private var loadJob: Job? = null
    private var loadRequestId = 0L

    /** Route가 전달한 코스 ID와 온보딩 세션의 상세 정보를 조회하고 최신 요청만 표시한다. */
    fun load(
        courseId: String,
        onboardingSessionId: String? = null,
        force: Boolean = false,
    ) {
        if (!force && this.courseId == courseId && this.onboardingSessionId == onboardingSessionId) return

        this.courseId = courseId
        this.onboardingSessionId = onboardingSessionId
        val requestId = ++loadRequestId
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = TravelDetailUiState.Loading

            val nextState = when (val result = getCourseDetail(courseId, onboardingSessionId)) {
                is AppResult.Success -> result.value.toUiState()
                is AppResult.Failure -> TravelDetailUiState.Error
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

    /** 서버 저장 기능이 연결되기 전까지 현재 화면의 저장 표시만 전환한다. */
    fun toggleSaved() {
        _uiState.update { state ->
            val content = state as? TravelDetailUiState.Content ?: return@update state
            content.copy(isSaved = !content.isSaved)
        }
    }
}

private fun Course.toUiState(): TravelDetailUiState =
    TravelDetailUiState.Content(
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
                            tags = place.tags,
                            latitude = place.coordinate?.latitude,
                            longitude = place.coordinate?.longitude,
                        )
                    },
                )
            },
        ),
        selectedDayNumber = days.firstOrNull()?.dayNumber ?: 1,
        selectedPlaceId = days.firstOrNull()?.places?.firstOrNull()?.placeId,
    )
