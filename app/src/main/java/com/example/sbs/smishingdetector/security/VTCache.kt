package com.example.sbs.smishingdetector.security

import android.content.Context
import android.content.SharedPreferences

class VTCache(context: Context) {
    private val pref: SharedPreferences =
        context.getSharedPreferences("vt_cache", Context.MODE_PRIVATE)
    private val ttlMs = 6 * 60 * 60 * 1000L // 6시간

    data class Hit(val malicious: Int, val suspicious: Int, val at: Long)

    fun put(url: String, malicious: Int, suspicious: Int, now: Long) {
        pref.edit()
            .putInt("${url}_m", malicious)
            .putInt("${url}_s", suspicious)
            .putLong("${url}_t", now)
            .apply()
    }

    fun get(url: String): Hit? {
        val t = pref.getLong("${url}_t", -1L)
        if (t < 0) return null
        if (System.currentTimeMillis() - t > ttlMs) return null
        val m = pref.getInt("${url}_m", 0)
        val s = pref.getInt("${url}_s", 0)
        return Hit(m, s, t)
    }
}
