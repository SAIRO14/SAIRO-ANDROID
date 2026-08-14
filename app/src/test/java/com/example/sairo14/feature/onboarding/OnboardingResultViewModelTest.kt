package com.example.sairo14.feature.onboarding

import com.example.sairo14.data.repository.InMemoryOnboardingAnalysisSessionStore
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.OnboardingAnalysisResult
import com.example.sairo14.domain.model.OnboardingRecommendation
import com.example.sairo14.domain.model.OnboardingCompletionToken
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.repository.OnboardingRepository
import com.example.sairo14.domain.usecase.CreateOnboardingCompletionRequestUseCase
import com.example.sairo14.domain.usecase.UpdateOnboardingCompletionUseCase
import com.example.sairo14.feature.onboarding.result.OnboardingResultUiState
import com.example.sairo14.feature.onboarding.result.OnboardingResultViewModel
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
class OnboardingResultViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `세션의 추천 결과를 표시한다`() = runTest(dispatcher) {
        val store = InMemoryOnboardingAnalysisSessionStore()
        val recommendations = listOf(OnboardingRecommendation("id", "course", "제주도", "설명", emptyList(), emptyList()))
        val token = store.beginRequest("session-1")
        store.saveIfCurrent(
            searchSessionId = "session-1",
            token = token,
            result = OnboardingAnalysisResult(emptyList(), "", recommendations, emptyMap()),
        )
        val repository = OnboardingRepo()
        val viewModel = OnboardingResultViewModel(
            sessionStore = store,
            createOnboardingCompletionRequest = CreateOnboardingCompletionRequestUseCase(repository),
            updateOnboardingCompletion = UpdateOnboardingCompletionUseCase(repository),
        )

        viewModel.load("session-1")
        advanceUntilIdle()

        assertEquals(recommendations, (viewModel.uiState.value as OnboardingResultUiState.Content).recommendations)
    }

    @Test fun `최신 세션 완료 상태 저장이 실패해도 이전 세션은 완료 상태를 덮어쓰지 않는다`() = runTest(dispatcher) {
        val store = InMemoryOnboardingAnalysisSessionStore()
        store.saveResult("session-A", recommendations = listOf(recommendation("A")))
        store.saveResult("session-B", recommendations = emptyList())
        val repository = DelayedOnboardingRepository()
        val viewModel = OnboardingResultViewModel(
            sessionStore = store,
            createOnboardingCompletionRequest = CreateOnboardingCompletionRequestUseCase(repository),
            updateOnboardingCompletion = UpdateOnboardingCompletionUseCase(repository),
        )

        viewModel.load("session-A")
        runCurrent()
        repository.awaitFirstCompletionUpdate()

        viewModel.load("session-B")
        runCurrent()
        repository.completeFirstUpdate()
        advanceUntilIdle()

        assertEquals(OnboardingResultUiState.Error, viewModel.uiState.value)
        assertEquals(false, repository.completed)
    }

    private class OnboardingRepo : OnboardingRepository {
        private var token = 0L
        override suspend fun getHasCompletedOnboarding() = AppResult.Success(false)
        override suspend fun createCompletionRequest() = AppResult.Success(
            OnboardingCompletionToken(++token),
        )
        override suspend fun updateCompletionIfCurrent(
            token: OnboardingCompletionToken,
            completed: Boolean,
        ) = AppResult.Success(true)
    }

    private class DelayedOnboardingRepository : OnboardingRepository {
        private val firstUpdateStarted = CompletableDeferred<Unit>()
        private val firstUpdateGate = CompletableDeferred<Unit>()
        private var updateCount = 0
        private var latestToken = 0L
        var completed: Boolean = false
            private set

        override suspend fun getHasCompletedOnboarding() = AppResult.Success(completed)

        override suspend fun createCompletionRequest() = AppResult.Success(
            OnboardingCompletionToken(++latestToken),
        )

        override suspend fun updateCompletionIfCurrent(
            token: OnboardingCompletionToken,
            completed: Boolean,
        ): AppResult<Boolean> = update(token, completed)

        suspend fun awaitFirstCompletionUpdate() {
            firstUpdateStarted.await()
        }

        fun completeFirstUpdate() {
            firstUpdateGate.complete(Unit)
        }

        private suspend fun update(
            token: OnboardingCompletionToken,
            nextCompleted: Boolean,
        ): AppResult<Boolean> = withContext(NonCancellable) {
            updateCount += 1
            if (updateCount == 1) {
                firstUpdateStarted.complete(Unit)
                firstUpdateGate.await()
            }
            if (token.value != latestToken) return@withContext AppResult.Success(false)
            if (updateCount == 2) return@withContext AppResult.Failure(AppError.StorageUnavailable)

            completed = nextCompleted
            AppResult.Success(true)
        }
    }

    private suspend fun InMemoryOnboardingAnalysisSessionStore.saveResult(
        searchSessionId: String,
        recommendations: List<OnboardingRecommendation>,
    ) {
        val token = beginRequest(searchSessionId)
        saveIfCurrent(
            searchSessionId = searchSessionId,
            token = token,
            result = OnboardingAnalysisResult(emptyList(), "", recommendations, emptyMap()),
        )
    }

    private fun recommendation(id: String) = OnboardingRecommendation(
        id = id,
        courseId = "course-$id",
        regionName = "제주도",
        description = "설명",
        imageUrls = emptyList(),
        placeNames = emptyList(),
    )
}
