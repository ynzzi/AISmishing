package com.example.sbs.smishingdetector.network

data class FeaturePayload(
    val userId : String,
    val appName: String,                  // 앱 이름
    val packageName: String,              // 패키지명
    val apk_permission_list: List<String>,// 퍼미션 리스트
    val apk_api_list: List<String>,       // API 리스트
    val entropy_mean: Double?,            // 엔트로피 평균
    val entropy_max: Double?,             // 엔트로피 최대
    val entropy_std: Double?,             // 엔트로피 표준편차
    val entropy_p95: Double?,             // 엔트로피 95퍼센트
    val ext_cnt_dex: Int?,                // .dex 파일 개수
    val ext_cnt_png: Int?,                // .png 파일 개수
    val ext_cnt_xml: Int?,                // .xml 파일 개수
    val sha256: String?,
    val iconBase64: String?               // ✅ 아이콘 Base64
)