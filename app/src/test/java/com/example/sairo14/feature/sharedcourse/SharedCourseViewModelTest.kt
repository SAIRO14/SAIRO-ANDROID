package com.example.sairo14.feature.sharedcourse

import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.CourseDay
import com.example.sairo14.domain.model.CoursePlace
import com.example.sairo14.domain.model.MapCoordinate
import com.example.sairo14.domain.model.SharedCourse
import com.example.sairo14.domain.repository.SharedCourseRepository
import com.example.sairo14.domain.usecase.GetSharedCourseUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SharedCourseViewModelTest {
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
    fun `공유 코스를 읽으면 첫 일차와 첫 장소를 선택 상태로 표시한다`() = runTest(dispatcher) {
        val viewModel = createViewModel(AppResult.Success(sharedCourse()))

        viewModel.load("share-1")
        advanceUntilIdle()

        val content = viewModel.uiState.value as SharedCourseUiState.Content
        assertEquals("제주도", content.course.regionName)
        assertEquals(1, content.selectedDayNumber)
        assertEquals("place-1", content.selectedPlaceId)
        assertEquals(33.0, content.selectedPlace?.latitude)
    }

    @Test
    fun `일차와 장소 선택은 지도 카메라 요청 식별자를 증가시킨다`() = runTest(dispatcher) {
        val viewModel = createViewModel(AppResult.Success(sharedCourse()))
        viewModel.load("share-1")
        advanceUntilIdle()

        viewModel.selectDay(2)
        viewModel.selectPlace("place-2")

        val content = viewModel.uiState.value as SharedCourseUiState.Content
        assertEquals(2, content.selectedDayNumber)
        assertEquals("place-2", content.selectedPlaceId)
        assertEquals(2L, content.cameraFocusRequestId)
    }

    @Test
    fun `공유 코스 조회 실패는 오류 상태로 표시한다`() = runTest(dispatcher) {
        val viewModel = createViewModel(AppResult.Failure(AppError.ResourceNotFound))

        viewModel.load("missing-share")
        advanceUntilIdle()

        assertEquals(
            SharedCourseUiState.Error(AppError.ResourceNotFound),
            viewModel.uiState.value,
        )
    }

    private fun createViewModel(result: AppResult<SharedCourse>) = SharedCourseViewModel(
        getSharedCourse = GetSharedCourseUseCase(SharedCourseResultRepository(result)),
    )

    private class SharedCourseResultRepository(
        private val result: AppResult<SharedCourse>,
    ) : SharedCourseRepository {
        override suspend fun getSharedCourse(shareId: String): AppResult<SharedCourse> = result
    }

    private fun sharedCourse() = SharedCourse(
        shareId = "share-1",
        courseId = "course-1",
        regionName = "제주도",
        days = listOf(
            CourseDay(
                dayNumber = 1,
                places = listOf(
                    CoursePlace(
                        placeId = "place-1",
                        name = "첫 장소",
                        imageUrl = null,
                        tags = listOf("상시 개방"),
                        coordinate = MapCoordinate(33.0, 126.0),
                    ),
                ),
            ),
            CourseDay(
                dayNumber = 2,
                places = listOf(
                    CoursePlace(
                        placeId = "place-2",
                        name = "둘째 장소",
                        imageUrl = null,
                        tags = emptyList(),
                        coordinate = MapCoordinate(34.0, 127.0),
                    ),
                ),
            ),
        ),
    )
}
