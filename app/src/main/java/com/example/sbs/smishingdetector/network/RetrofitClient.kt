//package com.example.sbs.smishingdetector.network
//
//import android.content.Context
//import com.example.sbs.smishingdetector.smsguard.TokenStorage
//import okhttp3.Interceptor
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import com.google.gson.GsonBuilder
//import java.util.concurrent.TimeUnit
//
//object RetrofitClient {
//    private const val BASE_URL = "http://20.196.64.253/" // 👉 실제 FastAPI 서버 주소로 바꿔줘
//
//    val apiService: ApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(ApiService::class.java)
//    }
//
//    val api = apiService
//
//}


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
    // ✅ 배포 IP로 통일
    private const val BASE_URL = "http://20.196.64.253/"

    private val gson by lazy { GsonBuilder().create() }

    private fun baseRetrofit(client: OkHttpClient? = null): Retrofit {
        val b = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
        if (client != null) b.client(client)
        return b.build()
    }

    private val logging by lazy {
        HttpLoggingInterceptor().apply {
            // 운영은 BASIC/NONE 권장
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /** ① 토큰 헤더 자동 첨부 버전 (이전 1번 기능) */
    fun api(context: Context): ApiService {
        val storage = TokenStorage(context)

        val headerInterceptor = Interceptor { chain ->
            val req = chain.request()
            val b = req.newBuilder()

            val dev = storage.getDeviceToken()?.trim()
            if (!dev.isNullOrEmpty()) b.header("X-Device-Token", dev)

            val jwt = storage.getAccessToken()?.trim()
            if (!jwt.isNullOrEmpty()) b.header("Authorization", "Bearer $jwt")

            chain.proceed(b.build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        return baseRetrofit(client).create(ApiService::class.java)
    }

    /** ② 헤더 없는 싱글톤 (2번과 동일 시그니처) — 기존 코드 호환용 */
    val api: ApiService by lazy {
        baseRetrofit().create(ApiService::class.java)
    }

    /** (필요 시) AI 클라이언트 하위호환 */
    fun getAIClient(): Retrofit = baseRetrofit()
}
