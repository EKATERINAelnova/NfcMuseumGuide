package com.example.nfcmuseumguide.data

import android.content.Context
import com.example.nfcmuseumguide.model.Exhibit
import com.example.nfcmuseumguide.model.ExhibitDraft
import com.example.nfcmuseumguide.model.ScanLog
import com.example.nfcmuseumguide.util.csvEscape
import org.json.JSONArray
import org.json.JSONObject

class MuseumRepository(context: Context) {
    private val prefs = context.getSharedPreferences("museum_guide_store", Context.MODE_PRIVATE)
    private val baseCatalog = defaultMuseumCatalog()

    fun allExhibits(): List<Exhibit> {
        val deleted = deletedIds()
        val merged = linkedMapOf<String, Exhibit>()

        baseCatalog
            .filterNot { it.id in deleted }
            .forEach { merged[it.id] = it }

        customExhibits()
            .filterNot { it.id in deleted }
            .forEach { merged[it.id] = it }

        return merged.values.sortedBy { it.routeOrder }
    }

    fun customExhibits(): List<Exhibit> = readArray(KEY_CUSTOM).mapNotNull { json ->
        runCatching { Exhibit.fromJson(json) }.getOrNull()
    }

    fun createExhibitFromDraft(draft: ExhibitDraft): Exhibit {
        return draftToExhibit(
            draft = draft,
            existing = null,
            id = "custom-${System.currentTimeMillis()}",
            routeOrder = nextRouteOrder()
        )
    }

    fun updateExhibitFromDraft(exhibitId: String, draft: ExhibitDraft): Exhibit? {
        val current = allExhibits().firstOrNull { it.id == exhibitId } ?: return null
        return draftToExhibit(
            draft = draft,
            existing = current,
            id = current.id,
            routeOrder = current.routeOrder
        )
    }

    private fun draftToExhibit(
        draft: ExhibitDraft,
        existing: Exhibit?,
        id: String,
        routeOrder: Int
    ): Exhibit {
        return Exhibit(
            id = id,
            titleRu = draft.titleRu,
            titleEn = draft.titleEn.ifBlank { draft.titleRu },
            subtitleRu = draft.subtitleRu.ifBlank { "Без подзаголовка" },
            subtitleEn = draft.subtitleEn.ifBlank { draft.subtitleRu.ifBlank { "No subtitle" } },
            descriptionRu = draft.descriptionRu.ifBlank { "Описание пока не заполнено." },
            descriptionEn = draft.descriptionEn.ifBlank { draft.descriptionRu.ifBlank { "Description is not filled yet." } },
            zone = draft.zone.ifBlank { "Новый зал" },
            floor = draft.floor,
            century = existing?.century ?: "Новая коллекция",
            category = draft.category.ifBlank { "Авторский экспонат" },
            routeOrder = routeOrder,
            tags = existing?.tags?.takeIf { it.isNotEmpty() } ?: listOf("custom", "editor"),
            facts = existing?.facts?.takeIf { it.isNotEmpty() } ?: listOf(
                "Создано локально",
                "Фото хранится во внутренней папке приложения",
                "Можно записать на NFC-метку"
            ),
            imageUri = draft.imageUri,
            nfcCode = existing?.nfcCode ?: id,
            isCustom = true
        )
    }
    fun addCustomExhibit(exhibit: Exhibit) {
        val updated = customExhibits().filterNot { it.id == exhibit.id } + exhibit.copy(isCustom = true)
        saveCustomExhibits(updated)
        removeDeletedId(exhibit.id)
        markExhibitDirty(exhibit.id)
    }


    fun syncFromServer(exhibits: List<Exhibit>) {
        val remoteIds = exhibits.map { it.id }.toSet()
        val deletedBaseIds = baseCatalog.map { it.id }.filter { it !in remoteIds }.toSet()

        saveCustomExhibits(
            exhibits
                .filter { it.id.isNotBlank() }
                .map { it.copy(isCustom = true) }
                .sortedBy { it.routeOrder }
        )
        prefs.edit()
            .putStringSet(KEY_DELETED, deletedBaseIds)
            .remove(KEY_DIRTY_EXHIBITS)
            .remove(KEY_PENDING_DELETES)
            .apply()
    }


