package ui
// 시스템 관련
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log

// 네트워크 관련
import okhttp3.OkHttpClient
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

// 예외 처리
import java.io.IOException

// 팝업 및 UI 핸들링 (스팸 감지용 팝업)
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog

// JSON 응답 파싱
import org.json.JSONObject


class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.extras
        if (bundle != null) {
            val pdus = bundle["pdus"] as? Array<*>
            if (pdus != null) {
                for (pdu in pdus) {
                    val format = bundle.getString("format")
                    val sms = SmsMessage.createFromPdu(pdu as ByteArray, format)

                    val sender = sms.originatingAddress
                    val body = sms.messageBody

                    Log.d("SmsReceiver", "📩 문자 수신됨 - 발신자: $sender, 내용: $body")

                    sendToServer(context, sender ?: "", body ?: "")
                }
            }
        }
    }


    private fun sendToServer(context: Context, sender: String, body: String) {
        val client = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("sender", sender)
            .add("message", body)
            .build()

        val request = Request.Builder()
            .url("http://192.168.0.50:8000/sms")  // <-- FastAPI 서버 주소
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SmsReceiver", "❌ 서버 전송 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body?.string()
                Log.d("SmsReceiver", "✅ 서버 응답: $responseText")

                // 여기서 응답이 "스팸"이면 팝업 띄우기 가능
            }
        })
    }
}
