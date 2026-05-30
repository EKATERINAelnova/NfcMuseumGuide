package com.example.nfcmuseumguide.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nfcmuseumguide.model.Exhibit
import com.example.nfcmuseumguide.model.MuseumUiState
import com.example.nfcmuseumguide.nfc.NfcMuseumCodec
import com.example.nfcmuseumguide.ui.components.MuseumCard
import com.example.nfcmuseumguide.ui.theme.MuseumTeal
import com.example.nfcmuseumguide.ui.theme.SoftText

@Composable
fun NfcScreen(
    state: MuseumUiState,
    onPrepareWrite: (String) -> Unit,
    onSelectExhibit: (Exhibit) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            MuseumCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Nfc, contentDescription = null, tint = MuseumTeal)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Метки экспонатов", fontWeight = FontWeight.Black)
                        Text(NfcMuseumCodec.MIME_TYPE, color = SoftText)
                    }
                }
                Text(
                    "Выберите экспонат и нажмите “Записать”. Затем приложите NFC-метку к телефону. На метке хранится только ID экспоната, а описание остаётся в офлайн-каталоге.",
                    color = SoftText
                )
            }
        }

        items(state.exhibits, key = { it.id }) { exhibit ->
            MuseumCard(Modifier.clickable { onSelectExhibit(exhibit) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(exhibit.title(state.lang), fontWeight = FontWeight.Bold)
                        Text("${exhibit.routeOrder}. ${exhibit.zone} • ${exhibit.id}", color = SoftText)
                    }
                    FilledTonalButton(onClick = { onPrepareWrite(exhibit.id) }) {
                        Text("Записать")
                    }
                }
            }
        }
    }
}
