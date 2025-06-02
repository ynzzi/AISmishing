package com.example.smishingdetector.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.smishingdetector.R

@Composable
fun SmsPermissionScreen(navController: NavController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상단 이미지
            Image(
                painter = painterResource(id = R.drawable.ic_sms_permission),
                contentDescription = "SMS Permission",
                modifier = Modifier
                    .size(180.dp)
                    .padding(bottom = 24.dp)
            )

            // 타이틀
            Text(
                text = "SMS 접근 권한",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5A4FCF)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 설명 텍스트
            Text(
                text = "앱에서 수신된 문자 메세지를 \n" +
                        "읽을 수 있도록 허용합니다",
                fontSize = 14.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 확인 버튼 (아직 다음 화면 없음)
            Button(
                onClick = {
                    // 아직 다음 화면 없으므로 멈추기
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A4FCF))
            ) {
                Text("확인", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}
