package com.example.smishingdetector

// SmishingPopupActivity.kt
import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.smishingdetector.R

class SmishingPopupActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_dialog)  // 기존 XML 재사용

        val phone = intent.getStringExtra("phone") ?: "Unknown"
        val msg = intent.getStringExtra("message") ?: ""

        findViewById<TextView>(R.id.messageText).text = "$phone \"$msg\""

        findViewById<Button>(R.id.reportButton).setOnClickListener { finish() }
        findViewById<Button>(R.id.ignoreButton).setOnClickListener { finish() }
    }
}
