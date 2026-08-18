package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.Course
import com.example.sairo14.domain.repository.CourseRepository
import com.example.sairo14.domain.repository.OnboardingAnalysisSessionStore
import javax.inject.Inject

/** 여행 상세 화면에 표시할 코스 정보를 조회한다. */
class GetCourseDetailUseCase @Inject constructor(
    private val courseRepository: CourseRepository,
    private val onboardingAnalysisSessionStore: OnboardingAnalysisSessionStore,
) {
    /** 온보딩 세션 코스를 우선해 코스 ID에 해당하는 지역·일차·장소 정보를 반환한다.
     *
     * @param courseId 조회할 코스의 안정적인 ID
     * @param onboardingSessionId 온보딩 분석 결과를 보관한 세션 ID. 없거나 해당 코스가 없으면 일반 코스
     * Repository를 조회한다
     */
    suspend operator fun invoke(
        courseId: String,
        onboardingSessionId: String? = null,
    ): AppResult<Course> =
        onboardingSessionId
            ?.let { sessionId -> onboardingAnalysisSessionStore.getCourse(sessionId, courseId) }
            ?.let { course -> AppResult.Success(course) }
            ?: courseRepository.getCourse(courseId)
}
