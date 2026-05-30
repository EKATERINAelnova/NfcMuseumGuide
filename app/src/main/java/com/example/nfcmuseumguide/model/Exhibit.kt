package com.example.nfcmuseumguide.model

import com.example.nfcmuseumguide.util.toStringList
import org.json.JSONArray
import org.json.JSONObject

data class Exhibit(
    val id: String,
    val titleRu: String,
    val titleEn: String,
    val subtitleRu: String,
    val subtitleEn: String,
    val descriptionRu: String,
    val descriptionEn: String,
    val zone: String,
    val floor: Int,
    val century: String,
    val category: String,
    val routeOrder: Int,
    val tags: List<String>,
    val facts: List<String>,
    val imageUri: String? = null,
    val nfcCode: String = id,
    val isCustom: Boolean = false
) {
    fun title(lang: GuideLang): String = if (lang == GuideLang.RU) titleRu else titleEn
    fun subtitle(lang: GuideLang): String = if (lang == GuideLang.RU) subtitleRu else subtitleEn
    fun description(lang: GuideLang): String = if (lang == GuideLang.RU) descriptionRu else descriptionEn

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("titleRu", titleRu)
        .put("titleEn", titleEn)
        .put("subtitleRu", subtitleRu)
        .put("subtitleEn", subtitleEn)
        .put("descriptionRu", descriptionRu)
        .put("descriptionEn", descriptionEn)
        .put("zone", zone)
        .put("floor", floor)
        .put("century", century)
        .put("category", category)
        .put("routeOrder", routeOrder)
        .put("tags", JSONArray(tags))
        .put("facts", JSONArray(facts))
        .put("imageUri", imageUri)
        .put("nfcCode", nfcCode)
        .put("isCustom", isCustom)

    companion object {
        fun fromJson(json: JSONObject): Exhibit = Exhibit(
            id = json.optString("id"),
            titleRu = json.optString("titleRu"),
            titleEn = json.optString("titleEn", json.optString("titleRu")),
            subtitleRu = json.optString("subtitleRu"),
            subtitleEn = json.optString("subtitleEn", json.optString("subtitleRu")),
            descriptionRu = json.optString("descriptionRu"),
            descriptionEn = json.optString("descriptionEn", json.optString("descriptionRu")),
            zone = json.optString("zone", "Неизвестный зал"),
            floor = json.optInt("floor", 1),
            century = json.optString("century", "Новая коллекция"),
            category = json.optString("category", "Коллекция"),
            routeOrder = json.optInt("routeOrder", 99),
            tags = json.optJSONArray("tags").toStringList(),
            facts = json.optJSONArray("facts").toStringList(),
            imageUri = json.optString("imageUri").takeIf { it.isNotBlank() && it != "null" },
            nfcCode = json.optString("nfcCode", json.optString("id")),
            isCustom = json.optBoolean("isCustom", true)
        )
    }
}

