package com.example.sairo14.data.remote

import com.example.sairo14.data.remote.dto.CourseResponseDto
import com.example.sairo14.data.remote.dto.PhotoResponseDto
import com.example.sairo14.data.remote.dto.SavedTripListResponseDto
import com.example.sairo14.data.remote.dto.SavedTripSaveRequestDto
import com.example.sairo14.data.remote.dto.SavedTripSaveResponseDto
import com.example.sairo14.data.remote.dto.TasteAnalysisRequestDto
import com.example.sairo14.data.remote.dto.TasteAnalysisResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

/** SAIRO 서버와 통신하는 Retrofit 계약이다. */
interface SairoApi {

    /** 현재 기기에서 생성한 코스의 지역과 일차별 장소 스냅샷을 조회한다.
     *
     * 장소 정보는 코스 생성 시점의 값이며, 누락된 운영 정보는 이 요청에서 보완하지 않는다.
     * @param courseId 조회할 코스의 안정적인 ID
     * @param deviceId 현재 기기의 UUID v4 익명 식별자
     */
    @GET("courses/{courseId}")
    suspend fun getCourse(
        @Path("courseId") courseId: String,
        @Header("X-Device-Id") deviceId: String,
    ): CourseResponseDto

    /** 현재 기기에 저장된 여행지를 최신 저장순으로 한 페이지 조회한다.
     *
     * 첫 페이지는 [cursor]를 전달하지 않고, 다음 페이지는 직전 응답의 커서를 수정 없이 전달한다.
     * @param deviceId 현재 기기의 UUID v4 익명 식별자
     * @param cursor 다음 페이지 조회에 사용할 서버 제공 커서. 첫 페이지면 `null`
     * @param size 한 번에 조회할 항목 수. 서버 허용 범위는 1~50이며 앱 기본값은 20이다
     */
    @GET("saved-trips")
    suspend fun getSavedTrips(
        @Header("X-Device-Id") deviceId: String,
        @Query("cursor") cursor: String? = null,
        @Query("size") size: Int = DefaultSavedTripPageSize,
    ): SavedTripListResponseDto

    /** 현재 기기에 코스를 저장하고 이후 해제에 필요한 저장 항목 ID를 반환한다.
     *
     * 서버는 중복 저장도 기존 저장 항목 정보와 함께 성공으로 반환한다.
     * @param deviceId 현재 기기의 UUID v4 익명 식별자
     * @param request 저장할 코스 ID를 담은 요청 본문
     */
    @POST("saved-trips")
    suspend fun saveTrip(
        @Header("X-Device-Id") deviceId: String,
        @Body request: SavedTripSaveRequestDto,
    ): SavedTripSaveResponseDto

    /** 현재 기기의 저장 여행지 한 건을 해제한다.
     *
     * 서버가 반환하는 `204 No Content`는 이미 삭제된 식별자를 포함해 모두 정상 해제로 처리한다.
     * @param deviceId 현재 기기의 UUID v4 익명 식별자
     * @param savedTripId 삭제할 저장 여행지의 안정적인 ID
     */
    @DELETE("saved-trips")
    suspend fun deleteSavedTrip(
        @Header("X-Device-Id") deviceId: String,
        @Query("savedTripId") savedTripId: String,
    )

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
private const val DefaultSavedTripPageSize = 20
