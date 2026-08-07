package com.example.sairo14.feature.onboarding

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.OnboardingRecommendation
import com.example.sairo14.domain.repository.OnboardingRecommendationRepository
import com.example.sairo14.domain.repository.OnboardingRepository
import com.example.sairo14.domain.usecase.GetOnboardingRecommendationsUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingResultViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `추천 결과가 없으면 온보딩 완료 상태를 해제하고 정상 콘텐츠 상태로 유지한다`() = runTest(dispatcher) {
        val onboardingRepository = ResultOnboardingRepository(completed = true)
        val viewModel = createViewModel(
            recommendations = emptyList(),
            onboardingRepository = onboardingRepository,
        )

        viewModel.load(selectedPhotoIds)
        advanceUntilIdle()

        val content = viewModel.uiState.value as OnboardingResultUiState.Content
        assertTrue(content.recommendations.isEmpty())
        assertTrue(!onboardingRepository.completed)
    }

    @Test
    fun `추천 결과와 온보딩 완료 저장을 함께 처리한다`() = runTest(dispatcher) {
        val recommendations = listOf(recommendation(id = "boeun"), recommendation(id = "gangneung"))
        val onboardingRepository = ResultOnboardingRepository()
        val viewModel = OnboardingResultViewModel(
            getOnboardingRecommendations = GetOnboardingRecommendationsUseCase(
                ResultRecommendationRepository(AppResult.Success(recommendations)),
            ),
            updateOnboardingCompletion = UpdateOnboardingCompletionUseCase(onboardingRepository),
        )

        viewModel.load(selectedPhotoIds)
        advanceUntilIdle()

        val content = viewModel.uiState.value as OnboardingResultUiState.Content
        assertEquals(recommendations, content.recommendations)
        assertTrue(onboardingRepository.completed)
    }

    @Test
    fun `북마크는 현재 화면의 추천 상태만 전환한다`() = runTest(dispatcher) {
        val viewModel = createViewModel(recommendations = listOf(recommendation(id = "boeun")))
        viewModel.load(selectedPhotoIds)
        advanceUntilIdle()

        viewModel.toggleSaved("boeun")

        val content = viewModel.uiState.value as OnboardingResultUiState.Content
        assertTrue(content.recommendations.single().isSaved)
    }

    @Test
    fun `추천 조회가 실패하면 오류 상태를 표시한다`() = runTest(dispatcher) {
        val onboardingRepository = ResultOnboardingRepository()
        val viewModel = OnboardingResultViewModel(
            getOnboardingRecommendations = GetOnboardingRecommendationsUseCase(
                ResultRecommendationRepository(AppResult.Failure(AppError.Unknown)),
            ),
            updateOnboardingCompletion = UpdateOnboardingCompletionUseCase(onboardingRepository),
        )

        viewModel.load(selectedPhotoIds)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is OnboardingResultUiState.Error)
        assertTrue(!onboardingRepository.completed)
    }

    private fun createViewModel(
        recommendations: List<OnboardingRecommendation>,
        onboardingRepository: ResultOnboardingRepository = ResultOnboardingRepository(),
    ): OnboardingResultViewModel = OnboardingResultViewModel(
        getOnboardingRecommendations = GetOnboardingRecommendationsUseCase(
            ResultRecommendationRepository(AppResult.Success(recommendations)),
        ),
        updateOnboardingCompletion = UpdateOnboardingCompletionUseCase(onboardingRepository),
    )

    private class ResultOnboardingRepository(
        var completed: Boolean = false,
    ) : OnboardingRepository {

        override suspend fun getHasCompletedOnboarding(): AppResult<Boolean> =
            AppResult.Success(completed)

        override suspend fun markOnboardingCompleted(): AppResult<Unit> {
            completed = true
            return AppResult.Success(Unit)
        }

        override suspend fun markOnboardingIncomplete(): AppResult<Unit> {
            completed = false
            return AppResult.Success(Unit)
        }
    }

    private class ResultRecommendationRepository(
        private val result: AppResult<List<OnboardingRecommendation>>,
    ) : OnboardingRecommendationRepository {
        override suspend fun getRecommendations(
            selectedPhotoIds: List<String>,
        ): AppResult<List<OnboardingRecommendation>> = result
    }

    private companion object {
        val selectedPhotoIds = listOf(
            "photo-quiet-lake",
            "photo-jeju-coast",
            "photo-forest-cabin",
            "photo-cafe-window",
            "photo-autumn-road",
        )

        fun recommendation(id: String) = OnboardingRecommendation(
            id = id,
            courseId = "course-$id",
            regionName = "충북 보은권",
            description = "고요한 자연과 전통의 분위기",
            imageUrls = emptyList(),
            placeNames = listOf("말티재 전망대", "세조길 숲 산책"),
        )
    }
}
