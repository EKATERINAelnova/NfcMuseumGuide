package com.example.nfcmuseumguide.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfcmuseumguide.model.Exhibit
import com.example.nfcmuseumguide.model.ExhibitDraft
import com.example.nfcmuseumguide.ui.components.ExhibitImage
import com.example.nfcmuseumguide.ui.components.MuseumCard
import com.example.nfcmuseumguide.ui.components.MuseumTextField
import com.example.nfcmuseumguide.ui.theme.DeepSpace
import com.example.nfcmuseumguide.ui.theme.MuseumGold
import com.example.nfcmuseumguide.ui.theme.MuseumTeal
import com.example.nfcmuseumguide.ui.theme.SoftText

@Composable
fun EditorScreen(
    nextRouteOrder: Int,
    editingExhibit: Exhibit?,
    onCopyImage: (Uri) -> String?,
    onAddDraft: (ExhibitDraft) -> Unit,
    onUpdateDraft: (String, ExhibitDraft) -> Unit,
    onCancelEdit: () -> Unit
) {
    val isEditing = editingExhibit != null

    var titleRu by remember { mutableStateOf("") }
    var titleEn by remember { mutableStateOf("") }
    var subtitleRu by remember { mutableStateOf("") }
    var subtitleEn by remember { mutableStateOf("") }
    var zone by remember { mutableStateOf("Новый зал") }
    var floorText by remember { mutableStateOf("1") }
    var category by remember { mutableStateOf("Авторский экспонат") }
    var descriptionRu by remember { mutableStateOf("") }
    var descriptionEn by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("Добавьте экспонат. Фото копируется во внутреннюю папку приложения.") }

    LaunchedEffect(editingExhibit?.id) {
        if (editingExhibit != null) {
            titleRu = editingExhibit.titleRu
            titleEn = editingExhibit.titleEn
            subtitleRu = editingExhibit.subtitleRu
            subtitleEn = editingExhibit.subtitleEn
            zone = editingExhibit.zone
            floorText = editingExhibit.floor.toString()
            category = editingExhibit.category
            descriptionRu = editingExhibit.descriptionRu
            descriptionEn = editingExhibit.descriptionEn
            imageUri = editingExhibit.imageUri
            message = "Вы редактируете экспонат. После сохранения изменения появятся в каталоге."
        } else {
            titleRu = ""
            titleEn = ""
            subtitleRu = ""
            subtitleEn = ""
            zone = "Новый зал"
            floorText = "1"
            category = "Авторский экспонат"
            descriptionRu = ""
            descriptionEn = ""
            imageUri = null
            message = "Добавьте экспонат. Фото копируется во внутреннюю папку приложения."
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { picked: Uri? ->
        if (picked != null) {
            val copied = onCopyImage(picked)
            if (copied != null) {
                imageUri = copied
                message = "Фото выбрано и скопировано во внутреннее хранилище приложения."
            } else {
                message = "Не получилось скопировать фото. Попробуйте выбрать другое изображение."
            }
        }
    }

    fun clearForm() {
        titleRu = ""
        titleEn = ""
        subtitleRu = ""
        subtitleEn = ""
        zone = "Новый зал"
        floorText = "1"
        category = "Авторский экспонат"
        descriptionRu = ""
        descriptionEn = ""
        imageUri = null
    }

    fun buildDraft(floor: Int): ExhibitDraft = ExhibitDraft(
        titleRu = titleRu,
        titleEn = titleEn,
        subtitleRu = subtitleRu,
        subtitleEn = subtitleEn,
        descriptionRu = descriptionRu,
        descriptionEn = descriptionEn,
        zone = zone,
        floor = floor,
        category = category,
        imageUri = imageUri
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                if (isEditing) "Редактировать экспонат" else "Добавить экспонат",
                fontWeight = FontWeight.Black,
                fontSize = 26.sp
            )
            if (isEditing) {
                Text("ID: ${editingExhibit?.id}", color = MuseumGold, fontWeight = FontWeight.Bold)
            } else {
                Text("Следующий номер маршрута: $nextRouteOrder", color = MuseumGold, fontWeight = FontWeight.Bold)
            }
            Text(message, color = SoftText)
        }

        item { Text("Русская версия", color = MuseumGold, fontWeight = FontWeight.Black, fontSize = 18.sp) }
        item { MuseumTextField(titleRu, { titleRu = it }, label = { Text("Название RU") }, modifier = Modifier.fillMaxWidth()) }
        item { MuseumTextField(subtitleRu, { subtitleRu = it }, label = { Text("Подзаголовок RU") }, modifier = Modifier.fillMaxWidth()) }
        item { MuseumTextField(descriptionRu, { descriptionRu = it }, label = { Text("Описание RU") }, minLines = 4, modifier = Modifier.fillMaxWidth()) }

        item { Text("English version", color = MuseumGold, fontWeight = FontWeight.Black, fontSize = 18.sp) }
        item { MuseumTextField(titleEn, { titleEn = it }, label = { Text("Title EN, optional") }, modifier = Modifier.fillMaxWidth()) }
        item { MuseumTextField(subtitleEn, { subtitleEn = it }, label = { Text("Subtitle EN, optional") }, modifier = Modifier.fillMaxWidth()) }
        item { MuseumTextField(descriptionEn, { descriptionEn = it }, label = { Text("Description EN, optional") }, minLines = 4, modifier = Modifier.fillMaxWidth()) }

        item { Text("Размещение", color = MuseumGold, fontWeight = FontWeight.Black, fontSize = 18.sp) }
        item { MuseumTextField(zone, { zone = it }, label = { Text("Зал") }, modifier = Modifier.fillMaxWidth()) }
        item {
            MuseumTextField(
                value = floorText,
                onValueChange = { floorText = it.filter { char -> char.isDigit() }.take(2) },
                label = { Text("Этаж") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item { MuseumTextField(category, { category = it }, label = { Text("Категория") }, modifier = Modifier.fillMaxWidth()) }

        item {
            MuseumCard {
                Text("Фото экспоната", fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                ExhibitImage(
                    imageUri = imageUri,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(20.dp))
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MuseumTeal, contentColor = DeepSpace)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (imageUri == null) "Выбрать фото" else "Заменить")
                    }
                    OutlinedButton(
                        onClick = { imageUri = null },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Без фото")
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                if (isEditing) {
                    OutlinedButton(
                        onClick = onCancelEdit,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Отмена")
                    }
                }
                Button(
                    onClick = {
                        val floor = floorText.toIntOrNull()
                        when {
                            titleRu.isBlank() -> message = "Введите русское название экспоната"
                            floor == null -> message = "Введите этаж числом"
                            isEditing && editingExhibit != null -> onUpdateDraft(editingExhibit.id, buildDraft(floor))
                            else -> {
                                onAddDraft(buildDraft(floor))
                                clearForm()
                                message = "Экспонат добавлен. Следующий номер маршрута обновится автоматически."
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MuseumGold, contentColor = DeepSpace)
                ) {
                    Icon(if (isEditing) Icons.Default.Save else Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEditing) "Сохранить" else "Добавить")
                }
            }
        }
    }
}
