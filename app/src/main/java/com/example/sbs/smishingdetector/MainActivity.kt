//package com.example.smishingdetector
//
//import android.Manifest
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.provider.Telephony
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.platform.LocalContext
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.navigation.compose.*
//import com.example.smishingdetector.ui.*
//import com.example.smishingdetector.ui.theme.SmishingDetectorTheme
//
//class MainActivity : ComponentActivity() {
//
//    /* ────────── 🔐 런타임 권한 목록 ────────── */
//    private val PERMISSIONS = arrayOf(
//        Manifest.permission.RECEIVE_SMS,
//        Manifest.permission.RECEIVE_MMS,
//        Manifest.permission.READ_SMS,
//        Manifest.permission.READ_PHONE_STATE
//    )
//    private val PERMISSION_REQ_CODE = 100
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//
//        checkPermissions()       // ① 권한 요청
//        requestDefaultSmsApp()   // ② 기본 메시지 앱 설정 유도(선택)
//
//        setContent {
//            SmishingDetectorApp()  // ③ 기존 Compose UI 그대로
//        }
//    }
//
//    /** 필요한 권한이 없으면 사용자에게 요청 */
//    private fun checkPermissions() {
//        val denied = PERMISSIONS.filter {
//            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
//        }
//        if (denied.isNotEmpty()) {
//            ActivityCompat.requestPermissions(
//                this,
//                denied.toTypedArray(),
//                PERMISSION_REQ_CODE
//            )
//        }
//    }
//
//    /** 현재 앱이 기본 SMS/MMS 앱이 아니라면 설정 화면으로 이동 */
//    private fun requestDefaultSmsApp() {
//        if (Telephony.Sms.getDefaultSmsPackage(this) != packageName) {
//            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
//                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
//            }
//            startActivity(intent)
//        }
//    }
//}
//
//
//@Composable
//fun SmishingDetectorApp() {
//    val navController = rememberNavController()
//    val context = LocalContext.current
//    val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
//    val isPermissionShown = prefs.getBoolean("PermissionScreenShown", false)
//    val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
//
//    val startDestination = when {
//        !isLoggedIn        -> "welcome"
//        !isPermissionShown -> "permission"
//        else               -> "main"
//    }
//
//    SmishingDetectorTheme {
//        NavHost(navController = navController, startDestination = startDestination) {
//            // 🟡 웰컴 화면 (가입하기 / 로그인 선택)
//            composable("welcome") {
//                WelcomeScreen(navController)
//            }
//
//            // 🔵 로그인 화면
//            composable("login") {
//                LoginScreen(navController, prefs)
//            }
//
//            // 🟢 회원가입 화면
//            composable("signup") {
//                SignUpScreen(navController)
//            }
//
//            // 🔒 권한 설정 안내 화면
//            composable("permission") {
//                PermissionScreen(navController, prefs)
//            }
//
//            // 🔐 SMS 권한 요청 화면
//            composable("sms_permission") {
//                SmsPermissionScreen(navController)
//            }
//
//            // 🏠 메인 대시보드 화면
//            composable("main") {
//                MainScreen(navController, prefs)
//            }
//            // 🧾 신고내역 및 스미싱 목록 화면 (🚨 새로 추가된 부분)
//            composable("report") {
//                ReportScreen(navController)
//            }
//        }
//    }
//}

// app/src/main/java/com/example/smishingdetector/MainActivity.kt
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sbs.smishingdetector.smsguard.TokenStorage
import com.example.smishingdetector.ui.LoginScreen
import com.example.smishingdetector.ui.ReportScreen
import com.example.smishingdetector.ui.SignUpScreen
import com.example.smishingdetector.ui.*
import com.example.smishingdetector.ui.theme.SmishingDetectorTheme

class MainActivity : ComponentActivity() {

    /* ────────── 🔐 런타임 권한 목록 ────────── */
    private val PERMISSIONS = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.RECEIVE_MMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_PHONE_STATE
    )
    private val PERMISSION_REQ_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkPermissions()       // ① 권한 요청
        // 기본 SMS 앱 전환은 사용자 경험을 위해 PermissionScreen에서 눌러 유도하는 걸 권장
        // requestDefaultSmsApp()

        setContent {
            SmishingDetectorApp()  // ③ Compose UI
        }
    }

    /** 필요한 권한이 없으면 사용자에게 요청 */
    private fun checkPermissions() {
        val denied = PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (denied.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                denied.toTypedArray(),
                PERMISSION_REQ_CODE
            )
        }
    }

    /** 현재 앱이 기본 SMS/MMS 앱이 아니라면 설정 화면으로 이동 (옵션) */
    private fun requestDefaultSmsApp() {
        if (Telephony.Sms.getDefaultSmsPackage(this) != packageName) {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
            }
            startActivity(intent)
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
    // ✅ 기기 토큰 보유 여부로 로그인 화면 스킵
    val hasDeviceToken = !TokenStorage(context).getDeviceToken().isNullOrBlank()

    // ✅ 시작 목적지 결정 규칙
    val startDestination = when {
        hasDeviceToken && !isPermissionShown -> "permission" // 토큰 있음 → 권한 안내 먼저
        hasDeviceToken -> "main"                             // 권한도 이미 봤으면 메인
        isLoggedIn && !isPermissionShown -> "permission"     // (보조) 로그인 플래그로도 처리
        isLoggedIn -> "main"
        else -> "welcome"
    }

    SmishingDetectorTheme {
        NavHost(navController = navController, startDestination = startDestination) {
            // 🟡 웰컴 화면 (가입하기 / 로그인 선택)
            composable("welcome") { WelcomeScreen(navController) }

            // 🔵 로그인 화면 (ID/PW → device_token 발급/저장)
            composable("login") { LoginScreen(navController, prefs) }

            // 🟢 회원가입 화면
            composable("signup") { SignUpScreen(navController) }

            // 🔒 권한 설정 안내 화면
            composable("permission") { PermissionScreen(navController, prefs) }

            // 🔐 SMS 권한 요청 화면
            composable("sms_permission") { SmsPermissionScreen(navController) }

            // 🏠 메인 대시보드 화면
            composable("main") { MainScreen(navController, prefs) }

            // 🧾 신고내역 및 스미싱 목록 화면
            composable("report") { ReportScreen(navController) }

            // ⚠️ 바이러스 검사 (파일검사) 화면
            composable("fileScan") { FileScanScreen(navController = navController) }

            // ⚠️ 바이러스 검사 결과 화면
            composable("scanResult") { ScanResultScreen(navController = navController) }
        }
    }
}
