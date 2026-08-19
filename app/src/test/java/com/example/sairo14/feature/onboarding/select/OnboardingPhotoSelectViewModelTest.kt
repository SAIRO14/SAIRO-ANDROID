package com.example.sairo14.feature.onboarding.select

import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.PhotoCandidate
import com.example.sairo14.domain.repository.PhotoSelectionRepository
import com.example.sairo14.domain.usecase.GetPhotoCandidatesUseCase
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingPhotoSelectViewModelTest {
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
    fun `사진은 선택 순서를 유지하고 최대 열 장까지만 선택한다`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val photoIds = photoCandidates.map(PhotoCandidate::id)
        photoIds.take(4).forEach(viewModel::togglePhotoSelection)
        assertFalse(viewModel.content().canComplete)

        viewModel.togglePhotoSelection(photoIds[4])
        assertTrue(viewModel.content().canComplete)

        photoIds.drop(5).take(5).forEach(viewModel::togglePhotoSelection)
        viewModel.togglePhotoSelection(photoIds[10])

        assertEquals(photoIds.take(10), viewModel.content().selectedPhotoIds)
        assertTrue(viewModel.content().hasReachedMaximumSelection)

        viewModel.removePhotoSelection(photoIds[2])
        assertEquals(photoIds.take(10).filterNot { id -> id == photoIds[2] }, viewModel.content().selectedPhotoIds)
        assertFalse(viewModel.content().hasReachedMaximumSelection)
    }

    @Test
    fun `사진 후보 마흔 장을 Pager에 전달할 콘텐츠 상태로 유지한다`() = runTest(dispatcher) {
        val candidates = List(40) { index -> photoCandidate(index + 1) }
        val viewModel = createViewModel(candidates)

        advanceUntilIdle()

        assertEquals(candidates, viewModel.content().photos.map(OnboardingPhotoUiModel::toPhotoCandidate))
    }

    @Test
    fun `확인한 사진은 순서대로 기록하고 중복이나 알 수 없는 사진은 추가하지 않는다`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.markPhotoViewed("photo-3")
        viewModel.markPhotoViewed("photo-1")
        viewModel.markPhotoViewed("photo-3")
        viewModel.markPhotoViewed("unknown")

        assertEquals(
            listOf("photo-3", "photo-1"),
            viewModel.content().viewedPhotoIds,
        )
    }

    @Test
    fun `완료 효과는 전체 선택 ID와 앞 다섯 장의 애니메이션 사진을 순서대로 전달한다`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val selectedIds = photoCandidates.take(6).map(PhotoCandidate::id).reversed()
        selectedIds.forEach(viewModel::togglePhotoSelection)
        val completion = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effect.first() }

        viewModel.completeSelection()

        val effect = completion.await() as OnboardingPhotoSelectEffect.SelectionCompleted
        assertEquals(
            selectedIds,
            effect.photoIds,
        )
        assertEquals(
            selectedIds.take(5),
            effect.animationPhotos.map(OnboardingPhotoUiModel::id),
        )
        assertEquals(
            selectedIds.take(5).map { id -> "https://example.com/$id.jpg" },
            effect.animationPhotos.map(OnboardingPhotoUiModel::imageUrl),
        )
    }

    private fun createViewModel(
        candidates: List<PhotoCandidate> = photoCandidates,
    ): OnboardingPhotoSelectViewModel =
        OnboardingPhotoSelectViewModel(
            getPhotoCandidates = GetPhotoCandidatesUseCase(
                photoSelectionRepository = StaticPhotoSelectionRepository(candidates),
            ),
        )

    private fun OnboardingPhotoSelectViewModel.content(): OnboardingPhotoSelectUiState.Content =
        uiState.value as OnboardingPhotoSelectUiState.Content

    private class StaticPhotoSelectionRepository(
        private val candidates: List<PhotoCandidate>,
    ) : PhotoSelectionRepository {
        override suspend fun getPhotoCandidates(limit: Int): AppResult<List<PhotoCandidate>> =
            AppResult.Success(candidates)
    }

    private companion object {
        val photoCandidates = List(11) { index -> photoCandidate(index + 1) }

        fun photoCandidate(index: Int): PhotoCandidate =
            PhotoCandidate(
                id = "photo-$index",
                imageUrl = "https://example.com/photo-$index.jpg",
                contentDescription = null,
            )
    }
}

private fun OnboardingPhotoUiModel.toPhotoCandidate(): PhotoCandidate =
    PhotoCandidate(
        id = id,
        imageUrl = imageUrl,
        contentDescription = contentDescription,
    )
