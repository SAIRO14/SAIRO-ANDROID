package com.example.sairo14.app

import android.app.Application
import com.example.sairo14.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * 앱 프로세스 전체에서 한 번만 필요한 전역 초기화를 수행한다.
 *
 * 화면 전환과 Compose UI 구성은 [com.example.sairo14.MainActivity] 및 [SairoApp]이 담당한다.
 */
@HiltAndroidApp
class SairoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
