package com.example.sbs.smishingdetector.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope  // ✅ 이거 꼭 필요
import com.example.sbs.smishingdetector.model.DetectionHistory
import com.example.sbs.smishingdetector.network.RetrofitClient
import kotlinx.coroutines.launch // ✅ 이것도 필요

class DetectionViewModel : ViewModel() {
    val detections = mutableStateListOf<DetectionHistory>()

    init {
        loadDetections("user001")
    }

    fun loadDetections(userId: String) {
        viewModelScope.launch {
            try {
                val result = RetrofitClient.api.getDetectionHistory(userId)
                detections.clear()
                detections.addAll(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // TableRow용 리스트로 변환
    val detectionRows: List<List<String>>
        get() = detections.map { listOf(it.received_at, "${it.sender}\n${it.message}") }
}