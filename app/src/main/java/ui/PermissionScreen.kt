package com.example.smishingdetector.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.core.content.ContextCompat
import com.example.smishingdetector.R

@Composable
fun PermissionScreen(navController: NavController, prefs: SharedPreferences) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var pushChecked by remember { mutableStateOf(true) }
    var contactChecked by remember { mutableStateOf(false) }

    // 진입 시/복귀 시 저장소 권한 확인
    var showStorageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showStorageDialog = !hasStoragePermission(context)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // 설정 화면 다녀온 뒤 재확인
                showStorageDialog = !hasStoragePermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (showStorageDialog) {
        AlertDialog(
            onDismissRequest = { showStorageDialog = false },
            title = { Text("저장소 접근 권한 필요") },
            text = {
                Text(
                    "파일 분석/선택 기능을 사용하려면 저장소 접근 권한이 필요합니다.\n" +
                            "설정 > 앱 권한에서 저장소(또는 미디어) 권한을 허용해 주세요."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStorageDialog = false
                        openStorageSettings(context)
                    }
                ) { Text("예") }
            },
            dismissButton = {
                TextButton(onClick = { showStorageDialog = false }) { Text("아니오") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "권한설정안내",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF322C91)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "서비스 이용을 위한 접근허용을 허용해주세요.",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        PermissionItem(
            icon = R.drawable.ic_push,
            title = "푸시 알림",
            description = "알림기능을 위해 허용합니다.",
            checked = pushChecked,
            onCheckedChange = { pushChecked = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        PermissionItem(
            icon = R.drawable.ic_contact,
            title = "연락처 액세스",
            description = "연락처 액세스를 위해 허용합니다.",
            checked = contactChecked,
            onCheckedChange = { contactChecked = it }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                prefs.edit().putBoolean("PermissionScreenShown", true).apply()
                navController.navigate("sms_permission") {
                    popUpTo("permission") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A4FCF))
        ) {
            Text("확인", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PermissionItem(
    icon: Int,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, onValueChange = onCheckedChange),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = title,
            modifier = Modifier.size(36.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(description, fontSize = 13.sp, color = Color.Gray)
        }

        Checkbox(
            checked = checked,
            onCheckedChange = null, // toggleable에서 이미 처리함
            enabled = false
        )

        Spacer(modifier = Modifier.width(8.dp))
        Text("동의합니다", color = Color.Gray, fontSize = 13.sp)
    }
}

/* ===== 권한/설정 이동 유틸 ===== */

private fun hasStoragePermission(context: Context): Boolean {
    // 1) All files 권한이 이미 있으면 OK (Android 11+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
        return true
    }

    return when {
        // 2) Android 13+ : 미디어 권한(이미지/비디오/오디오) 중 하나라도 부여되어 있으면 OK
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            val img = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
            val vid = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
            val aud = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            img || vid || aud
        }
        // 3) Android 12 이하 : READ_EXTERNAL_STORAGE 권한
        else -> {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}

private fun openStorageSettings(context: Context) {
    val pkgUri = Uri.parse("package:${context.packageName}")

    // 우선 All files 접근 설정 화면을 시도 (앱에서 해당 권한을 사용한다면 바로 진입)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, pkgUri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        } catch (_: Exception) {
            // 아래로 폴백
        }
    }

    // 앱 상세 설정(권한 탭)로 폴백
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, pkgUri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {
        // 최후 폴백: 일반 설정
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
