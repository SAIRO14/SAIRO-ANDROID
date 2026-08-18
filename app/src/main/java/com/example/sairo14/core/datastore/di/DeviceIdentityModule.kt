package com.example.sairo14.core.datastore.di

import com.example.sairo14.core.datastore.DataStoreDeviceIdProvider
import com.example.sairo14.core.datastore.DeviceIdProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** 기기 식별자 계약을 DataStore 기반 구현에 연결한다. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DeviceIdentityModule {

    @Binds
    @Singleton
    abstract fun bindDeviceIdProvider(
        provider: DataStoreDeviceIdProvider,
    ): DeviceIdProvider
}
