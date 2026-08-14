package com.example.sairo14.data.repository

import com.example.sairo14.domain.model.Course
import com.example.sairo14.domain.model.OnboardingAnalysisResult
import com.example.sairo14.domain.model.OnboardingAnalysisRequestToken
import com.example.sairo14.domain.repository.OnboardingAnalysisSessionStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** 앱 프로세스가 살아 있는 동안 온보딩 분석 결과를 탐색 세션별로 보관한다. */
@Singleton
class InMemoryOnboardingAnalysisSessionStore @Inject constructor() : OnboardingAnalysisSessionStore {
    private val mutex = Mutex()
    private val resultsBySessionId = mutableMapOf<String, OnboardingAnalysisResult>()
    private val latestRequestTokensBySessionId = mutableMapOf<String, OnboardingAnalysisRequestToken>()

    override suspend fun beginRequest(
        searchSessionId: String,
    ): OnboardingAnalysisRequestToken = mutex.withLock {
        val currentToken = latestRequestTokensBySessionId[searchSessionId]?.value ?: 0L
        val nextToken = OnboardingAnalysisRequestToken(currentToken + 1L)
        latestRequestTokensBySessionId[searchSessionId] = nextToken
        nextToken
    }

    override suspend fun saveIfCurrent(
        searchSessionId: String,
        token: OnboardingAnalysisRequestToken,
        result: OnboardingAnalysisResult,
    ): Boolean = mutex.withLock {
        if (latestRequestTokensBySessionId[searchSessionId] != token) {
            return@withLock false
        }

        resultsBySessionId[searchSessionId] = result
        true
    }

    override suspend fun getResult(searchSessionId: String): OnboardingAnalysisResult? = mutex.withLock {
        resultsBySessionId[searchSessionId]
    }

    override suspend fun getCourse(
        searchSessionId: String,
        courseId: String,
    ): Course? = mutex.withLock {
        resultsBySessionId[searchSessionId]?.courses?.get(courseId)
    }

    override suspend fun remove(searchSessionId: String) {
        mutex.withLock {
            resultsBySessionId.remove(searchSessionId)
            latestRequestTokensBySessionId.remove(searchSessionId)
        }
    }
}
