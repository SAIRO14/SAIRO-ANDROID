package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SharedCourseLink
import com.example.sairo14.domain.repository.CourseRepository
import javax.inject.Inject

/** 지정한 코스의 읽기 전용 공유 링크를 요청한다. */
class CreateCourseShareLinkUseCase @Inject constructor(
    private val courseRepository: CourseRepository,
) {
    /** 공유할 코스 ID를 Repository 계약으로 전달한다.
     * @param courseId 공유할 코스의 안정적인 ID
     */
    suspend operator fun invoke(courseId: String): AppResult<SharedCourseLink> =
        courseRepository.createShareLink(courseId)
}
