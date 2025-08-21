package com.example.sbs.smishingdetector.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sbs.smishingdetector.viewmodel.DetectionViewModel
import com.example.sbs.smishingdetector.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    navController: NavController,
    detectionViewModel: DetectionViewModel = viewModel(),
    reportViewModel: ReportViewModel = viewModel()
) {
    val context = LocalContext.current

    // 최초 로딩
    LaunchedEffect(Unit) {
        detectionViewModel.loadDetections(context)
        reportViewModel.loadReportRows(context)
    }

    // 표 데이터
    val detectionRows = detectionViewModel.detections.map {
        listOf(it.received_at, "${it.sender}\n${it.message}")
    }
    val pagedDetections = detectionRows.chunked(3)
    val pagedReports = reportViewModel.reportRows.chunked(3)

    var detectionPage by remember { mutableStateOf(0) }
    var reportPage by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("신고내역 및 스미싱 목록", fontSize = 18.sp) },
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
                .padding(16.dp)
                .fillMaxSize()
        ) {
            FilterSection()
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // 탐지내역
                item { TableTitle("탐지내역") }
                item { TableHeader(listOf("수신일", "탐지내역")) }

                val detPageList = pagedDetections.getOrNull(detectionPage).orEmpty()
                if (detPageList.isEmpty()) {
                    item { EmptyRow("탐지내역이 없습니다.") }
                } else {
                    items(detPageList) { row -> TableRow(row) }
                }
                item {
                    PaginationControls(
                        currentPage = detectionPage,
                        totalPages = maxOf(pagedDetections.size, 1),
                        onPrev = { if (detectionPage > 0) detectionPage-- },
                        onNext = { if (detectionPage < (pagedDetections.size - 1)) detectionPage++ }
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                // 신고내역
                item { TableTitle("신고내역") }
                item { TableHeader(listOf("신고일", "신고 내역")) }

                val repPageList = pagedReports.getOrNull(reportPage).orEmpty()
                if (repPageList.isEmpty()) {
                    item { EmptyRow("신고내역이 없습니다.") }
                } else {
                    items(repPageList) { row -> TableRow(row) }
                }
                item {
                    PaginationControls(
                        currentPage = reportPage,
                        totalPages = maxOf(pagedReports.size, 1),
                        onPrev = { if (reportPage > 0) reportPage-- },
                        onNext = { if (reportPage < (pagedReports.size - 1)) reportPage++ }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection() {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("스미싱") }
    val options = listOf("악성앱", "피싱전화", "스미싱")

    var fromDate by remember { mutableStateOf("2025-02-27") }
    var toDate by remember { mutableStateOf("2025-04-17") }

    val roundedShape: Shape = RoundedCornerShape(16.dp)

    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                label = { Text("스미싱") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor(),
                shape = roundedShape
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            selectedOption = selectionOption
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = fromDate,
                onValueChange = {},
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )
            Text(" ~ ", modifier = Modifier.padding(horizontal = 6.dp))
            OutlinedTextField(
                value = toDate,
                onValueChange = {},
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {},
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("조회")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            listOf("1개월", "3개월", "6개월").forEach {
                OutlinedButton(onClick = {}, shape = RoundedCornerShape(16.dp)) {
                    Text(it)
                }
            }
        }
    }
}

@Composable
fun TableTitle(title: String) {
    Text(
        title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun TableHeader(columns: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE0E0E0))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        columns.forEachIndexed { index, text ->
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier
                    .weight(if (index == 0) 1f else 3f)
                    .padding(start = 4.dp),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
fun TableRow(values: List<String>) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(Color(0xFFF9F9F9)),
        verticalAlignment = Alignment.Top
    ) {
        // 날짜 (수신일/신고일)
        Box(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp, max = 100.dp)
                .padding(6.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = values.getOrNull(0) ?: "",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        // 발신자 번호 + 메시지
        Box(
            modifier = Modifier
                .weight(3f)
                .heightIn(min = 48.dp, max = 100.dp)
                .padding(6.dp)
                .verticalScroll(scrollState)
        ) {
            val sender = values.getOrNull(1)?.substringBefore("\n") ?: ""
            val message = values.getOrNull(1)?.substringAfter("\n") ?: ""

            Text(
                buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(sender)
                    }
                    append("\n")
                    append(message)
                },
                fontSize = 12.sp,
                softWrap = true,
                overflow = TextOverflow.Ellipsis,
                maxLines = 10
            )
        }
    }

    Divider(thickness = 0.5.dp, color = Color(0xFFE0E0E0))
}

@Composable
fun EmptyRow(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, fontSize = 13.sp, color = Color(0xFF888888))
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = onPrev, enabled = currentPage > 0) { Text("이전") }
        Text("${currentPage + 1} / $totalPages", modifier = Modifier.padding(horizontal = 16.dp), fontSize = 14.sp)
        Button(onClick = onNext, enabled = currentPage < totalPages - 1) { Text("다음") }
    }
}
