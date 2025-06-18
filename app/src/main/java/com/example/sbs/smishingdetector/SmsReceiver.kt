package com.example.smishingdetector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.sbs.smishingdetector.SmsAnalyzer
import ui.SmishingAlertDialog  // 팝업 UI 클래스 임포트
import android.os.Handler
import android.os.Looper


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

        // 여러 조각 합쳐 전체 메시지 구성
        val message = smsMessages.joinToString(separator = "") { it.messageBody }

        Log.d("SmsReceiver", "📩 SMS 수신됨 - 발신자: $sender")
        Log.d("SmsReceiver", "📝 내용: $message")

        // 1️⃣ 로컬 서버 또는 외부 서버에 저장
        SmsSender.sendToServer(context, sender, message)

        // 2️⃣ AI 서버로 메시지 전송 및 분석
        SmsAnalyzer.analyzeWithAI(message) { result, score ->
            Log.d("SmsReceiver", "🤖 AI 분석 결과: $result (score=$score)")
            if (result == "spam") {
                // 3️⃣ 스팸 판단되면 팝업 생성
                val popupIntent = Intent(context, SmishingPopupActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra("phone", sender)
                    putExtra("message", message)
                }
                context.startActivity(popupIntent)


            }
        }
    }
}
