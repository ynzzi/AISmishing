// LoginScreen.kt
package com.example.smishingdetector.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.content.SharedPreferences

@Composable
fun LoginScreen(navController: NavController, prefs: SharedPreferences) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("이메일") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = "", onValueChange = {}, label = { Text("비밀번호") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            prefs.edit().putBoolean("isLoggedIn", true).apply()
            navController.navigate("permission")
        }, modifier = Modifier.fillMaxWidth()) {
            Text("로그인")
        }
    }
}