package com.example.sairo14.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.HomeContent
import com.example.sairo14.domain.usecase.GetHomeContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 홈 화면의 로딩·콘텐츠·오류 상태를 관리한다.
 *
 * [GetHomeContentUseCase]의 결과를 UI 모델로 변환해 [HomeUiState]로 노출한다. 데이터 출처는
 * Repository가 담당하며, 실패 시 [HomeUiState.Error]로 전환하고 재시도 시 다시 조회한다.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeContent: GetHomeContentUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    private var loadJob: Job? = null

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeContent()
    }

    /** 오류 상태에서 홈 콘텐츠를 다시 조회한다. */
    fun retry() {
        if (_uiState.value is HomeUiState.Error) {
            loadHomeContent()
        }
    }

    private fun loadHomeContent() {
        if (loadJob?.isActive == true) return

        _uiState.value = HomeUiState.Loading
        loadJob = viewModelScope.launch {
            _uiState.value = when (val result = getHomeContent()) {
                is AppResult.Success -> result.value.toUiModel()
                is AppResult.Failure -> HomeUiState.Error(result.error)
            }
        }
    }
}

private fun HomeContent.toUiModel(): HomeUiState.Content =
    HomeUiState.Content(
        discoveryImages = HomeDiscoveryImagesUiModel(
            backImageUrl = discoveryImages.backImageUrl,
            frontImageUrl = discoveryImages.frontImageUrl,
        ),
        savedTrips = savedTrips.map { savedTrip ->
            HomeSavedTripUiModel(
                savedTripId = savedTrip.savedTripId,
                courseId = savedTrip.courseId,
                regionName = savedTrip.regionName,
                thumbnailImageUrl = savedTrip.thumbnailImageUrl,
            )
        },
    )
