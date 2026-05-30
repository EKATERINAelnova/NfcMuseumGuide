package com.example.nfcmuseumguide.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfcmuseumguide.model.Exhibit
import com.example.nfcmuseumguide.model.MuseumScreen
import com.example.nfcmuseumguide.model.MuseumUiState
import com.example.nfcmuseumguide.ui.components.MuseumCard
import com.example.nfcmuseumguide.ui.components.MuseumTextField
import com.example.nfcmuseumguide.ui.theme.DeepSpace
import com.example.nfcmuseumguide.ui.theme.MuseumGold
import com.example.nfcmuseumguide.ui.theme.MuseumTeal
import com.example.nfcmuseumguide.ui.theme.SoftText

@Composable
fun AdminScreen(
    state: MuseumUiState,
    onAdminMode: (Boolean) -> Unit,
    onScreen: (MuseumScreen) -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit,
    onImportJson: (Uri) -> Unit,
    onStartEdit: (String) -> Unit,
    onDeleteExhibit: (String) -> Unit,
    onServerBaseUrl: (String) -> Unit,
    onTestServer: () -> Unit
) {
    var deleteCandidate by remember { mutableStateOf<Exhibit?>(null) }
    var serverUrl by remember(state.serverBaseUrl) { mutableStateOf(state.serverBaseUrl) }

    val jsonPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) onImportJson(uri)
    }

    deleteCandidate?.let { exhibit ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Удалить экспонат?") },
            text = {
                Text(
                    "Экспонат “${exhibit.title(state.lang)}” исчезнет из каталога, карты, экскурсии и NFC-списка. " +
                        "Если это встроенный экспонат, приложение просто скроет его локально."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExhibit(exhibit.id)
                        deleteCandidate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MuseumGold, contentColor = DeepSpace)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Админ-панель", fontWeight = FontWeight.Black, fontSize = 28.sp)
            Text("Здесь спрятаны функции куратора: добавление, редактирование, удаление экспонатов, запись NFC, статистика и перенос каталога.", color = SoftText)
        }

        item {
            MuseumCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Режим администратора", fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text(
                            if (state.adminMode) "Включён: доступны редактор, NFC-метки, импорт и экспорт." else "Выключен: посетитель видит только музейные функции.",
                            color = SoftText
                        )
                    }
                    Switch(checked = state.adminMode, onCheckedChange = onAdminMode)
                }
            }
        }

        if (state.adminMode) {
            item {
                MuseumCard {
                    Text("PostgreSQL / FastAPI сервер", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text("Адрес для эмулятора: http://10.0.2.2:8000. Для реального телефона укажите IP компьютера в Wi‑Fi сети, например http://192.168.1.10:8000.", color = SoftText)
                    Spacer(Modifier.height(10.dp))
                    MuseumTextField(
                        value = serverUrl,
                        onValueChange = { serverUrl = it },
                        label = { Text("Base URL сервера") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onServerBaseUrl(serverUrl) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MuseumTeal, contentColor = DeepSpace)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Сохранить")
                        }
                        OutlinedButton(onClick = onTestServer, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Analytics, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Проверить")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Синхронизация работает автоматически: каталог подтягивается при запуске и возврате в приложение, а изменения отправляются в момент добавления, редактирования, удаления, избранного и NFC-сканов.", color = SoftText)
                    Spacer(Modifier.height(8.dp))
                    Text(state.serverStatus, color = MuseumGold, fontWeight = FontWeight.Bold)
                }
            }

            item {
                MuseumCard {
                    Text("Управление музеем", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Spacer(Modifier.height(10.dp))
                    AdminButton("Добавить экспонат", Icons.Default.Edit) { onScreen(MuseumScreen.EDITOR) }
                    AdminButton("Записать / проверить NFC", Icons.Default.Nfc) { onScreen(MuseumScreen.NFC) }
                    AdminButton("Статистика", Icons.Default.Analytics) { onScreen(MuseumScreen.STATS) }
                }
            }

            item {
                MuseumCard {
                    Text("Редактирование каталога", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text(
                        "Можно редактировать и удалять любые экспонаты. Встроенные экспонаты при редактировании сохраняются как локальные версии с тем же ID.",
                        color = SoftText
                    )
                }
            }

            items(state.exhibits, key = { it.id }) { exhibit ->
                MuseumCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = exhibit.title(state.lang),
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${exhibit.routeOrder}. ${exhibit.zone} • ${exhibit.floor} этаж • ${if (exhibit.isCustom) "изменён/добавлен" else "встроенный"}",
                                color = SoftText,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onStartEdit(exhibit.id) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MuseumTeal, contentColor = DeepSpace)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Редактировать")
                        }
                        OutlinedButton(
                            onClick = { deleteCandidate = exhibit },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Удалить")
                        }
                    }
                }
            }

            item {
                MuseumCard {
                    Text("Импорт и экспорт", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text("CSV удобно для отчёта, JSON — для переноса каталога на другой телефон.", color = SoftText)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onExportCsv,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MuseumGold, contentColor = DeepSpace)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("CSV")
                        }
                        Button(
                            onClick = onExportJson,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MuseumTeal, contentColor = DeepSpace)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("JSON")
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { jsonPicker.launch("application/json") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Импортировать JSON")
                    }
                }
            }
        } else {
            item {
                MuseumCard {
                    Text("Админ-функции скрыты", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text("Включите режим администратора, чтобы добавить, редактировать, удалить экспонат, записать NFC-метку или экспортировать каталог.", color = SoftText)
                }
            }
        }
    }
}

@Composable
private fun AdminButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
    Spacer(Modifier.height(8.dp))
}
