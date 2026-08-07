package com.example.sairo14.app

import android.app.Application
import com.example.sairo14.BuildConfig
import com.kakao.vectormap.KakaoMapSdk
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

        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isBlank()) {
            if (BuildConfig.DEBUG) {
                Timber.w("KAKAO_NATIVE_APP_KEY가 없어 카카오 지도를 초기화하지 않습니다.")
            }
        } else {
            KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        }
    }
}
