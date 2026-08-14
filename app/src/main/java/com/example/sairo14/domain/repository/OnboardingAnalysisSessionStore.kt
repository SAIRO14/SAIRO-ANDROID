package com.example.sairo14.domain.repository

import com.example.sairo14.domain.model.Course
import com.example.sairo14.domain.model.OnboardingAnalysisResult
import com.example.sairo14.domain.model.OnboardingAnalysisRequestToken

/** 한 번의 온보딩 탐색에서 생성한 분석 결과를 화면 전환 동안 보관하는 계약이다. */
interface OnboardingAnalysisSessionStore {

    /** 분석 요청을 시작하고 해당 세션에서 유일한 최신 요청 토큰을 발급한다.
     *
     * 토큰 발급과 최신 요청 등록은 하나의 원자적 작업으로 처리한다. 따라서 Loading ViewModel이 다시
     * 생성돼도 이전 요청과 같은 토큰을 사용하지 않는다. 세션 ID의 생성과 만료 정책은 호출자가 소유하며,
     * 이 저장소는 앱 프로세스 안에서만 결과를 보관한다.
     * @param searchSessionId 온보딩 탐색을 구분하는 고유 식별자
     * @return 같은 세션에서 새 요청일수록 커지는 순서 토큰
     */
    suspend fun beginRequest(
        searchSessionId: String,
    ): OnboardingAnalysisRequestToken

    /** 토큰이 현재 세션의 최신 요청일 때만 분석 결과를 저장한다.
     *
     * 최신 토큰 확인과 결과 저장은 하나의 원자적 작업으로 처리한다.
     * @param searchSessionId 온보딩 탐색을 구분하는 고유 식별자
     * @param token 저장하려는 분석 요청의 순서를 나타내는 토큰
     * @param result 서버 응답을 Domain 모델로 변환한 분석 결과
     * @return 최신 요청이라 결과를 저장했으면 `true`, 이전 요청이라 무시했으면 `false`
     */
    suspend fun saveIfCurrent(
        searchSessionId: String,
        token: OnboardingAnalysisRequestToken,
        result: OnboardingAnalysisResult,
    ): Boolean

    /** 세션에 저장한 전체 분석 결과를 반환한다.
     *
     * 결과가 없으면 세션이 아직 분석을 완료하지 않았거나 앱 프로세스가 재시작된 상태다.
     * @param searchSessionId 조회할 온보딩 탐색 세션 ID
     */
    suspend fun getResult(searchSessionId: String): OnboardingAnalysisResult?

    /** 세션에 저장한 코스 상세 스냅샷을 코스 ID로 조회한다.
     *
     * 결과 화면에서 선택한 코스가 해당 세션에 없으면 `null`을 반환한다.
     * @param searchSessionId 조회할 온보딩 탐색 세션 ID
     * @param courseId 지도 상세에 표시할 코스 ID
     */
    suspend fun getCourse(
        searchSessionId: String,
        courseId: String,
    ): Course?

    /** 한 세션의 분석 결과를 삭제한다.
     *
     * 이미 결과가 없는 세션을 삭제해도 실패하지 않는다.
     * @param searchSessionId 삭제할 온보딩 탐색 세션 ID
     */
    suspend fun remove(searchSessionId: String)
}
