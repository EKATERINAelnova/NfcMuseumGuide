package com.example.nfcmuseumguide.util

import org.json.JSONArray

fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (i in 0 until length()) add(optString(i))
    }
}
