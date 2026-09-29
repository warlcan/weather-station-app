package com.example.weatherstation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weatherstation.ui.screens.LoginScreen
import com.example.weatherstation.ui.screens.MainScreen
import com.example.weatherstation.ui.screens.RegisterScreen
import com.example.weatherstation.ui.screens.SplashScreen
import kotlinx.serialization.Serializable

@Serializable
object Splash
@Serializable
object Login
@Serializable
object Register
@Serializable
object Main

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = Splash,
        ) {
            composable<Splash> {
                SplashScreen(
                    onTokenValid = {
                        navController.navigate(Main) {
                            popUpTo(Splash) { inclusive = true }
                        }
                    },
                    onTokenInvalid = {
                        navController.navigate(Login){
                            popUpTo(Splash) { inclusive = true }
                        }
                    }
                )
            }

            composable<Login> {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Main) {
                            popUpTo(Login) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Register)
                    }
                )
            }

            composable<Register> {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Main) {
                            popUpTo(Login) { inclusive = true }
                        }
                    },
                    onBackToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable<Main> {
                MainScreen()
            }
        }
    }
}
