package com.example.nfcmuseumguide.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfcmuseumguide.model.MuseumUiState
import com.example.nfcmuseumguide.ui.components.MuseumCard
import com.example.nfcmuseumguide.ui.theme.CardWarm
import com.example.nfcmuseumguide.ui.theme.DeepSpace
import com.example.nfcmuseumguide.ui.theme.MuseumGold
import com.example.nfcmuseumguide.ui.theme.MuseumTeal
import com.example.nfcmuseumguide.ui.theme.SoftText
import com.example.nfcmuseumguide.ui.theme.WarmText

@Composable
fun PassportScreen(state: MuseumUiState) {
    val total = state.exhibits.size.coerceAtLeast(1)
    val stamped = state.stampedIds.size
    val progress = stamped.toFloat() / total

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MuseumCard {
                Text("Музейный паспорт", fontWeight = FontWeight.Black, fontSize = 28.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Штамп появляется, когда посетитель открывает экспонат через NFC-метку. Это превращает прогулку по музею в коллекцию посещений.",
                    color = SoftText
                )
                Spacer(Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = MuseumGold,
                    trackColor = WarmText.copy(alpha = .12f)
                )
                Spacer(Modifier.height(8.dp))
                Text("Получено штампов: $stamped из ${state.exhibits.size}", color = MuseumGold, fontWeight = FontWeight.Bold)
            }
        }

        items(state.exhibits.sortedBy { it.routeOrder }, key = { it.id }) { exhibit ->
            val hasStamp = exhibit.id in state.stampedIds
            MuseumCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(54.dp)
                            .background(
                                if (hasStamp) Brush.radialGradient(listOf(MuseumGold, MuseumTeal))
                                else Brush.radialGradient(listOf(CardWarm, WarmText.copy(alpha = .08f))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasStamp) Icons.Default.Check else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (hasStamp) DeepSpace else SoftText
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(exhibit.title(state.lang), fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("${exhibit.zone} • ${exhibit.floor} этаж • №${exhibit.routeOrder}", color = SoftText, fontSize = 13.sp)
                    }
                    Text(if (hasStamp) "Штамп" else "Ждёт NFC", color = if (hasStamp) MuseumGold else SoftText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
