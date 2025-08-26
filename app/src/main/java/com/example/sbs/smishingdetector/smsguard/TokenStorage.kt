package com.example.sbs.smishingdetector.smsguard

import android.content.Context
import android.content.SharedPreferences

class TokenStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("smishing_tokens", Context.MODE_PRIVATE)

    // ✅ Access Token 저장
    fun saveAccessToken(token: String) {
        prefs.edit().putString("access_token", token).apply()
    }

    // ✅ Access Token 가져오기
    fun getAccessToken(): String? {
        return prefs.getString("access_token", null)
    }

    // ✅ Device Token 저장
    fun saveDeviceToken(token: String) {
        prefs.edit().putString("device_token", token).apply()
    }

    // ✅ Device Token 가져오기
    fun getDeviceToken(): String? {
        return prefs.getString("device_token", null)
    }

    // ✅ 로그아웃 등 전체 삭제
    fun clearTokens() {
        prefs.edit().clear().apply()
    }
}
