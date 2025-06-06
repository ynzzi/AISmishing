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
            // 📷 상단 이미지
            Image(
                painter = painterResource(id = R.drawable.ic_sms_permission),
                contentDescription = "SMS Permission",
                modifier = Modifier
                    .size(180.dp)
                    .padding(bottom = 24.dp)
            )

            // 📌 타이틀
            Text(
                text = "SMS 접근 허용",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5A4FCF)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 📝 설명 텍스트
            Text(
                text = "앱에서 수신된 문자 메세지를\n읽을 수 있도록 허용합니다",
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            // ✅ [허용] 버튼 → main 화면으로 이동
            Button(
                onClick = {
                    navController.navigate("main") {
                        popUpTo("sms_permission") { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A4FCF))
            ) {
                Text("허용", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ❌ [거부] 버튼 → 이전 화면으로 되돌아가기
            OutlinedButton(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5A4FCF)),
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Text("거부", fontSize = 16.sp)
            }
        }
    }
}
