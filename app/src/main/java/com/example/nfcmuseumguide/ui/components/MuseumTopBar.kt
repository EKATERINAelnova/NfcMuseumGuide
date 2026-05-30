package com.example.nfcmuseumguide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfcmuseumguide.model.GuideLang
import com.example.nfcmuseumguide.model.MuseumUiState
import com.example.nfcmuseumguide.ui.theme.DeepSpace
import com.example.nfcmuseumguide.ui.theme.MuseumGold
import com.example.nfcmuseumguide.ui.theme.MuseumTeal
import com.example.nfcmuseumguide.ui.theme.SoftText
import com.example.nfcmuseumguide.ui.theme.WarmText

@Composable
fun MuseumTopBar(
    state: MuseumUiState,
    onLanguage: (GuideLang) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Box(
            Modifier.size(48.dp)
                .background(Brush.linearGradient(listOf(MuseumGold, MuseumTeal)), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Nfc, contentDescription = null, tint = DeepSpace)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Музейный гид",
                fontWeight = FontWeight.Black,
                fontSize = 21.sp,
                color = WarmText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                state.nfcStatus,
                color = SoftText,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            Modifier
                .border(1.dp, MuseumGold.copy(.25f), RoundedCornerShape(50))
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GuideLang.entries.forEach { lang ->
                val active = state.lang == lang
                Text(
                    text = lang.shortName,
                    modifier = Modifier
                        .background(if (active) MuseumGold else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(50))
                        .clickable { onLanguage(lang) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    color = if (active) DeepSpace else WarmText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
