package com.example.weatherstation.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherstation.data.local.TokenManager
import com.example.weatherstation.data.model.SendCodeRequest
import com.example.weatherstation.data.model.VerifyCodeRequest
import com.example.weatherstation.data.network.NetworkClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    object CodeSent : LoginUiState
    object Success : LoginUiState
    object CodeError: LoginUiState
    object NetworkError: LoginUiState
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    val tokenManager = TokenManager(getApplication())
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun sendCodeEmail(email: String) {
        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            try {
                val request = SendCodeRequest(email = email)
                val response = NetworkClient.apiService.sendCode(request)

                if (response.success) {
                    _uiState.value = LoginUiState.CodeSent
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = LoginUiState.NetworkError
            }
        }
    }

    fun verifyCodeEmail(email: String, code: String) {
        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            try {
                val request = VerifyCodeRequest(email = email, code = code)
                val response = NetworkClient.apiService.verifyCode(request)

                if (response.success && response.token != null) {
                    tokenManager.saveToken(response.token)
                    _uiState.value = LoginUiState.Success
                } else {
                    _uiState.value = LoginUiState.CodeError
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = LoginUiState.NetworkError
            }
        }
    }
}