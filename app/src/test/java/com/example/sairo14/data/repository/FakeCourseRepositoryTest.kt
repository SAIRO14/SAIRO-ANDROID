package com.example.sairo14.data.repository

import com.example.sairo14.data.repository.fake.FakeCourseRepository
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

    private fun <T> AppResult<T>.successValue(): T =
        (this as? AppResult.Success<T>)?.value
            ?: error("성공 결과를 기대했습니다.")
}
