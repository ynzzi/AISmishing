package com.example.smishingdetector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SmsReceiver", "✅ onReceive 호출됨: ${intent.action}")

        // SMS_RECEIVED 외 액션 무시
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        // 문자 메시지 배열 추출
        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (smsMessages.isEmpty()) {
            Log.e("SmsReceiver", "❌ 수신된 메시지가 없음")
            return
        }

        // 발신자 번호
        val sender = smsMessages[0].originatingAddress ?: "Unknown"

        // 여러 조각 합치기
        val message = smsMessages.joinToString(separator = "") { it.messageBody }

        Log.d("SmsReceiver", "📩 SMS 수신됨 - 발신자: $sender")
        Log.d("SmsReceiver", "📝 내용: $message")

        // 서버로 전송
        SmsSender.sendToServer(context, sender, message)
    }
}
