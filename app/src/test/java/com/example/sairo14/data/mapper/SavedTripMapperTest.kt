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
    fun `목록 응답의 카드 정보와 다음 커서를 보존한다`() {
        val result = SavedTripListResponseDto(
            items = listOf(
                SavedTripResponseDto(
                    savedTripId = "saved-trip-1",
                    courseId = "course-1",
                    regionName = "제주",
                    regionArea = "의성군",
                    imageUrl = "https://example.com/hanok.jpg",
                    reason = "역사 속 고즈넉한 감성",
                    spotNames = listOf("덕양서원(의성)", "연일향교"),
                    imageUrls = listOf("https://example.com/a.jpg", "https://example.com/b.jpg"),
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
                        regionArea = "의성군",
                        imageUrl = "https://example.com/hanok.jpg",
                        reason = "역사 속 고즈넉한 감성",
                        spotNames = listOf("덕양서원(의성)", "연일향교"),
                        imageUrls = listOf("https://example.com/a.jpg", "https://example.com/b.jpg"),
                        createdAt = "2026-08-14T10:00:00Z",
                    ),
                ),
                nextCursor = "next-cursor",
            ),
            result,
        )
    }
}
