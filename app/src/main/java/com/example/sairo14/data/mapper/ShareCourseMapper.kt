package com.example.sairo14.data.mapper

import com.example.sairo14.data.remote.dto.ShareCourseResponseDto
import com.example.sairo14.domain.model.SharedCourseLink
import java.net.URI

/** 공유 API 응답을 UI와 네트워크 구현에서 분리된 [SharedCourseLink]로 변환한다. */
fun ShareCourseResponseDto.toDomain(): SharedCourseLink {
    val normalizedShareId = shareId.trim()
    val normalizedShareUrl = shareUrl.trim()
    require(normalizedShareId.isNotEmpty()) { "공유 스냅샷 ID가 비어 있습니다." }
    require(normalizedShareUrl.isHttpUrl()) { "공유 URL 형식이 올바르지 않습니다." }

    return SharedCourseLink(
        shareId = normalizedShareId,
        shareUrl = normalizedShareUrl,
    )
}

private fun String.isHttpUrl(): Boolean = runCatching {
    URI(this).let { uri ->
        uri.host != null && uri.scheme?.lowercase() in setOf("http", "https")
    }
}.getOrDefault(false)
