package com.example.sbs.smishingdetector.model

import com.google.gson.annotations.SerializedName

//data class ReportHistory(
//    val reported_at: String,
//    val sender: String,
//    val message: String
//)

data class ReportHistory(
    @SerializedName("report_id")   val report_id: Long? = null,
    @SerializedName("reported_at") val reported_at: String,
    @SerializedName("sender")      val sender: String,
    @SerializedName("message")     val message: String
)