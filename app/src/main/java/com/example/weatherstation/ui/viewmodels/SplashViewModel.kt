package com.example.weatherstation.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.weatherstation.data.local.TokenManager
import com.example.weatherstation.data.network.NetworkClient
import kotlinx.coroutines.flow.StateFlow

sealed interface SplashUiState {
    object Idle : SplashUiState
    object TokenInvalid : SplashUiState
    object TokenValid : SplashUiState
    object NetworkError: SplashUiState
}

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    val tokenManager = TokenManager(getApplication())
    private val _authState = MutableStateFlow<SplashUiState>(SplashUiState.Idle)
    val authState: StateFlow<SplashUiState> = _authState.asStateFlow()

    fun checkToken() {
        viewModelScope.launch {
            val token: String? = tokenManager.getToken()

            if (token.isNullOrEmpty()) {
                _authState.value = SplashUiState.TokenInvalid
                return@launch
            }

            try {
                val response = NetworkClient.apiService.verifyToken("Bearer $token")

                if (response.isSuccessful) {
                    _authState.value = SplashUiState.TokenValid
                } else if (response.code() == 401) {
                    tokenManager.saveToken("")
                    _authState.value = SplashUiState.TokenInvalid
                } else {
                    _authState.value = SplashUiState.NetworkError
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = SplashUiState.NetworkError
            }
        }
    }
}
