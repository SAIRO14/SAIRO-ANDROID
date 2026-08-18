package com.example.sairo14.domain.model

/** 온보딩 완료 상태 갱신의 최신 순서를 나타내는 값이다.
 *
 * @param value 새 결과 조회일수록 커지는 순서 값
 */
@JvmInline
value class OnboardingCompletionToken(
    val value: Long,
)
