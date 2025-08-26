package com.example.sbs.smishingdetector.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

fun buildAuthedOkHttp(context: Context): OkHttpClient {
    val logging = HttpLoggingInterceptor().apply {
        // 운영에서는 BASIC/NONE 권장
        level = HttpLoggingInterceptor.Level.BODY
    }
    return OkHttpClient.Builder()
        .addInterceptor(AuthHeaderInterceptor(context))
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()
}
