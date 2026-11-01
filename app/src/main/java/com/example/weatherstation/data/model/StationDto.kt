package com.example.weatherstation.data.model

import com.google.gson.annotations.SerializedName

data class AppWeatherStationDto(
    val uid: Long,
    @SerializedName("hardware_id") val hardwareId: String,
    val name: String?,
    @SerializedName("error_mask") val errorMask: Int,
    @SerializedName("gateway_uid") val gatewayUid: String
)

data class ListStationsResponse(
    val stations: List<AppWeatherStationDto>
)

data class AppMeasurementDto(
    val time: String,
    val temperature: Float?,
    val humidity: Float?,
    val pressure: Float?,
    @SerializedName("voltage_level") val voltageLevel: Float?
)

data class ListMeasurementsResponse(
    val measurements: Map<String, List<AppMeasurementDto>>
)