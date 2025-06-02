package com.example.smishingdetector.ui

import android.content.SharedPreferences
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, prefs: SharedPreferences) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("메뉴", modifier = Modifier.padding(16.dp))
                Divider()
                TextButton(onClick = {
                    prefs.edit().clear().apply()
                    navController.navigate("welcome") {
                        popUpTo("main") { inclusive = true }
                    }
                }) {
                    Text("로그아웃", color = Color.Red)
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("어플 이름") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        TextButton(onClick = {
                            prefs.edit().clear().apply()
                            navController.navigate("welcome") {
                                popUpTo("main") { inclusive = true }
                            }
                        }) {
                            Text("로그아웃", color = Color.White)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularStatusBox()
                Spacer(modifier = Modifier.height(24.dp))
                Text("실시간으로 검사하여 보이스피싱을 예방합니다", color = Color.Gray)
                Spacer(modifier = Modifier.height(48.dp))
                Text("스미싱 목록", fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatCard("악성앱", "1 건")
                    StatCard("피싱전화", "7 건")
                    StatCard("스미싱", "5 건")
                }
            }
        }
    }
}

@Composable
fun CircularStatusBox() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .border(8.dp, Color(0xFFE0E0E0), CircleShape)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("피싱 검사", color = Color(0xFF5A4FCF), fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("등록된 기기에서\n검사를 진행중입니다.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun StatCard(title: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 14.sp, color = Color.Gray)
        Text(count, fontSize = 16.sp, color = Color(0xFF3F51B5))
    }
}
