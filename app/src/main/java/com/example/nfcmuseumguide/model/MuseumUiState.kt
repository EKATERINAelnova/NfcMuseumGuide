package com.example.nfcmuseumguide.model

data class MuseumUiState(
    val exhibits: List<Exhibit> = emptyList(),
    val selectedId: String? = null,
    val screen: MuseumScreen = MuseumScreen.HOME,
    val lang: GuideLang = GuideLang.RU,
    val favorites: Set<String> = emptySet(),
    val scanCounts: Map<String, Int> = emptyMap(),
    val stampedIds: Set<String> = emptySet(),
    val logs: List<ScanLog> = emptyList(),
    val nfcStatus: String = "NFC готов",
    val pendingWriteId: String? = null,
    val query: String = "",
    val category: String = "Все",
    val adminMode: Boolean = false,
    val editingExhibitId: String? = null,
    val serverBaseUrl: String = "http://10.0.2.2:8000",
    val serverStatus: String = "Автосинхронизация ещё не выполнялась"
)
