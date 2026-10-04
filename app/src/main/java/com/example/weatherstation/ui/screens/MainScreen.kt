package com.example.weatherstation.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.weatherstation.ui.theme.WeatherStationTheme

@Composable
fun MainScreen(
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    WeatherStationTheme {
        MainScreen()
    }
}