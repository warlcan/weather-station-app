package com.example.weatherstation.ui.screens

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherstation.data.local.BleManager
import com.example.weatherstation.ui.theme.WeatherStationTheme
import com.example.weatherstation.ui.viewmodels.AddGatewayViewModel
import com.example.weatherstation.ui.viewmodels.GatewayUiState
import java.security.SecureRandom

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGatewayScreen(
    bleManager: BleManager,
    onBackClick: () -> Unit,
    viewModel: AddGatewayViewModel = viewModel {
        AddGatewayViewModel(
            bleManager = bleManager,
            application = checkNotNull(
                this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
            )
        )
    }
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var ssidError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = (permissions[Manifest.permission.BLUETOOTH_SCAN] ?: false) &&
                (permissions[Manifest.permission.BLUETOOTH_CONNECT] ?: false)
        if (hasPermissions) viewModel.startScan()
    }

    LaunchedEffect(uiState.message) {
        val msg = uiState.message ?: return@LaunchedEffect

        snackbarHostState.showSnackbar(
            message = msg,
            actionLabel = "OK"
        )

        if (msg == "Данные успешно отправлены") {
            onBackClick()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(16.dp)
                    .imePadding()
            )
        },
        topBar = {
            TopAppBar(
                title = { Text("Настройка шлюза", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        AddGatewayScreenUI(
            uiState = uiState,
            ssid = ssid,
            onSsidChange = {
                ssid = it
                if (ssidError != null) ssidError = null
            },
            ssidError = ssidError,
            password = password,
            onPasswordChange = {
                password = it
                if (passwordError != null) passwordError = null
            },
            passwordError = passwordError,
            hasPermissions = hasPermissions,
            modifier = Modifier.padding(innerPadding),
            onScanClick = {
                if (!hasPermissions) {
                    permissionsLauncher.launch(
                        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
                    )
                } else {
                    viewModel.startScan()
                }
            },
            onStopClick = { viewModel.stopScan() },
            onDeviceClick = { device ->
                when {
                    ssid.isBlank() -> {
                        ssidError = "Поле не может быть пустым"
                    }
                    password.isBlank() -> {
                        passwordError = "Поле не может быть пустым"
                    }
                    else -> {
                        ssidError = null
                        passwordError = null
                        viewModel.connectAndSend(device, ssid.trim(), password)
                    }
                }
            }
        )
    }
}

@Composable
fun AddGatewayScreenUI(
    uiState: GatewayUiState,
    ssid: String,
    onSsidChange: (String) -> Unit,
    ssidError: String?,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordError: String?,
    hasPermissions: Boolean,
    modifier: Modifier = Modifier,
    onScanClick: () -> Unit,
    onStopClick: () -> Unit,
    onDeviceClick: (BluetoothDevice) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Данные Wi-Fi",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = ssid,
                        onValueChange = onSsidChange,
                        label = { Text("SSID (Имя сети)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = ssidError != null,
                        supportingText = ssidError?.let { errorText -> { Text(text = errorText) } }
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("Пароль") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = passwordError != null,
                        supportingText = passwordError?.let { errorText -> { Text(text = errorText) } }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onScanClick,
                    enabled = !uiState.isScanning,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Сканировать")
                }
                Button(
                    onClick = onStopClick,
                    enabled = uiState.isScanning,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Стоп")
                }
            }
        }

        if (uiState.isScanning) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            }
        }

        if (hasPermissions) {
            item {
                Text(
                    text = "Найденные устройства",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(uiState.devices, key = { it.device.address }) { result ->
                DeviceItem(result.device, result.rssi) { onDeviceClick(result.device) }
            }
        } else {
            item {
                Text(
                    "Для поиска устройств необходимо разрешение на Bluetooth",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        if (uiState.isConnecting || uiState.isSending) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Обмен данными с ESP32...",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceItem(device: BluetoothDevice, rssi: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                @Suppress("MissingPermission")
                Text(
                    text = device.name ?: "Без имени",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = device.address,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$rssi dBm",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddGatewayScreenPreview() {
    WeatherStationTheme {
        AddGatewayScreenUI(
            uiState = GatewayUiState(
                isScanning = false,
                devices = emptyList(),
                message = "Данные успешно отправлены"
            ),
            ssid = "MyHomeWiFi",
            onSsidChange = {},
            ssidError = null,
            password = "12345678",
            onPasswordChange = {},
            passwordError = null,
            hasPermissions = true,
            onScanClick = {},
            onStopClick = {},
            onDeviceClick = {}
        )
    }
}