package com.example.weatherstation

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.example.weatherstation.data.local.BleManager
import com.example.weatherstation.ui.theme.WeatherStationTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val bleManager = BleManager(applicationContext)
        setContent {
            WeatherStationTheme {
                AppNavigation(bleManager = bleManager)
            }
        }
    }
}