package com.example.sbs.smishingdetector.model

import com.google.gson.annotations.SerializedName

data class DetectionHistory(
    @SerializedName("detection_id") val detection_id: Long? = null,
    @SerializedName("received_at")  val received_at: String,
    @SerializedName("sender")       val sender: String,
    @SerializedName("message")      val message: String
)