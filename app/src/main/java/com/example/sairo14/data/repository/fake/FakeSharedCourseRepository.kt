package com.example.sairo14.data.repository.fake

import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.CourseDay
import com.example.sairo14.domain.model.CoursePlace
import com.example.sairo14.domain.model.MapCoordinate
import com.example.sairo14.domain.model.SharedCourse
import com.example.sairo14.domain.repository.SharedCourseRepository
import javax.inject.Inject
import javax.inject.Singleton

/** 공개 공유 코스 수신 화면을 확인할 수 있도록 고정 스냅샷을 제공한다. */
@Singleton
class FakeSharedCourseRepository @Inject constructor() : SharedCourseRepository {

    override suspend fun getSharedCourse(shareId: String): AppResult<SharedCourse> =
        sampleCourses[shareId]
            ?.let { AppResult.Success(it) }
            ?: AppResult.Failure(AppError.ResourceNotFound)

    private companion object {
        val sampleCourses = listOf(
            SharedCourse(
                shareId = "7429b36b9d",
                courseId = "course-boeun",
                regionName = "충북 보은권",
                days = listOf(
                    CourseDay(
                        dayNumber = 1,
                        places = listOf(
                            CoursePlace(
                                placeId = "boeun-maltijae-observatory",
                                name = "말티재 전망대",
                                imageUrl = null,
                                tags = listOf("상시 개방"),
                                coordinate = MapCoordinate(36.4894, 127.7469),
                            ),
                        ),
                    ),
                    CourseDay(dayNumber = 2, places = emptyList()),
                ),
            ),
        ).associateBy(SharedCourse::shareId)
    }
}
