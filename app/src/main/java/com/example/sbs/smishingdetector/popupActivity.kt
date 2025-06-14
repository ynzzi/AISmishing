package com.example.sbs.smishingdetector

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ui.SmishingAlertDialog

class PopupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔄 전달받은 문자 정보
        val sender = intent.getStringExtra("sender") ?: "알 수 없음"
        val message = intent.getStringExtra("message") ?: "내용 없음"

        // ✅ 팝업 띄우기
        SmishingAlertDialog.show(this, sender, message)

        // 🛑 팝업 띄우고 액티비티 종료
        finish()
    }
}
