package com.example.nfcmuseumguide.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ContentPasteSearch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfcmuseumguide.model.Exhibit
import com.example.nfcmuseumguide.model.MuseumUiState
import com.example.nfcmuseumguide.ui.components.ExhibitImage
import com.example.nfcmuseumguide.ui.components.MuseumCard
import com.example.nfcmuseumguide.ui.theme.DeepSpace
import com.example.nfcmuseumguide.ui.theme.MuseumGold
import com.example.nfcmuseumguide.ui.theme.MuseumTeal
import com.example.nfcmuseumguide.ui.theme.SoftText
import com.example.nfcmuseumguide.ui.theme.WarmText

@Composable
fun TourScreen(
    state: MuseumUiState,
    onSelectExhibit: (Exhibit) -> Unit
) {
    val route = state.exhibits.sortedBy { it.routeOrder }
    var index by remember(route.size) { mutableIntStateOf(0) }
    val current = route.getOrNull(index)
    val progress = if (route.isEmpty()) 0f else (index + 1).toFloat() / route.size

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "Режим экскурсии",
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text("Идите по экспонатам в порядке маршрута. Это удобный режим для посетителя.", color = SoftText)
        }

        if (current != null) {
            item {
                MuseumCard {
                    Text("${index + 1} из ${route.size}", color = MuseumGold, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = MuseumTeal,
                        trackColor = WarmText.copy(alpha = .12f)
                    )
                    Spacer(Modifier.height(14.dp))
                    ExhibitImage(imageUri = current.imageUri, modifier = Modifier.fillMaxWidth().height(220.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        current.title(state.lang),
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("${current.zone} • ${current.floor} этаж • ${current.century}", color = SoftText)
                    Spacer(Modifier.height(8.dp))
                    Text(current.subtitle(state.lang), color = SoftText)
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onSelectExhibit(current) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MuseumGold, contentColor = DeepSpace)
                    ) {
                        Icon(Icons.Default.ContentPasteSearch, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Открыть экспонат")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { index = (index - 1).coerceAtLeast(0) },
                            enabled = index > 0,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Назад")
                        }
                        Button(
                            onClick = { index = (index + 1).coerceAtMost(route.lastIndex) },
                            enabled = index < route.lastIndex,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MuseumTeal, contentColor = DeepSpace)
                        ) {
                            Text("Далее")
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        } else {
            item {
                MuseumCard {
                    Text("В каталоге пока нет экспонатов", color = SoftText)
                }
            }
        }
    }
}
