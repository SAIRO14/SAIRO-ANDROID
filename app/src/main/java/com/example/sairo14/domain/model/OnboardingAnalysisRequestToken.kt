package com.example.sairo14.domain.model

/** 같은 온보딩 탐색 세션 안에서 분석 요청의 최신 순서를 나타내는 값이다.
 *
 * @param value 같은 세션에서 새 요청일수록 커지는 순서 값
 */
@JvmInline
value class OnboardingAnalysisRequestToken(
    val value: Long,
)
