package com.example.sairo14.core.network.di

import com.example.sairo14.core.network.AndroidNetworkStatusRepository
import com.example.sairo14.domain.repository.NetworkStatusRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Android 연결 상태 구현을 Domain 계약에 연결한다. */
@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkStatusModule {

    @Binds
    @Singleton
    abstract fun bindNetworkStatusRepository(
        repository: AndroidNetworkStatusRepository,
    ): NetworkStatusRepository
}
