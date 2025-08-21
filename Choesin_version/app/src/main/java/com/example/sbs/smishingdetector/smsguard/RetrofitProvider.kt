// app/src/main/java/com/example/sbs/smishingdetector/smsguard/RetrofitProvider.kt
package com.example.sbs.smishingdetector.smsguard

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitProvider {

    // ✅ 슬래시로 끝나야 함
    private const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"

    fun api(context: Context, baseUrl: String = DEFAULT_BASE_URL): Api {
        val storage = TokenStorage(context)

        // X-Device-Token / Authorization 자동 첨부 인터셉터
        val deviceTokenInterceptor = Interceptor { chain ->
            val req = chain.request()
            val b = req.newBuilder()

            val dev = storage.getDeviceToken()?.trim()
            if (!dev.isNullOrEmpty()) {
                b.header("X-Device-Token", dev)
                android.util.Log.d("HTTP", "➡️ X-Device-Token attached: ${dev.take(16)}…")
            } else {
                android.util.Log.w("HTTP", "❌ X-Device-Token missing (TokenStorage empty)")
            }

            val jwt = storage.getAccessToken()?.trim()
            if (!jwt.isNullOrEmpty()) {
                b.header("Authorization", "Bearer $jwt")
            }

            chain.proceed(b.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(deviceTokenInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl) // ← 반드시 '/'로 끝남
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create()) // Moshi 사용 시 서버 JSON과 키 매핑 확인
            .build()
            .create(Api::class.java)
    }
}
