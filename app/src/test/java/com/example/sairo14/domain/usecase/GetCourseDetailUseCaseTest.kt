package com.example.sairo14.domain.usecase

import com.example.sairo14.data.repository.InMemoryOnboardingAnalysisSessionStore
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.Course
import com.example.sairo14.domain.model.CourseDay
import com.example.sairo14.domain.model.OnboardingAnalysisRequestToken
import com.example.sairo14.domain.repository.CourseRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCourseDetailUseCaseTest {

    @Test
    fun `온보딩 세션 코스를 일반 Repository보다 우선한다`() = runTest {
        val sessionCourse = course("session-course")
        val store = InMemoryOnboardingAnalysisSessionStore().also { store ->
            store.registerRequest("session-1", OnboardingAnalysisRequestToken(1))
            store.saveIfCurrent(
                searchSessionId = "session-1",
                token = OnboardingAnalysisRequestToken(1),
                result = com.example.sairo14.domain.model.OnboardingAnalysisResult(
                    moodTags = emptyList(), summary = "", recommendations = emptyList(),
                    courses = mapOf("course-1" to sessionCourse),
                ),
            )
        }
        val useCase = GetCourseDetailUseCase(
            courseRepository = Repository(course("repository-course")),
            onboardingAnalysisSessionStore = store,
        )

        val result = useCase(courseId = "course-1", onboardingSessionId = "session-1")

        assertEquals(AppResult.Success(sessionCourse), result)
    }

    @Test
    fun `세션 코스가 없으면 일반 Repository를 사용한다`() = runTest {
        val repositoryCourse = course("repository-course")
        val useCase = GetCourseDetailUseCase(
            courseRepository = Repository(repositoryCourse),
            onboardingAnalysisSessionStore = InMemoryOnboardingAnalysisSessionStore(),
        )

        val result = useCase(courseId = "course-1", onboardingSessionId = "missing-session")

        assertEquals(AppResult.Success(repositoryCourse), result)
    }

    private class Repository(private val course: Course) : CourseRepository {
        override suspend fun getCourse(courseId: String) = AppResult.Success(course)
    }

    private fun course(id: String) = Course(id, "제주도", listOf(CourseDay(1, emptyList())))
}
