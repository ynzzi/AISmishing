// FileScanScreen.kt (수정본)
package com.example.smishingdetector.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sbs.smishingdetector.model.DetectionApkResult
import com.example.sbs.smishingdetector.viewmodel.FileScanViewModel

// ---- DetectionApkResult -> ScanFinding 매핑 ----
private fun DetectionApkResult.toScanFindingOrNull(): ScanFinding? {
    val pred = (this.prediction ?: "").trim().lowercase()
    val type = when (pred) {
        "악성", "malicious" -> ThreatType.MALWARE
        "위험", "suspicious", "risk", "notfound", "미등록" -> ThreatType.RISK
        else -> return null
    }

    val app = this.appName?.ifBlank { null } ?: this.packageName ?: "(알 수 없음)"
    val pkg = this.packageName ?: "(unknown)"
    val desc = when (type) {
        ThreatType.MALWARE -> "악성 파일입니다. 즉시 삭제할 것을 권고 합니다."
        ThreatType.RISK -> "악성 의심 파일로 분류되었습니다."
    }

    return ScanFinding(
        appName = app,
        packageName = pkg,
        description = desc,
        type = type
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileScanScreen(
    navController: NavController,
    viewModel: FileScanViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val primary = Color(0xFF5A4FCF)
    val progressBg = Color(0xFFF1D6F1)
    val ringTrack = Color(0xFFE6E6E6)

    LaunchedEffect(Unit) {
        viewModel.startFullStorageScan(context)
    }

    if (uiState.phase == FileScanViewModel.Phase.Done) {
        val findings: List<ScanFinding> =
            uiState.detections.mapNotNull { it.toScanFindingOrNull() }
                .sortedBy { if (it.type == ThreatType.MALWARE) 0 else 1 }

        // ⚠️ 여기서는 정의 안 하고 ScanResultScreen.kt 것을 불러옴
        ScanResultScreen(
            navController = navController,
            findings = findings
        )
        return
    }

    // --- 진행 중 화면 (기존 코드 동일) ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("바이러스 검사", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(60.dp))
            Text(
                text = "피싱 앱과 유사한 어플이\n설치되었는지 검사 중입니다.",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 26.sp,
                color = Color(0xFF222222),
                textAlign = TextAlign.Start
            )
            Spacer(Modifier.height(40.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = primary,
                        strokeWidth = 6.dp,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
            Spacer(Modifier.weight(1f))
            Text(uiState.statusText, color = Color.Gray, fontSize = 14.sp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(progressBg),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(uiState.progress.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF5A4FCF))
                )
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(uiState.progress.coerceIn(0f, 1f) * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(Modifier.height(60.dp))
        }
    }
}
