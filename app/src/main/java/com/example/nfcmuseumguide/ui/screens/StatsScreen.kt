package com.example.nfcmuseumguide.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfcmuseumguide.model.MuseumUiState
import com.example.nfcmuseumguide.ui.components.MetricTile
import com.example.nfcmuseumguide.ui.components.MuseumCard
import com.example.nfcmuseumguide.ui.theme.MuseumGold
import com.example.nfcmuseumguide.ui.theme.MuseumTeal
import com.example.nfcmuseumguide.ui.theme.SoftText
import com.example.nfcmuseumguide.util.formatMuseumTime

@Composable
fun StatsScreen(
    state: MuseumUiState,
    onClearStats: () -> Unit,
    onExport: () -> Unit
) {
    val top = state.exhibits.sortedByDescending { state.scanCounts[it.id] ?: 0 }
    val max = (state.scanCounts.values.maxOrNull() ?: 1).coerceAtLeast(1)

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Интерес посетителей", fontWeight = FontWeight.Black, fontSize = 24.sp)
            Text("Статистика хранится локально: сколько раз открывали экспонаты через NFC или вручную.", color = SoftText)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                MetricTile("Открытий", state.scanCounts.values.sum().toString(), Icons.Default.Nfc, Modifier.weight(1f))
                MetricTile("Логов", state.logs.size.toString(), Icons.Default.Badge, Modifier.weight(1f))
            }
        }

        items(top, key = { it.id }) { exhibit ->
            val scans = state.scanCounts[exhibit.id] ?: 0
            MuseumCard {
                Text(exhibit.title(state.lang), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(12.dp).background(SoftText.copy(.12f), RoundedCornerShape(50))) {
                    Box(
                        Modifier
                            .fillMaxWidth((scans.toFloat() / max).coerceIn(0f, 1f))
                            .height(12.dp)
                            .background(MuseumTeal, RoundedCornerShape(50))
                    )
                }
                Text("$scans открытий • ${exhibit.zone}", color = SoftText, fontSize = 12.sp)
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onExport, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("CSV")
                }
                OutlinedButton(onClick = onClearStats, modifier = Modifier.weight(1f)) {
                    Text("Очистить")
                }
            }
        }

        items(state.logs.take(10)) { log ->
            Text(
                "${log.timestamp.formatMuseumTime()}  •  ${log.source}  •  ${log.message}",
                color = SoftText,
                fontSize = 12.sp
            )
        }
    }
}
