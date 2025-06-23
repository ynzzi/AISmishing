package com.example.smishingdetector

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.sbs.smishingdetector.network.ApiService
import com.example.sbs.smishingdetector.network.RetrofitClient
import com.example.sbs.smishingdetector.network.AnalyzeRequest
import com.example.sbs.smishingdetector.network.AnalyzeResponse
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

object SmsSender {
    private const val TAG = "SmsSender"
    private val client = OkHttpClient()

    fun sendToServer(context: Context, sender: String, message: String) {
        val requestBody = FormBody.Builder()
            .add("user_id", "user001")
            .add("sender", sender)
            .add("message", message)
            .build()

        val request = Request.Builder()
            .url("http://192.168.0.247:8000/sms") // 🔁 DB 서버 주소
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "❌ DB 서버 전송 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                val bodyString = response.body?.string().orEmpty()
                Log.d(TAG, "✅ DB 서버 응답: $bodyString")

                var detectionID = -1
                var dbSpam = false

                try {
                    val json = JSONObject(bodyString)
                    val result = json.optString("result")
                    detectionID = json.optInt("detection_id")
                    dbSpam = result.equals("스팸", ignoreCase = true) || result.equals("spam", ignoreCase = true)
                } catch (ex: Exception) {
                    Log.e(TAG, "❌ JSON 파싱 오류: ${ex.message}")
                }

                // 🔁 AI 서버 분석 요청
                CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
                    try {
                        val aiService = RetrofitClient.getAIClient().create(ApiService::class.java)
                        val aiResponse: Response<AnalyzeResponse> = aiService.analyzeMessage(AnalyzeRequest(message))

                        if (aiResponse.isSuccessful) {
                            val aiResult = aiResponse.body()
                            val aiSpam = aiResult?.label == 1
                            val shouldAlert = dbSpam || aiSpam

                            if (shouldAlert) {
                                withContext(Dispatchers.Main) {
                                    val intent = Intent(context, SmishingPopupActivity::class.java).apply {
                                        putExtra("sender", sender)
                                        putExtra("message", message)
                                        putExtra("detection_id", detectionID)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        } else {
                            Log.e(TAG, "❌ AI 응답 실패: ${aiResponse.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ AI 호출 오류: ${e.message}")
                    }
                }
            }
        })
    }
}
