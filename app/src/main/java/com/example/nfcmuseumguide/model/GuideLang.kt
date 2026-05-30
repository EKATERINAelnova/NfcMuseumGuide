package com.example.nfcmuseumguide.model

import java.util.Locale

enum class GuideLang(val tag: String, val shortName: String, val displayName: String) {
    RU("ru-RU", "RU", "Русский"),
    EN("en-US", "EN", "English");

    fun locale(): Locale = Locale.forLanguageTag(tag)
}
