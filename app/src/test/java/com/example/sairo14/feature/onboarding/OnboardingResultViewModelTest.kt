package com.example.sairo14.feature.onboarding

import com.example.sairo14.data.repository.InMemoryOnboardingAnalysisSessionStore
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.OnboardingAnalysisResult
import com.example.sairo14.domain.model.OnboardingRecommendation
import com.example.sairo14.domain.model.OnboardingCompletionToken
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.SavedTripPage
import com.example.sairo14.domain.model.SavedTripSaveResult
import com.example.sairo14.domain.repository.OnboardingRepository
import com.example.sairo14.domain.repository.SavedTripRepository
import com.example.sairo14.domain.usecase.CreateOnboardingCompletionRequestUseCase
import com.example.sairo14.domain.usecase.DeleteSavedTripUseCase
import com.example.sairo14.domain.usecase.SaveTripUseCase
import com.example.sairo14.domain.usecase.UpdateOnboardingCompletionUseCase
import com.example.sairo14.feature.bookmark.BookmarkEffect
import com.example.sairo14.feature.bookmark.BookmarkChange
import com.example.sairo14.feature.bookmark.BookmarkChangeNotifier
import com.example.sairo14.feature.onboarding.result.OnboardingResultUiState
import com.example.sairo14.feature.onboarding.result.OnboardingResultViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingResultViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `세션의 추천 결과를 표시한다`() = runTest(dispatcher) {
        val store = InMemoryOnboardingAnalysisSessionStore()
        val recommendations = listOf(OnboardingRecommendation("id", "course", "제주도", "설명", emptyList(), emptyList()))
        val token = store.beginRequest("session-1")
        store.saveIfCurrent(
            searchSessionId = "session-1",
            token = token,
            result = OnboardingAnalysisResult(emptyList(), "", recommendations, emptyMap()),
        )
        val repository = OnboardingRepo()
        val viewModel = OnboardingResultViewModel(
            sessionStore = store,
            createOnboardingCompletionRequest = CreateOnboardingCompletionRequestUseCase(repository),
            updateOnboardingCompletion = UpdateOnboardingCompletionUseCase(repository),
            saveTripUseCase = SaveTripUseCase(SavedTripRepo()),
            deleteSavedTripUseCase = DeleteSavedTripUseCase(SavedTripRepo()),
            bookmarkChangeNotifier = BookmarkChangeNotifier(),
        )

        viewModel.load("session-1")
        advanceUntilIdle()

        val content = viewModel.uiState.value as OnboardingResultUiState.Content
        assertEquals(recommendations, content.recommendations)
        assertTrue(content.bookmarks.getValue("course").isSaved.not())
    }

    @Test fun `최신 세션 완료 상태 저장이 실패해도 이전 세션은 완료 상태를 덮어쓰지 않는다`() = runTest(dispatcher) {
        val store = InMemoryOnboardingAnalysisSessionStore()
        store.saveResult("session-A", recommendations = listOf(recommendation("A")))
        store.saveResult("session-B", recommendations = emptyList())
        val repository = DelayedOnboardingRepository()
        val viewModel = OnboardingResultViewModel(
            sessionStore = store,
            createOnboardingCompletionRequest = CreateOnboardingCompletionRequestUseCase(repository),
            updateOnboardingCompletion = UpdateOnboardingCompletionUseCase(repository),
            saveTripUseCase = SaveTripUseCase(SavedTripRepo()),
            deleteSavedTripUseCase = DeleteSavedTripUseCase(SavedTripRepo()),
            bookmarkChangeNotifier = BookmarkChangeNotifier(),
        )

        viewModel.load("session-A")
        runCurrent()
        repository.awaitFirstCompletionUpdate()

        viewModel.load("session-B")
        runCurrent()
        repository.completeFirstUpdate()
        advanceUntilIdle()

        assertEquals(OnboardingResultUiState.Error, viewModel.uiState.value)
        assertEquals(false, repository.completed)
    }

    @Test fun `저장 성공 후 응답의 savedTripId를 카드 상태에 보관한다`() = runTest(dispatcher) {
        val savedTripRepo = SavedTripRepo(
            saveResult = AppResult.Success(
                SavedTripSaveResult(savedTripId = "saved-trip-1", courseId = "unexpected-course"),
            ),
        )
        val notifier = BookmarkChangeNotifier()
        val viewModel = createLoadedViewModel(
            recommendations = listOf(recommendation("1")),
            savedTripRepo = savedTripRepo,
            bookmarkChangeNotifier = notifier,
        )
        val change = async { notifier.changes.first() }
        runCurrent()

        viewModel.onBookmarkClick("course-1")
        advanceUntilIdle()

        val bookmark = viewModel.content().bookmarks.getValue("course-1")
        assertTrue(bookmark.isSaved)
        assertEquals("saved-trip-1", bookmark.savedTripId)
        assertFalse(bookmark.isRequesting)
        assertEquals(listOf("course-1"), savedTripRepo.savedCourseIds)
        assertEquals(
            BookmarkChange("course-1", isSaved = true, savedTripId = "saved-trip-1"),
            change.await(),
        )
    }

    @Test fun `저장 실패 시 기존 미체크 상태를 유지하고 오류 효과를 전달한다`() = runTest(dispatcher) {
        val savedTripRepo = SavedTripRepo(
            saveResult = AppResult.Failure(AppError.NetworkUnavailable),
        )
        val notifier = BookmarkChangeNotifier()
        val viewModel = createLoadedViewModel(
            recommendations = listOf(recommendation("1")),
            savedTripRepo = savedTripRepo,
            bookmarkChangeNotifier = notifier,
        )
        val effect = async { viewModel.bookmarkEffect.first() }
        val change = async { notifier.changes.first() }
        runCurrent()

        viewModel.onBookmarkClick("course-1")
        advanceUntilIdle()

        val bookmark = viewModel.content().bookmarks.getValue("course-1")
        assertFalse(bookmark.isSaved)
        assertNull(bookmark.savedTripId)
        assertFalse(bookmark.isRequesting)
        assertEquals(BookmarkEffect.ShowError(AppError.NetworkUnavailable), effect.await())
        assertFalse(change.isCompleted)
        change.cancel()
    }

    @Test fun `삭제 성공 후 체크 상태와 savedTripId를 제거한다`() = runTest(dispatcher) {
        val savedTripRepo = SavedTripRepo()
        val notifier = BookmarkChangeNotifier()
        val viewModel = createLoadedViewModel(
            recommendations = listOf(recommendation("1")),
            savedTripRepo = savedTripRepo,
            bookmarkChangeNotifier = notifier,
        )

        viewModel.onBookmarkClick("course-1")
        advanceUntilIdle()
        val change = async { notifier.changes.first() }
        runCurrent()
        viewModel.onBookmarkClick("course-1")
        advanceUntilIdle()

        val bookmark = viewModel.content().bookmarks.getValue("course-1")
        assertFalse(bookmark.isSaved)
        assertNull(bookmark.savedTripId)
        assertEquals(listOf("saved-trip-1"), savedTripRepo.deletedSavedTripIds)
        assertEquals(
            BookmarkChange("course-1", isSaved = false, savedTripId = null),
            change.await(),
        )
    }

    @Test fun `삭제 실패 시 기존 체크 상태와 savedTripId를 유지한다`() = runTest(dispatcher) {
        val savedTripRepo = SavedTripRepo(deleteResult = AppResult.Failure(AppError.NetworkUnavailable))
        val viewModel = createLoadedViewModel(
            recommendations = listOf(recommendation("1")),
            savedTripRepo = savedTripRepo,
        )

        viewModel.onBookmarkClick("course-1")
        advanceUntilIdle()
        viewModel.onBookmarkClick("course-1")
        advanceUntilIdle()

        val bookmark = viewModel.content().bookmarks.getValue("course-1")
        assertTrue(bookmark.isSaved)
        assertEquals("saved-trip-1", bookmark.savedTripId)
        assertFalse(bookmark.isRequesting)
    }

    @Test fun `요청 중 연속 클릭은 저장 API를 한 번만 호출한다`() = runTest(dispatcher) {
        val savedTripRepo = SavedTripRepo()
        val viewModel = createLoadedViewModel(
            recommendations = listOf(recommendation("1")),
            savedTripRepo = savedTripRepo,
        )

        viewModel.onBookmarkClick("course-1")
        viewModel.onBookmarkClick("course-1")
        advanceUntilIdle()

        assertEquals(listOf("course-1"), savedTripRepo.savedCourseIds)
    }

    @Test fun `savedTripId 없는 체크 상태에서는 삭제 요청을 보내지 않는다`() = runTest(dispatcher) {
        val savedTripRepo = SavedTripRepo()
        val viewModel = createLoadedViewModel(
            recommendations = listOf(recommendation("1").copy(isSaved = true)),
            savedTripRepo = savedTripRepo,
        )

        viewModel.onBookmarkClick("course-1")
        advanceUntilIdle()

        val bookmark = viewModel.content().bookmarks.getValue("course-1")
        assertTrue(bookmark.isSaved)
        assertNull(bookmark.savedTripId)
        assertTrue(savedTripRepo.deletedSavedTripIds.isEmpty())
    }

    @Test fun `상세 화면의 성공 결과는 같은 코스의 추천 카드에만 반영한다`() = runTest(dispatcher) {
        val notifier = BookmarkChangeNotifier()
        val viewModel = createLoadedViewModel(
            recommendations = listOf(recommendation("1"), recommendation("2")),
            savedTripRepo = SavedTripRepo(),
            bookmarkChangeNotifier = notifier,
        )

        notifier.notify(
            BookmarkChange(
                courseId = "course-1",
                isSaved = true,
                savedTripId = "saved-trip-from-detail",
            ),
        )
        runCurrent()

        assertEquals(
            "saved-trip-from-detail",
            viewModel.content().bookmarks.getValue("course-1").savedTripId,
        )
        assertFalse(viewModel.content().bookmarks.getValue("course-2").isSaved)
    }

    private class OnboardingRepo : OnboardingRepository {
        private var token = 0L
        override suspend fun getHasCompletedOnboarding() = AppResult.Success(false)
        override suspend fun createCompletionRequest() = AppResult.Success(
            OnboardingCompletionToken(++token),
        )
        override suspend fun updateCompletionIfCurrent(
            token: OnboardingCompletionToken,
            completed: Boolean,
        ) = AppResult.Success(true)
    }

    private class DelayedOnboardingRepository : OnboardingRepository {
        private val firstUpdateStarted = CompletableDeferred<Unit>()
        private val firstUpdateGate = CompletableDeferred<Unit>()
        private var updateCount = 0
        private var latestToken = 0L
        var completed: Boolean = false
            private set

        override suspend fun getHasCompletedOnboarding() = AppResult.Success(completed)

        override suspend fun createCompletionRequest() = AppResult.Success(
            OnboardingCompletionToken(++latestToken),
        )

        override suspend fun updateCompletionIfCurrent(
            token: OnboardingCompletionToken,
            completed: Boolean,
        ): AppResult<Boolean> = update(token, completed)

        suspend fun awaitFirstCompletionUpdate() {
            firstUpdateStarted.await()
        }

        fun completeFirstUpdate() {
            firstUpdateGate.complete(Unit)
        }

        private suspend fun update(
            token: OnboardingCompletionToken,
            nextCompleted: Boolean,
        ): AppResult<Boolean> = withContext(NonCancellable) {
            updateCount += 1
            if (updateCount == 1) {
                firstUpdateStarted.complete(Unit)
                firstUpdateGate.await()
            }
            if (token.value != latestToken) return@withContext AppResult.Success(false)
            if (updateCount == 2) return@withContext AppResult.Failure(AppError.StorageUnavailable)

            completed = nextCompleted
            AppResult.Success(true)
        }
    }

    private suspend fun TestScope.createLoadedViewModel(
        recommendations: List<OnboardingRecommendation>,
        savedTripRepo: SavedTripRepo,
        bookmarkChangeNotifier: BookmarkChangeNotifier = BookmarkChangeNotifier(),
    ): OnboardingResultViewModel {
        val store = InMemoryOnboardingAnalysisSessionStore()
        store.saveResult(searchSessionId = "session-1", recommendations = recommendations)
        return OnboardingResultViewModel(
            sessionStore = store,
            createOnboardingCompletionRequest = CreateOnboardingCompletionRequestUseCase(OnboardingRepo()),
            updateOnboardingCompletion = UpdateOnboardingCompletionUseCase(OnboardingRepo()),
            saveTripUseCase = SaveTripUseCase(savedTripRepo),
            deleteSavedTripUseCase = DeleteSavedTripUseCase(savedTripRepo),
            bookmarkChangeNotifier = bookmarkChangeNotifier,
        ).also { viewModel ->
            viewModel.load("session-1")
            advanceUntilIdle()
        }
    }

    private fun OnboardingResultViewModel.content(): OnboardingResultUiState.Content =
        uiState.value as? OnboardingResultUiState.Content
            ?: error("추천 결과 콘텐츠 상태를 기대했습니다.")

    private class SavedTripRepo(
        var saveResult: AppResult<SavedTripSaveResult> = AppResult.Success(
            SavedTripSaveResult(savedTripId = "saved-trip-1", courseId = "course-1"),
        ),
        var deleteResult: AppResult<Unit> = AppResult.Success(Unit),
    ) : SavedTripRepository {
        val savedCourseIds = mutableListOf<String>()
        val deletedSavedTripIds = mutableListOf<String>()

        override suspend fun saveTrip(courseId: String): AppResult<SavedTripSaveResult> {
            savedCourseIds += courseId
            return saveResult
        }

        override suspend fun getSavedTrips(
            cursor: String?,
            size: Int,
        ): AppResult<SavedTripPage> = AppResult.Success(SavedTripPage(emptyList(), null))

        override suspend fun deleteSavedTrip(savedTripId: String): AppResult<Unit> {
            deletedSavedTripIds += savedTripId
            return deleteResult
        }
    }

    private suspend fun InMemoryOnboardingAnalysisSessionStore.saveResult(
        searchSessionId: String,
        recommendations: List<OnboardingRecommendation>,
    ) {
        val token = beginRequest(searchSessionId)
        saveIfCurrent(
            searchSessionId = searchSessionId,
            token = token,
            result = OnboardingAnalysisResult(emptyList(), "", recommendations, emptyMap()),
        )
    }

    private fun recommendation(id: String) = OnboardingRecommendation(
        id = id,
        courseId = "course-$id",
        regionName = "제주도",
        description = "설명",
        imageUrls = emptyList(),
        placeNames = emptyList(),
    )
}
