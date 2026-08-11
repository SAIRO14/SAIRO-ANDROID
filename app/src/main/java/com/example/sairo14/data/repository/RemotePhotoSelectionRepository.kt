package com.example.sairo14.data.repository

import com.example.sairo14.data.mapper.toDomain
import com.example.sairo14.data.remote.SairoApi
import com.example.sairo14.data.remote.runRemoteOperation
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.PhotoCandidate
import com.example.sairo14.domain.repository.PhotoSelectionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

/** SAIRO 사진 풀을 온보딩 사진 선택 도메인 계약으로 제공한다. */
@Singleton
class RemotePhotoSelectionRepository @Inject constructor(
    private val api: SairoApi,
    private val json: Json,
) : PhotoSelectionRepository {

    override suspend fun getPhotoCandidates(): AppResult<List<PhotoCandidate>> =
        runRemoteOperation(
            action = "온보딩 사진 후보를 불러오지 못했습니다.",
            json = json,
        ) {
            api.getPhotos().map { photo -> photo.toDomain() }
        }
}
