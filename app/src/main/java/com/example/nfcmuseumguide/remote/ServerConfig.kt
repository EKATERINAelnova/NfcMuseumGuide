package com.example.nfcmuseumguide.remote

import android.content.Context
import java.util.UUID

class ServerConfig(context: Context) {
    private val prefs = context.getSharedPreferences("server_config", Context.MODE_PRIVATE)

    fun baseUrl(): String = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL

    fun saveBaseUrl(url: String) {
        val normalized = url.trim().removeSuffix("/").ifBlank { DEFAULT_BASE_URL }
        prefs.edit().putString(KEY_BASE_URL, normalized).apply()
    }

    fun deviceId(): String {
        val existing = prefs.getString(KEY_DEVICE_ID, null)
        if (!existing.isNullOrBlank()) return existing
        val created = "android-" + UUID.randomUUID().toString()
        prefs.edit().putString(KEY_DEVICE_ID, created).apply()
        return created
    }

    companion object {
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_DEVICE_ID = "device_id"
        const val DEFAULT_BASE_URL = "http://10.115.235.39:8000"
    }
}
