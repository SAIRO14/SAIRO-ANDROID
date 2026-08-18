package com.example.sairo14.domain.usecase

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SharedCourse
import com.example.sairo14.domain.repository.SharedCourseRepository
import javax.inject.Inject

/** 공유 링크 수신 화면에서 읽기 전용 코스 스냅샷을 조회한다. */
class GetSharedCourseUseCase @Inject constructor(
    private val sharedCourseRepository: SharedCourseRepository,
) {
    suspend operator fun invoke(shareId: String): AppResult<SharedCourse> =
        sharedCourseRepository.getSharedCourse(shareId)
}
