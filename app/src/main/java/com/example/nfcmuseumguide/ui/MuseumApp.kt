package com.example.nfcmuseumguide.ui

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nfcmuseumguide.model.Exhibit
import com.example.nfcmuseumguide.model.ExhibitDraft
import com.example.nfcmuseumguide.model.GuideLang
import com.example.nfcmuseumguide.model.MuseumScreen
import com.example.nfcmuseumguide.model.MuseumUiState
import com.example.nfcmuseumguide.ui.components.MuseumBottomBar
import com.example.nfcmuseumguide.ui.components.MuseumCard
import com.example.nfcmuseumguide.ui.components.MuseumTopBar
import com.example.nfcmuseumguide.ui.screens.AdminScreen
import com.example.nfcmuseumguide.ui.screens.CatalogScreen
import com.example.nfcmuseumguide.ui.screens.DetailScreen
import com.example.nfcmuseumguide.ui.screens.EditorScreen
import com.example.nfcmuseumguide.ui.screens.FloorMapScreen
import com.example.nfcmuseumguide.ui.screens.HomeScreen
import com.example.nfcmuseumguide.ui.screens.NfcScreen
import com.example.nfcmuseumguide.ui.screens.PassportScreen
import com.example.nfcmuseumguide.ui.screens.RouteScreen
import com.example.nfcmuseumguide.ui.screens.StatsScreen
import com.example.nfcmuseumguide.ui.screens.TourScreen
import com.example.nfcmuseumguide.ui.theme.DeepSpace
import com.example.nfcmuseumguide.ui.theme.MuseumGold
import com.example.nfcmuseumguide.ui.theme.WarmText

@Composable
fun MuseumApp(
    state: MuseumUiState,
    nextRouteOrder: Int,
    onScreen: (MuseumScreen) -> Unit,
    onSelectExhibit: (Exhibit) -> Unit,
    onBack: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onLanguage: (GuideLang) -> Unit,
    onQuery: (String) -> Unit,
    onCategory: (String) -> Unit,
    onPrepareWrite: (String) -> Unit,
    onCancelWrite: () -> Unit,
    onSpeak: (Exhibit) -> Unit,
    onAddDraft: (ExhibitDraft) -> Unit,
    onUpdateDraft: (String, ExhibitDraft) -> Unit,
    onStartEdit: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onDeleteExhibit: (String) -> Unit,
    onCopyImage: (Uri) -> String?,
    onAdminMode: (Boolean) -> Unit,
    onClearStats: () -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit,
    onImportJson: (Uri) -> Unit,
    onServerBaseUrl: (String) -> Unit,
    onTestServer: () -> Unit
) {
    val selected = state.exhibits.firstOrNull { it.id == state.selectedId } ?: state.exhibits.firstOrNull()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepSpace,
        contentColor = WarmText
    ) {
        Box(Modifier.fillMaxSize().background(DeepSpace)) {
            Column(Modifier.fillMaxSize()) {
            MuseumTopBar(state = state, onLanguage = onLanguage)

            AnimatedVisibility(visible = state.pendingWriteId != null) {
                MuseumCard(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Upload, contentDescription = null, tint = MuseumGold)
                        Spacer(Modifier.width(10.dp))
                        Text("Режим записи NFC: приложите метку", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        IconButton(onClick = onCancelWrite) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = state.screen,
                label = "screen",
                modifier = Modifier.weight(1f)
            ) { screen ->
                when (screen) {
                    MuseumScreen.HOME -> HomeScreen(state, onScreen, onSelectExhibit, onPrepareWrite)
                    MuseumScreen.CATALOG -> CatalogScreen(state, onSelectExhibit, onToggleFavorite, onQuery, onCategory)
                    MuseumScreen.DETAIL -> selected?.let {
                        DetailScreen(state, it, onBack, onToggleFavorite, onPrepareWrite, onSpeak)
                    }
                    MuseumScreen.ROUTE -> RouteScreen(state, onSelectExhibit)
                    MuseumScreen.TOUR -> TourScreen(state, onSelectExhibit)
                    MuseumScreen.PASSPORT -> PassportScreen(state)
                    MuseumScreen.MAP -> FloorMapScreen(state, onSelectExhibit)
                    MuseumScreen.ADMIN -> AdminScreen(
                        state = state,
                        onAdminMode = onAdminMode,
                        onScreen = onScreen,
                        onExportCsv = onExportCsv,
                        onExportJson = onExportJson,
                        onImportJson = onImportJson,
                        onStartEdit = onStartEdit,
                        onDeleteExhibit = onDeleteExhibit,
                        onServerBaseUrl = onServerBaseUrl,
                        onTestServer = onTestServer
                    )
                    MuseumScreen.STATS -> StatsScreen(state, onClearStats, onExportCsv)
                    MuseumScreen.EDITOR -> EditorScreen(
                        nextRouteOrder = nextRouteOrder,
                        editingExhibit = state.exhibits.firstOrNull { it.id == state.editingExhibitId },
                        onCopyImage = onCopyImage,
                        onAddDraft = onAddDraft,
                        onUpdateDraft = onUpdateDraft,
                        onCancelEdit = onCancelEdit
                    )
                    MuseumScreen.NFC -> NfcScreen(state, onPrepareWrite, onSelectExhibit)
                }
            }

            MuseumBottomBar(current = state.screen, adminMode = state.adminMode, onScreen = onScreen)
            }
        }
    }
}
