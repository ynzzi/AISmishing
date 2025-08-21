package com.example.sbs.smishingdetector.network

import android.content.Context
import com.example.sbs.smishingdetector.smsguard.TokenStorage
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    fun api(context: Context): ApiService {
        val storage = TokenStorage(context)

        val headerInterceptor = Interceptor { chain ->
            val req = chain.request()
            val b = req.newBuilder()

            val dev = storage.getDeviceToken()?.trim()
            if (!dev.isNullOrEmpty()) {
                b.header("X-Device-Token", dev)
                android.util.Log.d("HTTP", "➡️ X-Device-Token attached: ${dev.take(16)}…")
            } else {
                android.util.Log.w("HTTP", "❌ X-Device-Token missing (TokenStorage is empty)")
            }

            val jwt = storage.getAccessToken()?.trim()
            if (!jwt.isNullOrEmpty()) {
                b.header("Authorization", "Bearer $jwt")
            }

            chain.proceed(b.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            // BODY면 헤더도 보입니다(X-Device-Token은 민감헤더가 아니라 기본 redaction 안됨)
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder().create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    fun getAIClient(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://172.30.1.32:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
