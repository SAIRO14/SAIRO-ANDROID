package com.example.sairo14.feature.onboarding

import com.example.sairo14.data.repository.InMemoryOnboardingAnalysisSessionStore
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.OnboardingAnalysisResult
import com.example.sairo14.domain.model.OnboardingRecommendation
import com.example.sairo14.domain.model.OnboardingAnalysisRequestToken
import com.example.sairo14.domain.repository.OnboardingRepository
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
        store.registerRequest("session-1", OnboardingAnalysisRequestToken(1))
        store.saveIfCurrent(
            searchSessionId = "session-1",
            token = OnboardingAnalysisRequestToken(1),
            result = OnboardingAnalysisResult(emptyList(), "", recommendations, emptyMap()),
        )
        val viewModel = OnboardingResultViewModel(store, UpdateOnboardingCompletionUseCase(OnboardingRepo()))

        viewModel.load("session-1")
        advanceUntilIdle()

        assertEquals(recommendations, (viewModel.uiState.value as OnboardingResultUiState.Content).recommendations)
    }

    @Test fun `늦은 이전 세션의 완료 상태 저장이 최신 세션 결과를 덮어쓰지 않는다`() = runTest(dispatcher) {
        val store = InMemoryOnboardingAnalysisSessionStore()
        store.saveResult("session-A", recommendations = listOf(recommendation("A")))
        store.saveResult("session-B", recommendations = emptyList())
        val repository = DelayedOnboardingRepository()
        val viewModel = OnboardingResultViewModel(
            sessionStore = store,
            updateOnboardingCompletion = UpdateOnboardingCompletionUseCase(repository),
        )

        viewModel.load("session-A")
        runCurrent()
        repository.awaitFirstCompletionUpdate()

        viewModel.load("session-B")
        runCurrent()
        repository.completeFirstUpdate()
        advanceUntilIdle()

        val content = viewModel.uiState.value as OnboardingResultUiState.Content
        assertEquals(emptyList<OnboardingRecommendation>(), content.recommendations)
        assertEquals(false, repository.completed)
    }

    private class OnboardingRepo : OnboardingRepository {
        override suspend fun getHasCompletedOnboarding() = AppResult.Success(false)
        override suspend fun markOnboardingCompleted() = AppResult.Success(Unit)
        override suspend fun markOnboardingIncomplete() = AppResult.Success(Unit)
    }

    private class DelayedOnboardingRepository : OnboardingRepository {
        private val firstUpdateStarted = CompletableDeferred<Unit>()
        private val firstUpdateGate = CompletableDeferred<Unit>()
        private var updateCount = 0
        var completed: Boolean? = null
            private set

        override suspend fun getHasCompletedOnboarding() = AppResult.Success(completed ?: false)

        override suspend fun markOnboardingCompleted(): AppResult<Unit> = update(true)

        override suspend fun markOnboardingIncomplete(): AppResult<Unit> = update(false)

        suspend fun awaitFirstCompletionUpdate() {
            firstUpdateStarted.await()
        }

        fun completeFirstUpdate() {
            firstUpdateGate.complete(Unit)
        }

        private suspend fun update(nextCompleted: Boolean): AppResult<Unit> = withContext(NonCancellable) {
            updateCount += 1
            if (updateCount == 1) {
                firstUpdateStarted.complete(Unit)
                firstUpdateGate.await()
            }
            completed = nextCompleted
            AppResult.Success(Unit)
        }
    }

    private suspend fun InMemoryOnboardingAnalysisSessionStore.saveResult(
        searchSessionId: String,
        recommendations: List<OnboardingRecommendation>,
    ) {
        val token = OnboardingAnalysisRequestToken(1)
        registerRequest(searchSessionId, token)
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
