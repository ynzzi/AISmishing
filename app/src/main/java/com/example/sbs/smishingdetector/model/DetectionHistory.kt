package com.example.sbs.smishingdetector.model

data class DetectionHistory(
    val received_at: String,
    val sender: String,
    val message: String
)