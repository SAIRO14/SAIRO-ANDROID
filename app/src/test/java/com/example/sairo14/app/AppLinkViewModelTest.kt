package com.example.sairo14.app

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppLinkViewModelTest {

    @Test
    fun `유효한 공유 URL은 대기 중인 공유 코스 ID로 보관한다`() {
        val viewModel = AppLinkViewModel(SavedStateHandle())

        viewModel.handleUrl("https://sairo.app/shared/7429b36b9d")

        assertEquals("7429b36b9d", viewModel.pendingSharedCourseId.value)
    }

    @Test
    fun `유효하지 않은 URL은 대기 중인 공유 코스 이동을 만들지 않는다`() {
        val viewModel = AppLinkViewModel(SavedStateHandle())

        viewModel.handleUrl("https://example.com/shared/7429b36b9d")

        assertNull(viewModel.pendingSharedCourseId.value)
    }

    @Test
    fun `소비한 공유 코스 ID만 대기 상태에서 제거한다`() {
        val viewModel = AppLinkViewModel(SavedStateHandle())
        viewModel.handleUrl("https://sairo.app/shared/share-1")

        viewModel.consumeSharedCourse("other-share")
        assertEquals("share-1", viewModel.pendingSharedCourseId.value)

        viewModel.consumeSharedCourse("share-1")
        assertNull(viewModel.pendingSharedCourseId.value)
    }
}
