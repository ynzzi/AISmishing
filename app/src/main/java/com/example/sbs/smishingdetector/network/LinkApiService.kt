package com.example.sbs.smishingdetector.network

import retrofit2.http.GET
import retrofit2.http.Path

interface LinkApiService {
    @GET("links/{domain}/malicious")
    suspend fun isMalicious(@Path("domain") domain: String): Boolean

    @GET("links/{domain}/count")
    suspend fun getReportCount(@Path("domain") domain: String): Int
}
