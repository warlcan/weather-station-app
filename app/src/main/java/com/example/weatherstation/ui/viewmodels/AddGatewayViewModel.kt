package com.example.weatherstation.ui.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherstation.data.local.BleManager
import com.example.weatherstation.data.local.TokenManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class GatewayUiState(
    val isScanning: Boolean = false,
    val devices: List<ScanResult> = emptyList(),
    val isConnecting: Boolean = false,
    val isSending: Boolean = false,
    val message: String? = null
)

class AddGatewayViewModel(
    private val bleManager: BleManager,
    application: Application
) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(getApplication())

    private val _uiState = MutableStateFlow(GatewayUiState())
    val uiState: StateFlow<GatewayUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    fun startScan() {
        scanJob?.cancel()
        _uiState.update { it.copy(isScanning = true, devices = emptyList()) }
        scanJob = viewModelScope.launch {
            bleManager.scanForDevices().collect { result ->
                _uiState.update { state ->
                    if (state.devices.any { it.device.address == result.device.address }) state
                    else state.copy(devices = state.devices + result)
                }
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        _uiState.update { it.copy(isScanning = false) }
    }

    fun connectAndSend(
        device: BluetoothDevice,
        ssid: String,
        password: String
    ) {
        viewModelScope.launch {
            stopScan()
            _uiState.update { it.copy(isConnecting = true, message = null) }

            val rawToken = tokenManager.getToken()
            if (rawToken.isNullOrBlank()) {
                _uiState.update {
                    it.copy(isConnecting = false, message = "Токен не найден. Войдите в аккаунт.")
                }
                return@launch
            }

            val tokenBytes = java.security.MessageDigest.getInstance("SHA-256")
                .digest(rawToken.toByteArray(Charsets.UTF_8))

            if (!bleManager.connect(device)) {
                _uiState.update { it.copy(isConnecting = false, message = "Ошибка подключения") }
                return@launch
            }
            _uiState.update { it.copy(isConnecting = false, isSending = true) }

            if (!bleManager.writeCharacteristic(
                    BleManager.SSID_CHAR_UUID,
                    ssid.toByteArray(Charsets.UTF_8)
                )
            ) {
                _uiState.update { it.copy(isSending = false, message = "Ошибка SSID") }
                bleManager.disconnect(); return@launch
            }
            delay(200.milliseconds)

            if (!bleManager.writeCharacteristic(
                    BleManager.PASS_CHAR_UUID,
                    password.toByteArray(Charsets.UTF_8)
                )
            ) {
                _uiState.update { it.copy(isSending = false, message = "Ошибка пароля") }
                bleManager.disconnect(); return@launch
            }
            delay(200.milliseconds)

            val ok = bleManager.writeCharacteristic(BleManager.TOKEN_CHAR_UUID, tokenBytes)
            _uiState.update {
                it.copy(
                    isSending = false,
                    message = if (ok) "Данные успешно отправлены" else "Ошибка отправки токена"
                )
            }
            bleManager.disconnect()
        }
    }

    @SuppressLint("EmptySuperCall")
    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
        bleManager.disconnect()
    }
}