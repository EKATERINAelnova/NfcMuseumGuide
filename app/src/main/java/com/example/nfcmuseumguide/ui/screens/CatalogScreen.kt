package com.example.nfcmuseumguide.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import com.example.nfcmuseumguide.ui.components.MuseumTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nfcmuseumguide.model.Exhibit
import com.example.nfcmuseumguide.model.MuseumUiState
import com.example.nfcmuseumguide.ui.components.ExhibitCard
import com.example.nfcmuseumguide.ui.components.MuseumChip

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CatalogScreen(
    state: MuseumUiState,
    onSelectExhibit: (Exhibit) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onQuery: (String) -> Unit,
    onCategory: (String) -> Unit
) {
    val categories = listOf("Все", "Избранное") + state.exhibits.map { it.category }.distinct()
    val filtered = state.exhibits.filter { exhibit ->
        val q = state.query.trim().lowercase()
        val matchesQuery = q.isBlank() || listOf(
            exhibit.titleRu,
            exhibit.titleEn,
            exhibit.zone,
            exhibit.category,
            exhibit.century
        ).any { it.lowercase().contains(q) }

        val matchesCategory = state.category == "Все" ||
            (state.category == "Избранное" && exhibit.id in state.favorites) ||
            exhibit.category == state.category

        matchesQuery && matchesCategory
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MuseumTextField(
                value = state.query,
                onValueChange = onQuery,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                label = { Text("Найти экспонат, зал или эпоху") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { category ->
                    MuseumChip(text = category, active = state.category == category) {
                        onCategory(category)
                    }
                }
            }
        }

        items(filtered, key = { it.id }) { exhibit ->
            ExhibitCard(
                exhibit = exhibit,
                lang = state.lang,
                isFavorite = exhibit.id in state.favorites,
                scans = state.scanCounts[exhibit.id] ?: 0,
                onClick = { onSelectExhibit(exhibit) },
                onFavoriteClick = { onToggleFavorite(exhibit.id) }
            )
        }
    }
}
