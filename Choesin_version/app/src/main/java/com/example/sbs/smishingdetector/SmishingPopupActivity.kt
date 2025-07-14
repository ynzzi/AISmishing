package com.example.smishingdetector

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import okhttp3.*
import java.io.IOException
import android.util.Log
import android.widget.Toast

class SmishingPopupActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_dialog)

        val phone = intent.getStringExtra("phone") ?: "Unknown"
        val msg = intent.getStringExtra("message") ?: ""
        val detectionID = intent.getIntExtra("detectionID", -1)

        findViewById<TextView>(R.id.messageText).text = "$phone \"$msg\""

        findViewById<Button>(R.id.reportButton).setOnClickListener {
            sendReportToServer(phone, detectionID)
        }
        findViewById<Button>(R.id.ignoreButton).setOnClickListener {
            finish()
        }
    }

    private fun sendReportToServer(phone: String, detectionID: Int) {
        val client = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("user_id", "user001")
            .add("sender", phone)
            .add("detection_id", detectionID.toString())
            .build()

        val request = Request.Builder()
            .url("http://172.30.1.32:8080/report")  // ← 환경에 따라 수정
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Popup", "❌ 신고 전송 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("Popup", "✅ 신고 전송 성공: ${response.code}")
                    runOnUiThread {
                        Toast.makeText(this@SmishingPopupActivity, "신고되었습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    val errorBody = response.body?.string() ?: "No body"
                    Log.e("Popup", "❌ 서버 오류 응답: ${response.code} - $errorBody")
                }
            }
        })
    }
}
