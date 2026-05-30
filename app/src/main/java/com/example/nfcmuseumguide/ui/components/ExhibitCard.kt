package com.example.nfcmuseumguide.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nfcmuseumguide.model.Exhibit
import com.example.nfcmuseumguide.model.GuideLang
import com.example.nfcmuseumguide.ui.theme.MuseumGold
import com.example.nfcmuseumguide.ui.theme.MuseumTeal
import com.example.nfcmuseumguide.ui.theme.SoftText

@Composable
fun ExhibitCard(
    exhibit: Exhibit,
    lang: GuideLang,
    isFavorite: Boolean,
    scans: Int,
    onClick: () -> Unit,
    onFavoriteClick: (() -> Unit)? = null,
    onWriteClick: (() -> Unit)? = null
) {
    MuseumCard(Modifier.clickable(onClick = onClick)) {
        Row {
            ExhibitImage(
                imageUri = exhibit.imageUri,
                modifier = Modifier.size(82.dp).clip(RoundedCornerShape(18.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = exhibit.title(lang),
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = exhibit.subtitle(lang),
                    color = SoftText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${exhibit.routeOrder}. ${exhibit.zone} • ${exhibit.floor} этаж • $scans открытий",
                    color = MuseumTeal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column {
                if (onFavoriteClick != null) {
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = MuseumGold
                        )
                    }
                }
                if (onWriteClick != null) {
                    IconButton(onClick = onWriteClick) {
                        Icon(Icons.Default.Nfc, contentDescription = null, tint = MuseumTeal)
                    }
                }
            }
        }
    }
}
