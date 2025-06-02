package com.example.smishingdetector.ui

import android.content.SharedPreferences
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun PermissionScreen(navController: NavController, prefs: SharedPreferences) {
    var pushChecked by remember { mutableStateOf(true) }
    var contactChecked by remember { mutableStateOf(false) }

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
                navController.navigate("main") {
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
