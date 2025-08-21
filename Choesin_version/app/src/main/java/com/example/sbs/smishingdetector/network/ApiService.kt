// app/src/main/java/com/example/sbs/smishingdetector/network/ApiService.kt
package com.example.sbs.smishingdetector.network

import com.example.sbs.smishingdetector.model.DetectionHistory
import com.example.sbs.smishingdetector.model.ReportHistory
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

// === 공용 모델 ===
data class AnalyzeRequest(
    @SerializedName("text") val text: String
)
data class AnalyzeResponse(
    @SerializedName("label") val label: Int?,          // 0/1
    @SerializedName("confidence") val confidence: Float?
)

// === 인증 ===
data class LoginReq(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("device_id") val device_id: String? = null
)

data class LoginRes(
    @SerializedName("access_token") val access_token: String?,
    @SerializedName("token_type") val token_type: String?,
    @SerializedName("device_token") val device_token: String?
)

data class SignupRes(
    @SerializedName("ok") val ok: Boolean? = null,
    @SerializedName("message") val message: String? = null
)

data class CheckUsernameRes(
    @SerializedName("available") val available: Boolean
)

// === /sms 응답 ===
data class SmsRes(
    @SerializedName("sender") val sender: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("result") val result: String?,         // "스팸"/"정상" 등
    @SerializedName("matched") val matched: String?,
    @SerializedName("similarity") val similarity: Double?,
    @SerializedName("by") val by: String?,                 // "db"/"ai" 등
    @SerializedName("user_id") val user_id: String?,
    // 서버가 detection_id도 내려주면 여기에 추가
    @SerializedName("detection_id") val detection_id: Int? = null
)

// === /report 응답 ===
data class ReportResult(
    @SerializedName("report_id") val report_id: Long?
)

interface ApiService {

    // ===== 인증 =====

    // JSON 로그인
    @POST("/auth/login")
    suspend fun loginJson(@Body body: LoginReq): Response<LoginRes>

    // (선택) Form 로그인
    @FormUrlEncoded
    @POST("/auth/login")
    suspend fun loginForm(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("device_id") deviceId: String? = null
    ): Response<LoginRes>

    // 아이디 중복 확인
    @GET("/auth/check-username")
    suspend fun checkUsername(@Query("username") username: String): Response<CheckUsernameRes>

    // 회원가입
    @FormUrlEncoded
    @POST("/auth/signup")
    suspend fun signup(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("phone") phone: String,
        @Field("email") email: String
    ): Response<SignupRes>

    // ===== SMS 분류/저장 =====
    // X-Device-Token 헤더는 RetrofitClient 인터셉터에서 자동 첨부됨
    @FormUrlEncoded
    @POST("/sms")
    suspend fun sendSms(
        @Field("sender") sender: String,
        @Field("message") message: String
    ): Response<SmsRes>

    // ===== 이력 조회 =====
    // 서버 라우트가 /history/* 라면 아래 주석을 교체 사용하세요.
    // @GET("/history/detections")
    // suspend fun getDetectionHistory(): Response<List<DetectionHistory>>
    //
    // @GET("/history/reports")
    // suspend fun getReportHistory(): Response<List<ReportHistory>>

    @GET("/detection")
    suspend fun getDetectionHistory(): Response<List<DetectionHistory>>

    @GET("/report")
    suspend fun getReportHistory(): Response<List<ReportHistory>>

    // ===== 신고 등록 =====
    @FormUrlEncoded
    @POST("/report")
    suspend fun reportSpam(
        @Field("detection_id") detectionId: Long
    ): Response<ReportResult>

    // ===== 분석 API (AI 서버) =====
    @POST("/analyze")
    suspend fun analyzeMessage(@Body request: AnalyzeRequest): Response<AnalyzeResponse>
}
