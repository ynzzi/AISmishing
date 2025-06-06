package com.example.smishingdetector.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("신고내역 및 스미싱 목록", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("main") }) {
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
                item { TableTitle("탐지내역") }
                item { TableHeader(listOf("탐지일", "경로", "스미싱 내역", "접속")) }
                items(getSmishingRows()) { row -> TableRow(row) }

                item { Spacer(modifier = Modifier.height(24.dp)) }
                item { TableTitle("신고내역") }
                item { TableHeader(listOf("신고일", "경로", "신고 내역")) }
                items(getReportRows()) { row -> TableRow(row) }
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
                shape = roundedShape,
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
                shape = roundedShape,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {},
                modifier = Modifier.height(48.dp),
                shape = roundedShape
            ) {
                Text("조회")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            listOf("1개월", "3개월", "6개월").forEach {
                OutlinedButton(onClick = {}, shape = roundedShape) {
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
            .padding(6.dp)
    ) {
        columns.forEach { text ->
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TableRow(values: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        values.forEachIndexed { index, value ->
            Text(
                text = value,
                fontSize = 12.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                maxLines = 4,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun getSmishingRows(): List<List<String>> {
    return listOf(
        listOf("2025-03-01", "문자", "010-1140-1004\nhttps://t.me/dgsds1 스미싱으로 의심되는 url이 탐지되었습니다. 앱 설치 유도 등에 주의하세요.", "X"),
        listOf("2025-04-14", "apk 파일", "Recipe finder\n보이스피싱 의심 앱 설치자가 탐지되었습니다!\n바로 삭제 하시기 바랍니다.\n금융정보가 유출되거나 피해의 위험이 있습니다.", "O")
    )
}

fun getReportRows(): List<List<String>> {
    return listOf(
        listOf("2025-03-01", "문자", "010-1140-1004\nhttps://t.me/dgsds1 스미싱으로 의심되는 url이 탐지되었습니다. 앱 설치 유도 등에 주의하세요."),
        listOf("2025-04-14", "apk 파일", "Recipe finder\n보이스피싱 의심 앱 설치자가 탐지되었습니다!\n바로 삭제 하시기 바랍니다.\n금융정보가 유출되거나 피해의 위험이 있습니다.")
    )
}
