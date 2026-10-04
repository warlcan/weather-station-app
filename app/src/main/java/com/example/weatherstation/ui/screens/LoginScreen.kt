package com.example.weatherstation.ui.screens

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherstation.ui.theme.WeatherStationTheme
import com.example.weatherstation.ui.viewmodels.LoginUiState
import com.example.weatherstation.ui.viewmodels.LoginViewModel


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    var showCodeField by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var codeError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.CodeSent -> showCodeField = true
            is LoginUiState.CodeError -> codeError = "Неправильный код"
            is LoginUiState.Success -> onLoginSuccess()
            is LoginUiState.NetworkError -> {
                snackbarHostState.showSnackbar(
                    message = "Проверьте подключение к интернету",
                    actionLabel = "OK"
                )
            }
            else -> {}
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // === Head ===
                Text(
                    text = "Вход",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(64.dp))

                // === Email text field ===
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError != null) emailError = null
                    },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    isError = emailError != null,
                    supportingText =
                        emailError?.let { errorText ->
                            { Text(text = errorText) }
                        },
                    enabled = !showCodeField,
                    modifier = Modifier.fillMaxWidth()
                )

                // === Code text field ===
                if (showCodeField) Spacer(modifier = Modifier.height(24.dp))
                AnimatedVisibility(
                    visible = showCodeField,
                    enter = scaleIn(initialScale = 0.8f) + fadeIn()
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Код") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Pin,
                                contentDescription = "Verification Code"
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        shape = RoundedCornerShape(12.dp),
                        isError = codeError != null,
                        supportingText = codeError?.let { errorText ->
                            { Text(text = errorText) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))

                // === Buttons ===
                if (!showCodeField) {
                    // === Get code ===
                    Button(
                        onClick = {
                            when {
                                email.isBlank() -> {
                                    emailError = "Поле не может быть пустым"
                                }

                                !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> {
                                    emailError = "Некорректный формат Email"
                                }

                                else -> {
                                    emailError = null
                                    viewModel.sendCodeEmail(email = email.trim())
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is LoginUiState.Loading
                    ) {
                        Text("Получить код", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // === Login ===
                    Button(
                        onClick = {
                            when {
                                code.isBlank() -> {
                                    codeError = "Поле не может быть пустым"
                                }

                                code.length != 6 -> {
                                    codeError = "Некорректный формат кода"
                                }

                                else -> {
                                    codeError = null
                                    viewModel.verifyCodeEmail(
                                        email = email.trim(),
                                        code = code.trim()
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is LoginUiState.Loading
                    ) {
                        Text("Войти", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // === Password login ===
                Text(
                    text = "Войти по паролю",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { }
                )
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .imePadding()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    WeatherStationTheme {
        LoginScreen(
            onLoginSuccess = {},
        )
    }
}
