package com.example.smishingdetector

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.OkHttpClient

object SmsSender {
    private const val TAG = "SmsSender"
    private val client = OkHttpClient()

    /**
     * 문자 내용·발신번호를 서버로 전송하고,
     * 서버 응답(result)이 "스팸"이면 투명 팝업 Activity를 실행한다.
     */
    fun sendToServer(context: Context, sender: String, message: String) {

        /* 1) 폼 데이터 */
        val requestBody = FormBody.Builder()
            .add("user_id", "user001")
            .add("sender", sender)
            .add("message", message)
            .build()

        /* 2) POST 요청 */
        val request = Request.Builder()
            .url("http://192.168.0.15:8000/sms")   // ← 서버 주소
            .post(requestBody)
            .build()

        /* 3) 비동기 전송 */
        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "❌ 서버 전송 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string().orEmpty()
                Log.d(TAG, "✅ 서버 응답: $bodyString")

                /* 4) JSON 파싱 → result 값 확인 */
                val isSpam = try {
                    val result = JSONObject(bodyString).optString("result")
                    result.equals("정상", ignoreCase = true) ||       /* 테스트 때문에 정상으로 설정 -> 잘 나옴 근데 어플에서만 나옴 */
                            result.equals("spam", ignoreCase = true)
                } catch (ex: Exception) {
                    Log.e(TAG, "❌ JSON 파싱 오류: ${ex.message}")
                    false
                }

                /* 5) 스팸이면 투명 팝업 Activity 실행 (UI 스레드) */
                if (isSpam) {
                    Handler(Looper.getMainLooper()).post {
                        val intent = Intent(context, SmishingPopupActivity::class.java).apply {
                            putExtra("phone", sender)
                            putExtra("message", message)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)   // 리시버·서비스에서도 실행 가능
                        }
                        context.startActivity(intent)
                    }
                }
            }
        })
    }
}
