package com.example.sairo14.domain.repository

import com.example.sairo14.domain.model.AppResult

/** 온보딩 완료 상태를 조회하고 저장하는 도메인 계약이다. */
interface OnboardingRepository {

    /** 저장된 온보딩 완료 여부를 한 번 읽는다. */
    suspend fun getHasCompletedOnboarding(): AppResult<Boolean>

    /** 온보딩 완료 상태를 영구 저장한다. */
    suspend fun markOnboardingCompleted(): AppResult<Unit>
}
