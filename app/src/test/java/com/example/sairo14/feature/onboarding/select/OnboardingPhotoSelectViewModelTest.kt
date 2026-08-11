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
    fun `완료 효과는 선택한 사진 ID를 선택 순서대로 전달한다`() = runTest(dispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val selectedIds = photoCandidates.take(5).map(PhotoCandidate::id).reversed()
        selectedIds.forEach(viewModel::togglePhotoSelection)
        val completion = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effect.first() }

        viewModel.completeSelection()

        assertEquals(
            selectedIds,
            (completion.await() as OnboardingPhotoSelectEffect.SelectionCompleted).photoIds,
        )
    }

    private fun createViewModel(): OnboardingPhotoSelectViewModel =
        OnboardingPhotoSelectViewModel(
            getPhotoCandidates = GetPhotoCandidatesUseCase(
                photoSelectionRepository = StaticPhotoSelectionRepository(),
            ),
        )

    private fun OnboardingPhotoSelectViewModel.content(): OnboardingPhotoSelectUiState.Content =
        uiState.value as OnboardingPhotoSelectUiState.Content

    private class StaticPhotoSelectionRepository : PhotoSelectionRepository {
        override suspend fun getPhotoCandidates(limit: Int): AppResult<List<PhotoCandidate>> =
            AppResult.Success(photoCandidates)
    }

    private companion object {
        val photoCandidates = List(11) { index ->
            PhotoCandidate(
                id = "photo-${index + 1}",
                imageUrl = "https://example.com/photo-${index + 1}.jpg",
                contentDescription = null,
            )
        }
    }
}
