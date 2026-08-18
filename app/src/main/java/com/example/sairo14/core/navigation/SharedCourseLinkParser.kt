package com.example.sairo14.core.navigation

import java.net.URI

/** Sairo 공개 공유 URL에서 화면 이동에 사용할 공유 스냅샷 ID를 안전하게 추출한다.
 *
 * `https://sairo.app/shared/{shareId}`의 단일 경로 세그먼트만 허용한다. 쿼리는 링크 추적 용도로
 * 무시하며, 반환한 ID를 Intent나 URL 전체 대신 Navigation Route에 전달하는 책임은 호출자에게 있다.
 */
object SharedCourseLinkParser {

    /** URL이 Sairo 공유 코스 형식이면 스냅샷 ID를, 아니면 `null`을 반환한다.
     * @param url 외부 Intent가 전달한 원본 URL 문자열
     */
    fun parseShareIdOrNull(url: String?): String? {
        val uri = url?.let(::parseUriOrNull) ?: return null
        if (!uri.scheme.equals(HttpsScheme, ignoreCase = true)) return null
        if (!uri.host.equals(SairoHost, ignoreCase = true)) return null
        if (uri.userInfo != null) return null
        if (uri.port != -1 && uri.port != DefaultHttpsPort) return null

        val rawPath = uri.rawPath ?: return null
        val shareId = rawPath.removePrefix(SharedPathPrefix)
        return shareId.takeIf {
            rawPath.startsWith(SharedPathPrefix) &&
                '/' !in it &&
                ShareIdPattern.matches(it)
        }
    }
}

private fun parseUriOrNull(url: String): URI? = runCatching { URI(url) }.getOrNull()

private const val HttpsScheme = "https"
private const val SairoHost = "sairo.app"
private const val DefaultHttpsPort = 443
private const val SharedPathPrefix = "/shared/"
private val ShareIdPattern = Regex("[A-Za-z0-9_-]{1,128}")
