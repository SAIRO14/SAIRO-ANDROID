package com.example.sairo14

import android.graphics.Color
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.sairo14.app.AppLinkViewModel
import com.example.sairo14.app.SairoApp
import com.example.sairo14.core.designsystem.theme.SairoTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Sairo의 단일 Compose Activity를 시작하고 시스템 Window 설정을 적용한다.
 *
 * 앱 진입 목적지와 화면 전환은 [SairoApp] 아래의 앱 내비게이션이 담당한다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val appLinkViewModel: AppLinkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleAppLinkIntent(intent)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            SairoTheme {
                SairoApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAppLinkIntent(intent)
    }

    private fun handleAppLinkIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            appLinkViewModel.handleUrl(intent.dataString)
        }
    }
}
