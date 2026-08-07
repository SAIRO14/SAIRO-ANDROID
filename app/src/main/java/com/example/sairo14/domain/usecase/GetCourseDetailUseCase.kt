package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.Course
import com.example.sairo14.domain.repository.CourseRepository
import javax.inject.Inject

/** 여행 상세 화면에 표시할 코스 정보를 조회한다. */
class GetCourseDetailUseCase @Inject constructor(
    private val courseRepository: CourseRepository,
) {
    /** 코스 ID에 해당하는 지역·일차·장소 정보를 반환한다.
     *
     * @param courseId 조회할 코스의 안정적인 ID
     */
    suspend operator fun invoke(courseId: String): AppResult<Course> =
        courseRepository.getCourse(courseId)
}
