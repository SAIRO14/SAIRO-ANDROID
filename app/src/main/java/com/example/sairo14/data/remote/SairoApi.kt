package com.example.sairo14.data.remote

import com.example.sairo14.data.remote.dto.PhotoResponseDto
import com.example.sairo14.data.remote.dto.TasteAnalysisRequestDto
import com.example.sairo14.data.remote.dto.TasteAnalysisResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
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

    /** 선택 사진을 분석해 무드 태그와 추천 코스를 생성한다.
     *
     * 익명 사용자별 분석 결과를 연결하기 위해 [deviceId]는 반드시 `X-Device-Id` 헤더로 전달한다.
     * @param deviceId 현재 기기의 UUID v4 익명 식별자
     * @param request 5~10개의 고유 사진 ID를 담은 분석 요청 본문
     */
    @POST("taste-analysis")
    suspend fun analyzeTaste(
        @Header("X-Device-Id") deviceId: String,
        @Body request: TasteAnalysisRequestDto,
    ): TasteAnalysisResponseDto
}

private const val DefaultPhotoLimit = 40
