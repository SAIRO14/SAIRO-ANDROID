package com.example.sairo14.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val ANONYMOUS_IDENTITY_PREFERENCES_NAME = "anonymous_identity_preferences"

private val Context.anonymousIdentityDataStore by preferencesDataStore(
    name = ANONYMOUS_IDENTITY_PREFERENCES_NAME,
)

/**
 * 익명 사용자 식별자를 온보딩 상태와 분리해 저장한다.
 *
 * 식별자 파일은 자동으로 빈 값으로 복구하지 않는다. 파일 손상은 기존 서버 데이터와의 연결 단절로
 * 이어질 수 있으므로, 호출 계층이 오류를 인지하고 제품 정책에 따라 재시도 또는 새 익명 사용자 생성을 결정한다.
 */
@Singleton
class AnonymousIdentityDataStore @Inject constructor(
    @ApplicationContext context: Context,
    private val legacyPreferencesDataStore: AppPreferencesDataStore,
) {
    private val dataStore = context.anonymousIdentityDataStore

    /** 현재 저장된 익명 사용자 식별자를 관찰한다. */
    val anonymousUserId: Flow<String?> = dataStore.data
        .map { preferences -> preferences[ANONYMOUS_USER_ID] }
        .distinctUntilChanged()

    /**
     * 저장된 익명 사용자 식별자를 반환하거나, 최초 사용이면 새 식별자를 생성한다.
     *
     * 이전 앱 버전의 통합 Preferences에 저장된 ID가 있으면 먼저 이전한다. 파일 손상 예외는 숨기지 않으며,
     * 호출 계층이 서버 연결 보존 정책을 결정할 수 있도록 그대로 전달한다.
     */
    suspend fun getOrCreateAnonymousUserId(): String {
        dataStore.data.first()[ANONYMOUS_USER_ID]?.let { return it }
        val legacyUserId = legacyPreferencesDataStore.getLegacyAnonymousUserId()
        var userId: String? = null

        dataStore.edit { preferences ->
            userId = preferences[ANONYMOUS_USER_ID]
                ?: legacyUserId
                ?: UUID.randomUUID().toString().also { id ->
                    preferences[ANONYMOUS_USER_ID] = id
                }

            if (preferences[ANONYMOUS_USER_ID] == null) {
                preferences[ANONYMOUS_USER_ID] = checkNotNull(userId)
            }
        }

        if (legacyUserId != null) {
            legacyPreferencesDataStore.removeLegacyAnonymousUserId()
        }

        return checkNotNull(userId)
    }

    private companion object {
        val ANONYMOUS_USER_ID = stringPreferencesKey("anonymous_user_id")
    }
}
