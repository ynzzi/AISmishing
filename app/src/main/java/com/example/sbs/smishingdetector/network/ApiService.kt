//package com.example.sbs.smishingdetector.network
//
//import com.example.sbs.smishingdetector.model.DetectionHistory
//import com.example.sbs.smishingdetector.model.ReportHistory
//import retrofit2.http.Body
//import retrofit2.http.GET
//import retrofit2.http.POST
//import retrofit2.http.Query
//import retrofit2.Response
//import retrofit2.http.Field
//import retrofit2.http.FormUrlEncoded
//
//
//data class AnalyzeRequest(val text: String)
//data class AnalyzeResponse(val label: Int, val confidence: Float)
//
//interface ApiService {
//    @GET("sms/detection")
//    suspend fun getDetectionHistory(
//        @Query("user_id") userId: String
//    ): List<DetectionHistory>
//
//    @GET("sms/report")
//    suspend fun getReportHistory(
//        @Query("user_id") userId: String
//    ): List<ReportHistory>
//
//    @POST("/analyze")
//    suspend fun analyzeMessage(@Body request: AnalyzeRequest): Response<AnalyzeResponse>
//
//    @FormUrlEncoded
//    @POST("sms/report")
//    suspend fun reportSpam(
//        @Field("user_id") userId: String,
//        @Field("detection_id") detectionId: Int
//    ): Response<Unit>
//
//}

package com.example.sbs.smishingdetector.network

import com.example.sbs.smishingdetector.model.DetectionHistory
import com.example.sbs.smishingdetector.model.ReportHistory
import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded


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

    // JSON 로그인
    @POST("/login")
    suspend fun loginJson(@Body body: LoginReq): Response<LoginRes>

    // (선택) Form 로그인
    @FormUrlEncoded
    @POST("/login")
    suspend fun loginForm(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("device_id") deviceId: String? = null
    ): Response<LoginRes>

    // 아이디 중복 확인
    @GET("/check-username")
    suspend fun checkUsername(@Query("username") username: String): Response<CheckUsernameRes>

    // 회원가입
    @FormUrlEncoded
    @POST("/signup")
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


    @GET("sms/detection")
    suspend fun getDetectionHistory(
        @Query("user_id") userId: String
    ): List<DetectionHistory>

    @GET("sms/report")
    suspend fun getReportHistory(
        @Query("user_id") userId: String
    ): List<ReportHistory>

    @POST("/analyze")
    suspend fun analyzeMessage(@Body request: AnalyzeRequest): Response<AnalyzeResponse>

    @FormUrlEncoded
    @POST("sms/report")
    suspend fun reportSpam(
        @Field("user_id") userId: String,
        @Field("detection_id") detectionId: Int
    ): Response<Unit>

}