package com.example.sairo14.feature.onboarding

import com.example.sairo14.core.navigation.OnboardingAnimationPhoto
import com.example.sairo14.data.repository.InMemoryOnboardingAnalysisSessionStore
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.OnboardingAnalysisResult
import com.example.sairo14.domain.repository.OnboardingRecommendationRepository
import com.example.sairo14.domain.usecase.AnalyzeAndStoreOnboardingTasteUseCase
import com.example.sairo14.feature.onboarding.loading.OnboardingLoadingCardCount
import com.example.sairo14.feature.onboarding.loading.OnboardingLoadingUiState
import com.example.sairo14.feature.onboarding.loading.OnboardingLoadingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingLoadingViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `분석 결과를 세션에 저장하고 API 태그를 노출한다`() = runTest(dispatcher) {
        val store = InMemoryOnboardingAnalysisSessionStore()
        val result = OnboardingAnalysisResult(listOf("고요한"), "요약", emptyList(), emptyMap())
        val viewModel = OnboardingLoadingViewModel(
            AnalyzeAndStoreOnboardingTasteUseCase(Repository(AppResult.Success(result)), store),
        )
        val photos = List(OnboardingLoadingCardCount) { index ->
            OnboardingAnimationPhoto("photo-$index", "https://example.com/$index.jpg")
        }

        viewModel.load("session-1", photos.map(OnboardingAnimationPhoto::id), photos)
        advanceUntilIdle()

        val content = viewModel.uiState.value as OnboardingLoadingUiState.Content
        assertEquals(listOf("고요한"), content.moodTags)
        assertEquals(result, store.getResult("session-1"))
    }

    private class Repository(private val result: AppResult<OnboardingAnalysisResult>) : OnboardingRecommendationRepository {
        override suspend fun analyzeTaste(selectedPhotoIds: List<String>) = result
    }
}
