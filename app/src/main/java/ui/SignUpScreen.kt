//package com.example.smishingdetector.ui
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SignUpScreen(navController: NavController) {
//    var id by remember { mutableStateOf("") }
//    var phone by remember { mutableStateOf("") }
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var confirmPassword by remember { mutableStateOf("") }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("가입하기", color = Color(0xFF3F51B5)) },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .padding(horizontal = 24.dp, vertical = 16.dp)
//                .fillMaxSize(),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            // 아이디 입력 + 중복 확인 버튼
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                TextField(
//                    value = id,
//                    onValueChange = { id = it },
//                    placeholder = { Text("아이디") },
//                    modifier = Modifier.weight(1f)
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Button(onClick = { /* 중복 확인 로직 */ }) {
//                    Text("중복 확인", fontSize = 12.sp)
//                }
//            }
//
//            // 전화번호 입력 + 인증 버튼
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                TextField(
//                    value = phone,
//                    onValueChange = { phone = it },
//                    placeholder = { Text("전화번호") },
//                    modifier = Modifier.weight(1f)
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Button(onClick = { /* 인증 로직 */ }) {
//                    Text("인증", fontSize = 12.sp)
//                }
//            }
//
//            // 이메일, 비밀번호, 비밀번호 확인
//            TextField(
//                value = email,
//                onValueChange = { email = it },
//                placeholder = { Text("이메일") },
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            TextField(
//                value = password,
//                onValueChange = { password = it },
//                placeholder = { Text("비밀번호") },
//                visualTransformation = PasswordVisualTransformation(),
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            TextField(
//                value = confirmPassword,
//                onValueChange = { confirmPassword = it },
//                placeholder = { Text("비밀번호 확인") },
//                visualTransformation = PasswordVisualTransformation(),
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            Button(
//                onClick = { /* 다음 단계 */ },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(48.dp)
//            ) {
//                Text("다음")
//            }
//        }
//    }
//}


// app/src/main/java/com/example/sbs/smishingdetector/ui/SignUpScreen.kt
package com.example.smishingdetector.ui

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun SignUpScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE) }
    val storage = remember { TokenStorage(context) }
    val scope = rememberCoroutineScope()

    var id by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isChecking by remember { mutableStateOf(false) }
    var isAvailable by remember { mutableStateOf<Boolean?>(null) }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var infoMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("가입하기") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 아이디 + 중복확인
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = id,
                    onValueChange = {
                        id = it
                        isAvailable = null // 타이핑하면 상태 초기화
                    },
                    label = { Text("아이디") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = {
                        if (id.isBlank()) {
                            errorMsg = "아이디를 입력해 주세요."
                            return@OutlinedButton
                        }
                        errorMsg = null
                        infoMsg = null
                        isChecking = true
                        scope.launch {
                            try {
                                val api = RetrofitClient.api(context)
                                val resp = api.checkUsername(id.trim())

                                if (resp.isSuccessful) {
                                    val data = resp.body()
                                    val available = data?.available == false
                                    isAvailable = available
                                    if (available) {
                                        infoMsg = "사용 가능한 아이디입니다."
                                    } else {
                                        errorMsg = "이미 사용 중인 아이디입니다."
                                    }
                                } else {
                                    val code = resp.code()
                                    val err = resp.errorBody()?.string()
                                    errorMsg = "중복 확인 실패 ($code): ${err ?: "알 수 없는 오류"}"
                                    isAvailable = null
                                }
                            } catch (e: HttpException) {
                                val code = e.code()
                                val err = e.response()?.errorBody()?.string()
                                e.printStackTrace()
                                errorMsg = "중복 확인 실패 ($code): ${err ?: e.message()}"
                                isAvailable = null
                            } catch (e: IOException) {
                                e.printStackTrace()
                                errorMsg = "네트워크 오류: 인터넷 연결을 확인하세요."
                                isAvailable = null
                            } catch (e: Exception) {
                                e.printStackTrace()
                                errorMsg = "중복 확인 중 오류가 발생했습니다."
                                isAvailable = null
                            } finally {
                                isChecking = false
                            }
                        }
                    }
                ) {
                    Text(if (isChecking) "확인 중..." else "중복 확인", fontSize = 12.sp)
                }
            }

            // 전화번호
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("전화번호(예: 01012345678)") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            // 이메일
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("이메일") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            // 비밀번호 / 확인
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("비밀번호") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("비밀번호 확인") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            if (!errorMsg.isNullOrBlank()) {
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
            if (!infoMsg.isNullOrBlank()) {
                Text(infoMsg!!, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    errorMsg = null
                    infoMsg = null

                    // 클라이언트 단 검증
                    when {
                        id.isBlank() || phone.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() ->
                        { errorMsg = "모든 항목을 입력해 주세요."; return@Button }
                        password != confirmPassword ->
                        { errorMsg = "비밀번호가 일치하지 않습니다."; return@Button }
                        password.length < 6 ->
                        { errorMsg = "비밀번호는 6자 이상이어야 합니다."; return@Button }
                        isAvailable == false ->
                        { errorMsg = "이미 사용 중인 아이디입니다."; return@Button }
                    }

                    isSubmitting = true
                    scope.launch {
                        try {
                            val api = RetrofitClient.api(context)

                            // 1) 회원가입
                            val signupResp = api.signup(
                                username = id.trim(),
                                password = password,
                                phone = phone.trim(),
                                email = email.trim()
                            )

                            if (!signupResp.isSuccessful) {
                                val code = signupResp.code()
                                val err = signupResp.errorBody()?.string()
                                errorMsg = "회원가입 실패 ($code): ${err ?: "알 수 없는 오류"}"
                                return@launch
                            }

                            val signupData = signupResp.body()
                            if (signupData?.ok != true) {
                                errorMsg = signupData?.message ?: "회원가입에 실패했습니다."
                                return@launch
                            }

                            // 2) 자동 로그인 → device_token 저장
                            val did = deviceId(context)
                            val loginResp = api.loginJson(LoginReq(username = id.trim(), password = password, device_id = did))

                            if (loginResp.isSuccessful) {
                                val loginData = loginResp.body()
                                if (loginData == null) {
                                    errorMsg = "로그인 응답이 비어 있습니다."
                                    return@launch
                                }
                                loginData.device_token?.let { storage.saveDeviceToken(it) }
                            } else {
                                val code = loginResp.code()
                                val err = loginResp.errorBody()?.string()
                                errorMsg = when (code) {
                                    400, 422 -> "로그인 요청 형식이 올바르지 않습니다. (코드 $code)"
                                    401 -> "아이디 또는 비밀번호가 올바르지 않습니다."
                                    else -> "로그인 실패 ($code): ${err ?: "알 수 없는 오류"}"
                                }
                                return@launch
                            }

                            // 3) 로그인 플래그 → 다음 화면
                            prefs.edit().putBoolean("isLoggedIn", true).apply()
                            navController.navigate("permission") {
                                popUpTo("signup") { inclusive = true }
                                launchSingleTop = true
                            }
                        } catch (e: HttpException) {
                            val code = e.code()
                            val err = e.response()?.errorBody()?.string()
                            e.printStackTrace()
                            errorMsg = "회원가입/로그인 실패 ($code): ${err ?: e.message()}"
                        } catch (e: IOException) {
                            e.printStackTrace()
                            errorMsg = "네트워크 오류: 인터넷 연결을 확인하세요."
                        } catch (e: Exception) {
                            e.printStackTrace()
                            errorMsg = "회원가입 중 오류가 발생했습니다."
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(if (isSubmitting) "처리 중..." else "가입하고 계속")
            }
        }
    }
}

private fun deviceId(context: android.content.Context): String {
    val id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    return id ?: UUID.randomUUID().toString()
}
