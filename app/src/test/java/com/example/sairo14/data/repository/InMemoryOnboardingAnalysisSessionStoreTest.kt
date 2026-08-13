package com.example.sairo14.data.repository

import com.example.sairo14.domain.model.Course
import com.example.sairo14.domain.model.CourseDay
import com.example.sairo14.domain.model.OnboardingAnalysisResult
import com.example.sairo14.domain.model.OnboardingRecommendation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InMemoryOnboardingAnalysisSessionStoreTest {

    @Test
    fun `세션별 분석 결과와 코스 상세를 저장하고 조회한다`() = runTest {
        val store = InMemoryOnboardingAnalysisSessionStore()

        store.save(searchSessionId = "session-1", result = analysisResult(courseId = "course-1"))

        assertEquals("고요한", store.getResult("session-1")?.moodTags?.single())
        assertEquals("course-1", store.getCourse("session-1", "course-1")?.courseId)
        assertNull(store.getCourse("session-1", "missing-course"))
    }

    @Test
    fun `같은 세션 저장은 이전 결과를 교체하고 삭제 후에는 조회되지 않는다`() = runTest {
        val store = InMemoryOnboardingAnalysisSessionStore()
        store.save(searchSessionId = "session-1", result = analysisResult(courseId = "course-old"))
        store.save(searchSessionId = "session-1", result = analysisResult(courseId = "course-new"))

        assertNull(store.getCourse("session-1", "course-old"))
        assertEquals("course-new", store.getCourse("session-1", "course-new")?.courseId)

        store.remove("session-1")

        assertNull(store.getResult("session-1"))
    }

    private fun analysisResult(courseId: String): OnboardingAnalysisResult {
        val course = Course(
            courseId = courseId,
            regionName = "제주도",
            days = listOf(CourseDay(dayNumber = 1, places = emptyList())),
        )
        return OnboardingAnalysisResult(
            moodTags = listOf("고요한"),
            summary = "자연 속에서 여유를 즐기는 취향이에요.",
            recommendations = listOf(
                OnboardingRecommendation(
                    id = courseId,
                    courseId = courseId,
                    regionName = course.regionName,
                    description = "추천 설명",
                    imageUrls = emptyList(),
                    placeNames = emptyList(),
                ),
            ),
            courses = mapOf(courseId to course),
        )
    }
}
