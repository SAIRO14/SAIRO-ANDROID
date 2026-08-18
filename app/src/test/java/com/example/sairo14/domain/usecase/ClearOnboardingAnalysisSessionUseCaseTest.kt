package com.example.sairo14.domain.usecase

import com.example.sairo14.data.repository.InMemoryOnboardingAnalysisSessionStore
import com.example.sairo14.domain.model.OnboardingAnalysisResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test

class ClearOnboardingAnalysisSessionUseCaseTest {

    @Test
    fun `지정한 세션의 분석 결과를 삭제한다`() = runTest {
        val store = InMemoryOnboardingAnalysisSessionStore()
        val token = store.beginRequest("session-1")
        store.saveIfCurrent(
            searchSessionId = "session-1",
            token = token,
            result = OnboardingAnalysisResult(
                moodTags = emptyList(),
                summary = "요약",
                recommendations = emptyList(),
                courses = emptyMap(),
            ),
        )
        val useCase = ClearOnboardingAnalysisSessionUseCase(store)

        useCase("session-1")

        assertNull(store.getResult("session-1"))
    }
}
