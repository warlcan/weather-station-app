package com.example.weatherstation.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.weatherstation.data.local.TokenManager
import com.example.weatherstation.data.model.TokenVerifyRequest
import com.example.weatherstation.data.network.NetworkClient
import kotlinx.coroutines.flow.StateFlow


class SplashViewModel(application: Application) : AndroidViewModel(application) {
    val tokenManager = TokenManager(getApplication())
    private val _authState = MutableStateFlow<String?>(null)
    val authState: StateFlow<String?> = _authState.asStateFlow()

    fun checkToken() {
        viewModelScope.launch {
            val token: String? = tokenManager.getToken()

            if (token.isNullOrEmpty()) {
                _authState.value = "INVALID"
                return@launch
            }

            try {
                val requestBody = TokenVerifyRequest(token = token)
                val response = NetworkClient.apiService.verifyToken(requestBody)

                if (response.isValid) {
                    _authState.value = "VALID"
                } else {
                    tokenManager.saveToken("")
                    _authState.value = "INVALID"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _authState.value = "NETWORK_ERROR"
            }
        }
    }
}
