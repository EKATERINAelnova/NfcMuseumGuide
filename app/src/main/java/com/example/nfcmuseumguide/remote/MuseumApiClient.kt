package com.example.nfcmuseumguide.remote

import android.content.Context
import android.net.Uri
import com.example.nfcmuseumguide.model.Exhibit
import com.example.nfcmuseumguide.model.ScanLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class MuseumApiClient(
    private val context: Context,
    private val config: ServerConfig
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun health(): String = withContext(Dispatchers.IO) {
        val response = executeGet("/health")
        JSONObject(response).optString("status", "unknown")
    }

    suspend fun downloadExhibits(): List<Exhibit> = withContext(Dispatchers.IO) {
        val response = executeGet("/api/exhibits")
        val array = JSONArray(response)
        buildList {
            for (i in 0 until array.length()) {
                add(Exhibit.fromJson(array.getJSONObject(i).serverToAndroidJson()))
            }
        }
    }

    suspend fun uploadExhibits(exhibits: List<Exhibit>) = withContext(Dispatchers.IO) {
        exhibits.forEach { exhibit ->
            runCatching { upsertExhibit(exhibit) }
        }
    }

    suspend fun upsertExhibit(exhibit: Exhibit): Exhibit = withContext(Dispatchers.IO) {
        val body = exhibit.toServerJson().toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url(url("/api/exhibits/${exhibit.id}"))
            .put(body)
            .build()
        val saved = Exhibit.fromJson(JSONObject(execute(request)).serverToAndroidJson())

        val imageUri = exhibit.imageUri.orEmpty()
        if (imageUri.isLocalImageUri()) {
            uploadImage(exhibit.id, imageUri)
                ?: error("Фото не удалось прочитать на устройстве")
        } else {
            saved
        }
    }

    suspend fun deleteExhibit(exhibitId: String) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url("/api/exhibits/$exhibitId"))
            .delete()
            .build()
        execute(request)
        Unit
    }

    suspend fun uploadScanLogs(logs: List<ScanLog>, deviceId: String = "android") = withContext(Dispatchers.IO) {
        logs.forEach { log -> runCatching { uploadScanLog(log, deviceId) } }
    }

    suspend fun uploadScanLog(log: ScanLog, deviceId: String = "android") = withContext(Dispatchers.IO) {
        val json = JSONObject()
            .put("exhibit_id", log.exhibitId)
            .put("source", log.source)
            .put("message", log.message)
            .put("device_id", deviceId)
            .put("timestamp_ms", log.timestamp)
        val request = Request.Builder()
            .url(url("/api/scan-logs"))
            .post(json.toString().toRequestBody(JSON))
            .build()
        execute(request)
        Unit
    }

    suspend fun setFavorite(exhibitId: String, isFavorite: Boolean, deviceId: String = "android") = withContext(Dispatchers.IO) {
        if (isFavorite) {
            val json = JSONObject()
                .put("device_id", deviceId)
                .put("exhibit_id", exhibitId)
            val request = Request.Builder()
                .url(url("/api/favorites"))
                .post(json.toString().toRequestBody(JSON))
                .build()
            execute(request)
        } else {
            val request = Request.Builder()
                .url(url("/api/favorites/$deviceId/$exhibitId"))
                .delete()
                .build()
            execute(request)
        }
        Unit
    }

    suspend fun downloadFavorites(deviceId: String = "android"): Set<String> = withContext(Dispatchers.IO) {
        val response = executeGet("/api/favorites/$deviceId")
        val array = JSONArray(response)
        buildSet {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                obj.optString("exhibit_id").takeIf { it.isNotBlank() }?.let { add(it) }
            }
        }
    }

    private fun uploadImage(exhibitId: String, imageUri: String): Exhibit? {
        val uri = Uri.parse(imageUri)
        val bytes = when (uri.scheme) {
            "file" -> File(uri.path.orEmpty()).readBytes()
            else -> context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } ?: return null

        val part = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                name = "image",
                filename = "${exhibitId}.jpg",
                body = bytes.toRequestBody("image/jpeg".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(url("/api/exhibits/$exhibitId/image"))
            .post(part)
            .build()
        return Exhibit.fromJson(JSONObject(execute(request)).serverToAndroidJson())
    }

    private fun executeGet(path: String): String {
        val request = Request.Builder().url(url(path)).get().build()
        return execute(request)
    }

    private fun execute(request: Request): String {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code}: ${body.ifBlank { response.message }}")
            }
            return body
        }
    }

    private fun url(path: String): String = "${config.baseUrl()}$path"

    private fun Exhibit.toServerJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("title_ru", titleRu)
        .put("title_en", titleEn)
        .put("subtitle_ru", subtitleRu)
        .put("subtitle_en", subtitleEn)
        .put("description_ru", descriptionRu)
        .put("description_en", descriptionEn)
        .put("zone", zone)
        .put("floor", floor)
        .put("century", century)
        .put("category", category)
        .put("route_order", routeOrder)
        .put("tags", JSONArray(tags))
        .put("facts", JSONArray(facts))
        .put("image_uri", imageUri.takeUnless { it.isLocalImageUri() })
        .put("nfc_code", nfcCode)
        .put("is_custom", isCustom)
        .put("is_deleted", false)

    private fun JSONObject.serverToAndroidJson(): JSONObject = JSONObject()
        .put("id", optString("id"))
        .put("titleRu", optString("title_ru"))
        .put("titleEn", optString("title_en", optString("title_ru")))
        .put("subtitleRu", optString("subtitle_ru"))
        .put("subtitleEn", optString("subtitle_en", optString("subtitle_ru")))
        .put("descriptionRu", optString("description_ru"))
        .put("descriptionEn", optString("description_en", optString("description_ru")))
        .put("zone", optString("zone"))
        .put("floor", optInt("floor", 1))
        .put("century", optString("century"))
        .put("category", optString("category"))
        .put("routeOrder", optInt("route_order", 99))
        .put("tags", optJSONArray("tags") ?: JSONArray())
        .put("facts", optJSONArray("facts") ?: JSONArray())
        .put("imageUri", normalizeImageUri(optString("image_uri").takeIf { it.isNotBlank() && it != "null" }))
        .put("nfcCode", optString("nfc_code", optString("id")))
        .put("isCustom", optBoolean("is_custom", true))

    private fun String?.isLocalImageUri(): Boolean {
        if (this.isNullOrBlank()) return false
        return startsWith("file:") || startsWith("content:")
    }

    private fun normalizeImageUri(raw: String?): String? {
        val value = raw?.takeIf { it.isNotBlank() && it != "null" } ?: return null
        if (value.startsWith("/uploads/")) return "${config.baseUrl()}$value"

        return runCatching {
            val parsed = Uri.parse(value)
            val path = parsed.encodedPath.orEmpty()
            val host = parsed.host.orEmpty()
            val isUpload = path.startsWith("/uploads/")
            val isLocalhost = host == "localhost" || host == "127.0.0.1" || host == "0.0.0.0" || host == "10.0.2.2"
            if ((parsed.scheme == "http" || parsed.scheme == "https") && isUpload && isLocalhost) {
                val query = parsed.encodedQuery?.let { "?$it" }.orEmpty()
                "${config.baseUrl()}$path$query"
            } else {
                value
            }
        }.getOrDefault(value)
    }

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
