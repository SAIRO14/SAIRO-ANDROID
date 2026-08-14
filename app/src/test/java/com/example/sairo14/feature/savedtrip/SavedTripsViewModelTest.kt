package com.example.sairo14.feature.savedtrip

import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SavedTrip
import com.example.sairo14.domain.model.SavedTripPage
import com.example.sairo14.domain.model.SavedTripSaveResult
import com.example.sairo14.domain.repository.SavedTripRepository
import com.example.sairo14.domain.usecase.DeleteSavedTripUseCase
import com.example.sairo14.domain.usecase.GetSavedTripsUseCase
import com.example.sairo14.feature.bookmark.BookmarkChange
import com.example.sairo14.feature.bookmark.BookmarkChangeNotifier
import java.util.ArrayDeque
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavedTripsViewModelTest {
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
    fun `첫 페이지 결과를 콘텐츠와 다음 커서로 표시한다`() = runTest(dispatcher) {
        val repository = RecordingSavedTripRepository(
            results = listOf(AppResult.Success(page(listOf(trip("saved-1")), "cursor-1"))),
        )
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        val content = viewModel.content()
        assertEquals(listOf("saved-1"), content.trips.map { it.savedTripId })
        assertEquals("cursor-1", content.nextCursor)
        assertFalse(content.isLoadingMore)
        assertNull(content.loadMoreError)
        assertEquals(listOf(null), repository.requestedCursors)
    }

    @Test
    fun `다음 페이지를 추가하고 savedTripId 기준 중복 항목을 제거한다`() = runTest(dispatcher) {
        val repository = RecordingSavedTripRepository(
            results = listOf(
                AppResult.Success(page(listOf(trip("saved-1")), "cursor-1")),
                AppResult.Success(page(listOf(trip("saved-1"), trip("saved-2")), null)),
            ),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        val content = viewModel.content()
        assertEquals(listOf("saved-1", "saved-2"), content.trips.map { it.savedTripId })
        assertNull(content.nextCursor)
        assertEquals(listOf(null, "cursor-1"), repository.requestedCursors)
    }

    @Test
    fun `다음 커서가 없으면 추가 요청을 보내지 않는다`() = runTest(dispatcher) {
        val repository = RecordingSavedTripRepository(
            results = listOf(AppResult.Success(page(listOf(trip("saved-1")), null))),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        assertEquals(listOf(null), repository.requestedCursors)
    }

    @Test
    fun `추가 조회 실패 시 기존 목록과 커서를 유지하고 하단 오류를 표시한다`() = runTest(dispatcher) {
        val repository = RecordingSavedTripRepository(
            results = listOf(
                AppResult.Success(page(listOf(trip("saved-1")), "cursor-1")),
                AppResult.Failure(AppError.NetworkUnavailable),
            ),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        val content = viewModel.content()
        assertEquals(listOf("saved-1"), content.trips.map { it.savedTripId })
        assertEquals("cursor-1", content.nextCursor)
        assertFalse(content.isLoadingMore)
        assertEquals(AppError.NetworkUnavailable, content.loadMoreError)
    }

    @Test
    fun `잘못된 커서는 첫 페이지를 다시 조회한다`() = runTest(dispatcher) {
        val repository = RecordingSavedTripRepository(
            results = listOf(
                AppResult.Success(page(listOf(trip("saved-1")), "cursor-1")),
                AppResult.Failure(AppError.InvalidCursor),
                AppResult.Success(page(listOf(trip("saved-2")), null)),
            ),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.loadMore()
        advanceUntilIdle()

        val content = viewModel.content()
        assertEquals(listOf("saved-2"), content.trips.map { it.savedTripId })
        assertNull(content.nextCursor)
        assertEquals(listOf(null, "cursor-1", null), repository.requestedCursors)
    }

    @Test
    fun `추가 조회 중에는 같은 커서 요청을 한 번만 보낸다`() = runTest(dispatcher) {
        val nextPageResult = CompletableDeferred<AppResult<SavedTripPage>>()
        val repository = BlockingSavedTripRepository(nextPageResult)
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.loadMore()
        viewModel.loadMore()
        runCurrent()

        assertEquals(listOf(null, "cursor-1"), repository.requestedCursors)
        assertTrue(viewModel.content().isLoadingMore)

        nextPageResult.complete(AppResult.Success(page(listOf(trip("saved-2")), null)))
        advanceUntilIdle()
    }

    @Test
    fun `저장 목록에서 해제 성공 후 첫 페이지로 다시 동기화한다`() = runTest(dispatcher) {
        val repository = RecordingSavedTripRepository(
            results = listOf(
                AppResult.Success(page(listOf(trip("saved-1"), trip("saved-2")), "cursor-1")),
                AppResult.Success(page(listOf(trip("saved-2")), null)),
            ),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.removeSavedTrip("saved-1")
        advanceUntilIdle()

        val content = viewModel.content()
        assertEquals(listOf("saved-2"), content.trips.map { it.savedTripId })
        assertNull(content.nextCursor)
        assertEquals(listOf(null, null), repository.requestedCursors)
    }

    @Test
    fun `삭제 뒤 재조회가 실패해도 즉시 제거한 목록을 유지한다`() = runTest(dispatcher) {
        val repository = RecordingSavedTripRepository(
            results = listOf(
                AppResult.Success(page(listOf(trip("saved-1"), trip("saved-2")), "cursor-1")),
                AppResult.Failure(AppError.NetworkUnavailable),
            ),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.removeSavedTrip("saved-1")
        advanceUntilIdle()

        val content = viewModel.content()
        assertEquals(listOf("saved-2"), content.trips.map { it.savedTripId })
        assertEquals("cursor-1", content.nextCursor)
        assertEquals(listOf(null, null), repository.requestedCursors)
    }

    @Test
    fun `상세 화면의 저장 해제 알림을 받으면 목록을 제거하고 첫 페이지를 다시 조회한다`() =
        runTest(dispatcher) {
            val notifier = BookmarkChangeNotifier()
            val repository = RecordingSavedTripRepository(
                results = listOf(
                    AppResult.Success(page(listOf(trip("saved-1"), trip("saved-2")), "cursor-1")),
                    AppResult.Success(page(listOf(trip("saved-2")), null)),
                ),
            )
            val viewModel = createViewModel(repository, notifier)
            advanceUntilIdle()

            notifier.notify(
                BookmarkChange(
                    courseId = "course-saved-1",
                    isSaved = false,
                    savedTripId = null,
                ),
            )
            advanceUntilIdle()

            val content = viewModel.content()
            assertEquals(listOf("saved-2"), content.trips.map { it.savedTripId })
            assertNull(content.nextCursor)
            assertEquals(listOf(null, null), repository.requestedCursors)
        }

    private fun createViewModel(
        repository: SavedTripRepository,
        bookmarkChangeNotifier: BookmarkChangeNotifier = BookmarkChangeNotifier(),
    ) = SavedTripsViewModel(
        getSavedTrips = GetSavedTripsUseCase(repository),
        deleteSavedTrip = DeleteSavedTripUseCase(repository),
        bookmarkChangeNotifier = bookmarkChangeNotifier,
    )

    private fun SavedTripsViewModel.content(): SavedTripsUiState.Content =
        uiState.value as? SavedTripsUiState.Content
            ?: error("저장 목록 콘텐츠 상태를 기대했습니다.")

    private class RecordingSavedTripRepository(
        results: List<AppResult<SavedTripPage>>,
    ) : SavedTripRepository {
        private val results = ArrayDeque(results)
        val requestedCursors = mutableListOf<String?>()

        override suspend fun getSavedTrips(
            cursor: String?,
            size: Int,
        ): AppResult<SavedTripPage> {
            requestedCursors += cursor
            return results.removeFirst()
        }

        override suspend fun saveTrip(courseId: String): AppResult<SavedTripSaveResult> =
            AppResult.Success(SavedTripSaveResult("saved-$courseId", courseId))

        override suspend fun deleteSavedTrip(savedTripId: String): AppResult<Unit> =
            AppResult.Success(Unit)
    }

    private class BlockingSavedTripRepository(
        private val nextPageResult: CompletableDeferred<AppResult<SavedTripPage>>,
    ) : SavedTripRepository {
        val requestedCursors = mutableListOf<String?>()

        override suspend fun getSavedTrips(
            cursor: String?,
            size: Int,
        ): AppResult<SavedTripPage> {
            requestedCursors += cursor
            return if (cursor == null) {
                AppResult.Success(page(listOf(trip("saved-1")), "cursor-1"))
            } else {
                nextPageResult.await()
            }
        }

        override suspend fun saveTrip(courseId: String): AppResult<SavedTripSaveResult> =
            AppResult.Success(SavedTripSaveResult("saved-$courseId", courseId))

        override suspend fun deleteSavedTrip(savedTripId: String): AppResult<Unit> =
            AppResult.Success(Unit)
    }

    private companion object {
        fun page(items: List<SavedTrip>, nextCursor: String?) = SavedTripPage(items, nextCursor)

        fun trip(savedTripId: String) = SavedTrip(
            savedTripId = savedTripId,
            courseId = "course-$savedTripId",
            regionName = "제주",
            regionArea = null,
            imageUrl = null,
            reason = null,
            createdAt = "2026-08-14T10:00:00Z",
        )
    }
}
