package com.example.sairo14.data.mapper

import com.example.sairo14.data.remote.dto.SavedTripSaveResponseDto
import com.example.sairo14.domain.model.SavedTripSaveResult
import org.junit.Assert.assertEquals
import org.junit.Test

class SavedTripMapperTest {

    @Test
    fun `저장 응답의 식별자를 Domain 결과로 보존한다`() {
        val result = SavedTripSaveResponseDto(
            savedTripId = "saved-trip-1",
            courseId = "course-1",
        ).toDomain()

        assertEquals(
            SavedTripSaveResult(savedTripId = "saved-trip-1", courseId = "course-1"),
            result,
        )
    }
}
