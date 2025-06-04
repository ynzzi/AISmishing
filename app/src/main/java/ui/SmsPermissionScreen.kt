package com.example.smishingdetector.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.smishingdetector.R
import com.example.smishingdetector.feature.permission.PermissionManager

@Composable
fun SmsPermissionScreen(navController: NavController) {
    val context = LocalContext.current

    // ✅ 현재 권한 상태 확인
    val hasPermission = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    }

    // ✅ 권한이 이미 있으면 → 바로 기본 문자 앱 요청 및 메인으로 이동
    LaunchedEffect(Unit) {
        if (hasPermission) {
            PermissionManager.requestDefaultSmsAppIfNeeded(context)
            navController.navigate("main") {
                popUpTo("sms_permission") { inclusive = true }
            }
        }
    }

    // ✅ 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.all { it.value }
        if (granted) {
            // ✅ 권한 허용되면 기본 문자 앱 설정 요청
            PermissionManager.requestDefaultSmsAppIfNeeded(context)
            navController.navigate("main") {
                popUpTo("sms_permission") { inclusive = true }
            }
        }
    }

    // ✅ 권한 없을 경우 UI 보여줌
    if (!hasPermission) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_sms_permission),
                    contentDescription = "SMS Permission Icon",
                    modifier = Modifier
                        .size(180.dp)
                        .padding(bottom = 24.dp)
                )

                Text(
                    text = "SMS 접근 권한",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A4FCF)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "앱에서 수신된 문자 메시지를\n읽을 수 있도록 허용해주세요.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        PermissionManager.requestSmsPermissions(
                            context = context,
                            launcher = permissionLauncher,
                            onGranted = { } // 람다는 사용 안 함
                        )
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
}
