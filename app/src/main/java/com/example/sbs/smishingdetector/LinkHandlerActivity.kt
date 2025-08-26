//package com.example.smishingdetector
//
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//
//// Compose 상태 관리
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//
//// Compose UI 레이아웃
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.MaterialTheme
//
//// Modifier 및 정렬
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//
//// Retrofit 네트워크
//import com.example.sbs.smishingdetector.network.LinkApiService
//import retrofit2.Retrofit
//import retrofit2.converter.moshi.MoshiConverterFactory
//
//// 팝업 컴포저블
//import ui.LinkWarningDialog
//
//class LinkHandlerActivity : ComponentActivity() {
//    private val api by lazy {
//        Retrofit.Builder()
//            .baseUrl("http://172.30.1.20:8000/")
//            .addConverterFactory(MoshiConverterFactory.create())
//            .build()
//            .create(LinkApiService::class.java)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val url = intent.getStringExtra("url") ?: return finish()
//        val domain = Uri.parse(url).host.orEmpty()
//
//        setContent {
//            MaterialTheme {
//                // null = 로딩, true = 악성, false = 정상
//                var isMalicious by remember { mutableStateOf<Boolean?>(null) }
//                var reportCount by remember { mutableStateOf(0) }
//
//                LaunchedEffect(domain) {
//                    try {
//                        isMalicious  = api.isMalicious(domain)
//                        reportCount = api.getReportCount(domain)
//                    } catch (e: Exception) {
//                        // 에러 처리: 기본값 false
//                        isMalicious = false
//                    }
//                }
//
//                when (isMalicious) {
//                    null -> Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator()
//                    }
//                    true -> LinkWarningDialog(
//                        reportCount = reportCount,
//                        onOpen  = { openUrl(url) },
//                        onBlock = { finish() }
//                    )
//                    false -> LaunchedEffect(Unit) {
//                        openUrl(url)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun openUrl(url: String) {
//        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
//        finish()
//    }
//}

// LinkHandlerActivity.kt (중요 부분)
package com.example.smishingdetector

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.sbs.smishingdetector.network.LinkApiService
import com.example.sbs.smishingdetector.network.LinkVerdict
import com.example.sbs.smishingdetector.ui.LinkWarningDialog
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class LinkHandlerActivity : ComponentActivity() {

    private val api by lazy {
        Retrofit.Builder()
            .baseUrl("http://20.196.64.253:443/") // ← PC IP
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(LinkApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ① 브라우저 핸들러 경로(intent.data) & ② 내부/ADB 경로(extra "url") 모두 지원
        val dataUrl = intent?.data?.toString()
        val extraUrl = intent?.getStringExtra("url")
        val url = dataUrl ?: extraUrl

        if (url.isNullOrBlank()) { finish(); return }

        setContent {
            MaterialTheme {
                var verdict by remember { mutableStateOf<LinkVerdict?>(null) }
                var error by remember { mutableStateOf<Throwable?>(null) }

                LaunchedEffect(url) {
                    try {
                        val v = api.checkLink(url)
                        Log.d("LinkHandler", "verdict = ${v.malicious}, source=${v.source}, count=${v.reportCount}")
                        verdict = v
                    } catch (e: Exception) {
                        Log.e("LinkHandler", "API 실패", e)
                        error = e
                    }
                }

                when {
                    verdict == null && error == null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    error != null -> {
                        Log.w("LinkHandler", "에러로 인한 허용 분기: ${error?.message}")
                        LaunchedEffect(Unit) { openUrl(url) }
                    }

                    verdict!!.malicious -> {
                        Log.d("LinkHandler", "팝업 분기 진입")
                        LinkWarningDialog(
                            reportCount = verdict!!.reportCount,
                            onOpen  = { openUrl(url) },
                            onBlock = { finish() }
                        )
                    }
                    else -> {
                        Log.d("LinkHandler", "정상(허용) 분기 → 바로 열기")
                        LaunchedEffect(Unit) { openUrl(url) }
                    }
                }
            }
        }
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        finish()
    }
}

