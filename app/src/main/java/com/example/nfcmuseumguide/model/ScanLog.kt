package com.example.nfcmuseumguide.model

import org.json.JSONObject

data class ScanLog(
    val timestamp: Long,
    val exhibitId: String,
    val source: String,
    val message: String
) {
    fun toJson(): JSONObject = JSONObject()
        .put("timestamp", timestamp)
        .put("exhibitId", exhibitId)
        .put("source", source)
        .put("message", message)

    companion object {
        fun fromJson(json: JSONObject): ScanLog = ScanLog(
            timestamp = json.optLong("timestamp"),
            exhibitId = json.optString("exhibitId"),
            source = json.optString("source"),
            message = json.optString("message")
        )
    }
}
