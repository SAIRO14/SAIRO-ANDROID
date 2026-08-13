package com.example.sairo14.feature.onboarding

import com.example.sairo14.data.repository.InMemoryOnboardingAnalysisSessionStore
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.OnboardingAnalysisResult
import com.example.sairo14.domain.model.OnboardingRecommendation
import com.example.sairo14.domain.repository.OnboardingRepository
import com.example.sairo14.domain.usecase.UpdateOnboardingCompletionUseCase
import com.example.sairo14.feature.onboarding.result.OnboardingResultUiState
import com.example.sairo14.feature.onboarding.result.OnboardingResultViewModel
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
class OnboardingResultViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `세션의 추천 결과를 표시한다`() = runTest(dispatcher) {
        val store = InMemoryOnboardingAnalysisSessionStore()
        val recommendations = listOf(OnboardingRecommendation("id", "course", "제주도", "설명", emptyList(), emptyList()))
        store.save("session-1", OnboardingAnalysisResult(emptyList(), "", recommendations, emptyMap()))
        val viewModel = OnboardingResultViewModel(store, UpdateOnboardingCompletionUseCase(OnboardingRepo()))

        viewModel.load("session-1")
        advanceUntilIdle()

        assertEquals(recommendations, (viewModel.uiState.value as OnboardingResultUiState.Content).recommendations)
    }

    private class OnboardingRepo : OnboardingRepository {
        override suspend fun getHasCompletedOnboarding() = AppResult.Success(false)
        override suspend fun markOnboardingCompleted() = AppResult.Success(Unit)
        override suspend fun markOnboardingIncomplete() = AppResult.Success(Unit)
    }
}
