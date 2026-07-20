package com.example.sairo14.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val APP_PREFERENCES_NAME = "app_preferences"

private val Context.appPreferencesDataStore by preferencesDataStore(
    name = APP_PREFERENCES_NAME,
)

@Singleton
class AppPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.appPreferencesDataStore

    val hasCompletedOnboarding: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[HAS_COMPLETED_ONBOARDING] ?: false }
        .distinctUntilChanged()

    val anonymousUserId: Flow<String?> = dataStore.data
        .map { preferences -> preferences[ANONYMOUS_USER_ID] }
        .distinctUntilChanged()

    suspend fun markOnboardingCompleted() {
        dataStore.edit { preferences ->
            preferences[HAS_COMPLETED_ONBOARDING] = true
        }
    }

    suspend fun getOrCreateAnonymousUserId(): String {
        var userId: String? = null

        dataStore.edit { preferences ->
            userId = preferences[ANONYMOUS_USER_ID]
                ?: UUID.randomUUID().toString().also { id ->
                    preferences[ANONYMOUS_USER_ID] = id
                }
        }

        return checkNotNull(userId)
    }

    private companion object {
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val ANONYMOUS_USER_ID = stringPreferencesKey("anonymous_user_id")
    }
}
