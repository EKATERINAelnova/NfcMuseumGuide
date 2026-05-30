package com.example.nfcmuseumguide.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfcmuseumguide.model.Exhibit
import com.example.nfcmuseumguide.model.MuseumUiState
import com.example.nfcmuseumguide.ui.components.MuseumCard
import com.example.nfcmuseumguide.ui.theme.DeepSpace
import com.example.nfcmuseumguide.ui.theme.MuseumGold
import com.example.nfcmuseumguide.ui.theme.MuseumTeal
import com.example.nfcmuseumguide.ui.theme.SoftText

@Composable
fun RouteScreen(
    state: MuseumUiState,
    onSelectExhibit: (Exhibit) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Маршрут прогулки", fontWeight = FontWeight.Black, fontSize = 26.sp)
            Text("Экспонаты идут по `routeOrder`. Новые экспонаты получают следующий номер автоматически.", color = SoftText)
        }

        items(state.exhibits.sortedBy { it.routeOrder }, key = { it.id }) { exhibit ->
            Row(
                Modifier.fillMaxWidth().clickable { onSelectExhibit(exhibit) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(42.dp).background(Brush.radialGradient(listOf(MuseumGold, MuseumTeal)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(exhibit.routeOrder.toString(), color = DeepSpace, fontWeight = FontWeight.Black)
                    }
                    Box(Modifier.width(2.dp).height(54.dp).background(MuseumGold.copy(.25f)))
                }
                Spacer(Modifier.width(12.dp))
                MuseumCard(Modifier.weight(1f)) {
                    Text(exhibit.title(state.lang), fontWeight = FontWeight.Black, fontSize = 18.sp)
                    Text("${exhibit.zone} • ${exhibit.floor} этаж • ${exhibit.century}", color = SoftText, fontSize = 13.sp)
                }
            }
        }
    }
}
