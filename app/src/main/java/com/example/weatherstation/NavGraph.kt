package com.example.weatherstation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weatherstation.ui.screens.ErrorScreen
import com.example.weatherstation.ui.screens.LoginScreen
import com.example.weatherstation.ui.screens.MainScreen
import com.example.weatherstation.ui.screens.SplashRoute
import kotlinx.serialization.Serializable

@Serializable
object Splash
@Serializable
object Login
@Serializable
object Main
@Serializable
object Error

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
                SplashRoute(
                    onTokenValid = {
                        navController.navigate(Main) {
                            popUpTo(Splash) { inclusive = true }
                        }
                    },
                    onTokenInvalid = {
                        navController.navigate(Login){
                            popUpTo(Splash) { inclusive = true }
                        }
                    },
                    onNetworkError = {
                        navController.navigate(Error){
                            popUpTo(Splash) { inclusive = true }
                        }
                    }
                )
            }

            composable<Login> {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Splash) {
                            popUpTo(Login) { inclusive = true }
                        }
                    }
                )
            }
            composable<Error> {
                ErrorScreen(
                    onRestart = {
                        navController.navigate(Splash) {
                            popUpTo(Error) { inclusive = true }
                        }
                    }
                )
            }

            composable<Main> {
                MainScreen()
            }
        }
    }
}
