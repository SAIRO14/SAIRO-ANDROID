package com.example.sairo14.feature.onboarding

import com.example.sairo14.core.navigation.OnboardingAnimationPhoto
import com.example.sairo14.data.repository.InMemoryOnboardingAnalysisSessionStore
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.OnboardingAnalysisResult
import com.example.sairo14.domain.repository.OnboardingRecommendationRepository
import com.example.sairo14.domain.usecase.AnalyzeAndStoreOnboardingTasteUseCase
import com.example.sairo14.feature.onboarding.loading.OnboardingLoadingCardCount
import com.example.sairo14.feature.onboarding.loading.OnboardingLoadingUiState
import com.example.sairo14.feature.onboarding.loading.OnboardingLoadingViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
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

    @Test fun `재시도 뒤 늦게 도착한 이전 실패는 최신 성공 화면을 덮어쓰지 않는다`() = runTest(dispatcher) {
        val store = InMemoryOnboardingAnalysisSessionStore()
        val repository = DelayedRepository()
        val viewModel = OnboardingLoadingViewModel(
            AnalyzeAndStoreOnboardingTasteUseCase(repository, store),
        )
        val photos = List(OnboardingLoadingCardCount) { index ->
            OnboardingAnimationPhoto("photo-$index", "https://example.com/$index.jpg")
        }

        viewModel.load("session-1", photos.map(OnboardingAnimationPhoto::id), photos)
        runCurrent()
        viewModel.retry()
        runCurrent()

        repository.respond(
            requestIndex = 1,
            result = AppResult.Success(
                OnboardingAnalysisResult(listOf("새로운 태그"), "요약", emptyList(), emptyMap()),
            ),
        )
        runCurrent()
        repository.respond(requestIndex = 0, result = AppResult.Failure(AppError.NetworkUnavailable))
        advanceUntilIdle()

        val content = viewModel.uiState.value as OnboardingLoadingUiState.Content
        assertEquals(listOf("새로운 태그"), content.moodTags)
    }

    @Test fun `취소된 이전 성공 응답은 최신 세션 결과를 덮어쓰지 않는다`() = runTest(dispatcher) {
        val store = InMemoryOnboardingAnalysisSessionStore()
        val repository = DelayedRepository()
        val viewModel = OnboardingLoadingViewModel(
            AnalyzeAndStoreOnboardingTasteUseCase(repository, store),
        )
        val photos = List(OnboardingLoadingCardCount) { index ->
            OnboardingAnimationPhoto("photo-$index", "https://example.com/$index.jpg")
        }
        val latestResult = OnboardingAnalysisResult(listOf("최신 태그"), "요약", emptyList(), emptyMap())

        viewModel.load("session-1", photos.map(OnboardingAnimationPhoto::id), photos)
        runCurrent()
        viewModel.retry()
        runCurrent()
        repository.respond(requestIndex = 1, result = AppResult.Success(latestResult))
        runCurrent()
        repository.respond(
            requestIndex = 0,
            result = AppResult.Success(
                OnboardingAnalysisResult(listOf("이전 태그"), "요약", emptyList(), emptyMap()),
            ),
        )
        advanceUntilIdle()

        assertEquals(latestResult, store.getResult("session-1"))
    }

    @Test fun `새 Loading ViewModel의 요청은 같은 세션에서 이전 요청 결과를 덮어쓰지 않는다`() = runTest(dispatcher) {
        val store = InMemoryOnboardingAnalysisSessionStore()
        val repository = DelayedRepository()
        val firstViewModel = OnboardingLoadingViewModel(
            AnalyzeAndStoreOnboardingTasteUseCase(repository, store),
        )
        val secondViewModel = OnboardingLoadingViewModel(
            AnalyzeAndStoreOnboardingTasteUseCase(repository, store),
        )
        val photos = List(OnboardingLoadingCardCount) { index ->
            OnboardingAnimationPhoto("photo-$index", "https://example.com/$index.jpg")
        }
        val latestResult = OnboardingAnalysisResult(listOf("새 요청 태그"), "요약", emptyList(), emptyMap())

        firstViewModel.load("session-1", photos.map(OnboardingAnimationPhoto::id), photos)
        runCurrent()
        secondViewModel.load("session-1", photos.map(OnboardingAnimationPhoto::id), photos)
        runCurrent()

        repository.respond(requestIndex = 1, result = AppResult.Success(latestResult))
        runCurrent()
        repository.respond(
            requestIndex = 0,
            result = AppResult.Success(
                OnboardingAnalysisResult(listOf("이전 요청 태그"), "요약", emptyList(), emptyMap()),
            ),
        )
        advanceUntilIdle()

        assertEquals(latestResult, store.getResult("session-1"))
    }

    private class Repository(private val result: AppResult<OnboardingAnalysisResult>) : OnboardingRecommendationRepository {
        override suspend fun analyzeTaste(selectedPhotoIds: List<String>) = result
    }

    private class DelayedRepository : OnboardingRecommendationRepository {
        private val responses = mutableListOf<CompletableDeferred<AppResult<OnboardingAnalysisResult>>>()

        override suspend fun analyzeTaste(
            selectedPhotoIds: List<String>,
        ): AppResult<OnboardingAnalysisResult> {
            val response = CompletableDeferred<AppResult<OnboardingAnalysisResult>>()
            responses += response
            return withContext(NonCancellable) { response.await() }
        }

        fun respond(
            requestIndex: Int,
            result: AppResult<OnboardingAnalysisResult>,
        ) {
            responses[requestIndex].complete(result)
        }
    }
}
