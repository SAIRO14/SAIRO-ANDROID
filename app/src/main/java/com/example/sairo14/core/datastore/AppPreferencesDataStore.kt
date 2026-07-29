package com.example.sairo14.core.datastore

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val APP_PREFERENCES_NAME = "app_preferences"

private val Context.appPreferencesDataStore by preferencesDataStore(
    name = APP_PREFERENCES_NAME,
    corruptionHandler = ReplaceFileCorruptionHandler {
        emptyPreferences()
    },
)

/**
 * 온보딩 완료 여부를 저장하고 손상 시 기본 상태로 복구한다.
 *
 * 이 DataStore는 온보딩 상태만 소유한다. 익명 사용자 식별자는 파일 손상 범위를 분리하기 위해
 * [AnonymousIdentityDataStore]에서 별도로 관리한다.
 */
@Singleton
class AppPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.appPreferencesDataStore

    val hasCompletedOnboarding: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[HAS_COMPLETED_ONBOARDING] ?: false }
        .distinctUntilChanged()

    suspend fun markOnboardingCompleted() {
        dataStore.edit { preferences ->
            preferences[HAS_COMPLETED_ONBOARDING] = true
        }
    }

    /**
     * 기존 통합 저장소에 남아 있는 익명 ID를 별도 저장소로 이전할 수 있게 읽는다.
     *
     * 새 설치에서는 항상 `null`을 반환한다. 반환된 ID는 호출자가 별도 저장소에 기록한 뒤 이 파일에서 제거한다.
     */
    internal suspend fun getLegacyAnonymousUserId(): String? =
        dataStore.data.first()[ANONYMOUS_USER_ID]

    /** 별도 저장소로 이전을 완료한 익명 ID를 기존 통합 저장소에서 제거한다. */
    internal suspend fun removeLegacyAnonymousUserId() {
        dataStore.edit { preferences ->
            preferences.remove(ANONYMOUS_USER_ID)
        }
    }

    private companion object {
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val ANONYMOUS_USER_ID = stringPreferencesKey("anonymous_user_id")
    }
}
