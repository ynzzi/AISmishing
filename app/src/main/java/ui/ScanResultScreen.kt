package com.example.smishingdetector.ui

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smishingdetector.R
import androidx.compose.ui.draw.paint

enum class ThreatType { MALWARE, RISK }

data class ScanFinding(
    val appName: String,
    val packageName: String,
    val description: String,
    val type: ThreatType,
    val iconBase64: String? = null // 앱 아이콘(Base64)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanResultScreen(
    navController: NavController,
    findings: List<ScanFinding> = emptyList(),
    onDeleteClick: () -> Unit = {} // (사용 안 함) 하단 버튼은 실제 언인스톨로 동작시킴
) {
    val primary = Color(0xFF5A4FCF)
    val redText = Color(0xFFD32F2F)
    val dangerCardBg = Color(0xFFFFE3E3)
    val borderGray = Color(0xFFE6E6E6)
    val safeGreen = Color(0xFF22C55E)
    val screenBg = Color(0xFFFDF7FF)

    // 화면에서 관리할 가변 목록(초기값 = 전달받은 findings)
    val findingList = remember { mutableStateListOf<ScanFinding>().apply { addAll(findings) } }

    // ⋮ 메뉴가 열린 항목 추적 (packageName 기준)
    var menuOpenFor by remember { mutableStateOf<String?>(null) }

    // 일괄/개별 언인스톨 큐 (packageName 목록)
    val uninstallQueue = remember { mutableStateListOf<String>() }

    // 런처 참조를 별도 state 에 저장(자기참조 방지용)
    val launcherRef = remember { mutableStateOf<androidx.activity.result.ActivityResultLauncher<Intent>?>(null) }

    // 언인스톨 런처: 결과 콜백 받아 성공 시 목록에서 제거, 다음 앱 이어서 실행
    val uninstallLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val finished = uninstallQueue.firstOrNull()
        if (finished != null) {
            if (result.resultCode == Activity.RESULT_OK) {
                val idx = findingList.indexOfFirst { it.packageName == finished }
                if (idx >= 0) findingList.removeAt(idx)
            }
            uninstallQueue.removeAt(0)
            if (uninstallQueue.isNotEmpty()) {
                val next = uninstallQueue.first()
                launcherRef.value?.let { launchUninstall(it, next) }
            }
        }
    }
    // 초기화 후 참조 저장
    LaunchedEffect(uninstallLauncher) {
        launcherRef.value = uninstallLauncher
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("바이러스 검사", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(screenBg)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            if (findingList.isEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "안전",
                        tint = safeGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("안전", color = safeGreen, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "위협이 발견되지 않았습니다.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "바이러스 검사 결과",
                    fontSize = 13.sp,
                    color = Color(0xFFB0B0B0)
                )

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text("뒤로가기", color = Color.White, fontSize = 16.sp)
                }

                Spacer(Modifier.height(24.dp))
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = "위험",
                        tint = redText,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("위험", color = redText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "${findingList.size}개의 위험이 탐지 되었습니다.",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "바이러스 검사 결과",
                    fontSize = 13.sp,
                    color = Color(0xFFB0B0B0)
                )

                Spacer(Modifier.height(18.dp))
                Text(
                    text = "탐지 내용(${findingList.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF222222)
                )
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(findingList, key = { it.packageName }) { item ->
                        ThreatCard(
                            item = item,
                            dangerCardBg = dangerCardBg,
                            borderGray = borderGray,
                            menuExpanded = (menuOpenFor == item.packageName),
                            onMenuToggle = { open -> menuOpenFor = if (open) item.packageName else null },
                            onDeleteClick = {
                                // 개별 언인스톨: 큐에 넣고 즉시 실행 or 다음 대기
                                if (uninstallQueue.isEmpty()) {
                                    uninstallQueue.add(item.packageName)
                                    launcherRef.value?.let { launchUninstall(it, item.packageName) }
                                } else {
                                    uninstallQueue.add(item.packageName)
                                }
                                menuOpenFor = null
                            }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }

                Button(
                    onClick = {
                        // 전체 언인스톨: 큐 비우고 현재 목록 패키지로 채운 뒤 순차 실행
                        if (findingList.isNotEmpty()) {
                            uninstallQueue.clear()
                            uninstallQueue.addAll(findingList.map { it.packageName })
                            launcherRef.value?.let { launchUninstall(it, uninstallQueue.first()) }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text("삭제하기", color = Color.White, fontSize = 16.sp)
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private fun launchUninstall(
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>,
    packageName: String
) {
    try {
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
            data = Uri.parse("package:$packageName")
            putExtra(Intent.EXTRA_RETURN_RESULT, true)
        }
        launcher.launch(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        // TODO: 사용자에게 "삭제 불가한 앱입니다" 안내
    }
}

@Composable
private fun ThreatCard(
    item: ScanFinding,
    dangerCardBg: Color,
    borderGray: Color,
    menuExpanded: Boolean,
    onMenuToggle: (Boolean) -> Unit,
    onDeleteClick: () -> Unit
) {
    val isMalware = item.type == ThreatType.MALWARE
    val containerColor = if (isMalware) dangerCardBg else Color.White
    val border = if (isMalware) null else BorderStroke(1.dp, borderGray)

    Surface(
        color = containerColor,
        tonalElevation = if (isMalware) 0.dp else 1.dp,
        shadowElevation = if (isMalware) 0.dp else 1.dp,
        shape = RoundedCornerShape(14.dp),
        border = border,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(base64 = item.iconBase64, size = 40.dp)

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.appName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF222222),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.description,
                    fontSize = 13.sp,
                    color = Color(0xFF5A5A5A),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box {
                IconButton(onClick = { onMenuToggle(!menuExpanded) }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "옵션")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { onMenuToggle(false) }
                ) {
                    DropdownMenuItem(
                        text = { Text("삭제하기") },
                        onClick = onDeleteClick
                    )
                }
            }
        }
    }
}

/** Base64 아이콘을 원형 박스에 표시. 실패 시 기본 런처 아이콘(전경)으로 폴백 */
@Composable
private fun AppIcon(base64: String?, size: Dp) {
    val imageBitmap: ImageBitmap? = remember(base64) {
        if (base64.isNullOrBlank()) return@remember null
        val cleaned = base64.substringAfter("base64,", base64)

        fun decode(flags: Int): ImageBitmap? = try {
            val bytes = Base64.decode(cleaned, flags)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        } catch (_: Exception) { null }

        // DEFAULT → NO_WRAP → URL_SAFE|NO_WRAP 순서로 시도
        decode(Base64.DEFAULT)
            ?: decode(Base64.NO_WRAP)
            ?: decode(Base64.URL_SAFE or Base64.NO_WRAP)
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFEFF7EE)),
        contentAlignment = Alignment.Center
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "앱 아이콘",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .padding(2.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "앱 아이콘",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .paint( // 배경으로 drawable 그리기
                        painterResource(R.drawable.ic_launcher_background),
                        contentScale = ContentScale.Crop
                    )
                    .padding(2.dp)
            )
        }
    }
}
