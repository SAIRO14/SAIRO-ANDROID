package com.example.sairo14.data.mapper

import com.example.sairo14.data.remote.dto.SavedTripListResponseDto
import com.example.sairo14.data.remote.dto.SavedTripResponseDto
import com.example.sairo14.data.remote.dto.SavedTripSaveResponseDto
import com.example.sairo14.domain.model.SavedTrip
import com.example.sairo14.domain.model.SavedTripPage
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

    @Test
    fun `목록 응답의 nullable 값과 다음 커서를 보존한다`() {
        val result = SavedTripListResponseDto(
            items = listOf(
                SavedTripResponseDto(
                    savedTripId = "saved-trip-1",
                    courseId = "course-1",
                    regionName = "제주",
                    regionArea = null,
                    imageUrl = null,
                    reason = null,
                    createdAt = "2026-08-14T10:00:00Z",
                ),
            ),
            nextCursor = "next-cursor",
        ).toDomain()

        assertEquals(
            SavedTripPage(
                items = listOf(
                    SavedTrip(
                        savedTripId = "saved-trip-1",
                        courseId = "course-1",
                        regionName = "제주",
                        regionArea = null,
                        imageUrl = null,
                        reason = null,
                        createdAt = "2026-08-14T10:00:00Z",
                    ),
                ),
                nextCursor = "next-cursor",
            ),
            result,
        )
    }
}
