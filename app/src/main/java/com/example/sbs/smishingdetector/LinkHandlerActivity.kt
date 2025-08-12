package com.example.smishingdetector

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

// Compose 상태 관리
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// Compose UI 레이아웃
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme

// Modifier 및 정렬
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// Retrofit 네트워크
import com.example.sbs.smishingdetector.network.LinkApiService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

// 팝업 컴포저블
import ui.LinkWarningDialog


class LinkHandlerActivity : ComponentActivity() {
    private val api by lazy {
        Retrofit.Builder()
            .baseUrl("http://172.30.1.20:8000/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(LinkApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LinkHandler", "onCreate 시작")    // ✱ 여기에 찍히는지 확인

        val url = intent.getStringExtra("url") ?: run {
            Log.e("LinkHandler", "url extra가 없습니다.")
            finish(); return
        }
        val domain = Uri.parse(url).host.orEmpty()
        Log.d("LinkHandler", "도메인: $domain")

        setContent {
            MaterialTheme {
                var isMalicious by remember { mutableStateOf<Boolean?>(null) }
                var reportCount by remember { mutableStateOf(0) }

                LaunchedEffect(domain) {
                    try {
                        Log.d("LinkHandler", "API 호출 시도")
                        isMalicious  = api.isMalicious(domain)
                        reportCount = api.getReportCount(domain)
                        Log.d("LinkHandler", "API 호출 결과: isMalicious=$isMalicious, count=$reportCount")
                    } catch (e: Exception) {
                        Log.e("LinkHandler", "API 호출 실패", e)
                        isMalicious = false
                    }
                }

                when (isMalicious) {
                    null -> {
                        Log.d("LinkHandler", "로딩 중 표시")
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    true -> {
                        Log.d("LinkHandler", "팝업 표시 분기")
                        LinkWarningDialog(
                            reportCount = reportCount,
                            onOpen  = { openUrl(url) },
                            onBlock = { finish() }
                        )
                    }
                    false -> {
                        Log.d("LinkHandler", "직접 오픈 분기")
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
