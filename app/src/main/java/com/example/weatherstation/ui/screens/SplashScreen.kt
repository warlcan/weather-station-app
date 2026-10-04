package com.example.weatherstation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherstation.ui.viewmodels.SplashViewModel
import com.example.weatherstation.ui.theme.WeatherStationTheme
import com.example.weatherstation.ui.viewmodels.SplashUiState

@Composable
fun SplashRoute(
    onTokenValid: () -> Unit,
    onTokenInvalid: () -> Unit,
    onNetworkError: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkToken()
    }

    LaunchedEffect(authState) {
        when (authState) {
            is SplashUiState.TokenValid -> onTokenValid()
            is SplashUiState.TokenInvalid-> onTokenInvalid()
            is SplashUiState.NetworkError-> onNetworkError()
            else -> {}
        }
    }

    SplashScreenContent()
}

@Composable
fun SplashScreenContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    WeatherStationTheme {
        SplashScreenContent()
    }
}