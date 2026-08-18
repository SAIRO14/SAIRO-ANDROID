package com.example.sairo14.core.datastore

import javax.inject.Inject

/** 서버 요청에 사용할 현재 기기의 익명 식별자를 제공한다. */
interface DeviceIdProvider {

    /** 저장된 기기 식별자를 반환하거나, 최초 요청이면 새 UUID v4를 준비한다. */
    suspend fun getDeviceId(): String
}

/** [AnonymousIdentityDataStore]를 서버 요청용 기기 식별자 계약으로 연결한다. */
class DataStoreDeviceIdProvider @Inject constructor(
    private val anonymousIdentityDataStore: AnonymousIdentityDataStore,
) : DeviceIdProvider {

    override suspend fun getDeviceId(): String =
        anonymousIdentityDataStore.getOrCreateAnonymousUserId()
}
