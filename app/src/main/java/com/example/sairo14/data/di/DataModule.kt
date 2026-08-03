package com.example.sairo14.data.di

import com.example.sairo14.data.repository.DefaultOnboardingRepository
import com.example.sairo14.data.repository.FakeHomeRepository
import com.example.sairo14.data.repository.FakePhotoSelectionRepository
import com.example.sairo14.domain.repository.HomeRepository
import com.example.sairo14.domain.repository.OnboardingRepository
import com.example.sairo14.domain.repository.PhotoSelectionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Data 계층 구현을 Domain Repository 계약에 연결한다. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(
        repository: DefaultOnboardingRepository,
    ): OnboardingRepository

    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        repository: FakeHomeRepository,
    ): HomeRepository

    @Binds
    @Singleton
    abstract fun bindPhotoSelectionRepository(
        repository: FakePhotoSelectionRepository,
    ): PhotoSelectionRepository
}
