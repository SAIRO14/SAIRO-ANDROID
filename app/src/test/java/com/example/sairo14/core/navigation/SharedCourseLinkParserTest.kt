package com.example.sairo14.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SharedCourseLinkParserTest {

    @Test
    fun `정상 공유 URL에서 공유 ID를 추출한다`() {
        assertEquals(
            "7429b36b9d",
            SharedCourseLinkParser.parseShareIdOrNull("https://sairo.app/shared/7429b36b9d"),
        )
    }

    @Test
    fun `추적 쿼리가 있어도 경로의 공유 ID만 사용한다`() {
        assertEquals(
            "share_1-2",
            SharedCourseLinkParser.parseShareIdOrNull(
                "https://sairo.app/shared/share_1-2?utm_source=kakao",
            ),
        )
    }

    @Test
    fun `다른 도메인과 스킴 및 잘못된 경로는 거부한다`() {
        listOf(
            "http://sairo.app/shared/share-1",
            "https://example.com/shared/share-1",
            "https://sairo.app/shared/",
            "https://sairo.app/shared/share-1/extra",
            "https://sairo.app/courses/share-1",
            "https://sairo.app/shared/share%2F1",
            "https://attacker@sairo.app/shared/share-1",
            "https://sairo.app:444/shared/share-1",
        ).forEach { url ->
            assertNull(SharedCourseLinkParser.parseShareIdOrNull(url))
        }
    }

    @Test
    fun `null 또는 URI가 아닌 문자열은 거부한다`() {
        assertNull(SharedCourseLinkParser.parseShareIdOrNull(null))
        assertNull(SharedCourseLinkParser.parseShareIdOrNull("not a url"))
    }
}
