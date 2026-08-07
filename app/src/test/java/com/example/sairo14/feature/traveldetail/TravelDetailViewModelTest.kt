package com.example.sairo14.feature.traveldetail

import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.Course
import com.example.sairo14.domain.model.CourseDay
import com.example.sairo14.domain.model.CoursePlace
import com.example.sairo14.domain.model.MapCoordinate
import com.example.sairo14.domain.repository.CourseRepository
import com.example.sairo14.domain.usecase.GetCourseDetailUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TravelDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `코스를 읽으면 첫 일차의 장소를 선택 상태로 표시한다`() = runTest(dispatcher) {
        val viewModel = createViewModel(AppResult.Success(course()))

        viewModel.load("course-boeun")
        advanceUntilIdle()

        val content = viewModel.uiState.value as TravelDetailUiState.Content
        assertEquals(1, content.selectedDayNumber)
        assertEquals("첫째 장소", content.selectedDay?.places?.first()?.name)
        assertEquals("first", content.selectedPlaceId)
        assertEquals(0L, content.cameraFocusRequestId)
    }

    @Test
    fun `일차를 선택하면 지도와 목록이 함께 사용할 선택 일차를 변경한다`() = runTest(dispatcher) {
        val viewModel = createViewModel(AppResult.Success(course()))
        viewModel.load("course-boeun")
        advanceUntilIdle()

        viewModel.selectDay(2)

        val content = viewModel.uiState.value as TravelDetailUiState.Content
        assertEquals(2, content.selectedDayNumber)
        assertEquals("둘째 장소", content.selectedDay?.places?.single()?.name)
        assertEquals("second", content.selectedPlaceId)
        assertEquals(1L, content.cameraFocusRequestId)
    }

    @Test
    fun `장소를 선택하면 지도 중심에 사용할 장소를 변경한다`() = runTest(dispatcher) {
        val viewModel = createViewModel(AppResult.Success(courseWithTwoPlaces()))
        viewModel.load("course-boeun")
        advanceUntilIdle()

        viewModel.selectPlace("second")

        val content = viewModel.uiState.value as TravelDetailUiState.Content
        assertEquals("second", content.selectedPlaceId)
        assertEquals("둘째 장소", content.selectedPlace?.name)
        assertEquals(37.0, content.selectedPlace?.latitude)
        assertEquals(1L, content.cameraFocusRequestId)
    }

    @Test
    fun `이미 선택된 장소를 다시 선택해도 새 카메라 이동 요청을 만든다`() = runTest(dispatcher) {
        val viewModel = createViewModel(AppResult.Success(course()))
        viewModel.load("course-boeun")
        advanceUntilIdle()

        viewModel.selectPlace("first")

        val content = viewModel.uiState.value as TravelDetailUiState.Content
        assertEquals("first", content.selectedPlaceId)
        assertEquals(1L, content.cameraFocusRequestId)
    }

    @Test
    fun `저장 표시는 현재 상세 화면 안에서 전환한다`() = runTest(dispatcher) {
        val viewModel = createViewModel(AppResult.Success(course()))
        viewModel.load("course-boeun")
        advanceUntilIdle()

        viewModel.toggleSaved()

        val content = viewModel.uiState.value as TravelDetailUiState.Content
        assertTrue(content.isSaved)
    }

    @Test
    fun `코스를 읽지 못하면 오류 상태를 표시한다`() = runTest(dispatcher) {
        val viewModel = createViewModel(AppResult.Failure(AppError.ResourceNotFound))

        viewModel.load("missing-course")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is TravelDetailUiState.Error)
    }

    private fun createViewModel(result: AppResult<Course>): TravelDetailViewModel =
        TravelDetailViewModel(
            getCourseDetail = GetCourseDetailUseCase(CourseResultRepository(result)),
        )

    private class CourseResultRepository(
        private val result: AppResult<Course>,
    ) : CourseRepository {
        override suspend fun getCourse(courseId: String): AppResult<Course> = result
    }

    private companion object {
        fun course() = Course(
            courseId = "course-boeun",
            regionName = "충북 보은권",
            days = listOf(
                CourseDay(
                    dayNumber = 1,
                    places = listOf(place("first", "첫째 장소")),
                ),
                CourseDay(
                    dayNumber = 2,
                    places = listOf(place("second", "둘째 장소")),
                ),
            ),
        )

        fun place(id: String, name: String, latitude: Double = 36.0) = CoursePlace(
            placeId = id,
            name = name,
            imageUrl = null,
            tags = emptyList(),
            coordinate = MapCoordinate(latitude = latitude, longitude = 127.0),
        )

        fun courseWithTwoPlaces() = Course(
            courseId = "course-boeun",
            regionName = "충북 보은권",
            days = listOf(
                CourseDay(
                    dayNumber = 1,
                    places = listOf(
                        place("first", "첫째 장소"),
                        place("second", "둘째 장소", latitude = 37.0),
                    ),
                ),
            ),
        )
    }
}
