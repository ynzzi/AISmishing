package com.example.smishingdetector

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import okhttp3.*
import java.io.IOException
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.sbs.smishingdetector.viewmodel.DetectionViewModel
import com.example.sbs.smishingdetector.viewmodel.ReportViewModel

class SmishingPopupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_dialog)

        val phone = intent.getStringExtra("phone") ?: "Unknown"
        val msg = intent.getStringExtra("message") ?: ""
        val detectionID = intent.getIntExtra("detectionID", -1)

        findViewById<TextView>(R.id.messageText).text = "$phone \"$msg\""

        // ✅ 팝업 생성 시 탐지 이력 ViewModel을 통해 불러오기
        val detectionViewModel = ViewModelProvider(this).get(DetectionViewModel::class.java)
        detectionViewModel.loadDetections("user001")  // 사용자 ID는 실제 로그인 사용자로 대체

        findViewById<Button>(R.id.reportButton).setOnClickListener {
            sendReportToServer(phone, detectionID)
        }

        findViewById<Button>(R.id.ignoreButton).setOnClickListener {
            finish()
        }
    }

    private fun sendReportToServer(phone: String, detectionID: Int) {
        val client = OkHttpClient()
        val msg = intent.getStringExtra("message") ?: ""

        val requestBody = FormBody.Builder()
            .add("user_id", "user001")
            .add("sender", phone)
            .add("message", msg)
            .add("result", "스팸")
            .build()

        val request = Request.Builder()
            .url("http://192.168.0.247:8000/report_log")  // ✅ 변경된 API
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
                        // ✅ 신고 성공 시 신고 이력 ViewModel을 통해 불러오기
                        val reportViewModel = ViewModelProvider(this@SmishingPopupActivity).get(ReportViewModel::class.java)
                        reportViewModel.loadReportRows("user001")

                        val detectionViewModel = ViewModelProvider(this@SmishingPopupActivity).get(DetectionViewModel::class.java)
                        detectionViewModel.loadDetections("user001")

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

