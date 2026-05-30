package com.example.nfcmuseumguide.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.ContentPasteSearch
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfcmuseumguide.model.Exhibit
import com.example.nfcmuseumguide.model.MuseumScreen
import com.example.nfcmuseumguide.model.MuseumUiState
import com.example.nfcmuseumguide.ui.components.ExhibitCard
import com.example.nfcmuseumguide.ui.components.MetricTile
import com.example.nfcmuseumguide.ui.components.MuseumCard
import com.example.nfcmuseumguide.ui.theme.DeepSpace
import com.example.nfcmuseumguide.ui.theme.MuseumGold
import com.example.nfcmuseumguide.ui.theme.SoftText

@Composable
fun HomeScreen(
    state: MuseumUiState,
    onScreen: (MuseumScreen) -> Unit,
    onSelectExhibit: (Exhibit) -> Unit,
    onPrepareWrite: (String) -> Unit
) {
    val top = state.exhibits.maxByOrNull { state.scanCounts[it.id] ?: 0 } ?: state.exhibits.firstOrNull()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            MuseumCard {
                Text(
                    "Добро пожаловать в музей",
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Поднесите телефон к NFC-метке рядом с экспонатом — приложение откроет карточку, аудиогид и поставит штамп в музейный паспорт.",
                    color = SoftText
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onScreen(MuseumScreen.TOUR) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MuseumGold, contentColor = DeepSpace)
                    ) {
                        Text("Экскурсия", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onScreen(MuseumScreen.CATALOG) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Каталог", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            ) {
                MetricTile("Экспонаты", state.exhibits.size.toString(), Icons.Default.ContentPasteSearch, Modifier.width(128.dp))
                MetricTile("Штампы", "${state.stampedIds.size}/${state.exhibits.size}", Icons.Default.ConfirmationNumber, Modifier.width(128.dp))
                MetricTile("Открытий", state.scanCounts.values.sum().toString(), Icons.Default.Nfc, Modifier.width(128.dp))
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
            ) {
                MetricTile("Маршрут", "Go", Icons.Default.Route, Modifier.width(128.dp))
                MetricTile("Карта", "Этажи", Icons.Default.Map, Modifier.width(128.dp))
                MetricTile("Избранное", state.favorites.size.toString(), Icons.Default.Favorite, Modifier.width(128.dp))
            }
        }

        if (top != null) {
            item {
                Text("Советуем начать с этого", fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.padding(top = 4.dp))
                ExhibitCard(
                    exhibit = top,
                    lang = state.lang,
                    isFavorite = top.id in state.favorites,
                    scans = state.scanCounts[top.id] ?: 0,
                    onClick = { onSelectExhibit(top) },
                    onWriteClick = if (state.adminMode) { { onPrepareWrite(top.id) } } else null
                )
            }
        }

        item {
            MuseumCard {
                Text("Как пользоваться", fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "1. Откройте каталог или приложите NFC-метку.\n2. Изучите карточку экспоната.\n3. В редакторе добавляйте свои экспонаты с фото.\n4. В разделе “Метки” записывайте экспонаты на NFC.",
                    color = SoftText
                )
            }
        }
    }
}
