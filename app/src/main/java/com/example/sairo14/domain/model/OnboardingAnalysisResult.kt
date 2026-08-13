package com.example.sairo14.domain.model

/** 온보딩 사진 분석에서 생성한 태그·추천 카드·코스 상세 스냅샷이다.
 *
 * 로딩 화면은 [moodTags]를, 결과 화면은 [recommendations]를, 지도 상세 화면은 [courses]의 코스 ID를
 * 사용한다. 데이터 출처와 세션 수명은 호출하는 Repository 또는 세션 저장소가 관리한다.
 * @param moodTags 선택 사진에서 분석한 분위기 태그
 * @param summary 분석 결과를 설명하는 요약 문구
 * @param recommendations 결과 카드에 표시할 추천 목록
 * @param courses 코스 ID를 키로 하여 지도 상세에 사용할 코스 스냅샷
 */
data class OnboardingAnalysisResult(
    val moodTags: List<String>,
    val summary: String,
    val recommendations: List<OnboardingRecommendation>,
    val courses: Map<String, Course>,
)
