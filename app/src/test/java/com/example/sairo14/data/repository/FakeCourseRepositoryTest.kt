package com.example.sairo14.data.repository

import com.example.sairo14.data.repository.fake.FakeCourseRepository
import com.example.sairo14.data.repository.fake.FakeSharedCourseRepository
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeCourseRepositoryTest {

    @Test
    fun `보은 코스는 순서가 있는 두 일차와 서로 다른 장소 좌표를 반환한다`() = runTest {
        val result = FakeCourseRepository().getCourse("course-boeun")

        val course = result.successValue()

        assertEquals("충북 보은권", course.regionName)
        assertEquals(listOf(1, 2), course.days.map { day -> day.dayNumber })
        assertEquals("말티재 전망대", course.days.first().places.first().name)
        assertTrue(
            course.days[0].places.map { place -> place.coordinate } !=
                course.days[1].places.map { place -> place.coordinate },
        )
    }

    @Test
    fun `알 수 없는 코스 ID는 찾을 수 없음 오류를 반환한다`() = runTest {
        val result = FakeCourseRepository().getCourse("missing-course")

        assertTrue(
            result is AppResult.Failure && result.error is AppError.ResourceNotFound,
        )
    }

    @Test
    fun `같은 코스 공유 요청은 같은 링크를 반환한다`() = runTest {
        val repository = FakeCourseRepository()

        assertEquals(
            repository.createShareLink("course-boeun"),
            repository.createShareLink("course-boeun"),
        )
    }

    @Test
    fun `알 수 없는 코스 공유 요청은 찾을 수 없음 오류를 반환한다`() = runTest {
        val result = FakeCourseRepository().createShareLink("missing-course")

        assertTrue(
            result is AppResult.Failure && result.error is AppError.ResourceNotFound,
        )
    }

    @Test
    fun `공유 링크 식별자로 읽기 전용 고정 스냅샷을 반환한다`() = runTest {
        val result = FakeSharedCourseRepository().getSharedCourse("7429b36b9d")

        val sharedCourse = result.successValue()

        assertEquals("7429b36b9d", sharedCourse.shareId)
        assertEquals("course-boeun", sharedCourse.courseId)
        assertEquals("충북 보은권", sharedCourse.regionName)
        assertEquals(listOf(1, 2), sharedCourse.days.map { it.dayNumber })
    }

    private fun <T> AppResult<T>.successValue(): T =
        (this as? AppResult.Success<T>)?.value
            ?: error("성공 결과를 기대했습니다.")
}
