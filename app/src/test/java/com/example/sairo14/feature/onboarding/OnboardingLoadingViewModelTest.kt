package com.example.sairo14.feature.onboarding

import com.example.sairo14.core.navigation.OnboardingAnimationPhoto
import com.example.sairo14.feature.onboarding.loading.OnboardingLoadingPhotoUiModel
import com.example.sairo14.feature.onboarding.loading.OnboardingLoadingCardCount
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
    fun `전달된 사진 순서대로 로딩 카드 정보를 구성한다`() = runTest(dispatcher) {
        val viewModel = OnboardingLoadingViewModel()
        val selectedPhotos = List(OnboardingLoadingCardCount) { index ->
            OnboardingAnimationPhoto(
                id = "photo-${index + 1}",
                imageUrl = "https://example.com/photo-${index + 1}.jpg",
            )
        }

        viewModel.load(selectedPhotos)
        advanceUntilIdle()

        val content = viewModel.uiState.value as OnboardingLoadingUiState.Content
        assertEquals(
            selectedPhotos.map(OnboardingAnimationPhoto::id),
            content.photos.map(OnboardingLoadingPhotoUiModel::id),
        )
        assertEquals(
            selectedPhotos.map(OnboardingAnimationPhoto::imageUrl),
            content.photos.map(OnboardingLoadingPhotoUiModel::imageUrl),
        )
    }

    @Test
    fun `다섯 장보다 적은 선택은 오류 상태로 표시한다`() = runTest(dispatcher) {
        val viewModel = OnboardingLoadingViewModel()

        viewModel.load(
            listOf(
                OnboardingAnimationPhoto(id = "photo-1", imageUrl = "https://example.com/photo-1.jpg"),
                OnboardingAnimationPhoto(id = "photo-2", imageUrl = "https://example.com/photo-2.jpg"),
            ),
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is OnboardingLoadingUiState.Error)
    }
}