    fun deleteExhibit(exhibitId: String) {
        val baseExists = baseCatalog.any { it.id == exhibitId }
        val updatedCustom = customExhibits().filterNot { it.id == exhibitId }
        saveCustomExhibits(updatedCustom)

        if (baseExists) addDeletedId(exhibitId) else removeDeletedId(exhibitId)
        markDeletePending(exhibitId)
        markExhibitSynced(exhibitId)

        val favorites = favoriteIds().toMutableSet().apply { remove(exhibitId) }
        prefs.edit().putStringSet(KEY_FAVORITES, favorites).apply()
    }

    fun nextRouteOrder(): Int = (allExhibits().maxOfOrNull { it.routeOrder } ?: 0) + 1

    fun dirtyExhibitIds(): Set<String> = prefs.getStringSet(KEY_DIRTY_EXHIBITS, emptySet()) ?: emptySet()

    fun pendingDeleteIds(): Set<String> = prefs.getStringSet(KEY_PENDING_DELETES, emptySet()) ?: emptySet()

    fun markExhibitSynced(id: String) {
        val next = dirtyExhibitIds().toMutableSet().apply { remove(id) }
        prefs.edit().putStringSet(KEY_DIRTY_EXHIBITS, next).apply()
    }

    fun markDeleteSynced(id: String) {
        val next = pendingDeleteIds().toMutableSet().apply { remove(id) }
        prefs.edit().putStringSet(KEY_PENDING_DELETES, next).apply()
    }

    private fun markExhibitDirty(id: String) {
        val next = dirtyExhibitIds().toMutableSet().apply { add(id) }
        prefs.edit().putStringSet(KEY_DIRTY_EXHIBITS, next).apply()
    }

    private fun markDeletePending(id: String) {
        val next = pendingDeleteIds().toMutableSet().apply { add(id) }
        prefs.edit().putStringSet(KEY_PENDING_DELETES, next).apply()
    }

    fun favoriteIds(): Set<String> = prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()

    fun setFavoriteIds(ids: Set<String>) {
        prefs.edit().putStringSet(KEY_FAVORITES, ids).apply()
    }

    fun toggleFavorite(id: String): Boolean {
        val next = favoriteIds().toMutableSet()
        val isNowFavorite = next.add(id)
        if (!isNowFavorite) next.remove(id)
        prefs.edit().putStringSet(KEY_FAVORITES, next).apply()
        return isNowFavorite
    }

    fun scanCounts(): Map<String, Int> {
        val json = JSONObject(prefs.getString(KEY_COUNTS, "{}") ?: "{}")
        return buildMap {
            json.keys().forEach { key -> put(key, json.optInt(key)) }
        }
    }

    fun addScan(exhibitId: String, source: String, message: String): ScanLog {
        val counts = JSONObject(prefs.getString(KEY_COUNTS, "{}") ?: "{}")
        counts.put(exhibitId, counts.optInt(exhibitId) + 1)

        val created = ScanLog(System.currentTimeMillis(), exhibitId, source, message)
        val logs = scanLogs().toMutableList()
        logs.add(0, created)
        val trimmed = logs.take(120)

        prefs.edit()
            .putString(KEY_COUNTS, counts.toString())
            .putString(KEY_LOGS, JSONArray(trimmed.map { it.toJson() }).toString())
            .apply()
        return created
    }

    fun scanLogs(): List<ScanLog> = readArray(KEY_LOGS).mapNotNull { json ->
        runCatching { ScanLog.fromJson(json) }.getOrNull()
    }

    fun nfcStampIds(): Set<String> {
        return scanLogs()
            .filter { it.source == "NFC_SCAN" || it.source == "NFC_INTENT" }
            .map { it.exhibitId }
            .toSet()
    }

