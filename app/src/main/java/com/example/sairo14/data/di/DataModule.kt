package com.example.sairo14.data.di

import com.example.sairo14.data.repository.DefaultHomeRepository
import com.example.sairo14.data.repository.DefaultOnboardingRepository
import com.example.sairo14.data.repository.InMemoryOnboardingAnalysisSessionStore
import com.example.sairo14.data.repository.remote.RemoteCourseRepository
import com.example.sairo14.data.repository.remote.RemoteOnboardingRecommendationRepository
import com.example.sairo14.data.repository.remote.RemotePhotoSelectionRepository
import com.example.sairo14.data.repository.remote.RemoteSavedTripRepository
import com.example.sairo14.data.repository.remote.RemoteSharedCourseRepository
import com.example.sairo14.domain.repository.CourseRepository
import com.example.sairo14.domain.repository.HomeRepository
import com.example.sairo14.domain.repository.OnboardingAnalysisSessionStore
import com.example.sairo14.domain.repository.OnboardingRepository
import com.example.sairo14.domain.repository.OnboardingRecommendationRepository
import com.example.sairo14.domain.repository.PhotoSelectionRepository
import com.example.sairo14.domain.repository.SavedTripRepository
import com.example.sairo14.domain.repository.SharedCourseRepository
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
        repository: DefaultHomeRepository,
    ): HomeRepository

    @Binds
    @Singleton
    abstract fun bindCourseRepository(
        repository: RemoteCourseRepository,
    ): CourseRepository

    @Binds
    @Singleton
    abstract fun bindSharedCourseRepository(
        repository: RemoteSharedCourseRepository,
    ): SharedCourseRepository

    @Binds
    @Singleton
    abstract fun bindSavedTripRepository(
        repository: RemoteSavedTripRepository,
    ): SavedTripRepository

    @Binds
    @Singleton
    abstract fun bindPhotoSelectionRepository(
        repository: RemotePhotoSelectionRepository,
    ): PhotoSelectionRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingRecommendationRepository(
        repository: RemoteOnboardingRecommendationRepository,
    ): OnboardingRecommendationRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingAnalysisSessionStore(
        store: InMemoryOnboardingAnalysisSessionStore,
    ): OnboardingAnalysisSessionStore
}
