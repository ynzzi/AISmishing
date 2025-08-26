package com.example.sbs.smishingdetector.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog  // ← 이 import가 반드시 필요합니다

@Composable
fun LinkWarningDialog(
    reportCount: Int,
    onOpen: () -> Unit,
    onBlock: () -> Unit
) {
    Dialog(onDismissRequest = { /* 닫기 방지 */ }) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFF635CFF),
                    modifier = Modifier.size(40.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "악성 사이트로 의심되는 링크입니다.\n접속하시겠습니까?",
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "- 스미싱 범죄 신고 건수 : ${reportCount}회",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onBlock,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("차단")
                    }
                    Button(
                        onClick = onOpen,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("연결")
                    }
                }
            }
        }
    }
}