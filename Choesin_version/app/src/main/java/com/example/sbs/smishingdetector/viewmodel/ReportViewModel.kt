package com.example.sbs.smishingdetector.viewmodel

import android.content.Context
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

    // UI에서 바로 쓰기 좋은 형태(표 렌더링용)
    var reportRows by mutableStateOf<List<List<String>>>(emptyList())
        private set

    /**
     * 로그인/기기토큰 기반으로 신고 이력 로드.
     * X-Device-Token은 RetrofitClient.api(context)에서 자동 첨부됨.
     */
    fun loadReportRows(context: Context) {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.api(context)   // X-Device-Token 자동 첨부
                val resp = api.getReportHistory()       // Response<List<ReportHistory>>

                if (resp.isSuccessful) {
                    val list = resp.body().orEmpty()
                    // 필요시 정렬 가능: list.sortedByDescending { it.reported_at }
                    reportRows = list.map {
                        // ReportHistory(reported_at, sender, message) 라는 가정
                        listOf(it.reported_at, "${it.sender}\n${it.message}")
                    }
                } else {
                    val code = resp.code()
                    val err = resp.errorBody()?.string()
                    Log.e(TAG, "getReportHistory() failed: $code ${err ?: ""}")
                    reportRows = emptyList()
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

    /**
     * 신고 API 호출 후 목록 갱신.
     */
    fun reportSpam(context: Context, detectionId: Long) {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.api(context)
                val resp = api.reportSpam(detectionId) // Response<ReportResult or ResponseBody>

                if (resp.isSuccessful) {
                    // 필요 시 resp.body()?.report_id 활용 가능
                    loadReportRows(context)
                } else {
                    val code = resp.code()
                    val err = resp.errorBody()?.string()
                    Log.e(TAG, "reportSpam() failed: $code ${err ?: ""}")
                }
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP ${e.code()}: ${e.response()?.errorBody()?.string()}", e)
            } catch (e: IOException) {
                Log.e(TAG, "Network error", e)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error", e)
            }
        }
    }

    companion object {
        private const val TAG = "ReportViewModel"
    }
}
