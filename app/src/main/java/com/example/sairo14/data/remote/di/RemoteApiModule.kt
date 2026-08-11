package com.example.sairo14.data.remote.di

import com.example.sairo14.data.remote.SairoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

/** Retrofit 인스턴스에서 SAIRO 서버 API 계약을 만든다. */
@Module
@InstallIn(SingletonComponent::class)
object RemoteApiModule {

    @Provides
    @Singleton
    fun provideSairoApi(retrofit: Retrofit): SairoApi = retrofit.create(SairoApi::class.java)
}
