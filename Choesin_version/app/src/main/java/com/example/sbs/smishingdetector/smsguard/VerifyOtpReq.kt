package com.example.sbs.smishingdetector.smsguard

import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

data class VerifyOtpReq(val phone: String, val otp: String, val device_id: String?)
data class VerifyOtpRes(val access_token: String, val token_type: String, val device_token: String?)
data class SmsRes(
    val sender: String,
    val message: String,
    val result: String,
    val matched: String?,
    val similarity: Double,
    val by: String,
    val user_id: String
)

interface Api {
    @POST("/auth/otp/verify")
    suspend fun verifyOtp(@Body body: VerifyOtpReq): VerifyOtpRes

    @FormUrlEncoded
    @POST("/sms")
    suspend fun sendSms(
        @Field("sender") sender: String,
        @Field("message") message: String
    ): SmsRes
}