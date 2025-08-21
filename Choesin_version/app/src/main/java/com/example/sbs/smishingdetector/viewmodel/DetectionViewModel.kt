package com.example.sbs.smishingdetector.viewmodel

import android.content.Context
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
     * 로그인된 사용자(또는 등록된 기기 토큰) 기준으로 이력 조회.
     * X-Device-Token은 RetrofitClient.api(context)에서 자동 첨부됨.
     */
    fun loadDetections(context: Context) {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.api(context)
                val resp = api.getDetectionHistory()  // Response<List<DetectionHistory>>

                if (resp.isSuccessful) {
                    val body = resp.body()
                    detections.clear()
                    if (body != null) {
                        // 필요시 정렬/중복제거 가능:
                        // val sorted = body.sortedByDescending { it.received_at }
                        // detections.addAll(sorted)
                        detections.addAll(body)
                    } else {
                        Log.w(TAG, "getDetectionHistory(): body is null")
                    }
                } else {
                    val code = resp.code()
                    val err = resp.errorBody()?.string()
                    Log.e(TAG, "getDetectionHistory() failed: $code ${err ?: ""}")
                    detections.clear()
                }
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
