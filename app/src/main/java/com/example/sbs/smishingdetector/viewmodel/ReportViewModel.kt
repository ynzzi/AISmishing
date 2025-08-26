//package com.example.sbs.smishingdetector.viewmodel
//
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.sbs.smishingdetector.network.RetrofitClient
//import kotlinx.coroutines.launch
//
//class ReportViewModel : ViewModel() {
//    var reportRows by mutableStateOf<List<List<String>>>(emptyList())
//        private set
//
//    init {
//        loadInitialReportRows()
//    }
//
//    // 초기 하드코딩용
//    private fun loadInitialReportRows() {
//        viewModelScope.launch {
//            try {
//                val userId = "user001"
//                val reports = RetrofitClient.apiService.getReportHistory(userId)
//                reportRows = reports.map { listOf(it.reported_at, "${it.sender}\n${it.message}") }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    // ✅ 외부에서 사용자가 지정된 ID로 호출 가능하게 공개 함수 선언
//    fun loadReportRows(userId: String) {
//        viewModelScope.launch {
//            try {
//                val reports = RetrofitClient.apiService.getReportHistory(userId)
//                reportRows = reports.map { listOf(it.reported_at, "${it.sender}\n${it.message}") }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//}

package com.example.sbs.smishingdetector.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sbs.smishingdetector.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ReportViewModel : ViewModel() {

    var reportRows by mutableStateOf<List<List<String>>>(emptyList())
        private set

    /**
     * 사용자 ID 기준으로 신고 이력 로드.
     * ApiService는 suspend로 List를 직접 반환하므로 isSuccessful 체크가 필요 없음.
     */
    fun loadReportRows(userId: String) {
        viewModelScope.launch {
            try {
                val list = RetrofitClient.api.getReportHistory(userId) // suspend -> List
                // 필요 시 정렬 가능: val sorted = list.sortedByDescending { it.reported_at }
                reportRows = list.map {
                    // ReportHistory(reported_at, sender, message) 라는 가정
                    listOf(it.reported_at, "${it.sender}\n${it.message}")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP ${e.code()}: ${e.response()?.errorBody()?.string()}", e)
                reportRows = emptyList()
            } catch (e: IOException) {
                Log.e(TAG, "Network error", e)
                reportRows = emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error", e)
                reportRows = emptyList()
            }
        }
    }

    companion object {
        private const val TAG = "ReportViewModel"
    }
}

