package com.example.sbs.smishingdetector.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class LinkVerdict(
    val malicious: Boolean,
    val source: String,
    val reportCount: Int
)

interface LinkApiService {
    @GET("links/{domain}/malicious")
    suspend fun isMalicious(@Path("domain") domain: String): Boolean

    @GET("links/{domain}/count")
    suspend fun getReportCount(@Path("domain") domain: String): Int

    @GET("links/check")
    suspend fun checkLink(@Query("url") url: String): LinkVerdict
}
