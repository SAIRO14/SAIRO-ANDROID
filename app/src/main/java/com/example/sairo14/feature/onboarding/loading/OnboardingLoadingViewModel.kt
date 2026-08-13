package com.example.sairo14.feature.onboarding.loading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sairo14.core.navigation.OnboardingAnimationPhoto
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.usecase.AnalyzeAndStoreOnboardingTasteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** 온보딩 로딩 화면의 카드 정보와 최신 취향 분석 요청 상태를 관리한다.
 *
 * 카드 정보와 분석 결과를 [OnboardingLoadingUiState]로 노출한다. 재시도 또는 새 요청이 시작되면
 * 이전 작업을 취소하고, 취소를 따르지 않은 이전 응답도 화면 상태에 반영하지 않는다.
 */
@HiltViewModel
class OnboardingLoadingViewModel @Inject constructor(
    private val analyzeAndStoreOnboardingTaste: AnalyzeAndStoreOnboardingTasteUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<OnboardingLoadingUiState>(OnboardingLoadingUiState.Loading)
    val uiState: StateFlow<OnboardingLoadingUiState> = _uiState.asStateFlow()

    private var request: LoadingRequest? = null
    private var analysisJob: Job? = null
    private var analysisGeneration = 0L

    /** 카드 애니메이션을 준비하고 같은 시점에 서버 취향 분석을 시작한다. */
    fun load(
        searchSessionId: String,
        selectedPhotoIds: List<String>,
        animationPhotos: List<OnboardingAnimationPhoto>,
    ) {
        val nextRequest = LoadingRequest(searchSessionId, selectedPhotoIds, animationPhotos)
        if (request == nextRequest) return
        request = nextRequest
        val content = animationPhotos.toLoadingContent() ?: run {
            cancelCurrentAnalysis()
            _uiState.value = OnboardingLoadingUiState.Error
            return
        }
        _uiState.value = content
        analyze(nextRequest, content.photos)
    }

    /** 마지막 분석 요청을 카드 애니메이션을 다시 시작하지 않고 재시도한다. */
    fun retry() {
        val currentRequest = request ?: return
        val content = currentRequest.animationPhotos.toLoadingContent() ?: return
        _uiState.value = content
        analyze(currentRequest, content.photos)
    }

    private fun analyze(request: LoadingRequest, photos: List<OnboardingLoadingPhotoUiModel>) {
        cancelCurrentAnalysis()
        val generation = analysisGeneration

        analysisJob = viewModelScope.launch {
            when (val result = analyzeAndStoreOnboardingTaste(
                searchSessionId = request.searchSessionId,
                selectedPhotoIds = request.selectedPhotoIds,
            )) {
                is AppResult.Failure -> {
                    if (generation != analysisGeneration) return@launch
                    _uiState.value = OnboardingLoadingUiState.AnalysisError
                }

                is AppResult.Success -> {
                    if (generation != analysisGeneration) return@launch
                    _uiState.value = OnboardingLoadingUiState.Content(
                        photos = photos,
                        moodTags = result.value.moodTags,
                    )
                }
            }
        }
    }

    private fun cancelCurrentAnalysis() {
        analysisGeneration += 1
        analysisJob?.cancel()
        analysisJob = null
    }
}

private data class LoadingRequest(
    val searchSessionId: String,
    val selectedPhotoIds: List<String>,
    val animationPhotos: List<OnboardingAnimationPhoto>,
)

private fun List<OnboardingAnimationPhoto>.toLoadingContent(): OnboardingLoadingUiState.Content? {
    val uniquePhotos = distinctBy(OnboardingAnimationPhoto::id)
    if (uniquePhotos.size != OnboardingLoadingCardCount) return null
    return OnboardingLoadingUiState.Content(photos = uniquePhotos.map(OnboardingAnimationPhoto::toLoadingUiModel))
}

private fun OnboardingAnimationPhoto.toLoadingUiModel() = OnboardingLoadingPhotoUiModel(
    id = id,
    imageUrl = imageUrl,
    contentDescription = null,
)
