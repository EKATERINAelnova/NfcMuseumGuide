package com.example.nfcmuseumguide.model

data class ExhibitDraft(
    val titleRu: String,
    val titleEn: String,
    val subtitleRu: String,
    val subtitleEn: String,
    val descriptionRu: String,
    val descriptionEn: String,
    val zone: String,
    val floor: Int,
    val category: String,
    val imageUri: String?
)