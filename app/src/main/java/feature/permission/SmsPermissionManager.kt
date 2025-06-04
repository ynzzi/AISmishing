package com.example.smishingdetector.feature.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.Manifest
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

object PermissionManager {
    val smsPermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    )

    // 앱에 SMS 권한 부여 요청
    fun requestSmsPermissions(
        context: Context,
        launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
        onGranted: () -> Unit
    ) {
        launcher.launch(smsPermissions)
        // 실제 권한 처리 결과는 launcher 등록 시 처리됨
        // onGranted는 그쪽에서 호출함
    }

    // 앱을 기본 문자 앱으로 설정 요청
    fun requestDefaultSmsAppIfNeeded(context: Context) {
        val defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context)
        if (defaultSmsApp != context.packageName) {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
