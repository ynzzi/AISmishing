//package com.example.smishingdetector
//
//import android.content.Context
//import android.content.Intent
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import com.example.sbs.smishingdetector.network.ApiService
//import com.example.sbs.smishingdetector.network.RetrofitClient
//import com.example.sbs.smishingdetector.network.AnalyzeRequest
//import com.example.sbs.smishingdetector.network.AnalyzeResponse
//import kotlinx.coroutines.*
//import okhttp3.*
//import org.json.JSONObject
//import retrofit2.Response
//import java.io.IOException
//
//object SmsSender {
//    private const val TAG = "SmsSender"
//    private val client = OkHttpClient()
//
//    fun sendToServer(context: Context, sender: String, message: String) {
//        val requestBody = FormBody.Builder()
//            .add("user_id", "user001")
//            .add("sender", sender)
//            .add("message", message)
//            .build()
//
//        val request = Request.Builder()
//            .url("http://20.196.64.253/sms")
//            .post(requestBody)
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.e(TAG, "❌ 서버 전송 실패: ${e.message}")
//            }
//
//            override fun onResponse(call: Call, response: okhttp3.Response) {
//                val bodyString = response.body?.string().orEmpty()
//                Log.d(TAG, "✅ 서버 응답: $bodyString")
//
//                try {
//                    val json = JSONObject(bodyString)
//                    val result = json.optString("result")
//                    val detectionID = json.optInt("detection_id")
//
//                    val isSpam = result.equals("스팸", ignoreCase = true) ||
//                            result.equals("spam", ignoreCase = true)
//
//                    if (isSpam) {
//                        val intent = Intent(context, SmishingPopupActivity::class.java).apply {
//                            putExtra("sender", sender)
//                            putExtra("message", message)
//                            putExtra("detectionID", detectionID)
//                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        }
//                        context.startActivity(intent)
//                    }
//                } catch (ex: Exception) {
//                    Log.e(TAG, "❌ JSON 파싱 오류: ${ex.message}")
//                }
//            }
//        })
//    }
//}

// app/src/main/java/com/example/smishingdetector/SmsSender.kt
package com.example.smishingdetector

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.sbs.smishingdetector.smsguard.TokenStorage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object SmsSender {
    private const val TAG = "SmsSender"
    private const val BASE_URL = "http://20.196.64.253"
    private const val SMS_ENDPOINT = "/sms"

    // 토큰 자동 첨부 + 타임아웃/로깅 적용된 클라이언트를 지연 생성
    @Volatile
    private var client: OkHttpClient? = null

    /** 로그인 직후(토큰 저장/갱신 후) 새 인터셉터가 반영되도록 클라이언트 리프레시 */
    fun refreshClient(context: Context) {
        client = buildAuthedOkHttp(context)
        Log.i(TAG, "🔄 OkHttpClient refreshed with latest tokens")
    }

    /** 현재 컨텍스트 기반으로 OkHttpClient를 준비(없으면 생성, 있으면 재사용) */
    private fun ensureClient(context: Context): OkHttpClient {
        return client ?: synchronized(this) {
            client ?: buildAuthedOkHttp(context).also { client = it }
        }
    }

    /** 토큰 자동 첨부 인터셉터 + 로깅 + 타임아웃 적용 */
    private fun buildAuthedOkHttp(context: Context): OkHttpClient {
        val headerInterceptor = Interceptor { chain ->
            val storage = TokenStorage(context)
            val original = chain.request()
            val builder = original.newBuilder()

            // X-Device-Token
            val dev = storage.getDeviceToken()?.trim()
            if (!dev.isNullOrEmpty()) {
                builder.header("X-Device-Token", dev)
                Log.d(TAG, "➡️ X-Device-Token attached: ${dev.take(16)}…")
            } else {
                Log.w(TAG, "❌ X-Device-Token missing (TokenStorage empty)")
            }

            // Authorization: Bearer <JWT>
            val jwt = storage.getAccessToken()?.trim()
            if (!jwt.isNullOrEmpty()) {
                builder.header("Authorization", "Bearer $jwt")
            } else {
                Log.w(TAG, "❌ Authorization JWT missing")
            }

            chain.proceed(builder.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            // 운영환경에서는 BASIC/NONE 권장
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    /**
     * 문자 수신 시 서버로 전송하고, 스팸이면 팝업을 띄운다.
     * - 기존 동작 유지: user_id/sender/message를 x-www-form-urlencoded로 전송
     * - 추가: 로그인 후 저장된 토큰이 있으면 자동으로 헤더에 첨부됨
     * - 팝업 실행은 메인 스레드에서 보장
     */
    fun sendToServer(context: Context, sender: String, message: String) {
        val body = FormBody.Builder()
            .add("user_id", "user001")   // 기존 코드 유지(하드코딩). 필요시 실제 로그인 사용자로 교체
            .add("sender", sender)
            .add("message", message)
            .build()

        val request = Request.Builder()
            .url("$BASE_URL$SMS_ENDPOINT")
            .post(body)
            .build()

        val http = ensureClient(context)

        http.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "❌ 서버 전송 실패: ${e.message}", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val code = response.code
                val raw = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    Log.e(TAG, "❌ 서버 응답 실패: code=$code body=$raw")
                    return
                }

                Log.d(TAG, "✅ 서버 응답($code): $raw")

                try {
                    // 기대 JSON: {"result": "...", "detection_id": 123, ...}
                    val json = JSONObject(raw)
                    val result = json.optString("result", "")
                    val detectionID = json.optInt("detection_id", -1)

                    val isSpam = result.equals("스팸", true) || result.equals("spam", true)

                    if (isSpam) {
                        val intent = Intent(context, SmishingPopupActivity::class.java).apply {
                            putExtra("sender", sender)
                            putExtra("message", message)
                            putExtra("detectionID", detectionID)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        // ✅ 반드시 메인 스레드에서 액티비티 시작
                        Handler(Looper.getMainLooper()).post {
                            context.startActivity(intent)
                        }
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "❌ JSON 파싱 오류: ${ex.message}", ex)
                }
            }
        })
    }
}
