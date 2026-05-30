package com.example.nfcmuseumguide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.nfcmuseumguide.ui.theme.CardWarm
import com.example.nfcmuseumguide.ui.theme.MuseumGold
import com.example.nfcmuseumguide.ui.theme.WarmText

@Composable
fun MuseumCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MuseumGold.copy(.16f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = CardWarm.copy(alpha = .96f),
            contentColor = WarmText
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            Modifier.background(
                Brush.verticalGradient(
                    listOf(WarmText.copy(.055f), CardWarm.copy(.2f), MuseumGold.copy(.035f))
                )
            )
        ) {
            CompositionLocalProvider(LocalContentColor provides WarmText) {
                Column(Modifier.padding(16.dp)) { content() }
            }
        }
    }
}
