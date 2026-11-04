package com.example.weatherstation.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherstation.Main
import com.example.weatherstation.ui.theme.WeatherStationTheme
import com.example.weatherstation.ui.viewmodels.MainViewModel
import com.example.weatherstation.ui.viewmodels.StationUiState
import com.example.weatherstation.ui.viewmodels.TimeLabel
import com.example.weatherstation.ui.viewmodels.WeatherStationData
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onAddGateway: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val stations by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Станции", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddGateway,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить шлюз"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(stations, key = { it.id }) { station ->
                WeatherStationCard(station = station)
            }
        }
    }
}

@Composable
fun WeatherStationCard(station: StationUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                SmoothTemperatureChartWithGrid(
                    pointsData = station.chartPoints,
                    timeLabels = station.timeLabels,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = station.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier.width(110.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = station.currentTemperature,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 44.sp,
                    maxLines = 1,
                    softWrap = false
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = station.pressureText,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = station.humidityText,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun SmoothTemperatureChartWithGrid(
    pointsData: List<Float>,
    timeLabels: List<TimeLabel>,
    modifier: Modifier = Modifier
) {
    if (pointsData.size < 2) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val axisLabelWidth = remember(density) { with(density) { 32.dp.toPx() } }
    val axisLabelHeight = remember(density) { with(density) { 20.dp.toPx() } }
    val labelStyle = remember(textColor) { TextStyle(color = textColor, fontSize = 10.sp) }

    val maxTemp = remember(pointsData) { pointsData.maxOrNull() ?: 0f }
    val minTemp = remember(pointsData) { pointsData.minOrNull() ?: 0f }
    val tempRange = maxTemp - minTemp
    val finalRange = if (tempRange == 0f) 1f else tempRange

    Canvas(modifier = modifier) {
        val widthPx = size.width
        val heightPx = size.height

        val usableWidth = widthPx - axisLabelWidth
        val usableHeight = heightPx - axisLabelHeight
        val stepX = usableWidth / (pointsData.size - 1)

        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        val strokeWidthPx = 1.dp.toPx()

        val yLinesCount = 6
        for (i in 0 until yLinesCount) {
            val fraction = i / (yLinesCount - 1).toFloat()
            val y = fraction * usableHeight

            drawLine(
                color = axisColor,
                start = Offset(axisLabelWidth, y),
                end = Offset(widthPx, y),
                strokeWidth = strokeWidthPx,
                pathEffect = dashEffect
            )

            val tempValue = (maxTemp - fraction * tempRange).roundToInt()
            val textLayoutResult = textMeasurer.measure("$tempValue°", labelStyle)

            drawText(
                textMeasurer = textMeasurer,
                text = "$tempValue°",
                style = labelStyle,
                topLeft = Offset(
                    x = 4.dp.toPx(),
                    y = y - textLayoutResult.size.height / 2f
                )
            )
        }

        val points = ArrayList<Offset>(pointsData.size)
        pointsData.forEachIndexed { index, temp ->
            val x = axisLabelWidth + (index * stepX)
            val normalizedY = (temp - minTemp) / finalRange
            val innerPaddingY = usableHeight * 0.15f
            val y = innerPaddingY + (1f - normalizedY) * (usableHeight - innerPaddingY * 2)
            points.add(Offset(x, y))
        }

        timeLabels.forEach { label ->
            val x = axisLabelWidth + (label.index * stepX)

            drawLine(
                color = axisColor,
                start = Offset(x, 0f),
                end = Offset(x, usableHeight),
                strokeWidth = strokeWidthPx,
                pathEffect = dashEffect
            )

            val textLayoutResult = textMeasurer.measure(label.text, labelStyle)
            val xOffset = when (label.index) {
                0 -> x
                pointsData.lastIndex -> x - textLayoutResult.size.width
                else -> x - textLayoutResult.size.width / 2f
            }

            drawText(
                textMeasurer = textMeasurer,
                text = label.text,
                style = labelStyle,
                topLeft = Offset(
                    x = xOffset,
                    y = usableHeight + 4.dp.toPx()
                )
            )
        }

        if (points.isNotEmpty()) {
            val strokePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]

                    val cpX1 = p0.x + (p1.x - p0.x) / 3f
                    val cpX2 = p0.x + 2f * (p1.x - p0.x) / 3f

                    cubicTo(cpX1, p0.y, cpX2, p1.y, p1.x, p1.y)
                }
            }

            drawPath(
                path = strokePath,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    WeatherStationTheme {
        MainScreen(onAddGateway = {})
    }
}