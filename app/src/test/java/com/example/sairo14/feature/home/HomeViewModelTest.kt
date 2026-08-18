package com.example.sairo14.feature.home

import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.DiscoveryImages
import com.example.sairo14.domain.model.HomeContent
import com.example.sairo14.domain.model.SavedTripSummary
import com.example.sairo14.domain.repository.HomeRepository
import com.example.sairo14.domain.usecase.GetHomeContentUseCase
import com.example.sairo14.core.dummyimage.SeasonalDummyImageProvider
import com.example.sairo14.feature.bookmark.BookmarkChange
import com.example.sairo14.feature.bookmark.BookmarkChangeNotifier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
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
    fun `저장 변경 알림을 받으면 로딩 화면 없이 최신 홈 콘텐츠로 교체한다`() = runTest(dispatcher) {
        val repository = RecordingHomeRepository(
            results = listOf(
                AppResult.Success(homeContent("saved-1")),
                AppResult.Success(homeContent("saved-2")),
            ),
        )
        val notifier = BookmarkChangeNotifier()
        val viewModel = createViewModel(repository, notifier)
        advanceUntilIdle()

        val refreshStates = mutableListOf<HomeUiState>()
        val stateCollection = launch {
            viewModel.uiState.collect { state -> refreshStates += state }
        }
        runCurrent()
        notifier.notify(savedChange())
        advanceUntilIdle()

        assertEquals(listOf("saved-2"), viewModel.content().savedTrips.map { it.savedTripId })
        assertEquals(2, repository.requestCount)
        assertFalse(refreshStates.any { it == HomeUiState.Loading })
        stateCollection.cancel()
    }

    @Test
    fun `백그라운드 갱신이 실패하면 기존 홈 콘텐츠를 유지한다`() = runTest(dispatcher) {
        val repository = RecordingHomeRepository(
            results = listOf(
                AppResult.Success(homeContent("saved-1")),
                AppResult.Failure(AppError.NetworkUnavailable),
            ),
        )
        val notifier = BookmarkChangeNotifier()
        val viewModel = createViewModel(repository, notifier)
        advanceUntilIdle()

        notifier.notify(savedChange())
        advanceUntilIdle()

        assertEquals(listOf("saved-1"), viewModel.content().savedTrips.map { it.savedTripId })
        assertEquals(2, repository.requestCount)
    }

    @Test
    fun `연속 변경 알림에서 늦은 이전 응답은 최신 홈 콘텐츠를 덮어쓰지 않는다`() = runTest(dispatcher) {
        val repository = OutOfOrderHomeRepository()
        val notifier = BookmarkChangeNotifier()
        val viewModel = createViewModel(repository, notifier)
        advanceUntilIdle()

        notifier.notify(savedChange())
        repository.firstRefreshStarted.await()
        notifier.notify(savedChange())
        runCurrent()

        repository.secondRefresh.complete(AppResult.Success(homeContent("saved-latest")))
        advanceUntilIdle()
        assertEquals(listOf("saved-latest"), viewModel.content().savedTrips.map { it.savedTripId })

        repository.firstRefresh.complete(AppResult.Success(homeContent("saved-stale")))
        advanceUntilIdle()
        assertEquals(listOf("saved-latest"), viewModel.content().savedTrips.map { it.savedTripId })
    }

    private fun createViewModel(
        repository: HomeRepository,
        bookmarkChangeNotifier: BookmarkChangeNotifier,
    ) = HomeViewModel(
        getHomeContent = GetHomeContentUseCase(repository),
        bookmarkChangeNotifier = bookmarkChangeNotifier,
        seasonalDummyImageProvider = SeasonalDummyImageProvider(),
    )

    private fun HomeViewModel.content(): HomeUiState.Content =
        uiState.value as? HomeUiState.Content
            ?: error("홈 콘텐츠 상태를 기대했습니다.")

    private class RecordingHomeRepository(
        results: List<AppResult<HomeContent>>,
    ) : HomeRepository {
        private val results = ArrayDeque(results)
        var requestCount = 0
            private set

        override suspend fun getHomeContent(): AppResult<HomeContent> {
            requestCount += 1
            return results.removeFirst()
        }
    }

    private class OutOfOrderHomeRepository : HomeRepository {
        val firstRefreshStarted = CompletableDeferred<Unit>()
        val firstRefresh = CompletableDeferred<AppResult<HomeContent>>()
        val secondRefresh = CompletableDeferred<AppResult<HomeContent>>()
        private var requestCount = 0

        override suspend fun getHomeContent(): AppResult<HomeContent> = when (++requestCount) {
            1 -> AppResult.Success(homeContent("saved-initial"))
            2 -> withContext(NonCancellable) {
                firstRefreshStarted.complete(Unit)
                firstRefresh.await()
            }

            3 -> secondRefresh.await()
            else -> error("예상하지 않은 홈 조회입니다.")
        }
    }
}

private fun homeContent(savedTripId: String): HomeContent = HomeContent(
    discoveryImages = DiscoveryImages(
        backImageUrl = null,
        frontImageUrl = null,
    ),
    savedTrips = listOf(
        SavedTripSummary(
            savedTripId = savedTripId,
            courseId = "course-$savedTripId",
            regionName = "제주",
            thumbnailImageUrl = null,
        ),
    ),
)

private fun savedChange(): BookmarkChange = BookmarkChange(
    courseId = "course-1",
    isSaved = true,
    savedTripId = "saved-trip-1",
)
