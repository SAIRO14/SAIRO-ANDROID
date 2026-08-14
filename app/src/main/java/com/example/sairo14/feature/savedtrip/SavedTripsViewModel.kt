package com.example.sairo14.feature.savedtrip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SavedTrip
import com.example.sairo14.domain.usecase.DeleteSavedTripUseCase
import com.example.sairo14.domain.usecase.GetSavedTripsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 저장된 여행지 목록의 조회 상태를 관리하고 재시도를 처리한다.
 *
 * 목록 결과를 [SavedTripsUiState]로 변환하고, 북마크 해제에 성공한 카드를 목록에서 제거한다.
 * 기기 식별과 API 헤더 준비는 Repository 구현이 소유하며, 화면 이동은 화면 호출자가 소유한다.
 */
@HiltViewModel
class SavedTripsViewModel @Inject constructor(
    private val getSavedTrips: GetSavedTripsUseCase,
    private val deleteSavedTrip: DeleteSavedTripUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<SavedTripsUiState>(SavedTripsUiState.Loading)

    val uiState: StateFlow<SavedTripsUiState> = _uiState.asStateFlow()

    init {
        loadSavedTrips()
    }

    /** 오류 상태에서 저장 목록을 다시 조회한다. */
    fun retry() {
        if (_uiState.value is SavedTripsUiState.Error) {
            loadSavedTrips()
        }
    }

    /** 북마크 해제 요청이 성공하면 해당 카드를 목록에서 제거한다. */
    fun removeSavedTrip(savedTripId: String) {
        val content = _uiState.value as? SavedTripsUiState.Content ?: return

        if (savedTripId in content.removingSavedTripIds) return

        _uiState.update { state ->
            val currentContent = state as? SavedTripsUiState.Content ?: return@update state
            currentContent.copy(
                removingSavedTripIds = currentContent.removingSavedTripIds + savedTripId,
            )
        }

        viewModelScope.launch {
            when (deleteSavedTrip(savedTripId)) {
                is AppResult.Success -> removeSavedTripFromState(savedTripId)
                is AppResult.Failure -> clearRemovingState(savedTripId)
            }
        }
    }

    private fun loadSavedTrips() {
        viewModelScope.launch {
            _uiState.value = SavedTripsUiState.Loading

            _uiState.value = when (val result = getSavedTrips()) {
                is AppResult.Failure -> SavedTripsUiState.Error
            is AppResult.Success -> result.value.items.toUiState()
            }
        }
    }

    private fun removeSavedTripFromState(savedTripId: String) {
        _uiState.update { state ->
            val content = state as? SavedTripsUiState.Content ?: return@update state
            val updatedTrips = content.trips.filterNot { trip -> trip.savedTripId == savedTripId }

            if (updatedTrips.isEmpty()) {
                SavedTripsUiState.Empty
            } else {
                content.copy(
                    trips = updatedTrips,
                    removingSavedTripIds = content.removingSavedTripIds - savedTripId,
                )
            }
        }
    }

    private fun clearRemovingState(savedTripId: String) {
        _uiState.update { state ->
            val content = state as? SavedTripsUiState.Content ?: return@update state
            content.copy(
                removingSavedTripIds = content.removingSavedTripIds - savedTripId,
            )
        }
    }
}

private fun List<SavedTrip>.toUiState(): SavedTripsUiState =
    if (isEmpty()) {
        SavedTripsUiState.Empty
    } else {
        SavedTripsUiState.Content(
            trips = map { trip ->
                SavedTripUiModel(
                    savedTripId = trip.savedTripId,
                    courseId = trip.courseId,
                    regionName = trip.regionName,
                    regionArea = trip.regionArea,
                    imageUrl = trip.imageUrl,
                    reason = trip.reason,
                )
            },
        )
    }
