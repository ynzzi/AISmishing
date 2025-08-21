// app/src/main/java/com/example/smishingdetector/SmsSender.kt
package com.example.smishingdetector

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sbs.smishingdetector.network.ApiService
import com.example.sbs.smishingdetector.network.RetrofitClient
import com.example.sbs.smishingdetector.network.AnalyzeRequest
import com.example.sbs.smishingdetector.network.AnalyzeResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

object SmsSender {
    private const val TAG = "SmsSender"

    /**
     * 문자 수신 시:
     * 1) DB 서버 /sms 호출(헤더는 RetrofitClient.api(context) 인터셉터에서 자동 첨부)
     * 2) AI 서버 /analyze 호출
     * 3) 둘 중 하나라도 스팸이면 팝업 노출
     */
    fun sendToServer(context: Context, sender: String, message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1) DB 서버 호출
                val dbService: ApiService = RetrofitClient.api(context)
                val dbResp = dbService.sendSms(sender = sender, message = message)

                var detectionID = -1
                var dbSpam = false

                if (dbResp.isSuccessful) {
                    val body = dbResp.body()
                    detectionID = body?.detection_id ?: -1
                    val result = body?.result.orEmpty()
                    dbSpam = result.equals("스팸", ignoreCase = true) || result.equals("spam", ignoreCase = true)
                    Log.d(TAG, "✅ DB 응답: detection_id=$detectionID, result=$result")
                } else {
                    val code = dbResp.code()
                    val err = dbResp.errorBody()?.string().orEmpty()
                    Log.e(TAG, "❌ DB 응답 실패: code=$code err=$err")
                }

                // 2) AI 서버 분석
                val aiService = RetrofitClient.getAIClient().create(ApiService::class.java)
                val aiResponse: Response<AnalyzeResponse> =
                    aiService.analyzeMessage(AnalyzeRequest(text = message))

                var aiSpam = false
                if (aiResponse.isSuccessful) {
                    val ai = aiResponse.body()
                    aiSpam = (ai?.label == 1)
                    Log.d(TAG, "✅ AI 응답: label=${ai?.label} confidence=${ai?.confidence}")
                } else {
                    Log.e(TAG, "❌ AI 응답 실패: code=${aiResponse.code()} err=${aiResponse.errorBody()?.string().orEmpty()}")
                }

                // 3) 스팸 판단 → 팝업
                if (dbSpam || aiSpam) {
                    withContext(Dispatchers.Main) {
                        val intent = Intent(context, SmishingPopupActivity::class.java).apply {
                            putExtra("sender", sender)
                            putExtra("message", message)
                            putExtra("detectionID", detectionID)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ 전송/분석 처리 중 오류: ${e.message}", e)
            }
        }
    }
}
