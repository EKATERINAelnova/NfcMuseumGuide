package com.example.nfcmuseumguide.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfcmuseumguide.model.Exhibit
import com.example.nfcmuseumguide.model.MuseumUiState
import com.example.nfcmuseumguide.ui.components.ExhibitImage
import com.example.nfcmuseumguide.ui.components.MuseumCard
import com.example.nfcmuseumguide.ui.components.MuseumChip
import com.example.nfcmuseumguide.ui.theme.DeepSpace
import com.example.nfcmuseumguide.ui.theme.MuseumGold
import com.example.nfcmuseumguide.ui.theme.MuseumTeal
import com.example.nfcmuseumguide.ui.theme.SoftText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    state: MuseumUiState,
    exhibit: Exhibit,
    onBack: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onPrepareWrite: (String) -> Unit,
    onSpeak: (Exhibit) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
                Text(
                    text = exhibit.title(state.lang),
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onToggleFavorite(exhibit.id) }) {
                    Icon(
                        if (exhibit.id in state.favorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = MuseumGold
                    )
                }
            }
        }

        item {
            ExhibitImage(
                imageUri = exhibit.imageUri,
                modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(24.dp))
            )
        }

        item {
            MuseumCard {
                Text(exhibit.subtitle(state.lang), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MuseumTeal)
                Spacer(Modifier.height(8.dp))
                Text(exhibit.description(state.lang), color = SoftText, lineHeight = 22.sp)
                Spacer(Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("${exhibit.routeOrder} в маршруте", exhibit.zone, "${exhibit.floor} этаж", exhibit.century, exhibit.category).forEach {
                        MuseumChip(text = it, active = false) {}
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onSpeak(exhibit) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MuseumGold, contentColor = DeepSpace)
                ) {
                    Icon(Icons.Default.Headphones, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Аудиогид")
                }
                if (state.adminMode) {
                    Button(
                        onClick = { onPrepareWrite(exhibit.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MuseumTeal, contentColor = DeepSpace)
                    ) {
                        Icon(Icons.Default.Nfc, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Записать NFC")
                    }
                }
            }
        }

        item {
            MuseumCard {
                Text("Интересные детали", fontWeight = FontWeight.Black, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                exhibit.facts.forEach { fact ->
                    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                        Text("•", color = MuseumGold, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Text(fact, color = SoftText)
                    }
                }
            }
        }
    }
}
