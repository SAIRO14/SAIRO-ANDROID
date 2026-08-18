package com.example.sairo14.data.repository

import androidx.datastore.core.CorruptionException
import com.example.sairo14.core.datastore.AppPreferencesDataStore
import com.example.sairo14.domain.model.AppError
import com.example.sairo14.domain.model.AppResult
import com.example.sairo14.domain.model.OnboardingCompletionToken
import com.example.sairo14.domain.repository.OnboardingRepository
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import timber.log.Timber

/** DataStore의 온보딩 완료 상태를 도메인 계약으로 변환한다. */
@Singleton
class DefaultOnboardingRepository @Inject constructor(
    private val preferencesDataStore: AppPreferencesDataStore,
) : OnboardingRepository {

    override suspend fun getHasCompletedOnboarding(): AppResult<Boolean> =
        runDataStoreOperation(
            action = "온보딩 완료 여부를 읽지 못했습니다.",
        ) {
            preferencesDataStore.hasCompletedOnboarding.first()
        }

    override suspend fun createCompletionRequest(): AppResult<OnboardingCompletionToken> =
        runDataStoreOperation(
            action = "온보딩 완료 상태 요청을 등록하지 못했습니다.",
        ) {
            OnboardingCompletionToken(
                preferencesDataStore.createOnboardingCompletionRequest(),
            )
        }

    override suspend fun updateCompletionIfCurrent(
        token: OnboardingCompletionToken,
        completed: Boolean,
    ): AppResult<Boolean> =
        runDataStoreOperation(
            action = "온보딩 완료 상태를 갱신하지 못했습니다.",
        ) {
            preferencesDataStore.updateOnboardingCompletionIfCurrent(token.value, completed)
        }

    private suspend fun <T> runDataStoreOperation(
        action: String,
        block: suspend () -> T,
    ): AppResult<T> = try {
        AppResult.Success(block())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (exception: CorruptionException) {
        Timber.e(exception, action)
        AppResult.Failure(AppError.StorageCorrupted)
    } catch (exception: IOException) {
        Timber.e(exception, action)
        AppResult.Failure(AppError.StorageUnavailable)
    } catch (exception: Exception) {
        Timber.e(exception, action)
        AppResult.Failure(AppError.Unknown)
    }
}
