package com.example.sairo14.data.remote

import com.example.sairo14.data.remote.dto.PhotoResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/** SAIRO 서버와 통신하는 Retrofit 계약이다. */
interface SairoApi {

    /** 온보딩 취향 선택에 표시할 사진 후보를 무작위로 조회한다.
     *
     * 이 API는 공개 사진 풀을 반환하므로 `X-Device-Id` 헤더를 보내지 않는다.
     * @param limit 반환할 사진 수. 서버는 1~100 범위를 허용하며 기본값은 40이다
     */
    @GET("photos")
    suspend fun getPhotos(
        @Query("limit") limit: Int = DefaultPhotoLimit,
    ): List<PhotoResponseDto>
}

private const val DefaultPhotoLimit = 40
