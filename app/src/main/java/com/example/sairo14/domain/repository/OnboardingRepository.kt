package com.example.sairo14.domain.repository

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.OnboardingCompletionToken

/** 온보딩 완료 상태를 조회하고 저장하는 도메인 계약이다. */
interface OnboardingRepository {

    /** 저장된 온보딩 완료 여부를 한 번 읽는다. */
    suspend fun getHasCompletedOnboarding(): AppResult<Boolean>

    /** 완료 상태를 갱신할 다음 결과 조회 토큰을 발급하고 최신 요청으로 등록한다.
     *
     * 이전 토큰은 최신 토큰이 등록된 뒤 완료 상태를 변경할 수 없다.
     * @return DataStore가 발급한 결과 조회 순서 토큰
     */
    suspend fun createCompletionRequest(): AppResult<OnboardingCompletionToken>

    /** 토큰이 최신 결과 조회일 때만 온보딩 완료 상태를 영구 저장한다.
     * @param token 결과 조회 순서를 나타내는 토큰
     * @param completed 저장할 온보딩 완료 여부
     * @return 최신 토큰이라 상태를 저장했으면 `true`, 이전 토큰이라 무시했으면 `false`
     */
    suspend fun updateCompletionIfCurrent(
        token: OnboardingCompletionToken,
        completed: Boolean,
    ): AppResult<Boolean>
}
