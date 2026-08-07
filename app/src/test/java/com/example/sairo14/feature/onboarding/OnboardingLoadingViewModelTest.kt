package com.example.sairo14.feature.onboarding

import com.example.sairo14.data.repository.FakePhotoSelectionRepository
import com.example.sairo14.domain.usecase.GetPhotoCandidatesUseCase
import com.example.sairo14.feature.onboarding.loading.OnboardingLoadingPhotoUiModel
import com.example.sairo14.feature.onboarding.loading.OnboardingLoadingUiState
import com.example.sairo14.feature.onboarding.loading.OnboardingLoadingViewModel
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
class OnboardingLoadingViewModelTest {
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
    fun `선택한 사진 ID 순서대로 로딩 카드 정보를 복원한다`() = runTest(dispatcher) {
        val viewModel = OnboardingLoadingViewModel(
            getPhotoCandidates = GetPhotoCandidatesUseCase(FakePhotoSelectionRepository()),
        )
        val selectedIds = listOf(
            "photo-quiet-lake",
            "photo-jeju-coast",
            "photo-forest-cabin",
            "photo-cafe-window",
            "photo-autumn-road",
        )

        viewModel.load(selectedIds)
        advanceUntilIdle()

        val content = viewModel.uiState.value as OnboardingLoadingUiState.Content
        assertEquals(selectedIds, content.photos.map(OnboardingLoadingPhotoUiModel::id))
    }

    @Test
    fun `다섯 장보다 적은 선택은 오류 상태로 표시한다`() = runTest(dispatcher) {
        val viewModel = OnboardingLoadingViewModel(
            getPhotoCandidates = GetPhotoCandidatesUseCase(FakePhotoSelectionRepository()),
        )

        viewModel.load(listOf("photo-jeju-coast", "photo-quiet-lake"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is OnboardingLoadingUiState.Error)
    }
}
