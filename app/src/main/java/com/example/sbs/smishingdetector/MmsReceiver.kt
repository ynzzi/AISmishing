package com.example.smishingdetector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log

class MmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action != "android.provider.Telephony.WAP_PUSH_RECEIVED" ||
            intent.type   != "application/vnd.wap.mms-message"
        ) return

        Log.d(TAG, "📡 WAP_PUSH_RECEIVED 수신 → 1초 지연 후 DB 조회")

        /* ① 1초 딜레이 후 실행 (메시지가 DB에 저장될 시간을 확보) */
        Handler(Looper.getMainLooper()).postDelayed({
            fetchLatestMms(context)
        }, 1000)   // 1000ms = 1초
    }

    // ───────────────────────────────── fetch ──────────────────────────────────

    private fun fetchLatestMms(context: Context) {
        val nowSec   = System.currentTimeMillis() / 1000          // MMS DB는 초 단위
        val fromTime = nowSec - 5                                  // 최근 5초 이내만 조회
        val inboxUri = Uri.parse("content://mms/inbox")

        val selection     = "date>=?"
        val selectionArgs = arrayOf(fromTime.toString())
        val sortOrder     = "date DESC"

        context.contentResolver.query(
            inboxUri, null, selection, selectionArgs, sortOrder
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val mmsId   = cursor.getString(cursor.getColumnIndexOrThrow("_id"))
                if (mmsId == lastProcessedId) {
                    Log.d(TAG, "🔄 이미 처리한 MMS(ID=$mmsId) — 무시")
                    return
                }

                val address = getAddress(context, mmsId)
                val message    = getTextParts(context, mmsId)

                Log.d(TAG, "📩 NEW MMS(ID=$mmsId) from: $address\nbody: $message")

                SmsSender.sendToServer(context, address, message)
                lastProcessedId = mmsId                              // 중복 방지 저장
            } else {
                Log.w(TAG, "⚠️ 최신 구간(5초) 안에 MMS가 없음")
            }
        }
    }

    // ─────────────────────────── address / message 파싱 ───────────────────────────

    private fun getAddress(context: Context, mmsId: String): String {
        val addrUri = Uri.parse("content://mms/$mmsId/addr")
        val proj    = arrayOf("address", "type")
        context.contentResolver.query(addrUri, proj, null, null, null)?.use { c ->
            val idxAddr = c.getColumnIndex("address")
            val idxType = c.getColumnIndex("type")
            while (c.moveToNext()) {
                if (c.getInt(idxType) == 137) {    // 137 = FROM
                    val num = c.getString(idxAddr)
                    if (!num.isNullOrEmpty() && num != "insert-address-token")
                        return num
                }
            }
        }
        return "Unknown"
    }

    private fun getTextParts(context: Context, mmsId: String): String {
        val partUri   = Uri.parse("content://mms/part")
        val selection = "mid=?"
        val proj      = arrayOf("_id", "ct", "_data", "text")
        val sb        = StringBuilder()

        context.contentResolver.query(partUri, proj, selection, arrayOf(mmsId), null)?.use { c ->
            val idxCt   = c.getColumnIndex("ct")
            val idxData = c.getColumnIndex("_data")
            val idxText = c.getColumnIndex("text")
            val idxId   = c.getColumnIndex("_id")

            while (c.moveToNext()) {
                if (c.getString(idxCt) == "text/plain") {
                    val data = c.getString(idxData)
                    val text = c.getString(idxText) ?: ""
                    if (data != null) {
                        val partId  = c.getString(idxId)
                        val bodyUri = Uri.parse("content://mms/part/$partId")
                        val content = context.contentResolver.openInputStream(bodyUri)
                            ?.bufferedReader()?.use { it.readText() } ?: ""
                        sb.append(content)
                    } else sb.append(text)
                }
            }
        }
        return sb.toString()
    }

    companion object {
        private const val TAG = "MmsReceiver"
        private var lastProcessedId: String? = null    // 최근 처리한 MMS ID 저장
    }
}
    