package com.example.sairo14.data.di

import com.example.sairo14.data.repository.DefaultOnboardingRepository
import com.example.sairo14.data.repository.fake.FakeCourseRepository
import com.example.sairo14.data.repository.fake.FakeHomeRepository
import com.example.sairo14.data.repository.fake.FakeOnboardingRecommendationRepository
import com.example.sairo14.data.repository.fake.FakeSavedTripRepository
import com.example.sairo14.data.repository.remote.RemotePhotoSelectionRepository
import com.example.sairo14.domain.repository.CourseRepository
import com.example.sairo14.domain.repository.HomeRepository
import com.example.sairo14.domain.repository.OnboardingRepository
import com.example.sairo14.domain.repository.OnboardingRecommendationRepository
import com.example.sairo14.domain.repository.PhotoSelectionRepository
import com.example.sairo14.domain.repository.SavedTripRepository
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
    abstract fun bindCourseRepository(
        repository: FakeCourseRepository,
    ): CourseRepository

    @Binds
    @Singleton
    abstract fun bindSavedTripRepository(
        repository: FakeSavedTripRepository,
    ): SavedTripRepository

    @Binds
    @Singleton
    abstract fun bindPhotoSelectionRepository(
        repository: RemotePhotoSelectionRepository,
    ): PhotoSelectionRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingRecommendationRepository(
        repository: FakeOnboardingRecommendationRepository,
    ): OnboardingRecommendationRepository
}
