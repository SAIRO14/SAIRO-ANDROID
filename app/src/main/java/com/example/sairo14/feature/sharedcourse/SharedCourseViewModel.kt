package com.example.sairo14.feature.sharedcourse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SharedCourse
import com.example.sairo14.domain.usecase.GetSharedCourseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** 공유 링크로 열린 읽기 전용 코스 화면의 조회와 선택 상태를 관리한다.
 *
 * 코스 조회 결과와 일차·장소 선택은 [SharedCourseUiState]로 노출한다. 저장·공유 같은 소유자 전용
 * 행동은 이 ViewModel이 제공하지 않으며, 같은 링크의 중복 로드는 피하고 최신 요청의 결과만 반영한다.
 */
@HiltViewModel
class SharedCourseViewModel @Inject constructor(
    private val getSharedCourse: GetSharedCourseUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SharedCourseUiState>(SharedCourseUiState.Loading)

    val uiState: StateFlow<SharedCourseUiState> = _uiState.asStateFlow()

    private var shareId: String? = null
    private var loadJob: Job? = null
    private var loadRequestId = 0L

    /** Route가 전달한 공유 스냅샷을 조회하고 최신 요청만 표시한다. */
    fun load(shareId: String, force: Boolean = false) {
        if (!force && this.shareId == shareId) return

        this.shareId = shareId
        val requestId = ++loadRequestId
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = SharedCourseUiState.Loading

            val nextState = when (val result = getSharedCourse(shareId)) {
                is AppResult.Success -> result.value.toUiState()
                is AppResult.Failure -> SharedCourseUiState.Error(result.error)
            }
            if (requestId == loadRequestId) {
                _uiState.value = nextState
            }
        }
    }

    /** 실패한 마지막 공유 코스 조회를 다시 시도한다. */
    fun retry() {
        shareId?.let { load(shareId = it, force = true) }
    }

    /** 지도와 목록에 표시할 일차를 변경한다. */
    fun selectDay(dayNumber: Int) {
        _uiState.update { state ->
            val content = state as? SharedCourseUiState.Content ?: return@update state
            val selectedDay = content.course.days.firstOrNull { it.dayNumber == dayNumber }
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
            val content = state as? SharedCourseUiState.Content ?: return@update state
            if (content.selectedDay?.places?.any { it.placeId == placeId } != true) return@update content

            content.copy(
                selectedPlaceId = placeId,
                cameraFocusRequestId = content.cameraFocusRequestId + 1,
            )
        }
    }
}

private fun SharedCourse.toUiState(): SharedCourseUiState.Content = SharedCourseUiState.Content(
    course = SharedCourseUiModel(
        shareId = shareId,
        regionName = regionName,
        days = days.map { day ->
            SharedCourseDayUiModel(
                dayNumber = day.dayNumber,
                places = day.places.map { place ->
                    SharedCoursePlaceUiModel(
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
