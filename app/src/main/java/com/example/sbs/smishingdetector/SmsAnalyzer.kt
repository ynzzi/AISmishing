package com.example.sbs.smishingdetector

import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType

object SmsAnalyzer {  // object로 변경 → 정적 메서드 호출 가능

    fun analyzeWithAI(text: String, onResult: (String, Float) -> Unit) {
        val client = OkHttpClient()

        val json = JSONObject()
        json.put("text", text)

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            json.toString()
        )

        val request = Request.Builder()
            .url("http://172.30.1.20:10000/vectorize")  // 서버 주소 수정 필요 시 이곳만 변경
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { responseBody ->
                    val jsonObject = JSONObject(responseBody)
                    val result = jsonObject.getString("result")   // "spam" or "normal"
                    val score = jsonObject.getDouble("score").toFloat()
                    onResult(result, score)
                }
            }
        })
    }
}