package com.example.sairo14.data.repository.remote

import com.example.sairo14.data.mapper.toDomain
import com.example.sairo14.data.remote.SairoApi
import com.example.sairo14.data.remote.runRemoteOperation
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.SharedCourse
import com.example.sairo14.domain.repository.SharedCourseRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

/** 공개 공유 링크의 코스 스냅샷을 기기 식별자 없이 조회한다. */
@Singleton
class RemoteSharedCourseRepository @Inject constructor(
    private val api: SairoApi,
    private val json: Json,
) : SharedCourseRepository {

    override suspend fun getSharedCourse(shareId: String): AppResult<SharedCourse> =
        runRemoteOperation(
            action = "공유 코스 정보를 불러오지 못했습니다.",
            json = json,
        ) {
            api.getSharedCourse(shareId).toDomain(shareId)
        }
}
