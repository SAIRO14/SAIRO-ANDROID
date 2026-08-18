package com.example.sairo14.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.HomeContent
import com.example.sairo14.domain.usecase.GetHomeContentUseCase
import com.example.sairo14.feature.bookmark.BookmarkChangeNotifier
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 홈 화면의 로딩·콘텐츠·오류 상태를 관리한다.
 *
 * [GetHomeContentUseCase]의 결과를 UI 모델로 변환해 [HomeUiState]로 노출한다. 저장 상태 변경 알림을
 * 받으면 기존 콘텐츠를 유지한 채 최신 목록을 다시 조회하며, 최초 조회 실패만 [HomeUiState.Error]로
 * 전환한다.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeContent: GetHomeContentUseCase,
    private val bookmarkChangeNotifier: BookmarkChangeNotifier,
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    private var homeContentJob: Job? = null
    private var homeContentRequestId = 0L

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeBookmarkChanges()
        loadHomeContent(showLoading = true)
    }

    /** 오류 상태에서 홈 콘텐츠를 다시 조회한다. */
    fun retry() {
        if (_uiState.value is HomeUiState.Error) {
            loadHomeContent(showLoading = true)
        }
    }

    private fun observeBookmarkChanges() {
        viewModelScope.launch {
            bookmarkChangeNotifier.changes.collect {
                loadHomeContent(showLoading = false)
            }
        }
    }

    private fun loadHomeContent(showLoading: Boolean) {
        val requestId = ++homeContentRequestId
        val keepsExistingContent = !showLoading && _uiState.value is HomeUiState.Content
        homeContentJob?.cancel()
        homeContentJob = viewModelScope.launch {
            if (showLoading) _uiState.value = HomeUiState.Loading

            when (val result = getHomeContent()) {
                is AppResult.Success -> {
                    if (requestId == homeContentRequestId) {
                        _uiState.value = result.value.toUiModel()
                    }
                }

                is AppResult.Failure -> {
                    if (requestId == homeContentRequestId && !keepsExistingContent) {
                        _uiState.value = HomeUiState.Error
                    }
                }
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
