package com.example.smishingdetector.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🛡️ 중앙 아이콘
        Image(
            painter = painterResource(id = R.drawable.ic_protect),
            contentDescription = "보호 아이콘",
            modifier = Modifier
                .height(200.dp)
                .padding(bottom = 32.dp)
        )

        // 📝 제목
        Text(
            text = "불법 스미싱 탐지",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 🧾 부제
        Text(
            text = "\"당신의 통화와 메시지를 안전하게 지켜드립니다.\"",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 🔐 로그인 버튼
        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5A4FCF),
                contentColor = Color.White
            )
        ) {
            Text("로그인")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🧾 가입하기 버튼 (테두리만 있음)
        OutlinedButton(
            onClick = { navController.navigate("signup") },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF5A4FCF)
            ),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Text("가입하기")
        }
    }
}
