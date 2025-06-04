package com.example.sbs.smishingdetector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import okhttp3.*
import java.io.IOException

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SmsReceiver", "✅ onReceive 호출됨")
        Log.d("SmsReceiver", "📦 intent.action: ${intent.action}")

        val bundle: Bundle? = intent.extras
        if (bundle == null) {
            Log.e("SmsReceiver", "❌ Bundle is null")
            return
        }

        val pdus = bundle.get("pdus") as? Array<*>
        if (pdus == null || pdus.isEmpty()) {
            Log.e("SmsReceiver", "❌ PDUs가 없음")
            return
        }

        for (pdu in pdus) {
            val format = bundle.getString("format")
            val message = SmsMessage.createFromPdu(pdu as ByteArray, format)

            val sender = message.originatingAddress
            val body = message.messageBody

            Log.d("SmsReceiver", "📩 문자 수신됨 - 발신자: $sender, 내용: $body")

            sendToServer(sender ?: "", body ?: "")
        }
    }

    private fun sendToServer(sender: String, body: String) {
        val client = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("sender", sender)
            .add("message", body)
            .build()

        val request = Request.Builder()
            .url("http://172.30.1.66:8000/sms") // 서버 컴퓨터 주소로 바꿔야 오류 안뜸!!! 확인 방법 CMD -> ipconfig
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SmsReceiver", "❌ 서버 전송 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body?.string()
                Log.d("SmsReceiver", "✅ 서버 응답: $responseText")
            }
        })
    }
}
