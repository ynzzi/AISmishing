package com.example.sbs.smishingdetector.network

import com.example.sbs.smishingdetector.model.DetectionHistory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://172.30.1.20:8000/" // 👉 실제 FastAPI 서버 주소로 바꿔줘

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val api = apiService

    fun getAIClient(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://172.30.1.20:8010/")  // 🔁 실제 AI 서버 주소로 수정
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}
