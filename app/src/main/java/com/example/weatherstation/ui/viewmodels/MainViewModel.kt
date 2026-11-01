package com.example.weatherstation.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherstation.data.local.TokenManager
import com.example.weatherstation.data.network.NetworkClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class WeatherStationData(
    val uid: Long,
    val name: String,
    val lastTemperature: Float?,
    val lastPressure: Float?,
    val lastHumidity: Float?,
    val lastMeasurementHour: Int,
    val temperatureHistory: List<Float>
)

data class StationUiState(
    val id: Long,
    val name: String,
    val currentTemperature: String,
    val pressureText: String,
    val humidityText: String,
    val chartPoints: List<Float>,
    val timeLabels: List<TimeLabel>
)

data class TimeLabel(
    val index: Int,
    val text: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    private val _stationsData = MutableStateFlow<List<WeatherStationData>>(emptyList())

    val uiState: StateFlow<List<StationUiState>> = _stationsData
        .map { stations -> stations.map { transformToUiState(it) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadStations()
    }

    private fun loadStations() {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrBlank()) {
                    _stationsData.value = emptyList()
                    return@launch
                }
                val bearer = "Bearer $token"

                val stationsResp = NetworkClient.apiService.getStations(bearer)
                if (!stationsResp.isSuccessful) {
                    _stationsData.value = emptyList()
                    return@launch
                }
                val stations = stationsResp.body()?.stations.orEmpty()
                if (stations.isEmpty()) {
                    _stationsData.value = emptyList()
                    return@launch
                }

                val uids = stations.joinToString(",") { it.uid.toString() }
                val measResp = NetworkClient.apiService.getMeasurements(bearer, uids)
                val measurements = if (measResp.isSuccessful) {
                    measResp.body()?.measurements.orEmpty()
                } else {
                    emptyMap()
                }

                _stationsData.value = stations.map { dto ->
                    val list = measurements[dto.uid.toString()]
                        .orEmpty()
                        .sortedBy { it.time }
                    val last = list.lastOrNull()

                    WeatherStationData(
                        uid = dto.uid,
                        name = dto.name ?: "Станция ${dto.hardwareId}",
                        lastTemperature = last?.temperature,
                        lastPressure = last?.pressure,
                        lastHumidity = last?.humidity,
                        lastMeasurementHour = parseHour(last?.time),
                        temperatureHistory = list.mapNotNull { it.temperature }
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _stationsData.value = emptyList()
            }
        }
    }

    private fun parseHour(time: String?): Int {
        if (time.isNullOrEmpty()) return 0
        return try {
            val cleaned = time.removeSuffix("Z")
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = sdf.parse(cleaned) ?: return 0
            val cal = java.util.Calendar.getInstance()
            cal.time = date
            cal.get(java.util.Calendar.HOUR_OF_DAY)
        } catch (e: Exception) {
            0
        }
    }

    private fun transformToUiState(station: WeatherStationData): StationUiState {
        val last24History = station.temperatureHistory.takeLast(24)
        val timeLabels = mutableListOf<TimeLabel>()

        if (last24History.size >= 2) {
            val totalPoints = last24History.size
            val startHour = station.lastMeasurementHour - (totalPoints - 1)

            for (index in 0 until totalPoints) {
                val hour = (startHour + index + 24) % 24
                val hoursAgo = (station.lastMeasurementHour - hour + 24) % 24

                if (index == 0 || index == totalPoints - 1 || hoursAgo % 6 == 0) {
                    timeLabels.add(TimeLabel(index = index, text = "${hour.toString().padStart(2, '0')}ч"))
                }
            }
        }

        return StationUiState(
            id = station.uid,
            name = station.name,
            currentTemperature = station.lastTemperature?.let { "${it.roundToInt()}°" } ?: "—",
            pressureText = station.lastPressure?.let { "${it.roundToInt()} гПа" } ?: "—",
            humidityText = station.lastHumidity?.let { "${it.roundToInt()}%" } ?: "—",
            chartPoints = last24History,
            timeLabels = timeLabels
        )
    }
}