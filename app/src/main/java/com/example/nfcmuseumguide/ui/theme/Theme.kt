package com.example.nfcmuseumguide.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun MuseumTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = MuseumGold,
            onPrimary = DeepSpace,
            secondary = MuseumTeal,
            onSecondary = DeepSpace,
            tertiary = MuseumRose,
            onTertiary = DeepSpace,
            background = DeepSpace,
            onBackground = WarmText,
            surface = CardWarm,
            onSurface = WarmText,
            surfaceVariant = CardWarm,
            onSurfaceVariant = SoftText,
            outline = MuseumGold.copy(alpha = .35f)
        ),
        content = content
    )
}
