package com.example.nfcmuseumguide

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.nfcmuseumguide.data.ImageStorage
import com.example.nfcmuseumguide.data.MuseumRepository
import com.example.nfcmuseumguide.model.Exhibit
import com.example.nfcmuseumguide.model.ExhibitDraft
import com.example.nfcmuseumguide.model.MuseumScreen
import com.example.nfcmuseumguide.model.MuseumUiState
import com.example.nfcmuseumguide.nfc.NfcMuseumCodec
import com.example.nfcmuseumguide.remote.MuseumApiClient
import com.example.nfcmuseumguide.remote.ServerConfig
import com.example.nfcmuseumguide.ui.MuseumApp
import com.example.nfcmuseumguide.ui.theme.MuseumTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), NfcAdapter.ReaderCallback, TextToSpeech.OnInitListener {
    private lateinit var repository: MuseumRepository
    private lateinit var imageStorage: ImageStorage
    private lateinit var serverConfig: ServerConfig
    private lateinit var apiClient: MuseumApiClient
    private var nfcAdapter: NfcAdapter? = null
    private var tts: TextToSpeech? = null

    private var state by mutableStateOf(MuseumUiState())
    private var isAutoSyncRunning = false
    private var lastAutoPullAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = MuseumRepository(applicationContext)
        imageStorage = ImageStorage(applicationContext)
        serverConfig = ServerConfig(applicationContext)
        apiClient = MuseumApiClient(applicationContext, serverConfig)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        tts = TextToSpeech(this, this)

        reloadState(
            status = if (nfcAdapter == null) {
                "NFC недоступен на этом устройстве"
            } else {
                "NFC готов: приложите метку к телефону"
            }
        )
        handleNfcIntent(intent)

        setContent {
            MuseumTheme {
                MuseumApp(
                    state = state,
                    nextRouteOrder = repository.nextRouteOrder(),
                    onScreen = { screen -> state = state.copy(screen = screen) },
                    onSelectExhibit = { exhibit -> openExhibit(exhibit.id, "manual") },
                    onBack = { state = state.copy(screen = MuseumScreen.CATALOG) },
                    onToggleFavorite = { id ->
                        repository.toggleFavorite(id)
                        reloadState()
                    },
                    onLanguage = { lang -> state = state.copy(lang = lang) },
                    onQuery = { query -> state = state.copy(query = query) },
                    onCategory = { category -> state = state.copy(category = category) },
                    onPrepareWrite = { id ->
                        state = state.copy(
                            pendingWriteId = id,
                            nfcStatus = "Поднесите пустую NFC-метку, чтобы записать экспонат"
                        )
                    },
                    onCancelWrite = {
                        state = state.copy(pendingWriteId = null, nfcStatus = "Запись отменена")
                    },
                    onSpeak = { exhibit -> speak(exhibit) },
                    onAddDraft = { draft -> addDraft(draft) },
                    onUpdateDraft = { id, draft -> updateDraft(id, draft) },
                    onStartEdit = { id ->
                        state = state.copy(
                            editingExhibitId = id,
                            screen = MuseumScreen.EDITOR,
                            nfcStatus = "Редактирование экспоната"
                        )
                    },
                    onCancelEdit = {
                        state = state.copy(editingExhibitId = null, screen = MuseumScreen.ADMIN)
                    },
                    onDeleteExhibit = { id -> deleteExhibit(id) },
                    onCopyImage = { uri -> imageStorage.copyImageToAppStorage(uri) },
                    onAdminMode = { enabled ->
                        repository.setAdminMode(enabled)
                        reloadState(status = if (enabled) "Режим администратора включён" else "Режим администратора выключен")
                    },
                    onClearStats = {
                        repository.clearStats()
                        reloadState(status = "Статистика очищена")
                    },
                    onExportCsv = { shareCsv() },
                    onExportJson = { shareCatalogJson() },
                    onImportJson = { uri -> importCatalogJson(uri) },
                    onServerBaseUrl = { url -> updateServerBaseUrl(url) },
                    onTestServer = { testServerConnection() }
                )
            }
        }
        autoPullFromServer(force = true, reason = "старт приложения")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        val flags = NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
            NfcAdapter.FLAG_READER_NFC_BARCODE
        nfcAdapter?.enableReaderMode(this, this, flags, null)
        autoPullFromServer(reason = "возврат в приложение")
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = state.lang.locale()
        }
    }

    override fun onTagDiscovered(tag: Tag) {
        val pendingId = state.pendingWriteId
        if (pendingId != null) {
            val exhibit = state.exhibits.firstOrNull { it.id == pendingId }
            if (exhibit != null) {
                val result = runCatching {
                    NfcMuseumCodec.writeToTag(tag, NfcMuseumCodec.createMessage(exhibit))
                    val log = repository.addScan(exhibit.id, "NFC_WRITE", "На метку записан экспонат: ${exhibit.titleRu}")
                    runOnUiThread { pushScanLogToServer(log) }
                    "Готово: метка привязана к “${exhibit.titleRu}”"
                }.getOrElse { error ->
                    "Ошибка записи: ${error.message}"
                }

                runOnUiThread {
                    reloadState(status = result, pendingWriteId = null)
                }
                return
            }
        }

        val id = NfcMuseumCodec.readExhibitId(tag)
        runOnUiThread {
            if (id == null) {
                reloadState(status = "Метка прочитана, но музейный NDEF-профиль не найден")
            } else {
                openExhibit(id, "NFC_SCAN")
            }
        }
    }

    private fun handleNfcIntent(intent: Intent?) {
        if (intent?.action != NfcAdapter.ACTION_NDEF_DISCOVERED) return

        @Suppress("DEPRECATION")
        val messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            ?.filterIsInstance<NdefMessage>()
            .orEmpty()

        val id = messages.firstNotNullOfOrNull { NfcMuseumCodec.parseExhibitId(it) }
        if (id != null) openExhibit(id, "NFC_INTENT")
    }

    private fun openExhibit(id: String, source: String) {
        val exhibit = repository.allExhibits().firstOrNull { it.id == id || it.nfcCode == id }
        if (exhibit == null) {
            reloadState(status = "Экспонат с NFC id=$id не найден в офлайн-каталоге")
        } else {
            val log = repository.addScan(exhibit.id, source, "Открыт экспонат: ${exhibit.titleRu}")
            pushScanLogToServer(log)
            reloadState(
                status = "Открыт экспонат: ${exhibit.titleRu}",
                selectedId = exhibit.id,
                screen = MuseumScreen.DETAIL
            )
        }
    }

    private fun addDraft(draft: ExhibitDraft) {
        val exhibit = repository.createExhibitFromDraft(draft)
        repository.addCustomExhibit(exhibit)
        reloadState(
            status = "Экспонат добавлен: ${exhibit.titleRu}",
            selectedId = exhibit.id,
            screen = MuseumScreen.DETAIL,
            editingExhibitId = null,
            serverStatus = "Экспонат сохранён локально, отправляем на сервер…"
        )
        pushExhibitToServer(exhibit, "Экспонат добавлен")
    }

    private fun updateDraft(exhibitId: String, draft: ExhibitDraft) {
        val updated = repository.updateExhibitFromDraft(exhibitId, draft)
        if (updated == null) {
            reloadState(status = "Не удалось найти экспонат для редактирования", editingExhibitId = null)
            return
        }
        repository.addCustomExhibit(updated)
        reloadState(
            status = "Экспонат обновлён: ${updated.titleRu}",
            selectedId = updated.id,
            screen = MuseumScreen.DETAIL,
            editingExhibitId = null,
            serverStatus = "Экспонат обновлён локально, отправляем на сервер…"
        )
        pushExhibitToServer(updated, "Экспонат обновлён")
    }

    private fun deleteExhibit(exhibitId: String) {
        val title = repository.allExhibits().firstOrNull { it.id == exhibitId }?.titleRu ?: exhibitId
        repository.deleteExhibit(exhibitId)
        val nextSelected = if (state.selectedId == exhibitId) null else state.selectedId
        reloadState(
            status = "Экспонат удалён: $title",
            selectedId = nextSelected,
            screen = MuseumScreen.ADMIN,
            editingExhibitId = null,
            serverStatus = "Удаление сохранено локально, отправляем на сервер…"
        )
        deleteExhibitFromServer(exhibitId, title)
    }

    private fun speak(exhibit: Exhibit) {
        val text = "${exhibit.title(state.lang)}. ${exhibit.subtitle(state.lang)}. ${exhibit.description(state.lang)}"
        tts?.language = state.lang.locale()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "exhibit-${exhibit.id}")
        state = state.copy(nfcStatus = "Аудиогид запущен: ${exhibit.title(state.lang)}")
    }

    private fun reloadState(
        status: String = state.nfcStatus,
        selectedId: String? = state.selectedId,
        screen: MuseumScreen = state.screen,
        pendingWriteId: String? = state.pendingWriteId,
        editingExhibitId: String? = state.editingExhibitId,
        serverStatus: String = state.serverStatus
    ) {
        state = state.copy(
            exhibits = repository.allExhibits(),
            favorites = repository.favoriteIds(),
            scanCounts = repository.scanCounts(),
            stampedIds = repository.nfcStampIds(),
            logs = repository.scanLogs(),
            adminMode = repository.isAdminMode(),
            nfcStatus = status,
            selectedId = selectedId,
            screen = screen,
            pendingWriteId = pendingWriteId,
            editingExhibitId = editingExhibitId,
            serverBaseUrl = serverConfig.baseUrl(),
            serverStatus = serverStatus
        )
    }


    private fun updateServerBaseUrl(url: String) {
        serverConfig.saveBaseUrl(url)
        reloadState(status = "Адрес сервера сохранён", serverStatus = "Сервер: ${serverConfig.baseUrl()}. Подтягиваем данные…")
        autoPullFromServer(force = true, reason = "изменён адрес сервера")
    }

    private fun testServerConnection() {
        reloadState(status = "Проверяем сервер…", serverStatus = "Проверка подключения…")
        lifecycleScope.launch {
            runCatching { apiClient.health() }
                .onSuccess { status ->
                    reloadState(status = "Сервер отвечает", serverStatus = "OK: $status • ${serverConfig.baseUrl()}")
                }
                .onFailure { error ->
                    reloadState(status = "Сервер недоступен", serverStatus = "Ошибка: ${error.message}")
                }
        }
    }


    private fun shareCsv() {
        val csv = repository.exportCsv(state.exhibits, state.favorites, state.scanCounts)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "NFC Museum Guide export.csv")
            putExtra(Intent.EXTRA_TEXT, csv)
        }
        startActivity(Intent.createChooser(intent, "Экспортировать CSV"))
    }

    private fun shareCatalogJson() {
        val json = repository.exportCatalogJson()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_SUBJECT, "NFC Museum Guide catalog.json")
            putExtra(Intent.EXTRA_TEXT, json)
        }
        startActivity(Intent.createChooser(intent, "Экспортировать каталог JSON"))
    }

    private fun importCatalogJson(uri: Uri) {
        val result = runCatching {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: error("Файл не прочитан")
        }.mapCatching { raw ->
            repository.importCatalogJson(raw)
        }

        result.onSuccess { count ->
            reloadState(status = "Импортировано экспонатов: $count", serverStatus = "Импорт сохранён локально, отправляем каталог на сервер…")
            pushAllExhibitsToServer("Импорт JSON")
        }.onFailure { error ->
            reloadState(status = "Ошибка импорта JSON: ${error.message}")
        }
    }

    private fun autoPullFromServer(force: Boolean = false, reason: String = "нужно обновить данные") {
        val now = System.currentTimeMillis()
        if (!force && now - lastAutoPullAt < AUTO_PULL_INTERVAL_MS) return
        if (isAutoSyncRunning) return
        isAutoSyncRunning = true
        reloadState(serverStatus = "Автосинхронизация: получаем данные с сервера ($reason)…")
        lifecycleScope.launch {
            runCatching {
                pushPendingLocalChanges()
                val exhibits = apiClient.downloadExhibits()
                val favorites = apiClient.downloadFavorites(serverConfig.deviceId())
                exhibits to favorites
            }.onSuccess { (exhibits, favorites) ->
                repository.syncFromServer(exhibits)
                repository.setFavoriteIds(favorites)
                lastAutoPullAt = System.currentTimeMillis()
                reloadState(
                    status = "Каталог обновлён с сервера",
                    serverStatus = "Автосинхронизация OK: ${exhibits.size} экспонатов • ${serverConfig.baseUrl()}"
                )
            }.onFailure { error ->
                reloadState(
                    status = state.nfcStatus,
                    serverStatus = "Офлайн-режим: сервер недоступен (${error.message})"
                )
            }
            isAutoSyncRunning = false
        }
    }

    private fun pushExhibitToServer(exhibit: Exhibit, action: String) {
        lifecycleScope.launch {
            runCatching { apiClient.upsertExhibit(exhibit) }
                .onSuccess { savedFromServer ->
                    repository.addCustomExhibit(savedFromServer)
                    repository.markExhibitSynced(savedFromServer.id)
                    reloadState(serverStatus = "$action: отправлено на сервер${if (!savedFromServer.imageUri.isNullOrBlank()) " • фото загружено" else ""}")
                }
                .onFailure { error ->
                    reloadState(serverStatus = "$action: сохранено локально, сервер недоступен (${error.message})")
                }
        }
    }

    private fun deleteExhibitFromServer(exhibitId: String, title: String) {
        lifecycleScope.launch {
            runCatching { apiClient.deleteExhibit(exhibitId) }
                .onSuccess {
                    repository.markDeleteSynced(exhibitId)
                    reloadState(serverStatus = "Удаление отправлено на сервер: $title")
                }
                .onFailure { error -> reloadState(serverStatus = "Удаление сохранено локально, сервер недоступен (${error.message})") }
        }
    }

    private fun pushFavoriteToServer(exhibitId: String, isFavorite: Boolean) {
        lifecycleScope.launch {
            runCatching { apiClient.setFavorite(exhibitId, isFavorite, serverConfig.deviceId()) }
                .onSuccess { reloadState(serverStatus = "Избранное синхронизировано") }
                .onFailure { error -> reloadState(serverStatus = "Избранное сохранено локально, сервер недоступен (${error.message})") }
        }
    }

    private fun pushScanLogToServer(log: com.example.nfcmuseumguide.model.ScanLog) {
        lifecycleScope.launch {
            runCatching { apiClient.uploadScanLog(log, serverConfig.deviceId()) }
                .onSuccess { reloadState(serverStatus = "Событие отправлено на сервер") }
                .onFailure { error -> reloadState(serverStatus = "Событие сохранено локально, сервер недоступен (${error.message})") }
        }
    }

    private fun pushAllExhibitsToServer(reason: String) {
        lifecycleScope.launch {
            runCatching {
                repository.allExhibits().forEach { exhibit ->
                    val savedFromServer = apiClient.upsertExhibit(exhibit)
                    repository.addCustomExhibit(savedFromServer)
                    repository.markExhibitSynced(savedFromServer.id)
                }
                repository.pendingDeleteIds().forEach { id ->
                    apiClient.deleteExhibit(id)
                    repository.markDeleteSynced(id)
                }
                apiClient.uploadScanLogs(repository.scanLogs(), serverConfig.deviceId())
            }.onSuccess {
                reloadState(serverStatus = "$reason: каталог отправлен на сервер")
            }.onFailure { error ->
                reloadState(serverStatus = "$reason: сохранено локально, сервер недоступен (${error.message})")
            }
        }
    }

    private suspend fun pushPendingLocalChanges() {
        val localById = repository.allExhibits().associateBy { it.id }
        repository.dirtyExhibitIds().forEach { id ->
            val exhibit = localById[id] ?: return@forEach
            val savedFromServer = apiClient.upsertExhibit(exhibit)
            repository.addCustomExhibit(savedFromServer)
            repository.markExhibitSynced(savedFromServer.id)
        }
        repository.pendingDeleteIds().forEach { id ->
            apiClient.deleteExhibit(id)
            repository.markDeleteSynced(id)
        }
    }

    companion object {
        private const val AUTO_PULL_INTERVAL_MS = 60_000L
    }
}
