package com.example.nfcmuseumguide.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nfcmuseumguide.model.MuseumScreen
import com.example.nfcmuseumguide.ui.theme.CardWarm
import com.example.nfcmuseumguide.ui.theme.DeepSpace
import com.example.nfcmuseumguide.ui.theme.MuseumGold
import com.example.nfcmuseumguide.ui.theme.WarmText

@Composable
fun MuseumBottomBar(
    current: MuseumScreen,
    adminMode: Boolean,
    onScreen: (MuseumScreen) -> Unit
) {
    val items = listOf(
        MuseumScreen.HOME to Icons.Default.Home,
        MuseumScreen.CATALOG to Icons.Default.Search,
        MuseumScreen.TOUR to Icons.Default.Route,
        MuseumScreen.PASSPORT to Icons.Default.ConfirmationNumber,
        MuseumScreen.MAP to Icons.Default.Map,
        MuseumScreen.ADMIN to Icons.Default.AdminPanelSettings
    )

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .background(CardWarm.copy(.96f), RoundedCornerShape(28.dp))
            .border(1.dp, MuseumGold.copy(.18f), RoundedCornerShape(28.dp))
            .horizontalScroll(rememberScrollState())
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (screen, icon) ->
            val active = current == screen
            val adminAttention = screen == MuseumScreen.ADMIN && adminMode
            Box(
                Modifier
                    .size(48.dp)
                    .background(
                        if (active || adminAttention) MuseumGold else androidx.compose.ui.graphics.Color.Transparent,
                        RoundedCornerShape(18.dp)
                    )
                    .clickable { onScreen(screen) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = screen.title,
                    tint = if (active || adminAttention) DeepSpace else WarmText.copy(.78f)
                )
            }
        }
    }
}
