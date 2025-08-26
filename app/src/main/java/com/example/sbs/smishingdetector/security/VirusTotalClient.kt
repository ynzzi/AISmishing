package com.example.sbs.smishingdetector.security

import android.app.Activity
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class VirusTotalClient(
    private val apiKey: String,
    private val cache: VTCache? = null
) {
    private val base = "https://www.virustotal.com/api/v3"
    private val http = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .build()

    /**
     * DB 미히트 시 호출: VT 검사 → 결과에 따라 팝업 → '연결' 선택 시 onAllow(url) 호출
     */
    suspend fun checkUrlAndPrompt(
        activity: Activity,
        url: String,
        onAllow: (String) -> Unit
    ) = withContext(Dispatchers.Main) {
        // 1) 캐시 확인(선택)
        cache?.get(url)?.let { hit ->
            handleVerdictUi(activity, url, hit.malicious, hit.suspicious, onAllow)
            return@withContext
        }

        // 2) 분석 요청 & 폴링
        val result = withContext(Dispatchers.IO) {
            val id = submitUrl(url) ?: return@withContext null
            pollAnalysis(id, maxTries = 10, intervalMs = 1500L)
        }

        if (result == null) {
            Toast.makeText(activity, "VirusTotal 분석 실패/시간초과", Toast.LENGTH_SHORT).show()
            return@withContext
        }

        val stats = result.optJSONObject("data")
            ?.optJSONObject("attributes")
            ?.optJSONObject("stats")

        val malicious = stats?.optInt("malicious", 0) ?: 0
        val suspicious = stats?.optInt("suspicious", 0) ?: 0

        // 3) 캐시 저장(선택)
        cache?.put(url, malicious, suspicious, System.currentTimeMillis())

        // 4) UI 처리
        handleVerdictUi(activity, url, malicious, suspicious, onAllow)
    }

    private fun handleVerdictUi(
        activity: Activity,
        url: String,
        malicious: Int,
        suspicious: Int,
        onAllow: (String) -> Unit
    ) {
        val risky = (malicious > 0 || suspicious > 0)
        if (!risky) {
            onAllow(url) // 안전: 바로 연결
            return
        }

        AlertDialog.Builder(activity)
            .setTitle("⚠ 악성 링크 의심")
            .setMessage(
                "해당 링크는 보고된 이력이 있습니다.\n" +
                        "malicious: $malicious, suspicious: $suspicious\n" +
                        "그래도 접속하시겠습니까?"
            )
            .setPositiveButton("연결") { _, _ -> onAllow(url) }
            .setNegativeButton("차단") { _, _ ->
                Toast.makeText(activity, "접속이 차단되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .setCancelable(false)
            .show()
    }

    // --- VT REST ---

    private fun submitUrl(url: String): String? {
        val body = FormBody.Builder()
            .add("url", url)
            .build()

        val req = Request.Builder()
            .url("$base/urls")
            .addHeader("x-apikey", apiKey)
            .post(body)
            .build()

        http.newCall(req).execute().use { res ->
            if (!res.isSuccessful) return null
            val text = res.body?.string() ?: return null
            val json = JSONObject(text)
            return json.optJSONObject("data")?.optString("id", null)
        }
    }

    private fun pollAnalysis(id: String, maxTries: Int, intervalMs: Long): JSONObject? {
        repeat(maxTries) {
            val req = Request.Builder()
                .url("$base/analyses/$id")
                .addHeader("x-apikey", apiKey)
                .get()
                .build()

            http.newCall(req).execute().use { res ->
                val body = res.body?.string()
                if (res.isSuccessful && !body.isNullOrBlank()) {
                    val json = JSONObject(body)
                    val status = json.optJSONObject("data")
                        ?.optJSONObject("attributes")
                        ?.optString("status", "")
                    if (status.equals("completed", ignoreCase = true)) {
                        return json
                    }
                }
            }
            // 간단 폴링
            Thread.sleep(intervalMs) // 필요 시 delay(intervalMs)로 바꾸고 코루틴 컨텍스트 조정 가능
        }
        return null
    }
}
