// app/src/main/java/com/example/sbs/smishingdetector/ui/LoginScreen.kt
package com.example.sbs.smishingdetector.ui

import android.content.SharedPreferences
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.sbs.smishingdetector.network.LoginReq
import com.example.sbs.smishingdetector.network.RetrofitClient
import com.example.sbs.smishingdetector.smsguard.TokenStorage
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, prefs: SharedPreferences) {
    val context = LocalContext.current
    val storage = remember { TokenStorage(context) }
    val scope = rememberCoroutineScope()

    // 이미 device_token 있으면 로그인 화면 스킵
    LaunchedEffect(Unit) {
        val existing = storage.getDeviceToken()
        if (!existing.isNullOrBlank()) {
            prefs.edit().putBoolean("isLoggedIn", true).apply()
            navController.navigate("permission") {
                popUpTo("login") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoggingIn by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("로그인", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("아이디") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                errorMsg = null
                isLoggingIn = true
                scope.launch {
                    try {
                        val api = RetrofitClient.api(context)
                        val did = deviceId(context)
                        val body = LoginReq(
                            username = username.trim(),
                            password = password,
                            device_id = did
                        )

                        // ✅ JSON 방식 호출: ApiService.loginJson(...)은 Response<LoginRes> 여야 함
                        val resp = api.loginJson(body)

                        if (resp.isSuccessful) {
                            val data = resp.body()
                            if (data == null) {
                                errorMsg = "서버 응답이 비어있습니다."
                            } else {
                                // ✅ TokenStorage에 두 개 다 저장
                                TokenStorage(context).apply {
                                    data.access_token?.let { saveAccessToken(it) }
                                    data.device_token?.let { saveDeviceToken(it) }
                                }

                                // ✅ 로그인 상태 플래그 저장
                                prefs.edit().putBoolean("isLoggedIn", true).apply()

                                // ✅ 다음 화면으로 이동
                                navController.navigate("permission") {
                                    popUpTo("login") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        } else {
                            val code = resp.code()
                            val err = resp.errorBody()?.string()
                            errorMsg = when (code) {
                                400, 422 -> "요청 형식이 올바르지 않습니다. (코드 $code)"
                                401 -> "아이디 또는 비밀번호가 올바르지 않습니다."
                                else -> "로그인 실패 ($code): ${err ?: "알 수 없는 오류"}"
                            }
                        }
                    } catch (e: HttpException) {
                        val code = e.code()
                        val err = e.response()?.errorBody()?.string()
                        e.printStackTrace()
                        errorMsg = "로그인 실패 ($code): ${err ?: e.message()}"
                    } catch (e: IOException) {
                        // 네트워크/타임아웃
                        e.printStackTrace()
                        errorMsg = "서버에 연결할 수 없습니다. 네트워크를 확인하세요."
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorMsg = "알 수 없는 오류가 발생했습니다."
                    } finally {
                        isLoggingIn = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = username.isNotBlank() && password.isNotBlank() && !isLoggingIn
        ) {
            Text(if (isLoggingIn) "로그인 중..." else "로그인")
        }

        if (errorMsg != null) {
            Spacer(Modifier.height(12.dp))
            Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
        }
    }
}

private fun deviceId(context: android.content.Context): String {
    val id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    return id ?: UUID.randomUUID().toString()
}