    fun isAdminMode(): Boolean = prefs.getBoolean(KEY_ADMIN_MODE, false)

    fun setAdminMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ADMIN_MODE, enabled).apply()
    }

    fun clearStats() {
        prefs.edit().remove(KEY_COUNTS).remove(KEY_LOGS).apply()
    }

    fun exportCsv(exhibits: List<Exhibit>, favorites: Set<String>, counts: Map<String, Int>): String {
        val header = "id,title,zone,floor,category,century,route_order,favorite,scans,image_uri"
        val rows = exhibits.map { exhibit ->
            listOf(
                exhibit.id,
                exhibit.titleRu,
                exhibit.zone,
                exhibit.floor.toString(),
                exhibit.category,
                exhibit.century,
                exhibit.routeOrder.toString(),
                (exhibit.id in favorites).toString(),
                (counts[exhibit.id] ?: 0).toString(),
                exhibit.imageUri.orEmpty()
            ).joinToString(",") { it.csvEscape() }
        }
        return (listOf(header) + rows).joinToString("\n")
    }

    fun exportCatalogJson(): String {
        val json = JSONObject()
            .put("app", "NFC Museum Guide")
            .put("version", 1)
            .put("exportedAt", System.currentTimeMillis())
            .put("exhibits", JSONArray(allExhibits().map { it.toJson() }))
            .put("customOnly", JSONArray(customExhibits().map { it.toJson() }))
        return json.toString(2)
    }

    fun importCatalogJson(raw: String): Int {
        val trimmed = raw.trim()
        val array = if (trimmed.startsWith("[")) {
            JSONArray(trimmed)
        } else {
            val root = JSONObject(trimmed)
            root.optJSONArray("customOnly") ?: root.optJSONArray("exhibits") ?: JSONArray()
        }

        val imported = buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val exhibit = runCatching { Exhibit.fromJson(obj) }.getOrNull() ?: continue
                if (exhibit.id.isNotBlank()) add(exhibit.copy(isCustom = true))
            }
        }

        if (imported.isEmpty()) return 0
        val existing = customExhibits().associateBy { it.id }.toMutableMap()
        imported.forEach { exhibit ->
            existing[exhibit.id] = exhibit
            removeDeletedId(exhibit.id)
            markExhibitDirty(exhibit.id)
        }
        saveCustomExhibits(existing.values.sortedBy { it.routeOrder })
        return imported.size
    }

    private fun saveCustomExhibits(exhibits: List<Exhibit>) {
        prefs.edit().putString(KEY_CUSTOM, JSONArray(exhibits.map { it.toJson() }).toString()).apply()
    }

    private fun deletedIds(): Set<String> = prefs.getStringSet(KEY_DELETED, emptySet()) ?: emptySet()

    private fun addDeletedId(id: String) {
        val next = deletedIds().toMutableSet().apply { add(id) }
        prefs.edit().putStringSet(KEY_DELETED, next).apply()
    }

    private fun removeDeletedId(id: String) {
        val next = deletedIds().toMutableSet().apply { remove(id) }
        prefs.edit().putStringSet(KEY_DELETED, next).apply()
    }

    private fun readArray(key: String): List<JSONObject> {
        val raw = prefs.getString(key, "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrElse { JSONArray() }
        return buildList {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i)
                if (obj != null) add(obj)
            }
        }
    }

    private companion object {
        const val KEY_CUSTOM = "custom_exhibits"
        const val KEY_FAVORITES = "favorite_ids"
        const val KEY_COUNTS = "scan_counts"
        const val KEY_LOGS = "scan_logs"
        const val KEY_ADMIN_MODE = "admin_mode"
        const val KEY_DELETED = "deleted_exhibit_ids"
        const val KEY_DIRTY_EXHIBITS = "dirty_exhibit_ids"
        const val KEY_PENDING_DELETES = "pending_delete_ids"
    }
}
