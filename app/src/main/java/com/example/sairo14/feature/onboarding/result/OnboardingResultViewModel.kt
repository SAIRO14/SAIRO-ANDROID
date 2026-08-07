package com.example.sairo14.feature.onboarding.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.usecase.GetOnboardingRecommendationsUseCase
import com.example.sairo14.domain.usecase.UpdateOnboardingCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 온보딩 추천 결과를 조회하고 카드의 화면 상태를 관리한다.
 *
 * 선택 사진 ID로 추천을 조회한 뒤 결과 수에 따라 온보딩 완료 상태를 저장하거나 해제해
 * [OnboardingResultUiState]로 노출한다. 북마크는 저장 여행 기능이 연결되기 전까지 현재 화면의
 * 표시 상태만 변경한다.
 */
@HiltViewModel
class OnboardingResultViewModel @Inject constructor(
    private val getOnboardingRecommendations: GetOnboardingRecommendationsUseCase,
    private val updateOnboardingCompletion: UpdateOnboardingCompletionUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<OnboardingResultUiState>(OnboardingResultUiState.Loading)

    val uiState: StateFlow<OnboardingResultUiState> = _uiState.asStateFlow()

    private var selectedPhotoIds: List<String>? = null

    /** 선택 사진으로 추천을 한 번 조회하고 결과 수에 맞게 온보딩 완료 상태를 갱신한다. */
    fun load(selectedPhotoIds: List<String>, force: Boolean = false) {
        if (!force && this.selectedPhotoIds == selectedPhotoIds) return

        this.selectedPhotoIds = selectedPhotoIds
        viewModelScope.launch {
            _uiState.value = OnboardingResultUiState.Loading

            when (val result = getOnboardingRecommendations(selectedPhotoIds)) {
                is AppResult.Failure -> _uiState.value = OnboardingResultUiState.Error
                is AppResult.Success -> {
                    _uiState.value = when (updateOnboardingCompletion(result.value)) {
                        is AppResult.Success -> OnboardingResultUiState.Content(result.value)
                        is AppResult.Failure -> OnboardingResultUiState.Error
                    }
                }
            }
        }
    }

    /** 마지막 조회에 실패했을 때 같은 선택 사진으로 결과를 다시 요청한다. */
    fun retry() {
        selectedPhotoIds?.let { photoIds -> load(photoIds, force = true) }
    }

    /** 현재 화면에서 추천 카드의 북마크 표시만 전환한다. */
    fun toggleSaved(recommendationId: String) {
        _uiState.update { state ->
            val content = state as? OnboardingResultUiState.Content ?: return@update state
            content.copy(
                recommendations = content.recommendations.map { recommendation ->
                    if (recommendation.id == recommendationId) {
                        recommendation.copy(isSaved = !recommendation.isSaved)
                    } else {
                        recommendation
                    }
                },
            )
        }
    }
}
