package com.example.sbs.smishingdetector.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sbs.smishingdetector.network.RetrofitClient
import kotlinx.coroutines.launch

class ReportViewModel : ViewModel() {
    var reportRows by mutableStateOf<List<List<String>>>(emptyList())
        private set

    init {
        loadInitialReportRows()
    }

    // 초기 하드코딩용
    private fun loadInitialReportRows() {
        viewModelScope.launch {
            try {
                val userId = "user001"
                val reports = RetrofitClient.apiService.getReportHistory(userId)
                reportRows = reports.map { listOf(it.reported_at, "${it.sender}\n${it.message}") }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ✅ 외부에서 사용자가 지정된 ID로 호출 가능하게 공개 함수 선언
    fun loadReportRows(userId: String) {
        viewModelScope.launch {
            try {
                val reports = RetrofitClient.apiService.getReportHistory(userId)
                reportRows = reports.map { listOf(it.reported_at, "${it.sender}\n${it.message}") }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
