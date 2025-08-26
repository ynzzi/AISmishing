package com.example.sbs.smishingdetector.model

data class DetectionApkResult(
    val appName: String?,
    val packageName: String?,
    val prediction: String?,   // "악성", "위험", "정상"
    val sha256: String? = null
)

// 서버 응답 래퍼 (results 배열 포함)
data class DetectionApkResponse(
    val results: List<DetectionApkResult>
)