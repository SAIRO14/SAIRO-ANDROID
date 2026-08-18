package com.example.sairo14.data.mapper

import com.example.sairo14.data.remote.dto.ShareCourseResponseDto
import com.example.sairo14.domain.model.SharedCourseLink
import org.junit.Assert.assertEquals
import org.junit.Test

class ShareCourseMapperTest {

    @Test
    fun `공유 응답의 식별자와 HTTPS URL을 Domain으로 변환한다`() {
        assertEquals(
            SharedCourseLink("share-1", "https://example.com/shared/share-1"),
            ShareCourseResponseDto(
                shareId = " share-1 ",
                shareUrl = " https://example.com/shared/share-1 ",
            ).toDomain(),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `빈 공유 식별자는 허용하지 않는다`() {
        ShareCourseResponseDto(" ", "https://example.com/shared/share-1").toDomain()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `HTTP가 아닌 공유 URL은 허용하지 않는다`() {
        ShareCourseResponseDto("share-1", "ftp://example.com/shared/share-1").toDomain()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `host가 없는 공유 URL은 허용하지 않는다`() {
        ShareCourseResponseDto("share-1", "https:/shared/share-1").toDomain()
    }
}
