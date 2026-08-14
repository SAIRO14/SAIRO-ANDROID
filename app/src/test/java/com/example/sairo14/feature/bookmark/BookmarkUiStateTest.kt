package com.example.sairo14.feature.bookmark

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookmarkUiStateTest {

    @Test
    fun `기본 상태는 저장되지 않았고 요청 중이 아니다`() {
        val state = BookmarkUiState()

        assertFalse(state.isSaved)
        assertNull(state.savedTripId)
        assertFalse(state.isRequesting)
    }

    @Test
    fun `저장 항목 ID는 저장 표시 상태를 결정하지 않는다`() {
        val state = BookmarkUiState(savedTripId = "saved-trip-1")

        assertFalse(state.isSaved)
        assertTrue(state.savedTripId != null)
    }
}
