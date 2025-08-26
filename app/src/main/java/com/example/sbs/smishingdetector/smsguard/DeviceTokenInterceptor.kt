package com.example.sbs.smishingdetector.smsguard

import okhttp3.Interceptor
import okhttp3.Response
import com.example.sbs.smishingdetector.smsguard.TokenStorage

class DeviceTokenInterceptor(private val storage: TokenStorage): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request().newBuilder()
        storage.getDeviceToken()?.let { req.header("X-Device-Token", it) }
        return chain.proceed(req.build())
    }
}