package com.example.nfcmuseumguide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfcmuseumguide.ui.theme.DeepSpace
import com.example.nfcmuseumguide.ui.theme.MuseumGold
import com.example.nfcmuseumguide.ui.theme.WarmText

@Composable
fun MuseumChip(
    text: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier = Modifier
            .background(if (active) MuseumGold else WarmText.copy(.07f), RoundedCornerShape(50))
            .border(1.dp, if (active) MuseumGold else MuseumGold.copy(.16f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        color = if (active) DeepSpace else WarmText,
        fontWeight = if (active) FontWeight.Black else FontWeight.Medium,
        fontSize = 12.sp
    )
}
