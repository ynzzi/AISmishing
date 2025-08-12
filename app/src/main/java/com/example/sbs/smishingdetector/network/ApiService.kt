package com.example.sbs.smishingdetector.network

import com.example.sbs.smishingdetector.model.DetectionHistory
import com.example.sbs.smishingdetector.model.ReportHistory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded


data class AnalyzeRequest(val text: String)
data class AnalyzeResponse(val label: Int, val confidence: Float)

interface ApiService {
    @GET("detection")
    suspend fun getDetectionHistory(
        @Query("user_id") userId: String
    ): List<DetectionHistory>

    @GET("report")
    suspend fun getReportHistory(
        @Query("user_id") userId: String
    ): List<ReportHistory>

    @POST("/analyze")
    suspend fun analyzeMessage(@Body request: AnalyzeRequest): Response<AnalyzeResponse>

    @FormUrlEncoded
    @POST("/report")
    suspend fun reportSpam(
        @Field("user_id") userId: String,
        @Field("detection_id") detectionId: Int
    ): Response<Unit>

}

