package com.example.sairo14.domain.usecase

import com.example.sairo14.data.repository.InMemoryOnboardingAnalysisSessionStore
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.OnboardingAnalysisResult
import com.example.sairo14.domain.repository.OnboardingRecommendationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AnalyzeAndStoreOnboardingTasteUseCaseTest {

    @Test
    fun `성공한 분석 결과를 지정한 세션에 저장한다`() = runTest {
        val result = OnboardingAnalysisResult(emptyList(), "요약", emptyList(), emptyMap())
        val store = InMemoryOnboardingAnalysisSessionStore()
        val useCase = AnalyzeAndStoreOnboardingTasteUseCase(
            onboardingRecommendationRepository = Repository(AppResult.Success(result)),
            sessionStore = store,
        )

        val returned = useCase(
            searchSessionId = "session-1",
            selectedPhotoIds = photoIds,
        )

        assertEquals(AppResult.Success(result), returned)
        assertEquals(result, store.getResult("session-1"))
    }

    @Test
    fun `분석 실패는 세션에 저장하지 않는다`() = runTest {
        val store = InMemoryOnboardingAnalysisSessionStore()
        val useCase = AnalyzeAndStoreOnboardingTasteUseCase(
            onboardingRecommendationRepository = Repository(AppResult.Failure(AppError.NetworkUnavailable)),
            sessionStore = store,
        )

        val returned = useCase(
            searchSessionId = "session-1",
            selectedPhotoIds = photoIds,
        )

        assertEquals(AppResult.Failure(AppError.NetworkUnavailable), returned)
        assertNull(store.getResult("session-1"))
    }

    private class Repository(
        private val result: AppResult<OnboardingAnalysisResult>,
    ) : OnboardingRecommendationRepository {
        override suspend fun analyzeTaste(
            selectedPhotoIds: List<String>,
        ): AppResult<OnboardingAnalysisResult> = result
    }

    private companion object {
        val photoIds = listOf("photo-1", "photo-2", "photo-3", "photo-4", "photo-5")
    }
}
