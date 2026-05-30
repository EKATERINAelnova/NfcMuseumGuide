package com.example.nfcmuseumguide.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nfcmuseumguide.model.Exhibit
import com.example.nfcmuseumguide.model.MuseumUiState
import com.example.nfcmuseumguide.ui.components.MuseumCard
import com.example.nfcmuseumguide.ui.theme.CardWarm
import com.example.nfcmuseumguide.ui.theme.DeepSpace
import com.example.nfcmuseumguide.ui.theme.MuseumGold
import com.example.nfcmuseumguide.ui.theme.MuseumRose
import com.example.nfcmuseumguide.ui.theme.MuseumTeal
import com.example.nfcmuseumguide.ui.theme.SoftText
import com.example.nfcmuseumguide.ui.theme.WarmText

@Composable
fun FloorMapScreen(
    state: MuseumUiState,
    onSelectExhibit: (Exhibit) -> Unit
) {
    val floors = state.exhibits.groupBy { it.floor }
    val floorNumbers = state.exhibits.map { it.floor }.distinct().sortedDescending()
    var selectedFloor by remember(floorNumbers.joinToString()) {
        mutableIntStateOf(floorNumbers.firstOrNull() ?: 1)
    }

    LaunchedEffect(floorNumbers.joinToString()) {
        if (floorNumbers.isNotEmpty() && selectedFloor !in floorNumbers) {
            selectedFloor = floorNumbers.first()
        }
    }

    val exhibitsOnSelectedFloor = floors[selectedFloor].orEmpty().sortedBy { it.routeOrder }
    val zonesOnSelectedFloor = exhibitsOnSelectedFloor.groupBy { it.zone }.toSortedMap()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Карта музея",
                color = WarmText,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        item {
            FloorSelectorChips(
                floors = floorNumbers.sorted(),
                selectedFloor = selectedFloor,
                onFloorClick = { selectedFloor = it }
            )
        }

        item {
            Text(
                text = "$selectedFloor этаж",
                color = MuseumGold,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (zonesOnSelectedFloor.isEmpty()) {
                    "На этом этаже пока нет экспонатов."
                } else {
                    "Залы и экспонаты выбранного этажа."
                },
                color = SoftText
            )
        }

        zonesOnSelectedFloor.forEach { (zone, exhibitsInZone) ->
            item {
                MuseumCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MuseumGold.copy(alpha = .18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = MuseumGold)
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                text = zone,
                                color = WarmText,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Экспонатов: ${exhibitsInZone.size}",
                                color = SoftText,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    exhibitsInZone.sortedBy { it.routeOrder }.forEach { exhibit ->
                        FloorExhibitRow(
                            exhibit = exhibit,
                            title = exhibit.title(state.lang),
                            onClick = { onSelectExhibit(exhibit) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FloorSelectorChips(
    floors: List<Int>,
    selectedFloor: Int,
    onFloorClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        floors.forEach { floor ->
            val active = floor == selectedFloor
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onFloorClick(floor) }
                    .background(if (active) MuseumGold else WarmText.copy(alpha = .07f))
                    .border(
                        width = 1.dp,
                        color = if (active) MuseumGold else MuseumGold.copy(alpha = .18f),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$floor этаж",
                    color = if (active) DeepSpace else WarmText,
                    fontWeight = if (active) FontWeight.Black else FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun PrettyMuseumBuilding(
    floors: List<Int>,
    selectedFloor: Int,
    exhibitCountByFloor: Map<Int, Int>,
    onFloorClick: (Int) -> Unit
) {
    val visibleFloors = floors.ifEmpty { listOf(1) }
    val buildingHeight = (visibleFloors.size * 76 + 160).coerceIn(380, 780).dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 380.dp, max = 780.dp)
            .height(buildingHeight)
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        MuseumTeal.copy(alpha = .15f),
                        CardWarm.copy(alpha = .98f),
                        DeepSpace.copy(alpha = .84f)
                    )
                )
            )
            .border(1.dp, MuseumGold.copy(alpha = .20f), RoundedCornerShape(30.dp))
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Canvas(Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            val buildingLeft = width * .12f
            val buildingWidth = width * .76f
            val roofHeight = 62.dp.toPx()
            val baseHeight = 36.dp.toPx()
            val bodyTop = roofHeight + 12.dp.toPx()
            val bodyHeight = height - bodyTop - baseHeight - 18.dp.toPx()
            val floorHeight = bodyHeight / visibleFloors.size

            // Soft background glow behind the museum.
            drawCircle(
                color = MuseumGold.copy(alpha = .12f),
                radius = width * .42f,
                center = Offset(width * .50f, height * .32f)
            )
            drawCircle(
                color = MuseumTeal.copy(alpha = .10f),
                radius = width * .30f,
                center = Offset(width * .78f, height * .18f)
            )

            // Shadow.
            drawRoundRect(
                color = Color.Black.copy(alpha = .26f),
                topLeft = Offset(buildingLeft + 12.dp.toPx(), bodyTop + 14.dp.toPx()),
                size = Size(buildingWidth, bodyHeight + 16.dp.toPx()),
                cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
            )

            // Roof triangle.
            val roof = Path().apply {
                moveTo(buildingLeft - 20.dp.toPx(), bodyTop + 5.dp.toPx())
                lineTo(width / 2f, 6.dp.toPx())
                lineTo(buildingLeft + buildingWidth + 20.dp.toPx(), bodyTop + 5.dp.toPx())
                close()
            }
            drawPath(
                path = roof,
                brush = Brush.linearGradient(
                    listOf(
                        MuseumGold.copy(alpha = .88f),
                        MuseumRose.copy(alpha = .55f),
                        MuseumGold.copy(alpha = .72f)
                    )
                )
            )
            drawPath(
                path = roof,
                color = MuseumGold.copy(alpha = .85f),
                style = Stroke(width = 2.dp.toPx())
            )

            // Small top ornament.
            drawCircle(
                color = MuseumGold.copy(alpha = .9f),
                radius = 7.dp.toPx(),
                center = Offset(width / 2f, 14.dp.toPx())
            )

            // Main body.
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(
                        WarmText.copy(alpha = .12f),
                        WarmText.copy(alpha = .05f),
                        MuseumGold.copy(alpha = .075f)
                    )
                ),
                topLeft = Offset(buildingLeft, bodyTop),
                size = Size(buildingWidth, bodyHeight),
                cornerRadius = CornerRadius(30.dp.toPx(), 30.dp.toPx())
            )
            drawRoundRect(
                color = MuseumGold.copy(alpha = .28f),
                topLeft = Offset(buildingLeft, bodyTop),
                size = Size(buildingWidth, bodyHeight),
                cornerRadius = CornerRadius(30.dp.toPx(), 30.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )

            // Decorative side columns.
            val columnWidth = 14.dp.toPx()
            listOf(buildingLeft + 24.dp.toPx(), buildingLeft + buildingWidth - 24.dp.toPx() - columnWidth).forEach { x ->
                drawRoundRect(
                    color = MuseumGold.copy(alpha = .18f),
                    topLeft = Offset(x, bodyTop + 12.dp.toPx()),
                    size = Size(columnWidth, bodyHeight - 24.dp.toPx()),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )
            }

            visibleFloors.forEachIndexed { index, floor ->
                val y = bodyTop + index * floorHeight
                val isSelected = floor == selectedFloor
                val floorTop = y + 5.dp.toPx()
                val floorBottom = y + floorHeight - 6.dp.toPx()

                // Floor slab.
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        if (isSelected) {
                            listOf(
                                MuseumGold.copy(alpha = .16f),
                                MuseumGold.copy(alpha = .38f),
                                MuseumGold.copy(alpha = .16f)
                            )
                        } else {
                            listOf(
                                WarmText.copy(alpha = .035f),
                                WarmText.copy(alpha = .075f),
                                WarmText.copy(alpha = .035f)
                            )
                        }
                    ),
                    topLeft = Offset(buildingLeft + 12.dp.toPx(), floorTop),
                    size = Size(buildingWidth - 24.dp.toPx(), floorBottom - floorTop),
                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                )

                drawLine(
                    color = MuseumGold.copy(alpha = if (isSelected) .62f else .16f),
                    start = Offset(buildingLeft + 18.dp.toPx(), y + floorHeight),
                    end = Offset(buildingLeft + buildingWidth - 18.dp.toPx(), y + floorHeight),
                    strokeWidth = 1.3.dp.toPx()
                )

                // Windows. More polished: large central windows and two side windows.
                val windowHeight = kotlin.math.min(24.dp.toPx(), floorHeight * .26f)
                val windowWidth = windowHeight * 1.25f
                val windowY = y + floorHeight * .54f - windowHeight / 2f
                val centerX = buildingLeft + buildingWidth / 2f
                val windowCenters = listOf(
                    centerX - buildingWidth * .24f,
                    centerX,
                    centerX + buildingWidth * .24f
                )

                windowCenters.forEachIndexed { windowIndex, cx ->
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            if (isSelected) {
                                listOf(MuseumGold.copy(alpha = .92f), MuseumGold.copy(alpha = .48f))
                            } else {
                                listOf(MuseumTeal.copy(alpha = .42f), MuseumTeal.copy(alpha = .16f))
                            }
                        ),
                        topLeft = Offset(cx - windowWidth / 2f, windowY),
                        size = Size(windowWidth, windowHeight),
                        cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
                    )

                    if (windowIndex == 1) {
                        drawLine(
                            color = WarmText.copy(alpha = .23f),
                            start = Offset(cx, windowY + 3.dp.toPx()),
                            end = Offset(cx, windowY + windowHeight - 3.dp.toPx()),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }

            // Museum entrance/base.
            val baseY = bodyTop + bodyHeight + 9.dp.toPx()
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(
                        MuseumGold.copy(alpha = .20f),
                        MuseumGold.copy(alpha = .44f),
                        MuseumGold.copy(alpha = .20f)
                    )
                ),
                topLeft = Offset(buildingLeft - 22.dp.toPx(), baseY),
                size = Size(buildingWidth + 44.dp.toPx(), 24.dp.toPx()),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
            )

            val doorWidth = buildingWidth * .18f
            val doorHeight = 30.dp.toPx()
            drawRoundRect(
                color = DeepSpace.copy(alpha = .58f),
                topLeft = Offset(width / 2f - doorWidth / 2f, baseY - doorHeight + 8.dp.toPx()),
                size = Size(doorWidth, doorHeight),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
        }

        // Click/touch layer. It follows the same floor order as the drawing: top floor first.
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp, start = 28.dp, end = 28.dp, bottom = 62.dp)
                .fillMaxWidth(.78f)
        ) {
            visibleFloors.forEach { floor ->
                val isSelected = floor == selectedFloor
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onFloorClick(floor) }
                        .background(if (isSelected) MuseumGold.copy(alpha = .13f) else Color.Transparent)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.dp,
                            color = if (isSelected) MuseumGold.copy(alpha = .82f) else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MuseumGold else DeepSpace.copy(alpha = .62f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = floor.toString(),
                                color = if (isSelected) DeepSpace else WarmText,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "$floor этаж",
                                color = WarmText,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${exhibitCountByFloor[floor] ?: 0} экспонатов",
                                color = SoftText,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "Все этажи строятся по текущему каталогу",
            color = SoftText,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun FloorExhibitRow(
    exhibit: Exhibit,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(MuseumTeal.copy(alpha = .15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = exhibit.routeOrder.toString(),
                color = MuseumTeal,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = WarmText,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = exhibit.category,
                color = SoftText,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
