//package com.example.sbs.smishingdetector.viewmodel
//
//import android.util.Log
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateListOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope  // ✅ 이거 꼭 필요
//import com.example.sbs.smishingdetector.model.DetectionHistory
//import com.example.sbs.smishingdetector.network.RetrofitClient
//import kotlinx.coroutines.launch // ✅ 이것도 필요
//
//class DetectionViewModel : ViewModel() {
//    val detections = mutableStateListOf<DetectionHistory>()
//
//    init {
//        loadDetections("user001")
//    }
//
//    fun loadDetections(userId: String) {
//        viewModelScope.launch {
//            try {
//                val result = RetrofitClient.api.getDetectionHistory(userId)
//                detections.clear()
//                detections.addAll(result)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    // TableRow용 리스트로 변환
//    val detectionRows: List<List<String>>
//        get() = detections.map { listOf(it.received_at, "${it.sender}\n${it.message}") }
//}

package com.example.sbs.smishingdetector.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sbs.smishingdetector.model.DetectionHistory
import com.example.sbs.smishingdetector.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class DetectionViewModel : ViewModel() {

    // UI에서 바로 관찰 가능한 상태 리스트
    val detections = mutableStateListOf<DetectionHistory>()

    /**
     * 사용자 ID 기준으로 이력 조회.
     * ApiService는 suspend로 List를 직접 반환하므로 isSuccessful 체크가 필요 없음.
     */
    fun loadDetections(userId: String) {
        viewModelScope.launch {
            try {
                val list = RetrofitClient.api.getDetectionHistory(userId) // suspend -> List
                detections.clear()
                // 필요 시 정렬/중복제거 가능:
                // val sorted = list.sortedByDescending { it.received_at }
                detections.addAll(list)
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP ${e.code()}: ${e.response()?.errorBody()?.string()}", e)
                detections.clear()
            } catch (e: IOException) {
                Log.e(TAG, "Network error", e)
                detections.clear()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error", e)
                detections.clear()
            }
        }
    }

    // 표 렌더링용 2열 리스트로 변환
    val detectionRows: List<List<String>>
        get() = detections.map { listOf(it.received_at, "${it.sender}\n${it.message}") }

    companion object {
        private const val TAG = "DetectionViewModel"
    }
}

