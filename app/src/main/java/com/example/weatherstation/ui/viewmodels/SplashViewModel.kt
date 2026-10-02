package com.example.weatherstation.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.weatherstation.auth.TokenManager
import kotlinx.coroutines.flow.StateFlow


class SplashViewModel(application: Application) : AndroidViewModel(application) {
    val tokenManager = TokenManager(getApplication())
    private val _authState = MutableStateFlow<String?>(null)
    val authState: StateFlow<String?> = _authState.asStateFlow()

    suspend fun checkToken() {
        viewModelScope.launch {
            val token: String? = tokenManager.getToken()

            if (token != null) {
                _authState.value = "VALID"
            } else {
                _authState.value = "INVALID"
            }
        }
    }
}
