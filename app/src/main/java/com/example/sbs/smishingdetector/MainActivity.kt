package com.example.smishingdetector

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.*
import com.example.sbs.smishingdetector.ai.AiRunner
import com.example.smishingdetector.ui.*
import com.example.smishingdetector.ui.theme.SmishingDetectorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AiRunner.loadModel(this)
        enableEdgeToEdge()
        setContent {
            SmishingDetectorApp()
        }
    }
}

@Composable
fun SmishingDetectorApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    val isPermissionShown = prefs.getBoolean("PermissionScreenShown", false)
    val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

    val startDestination = when {
        !isLoggedIn -> "welcome"
        !isPermissionShown -> "permission"
        else -> "main"
    }

    SmishingDetectorTheme {
        NavHost(navController = navController, startDestination = startDestination) {
            // 🟡 웰컴 화면 (가입하기 / 로그인 선택)
            composable("welcome") {
                WelcomeScreen(navController)
            }

            // 🔵 로그인 화면
            composable("login") {
                LoginScreen(navController, prefs)
            }

            // 🟢 회원가입 화면
            composable("signup") {
                SignUpScreen(navController)
            }

            // 🔒 권한 설정 안내 화면
            composable("permission") {
                PermissionScreen(navController, prefs)
            }

            // 🔐 SMS 권한 요청 화면
            composable("sms_permission") {
                SmsPermissionScreen(navController)
            }

            // 🏠 메인 대시보드 화면
            composable("main") {
                MainScreen(navController, prefs)
            }

            // 🧾 신고내역 및 스미싱 목록 화면 (🚨 새로 추가된 부분)
            composable("report") {
                ReportScreen(navController)
            }
        }
    }
}
