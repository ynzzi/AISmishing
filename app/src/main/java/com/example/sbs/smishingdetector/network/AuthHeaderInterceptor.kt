package com.example.sbs.smishingdetector.network

import android.content.Context
import android.util.Log
import com.example.sbs.smishingdetector.smsguard.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 로그인 후 TokenStorage 에 저장된
 * - X-Device-Token
 * - Authorization: Bearer <JWT>
 * 를 자동으로 붙여주는 인터셉터
 */
class AuthHeaderInterceptor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val storage = TokenStorage(context)
        val req = chain.request()
        val b = req.newBuilder()

        val dev = storage.getDeviceToken()?.trim()
        if (!dev.isNullOrEmpty()) {
            b.header("X-Device-Token", dev)
            Log.d("HTTP", "➡️ X-Device-Token attached: ${dev.take(16)}…")
        } else {
            Log.w("HTTP", "❌ X-Device-Token missing")
        }

        val jwt = storage.getAccessToken()?.trim()
        if (!jwt.isNullOrEmpty()) {
            b.header("Authorization", "Bearer $jwt")
        } else {
            Log.w("HTTP", "❌ Authorization JWT missing")
        }

        return chain.proceed(b.build())
    }
}
