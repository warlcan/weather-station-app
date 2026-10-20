package com.example.weatherstation.data.network

import com.example.weatherstation.data.model.ListMeasurementsResponse
import com.example.weatherstation.data.model.ListStationsResponse
import com.example.weatherstation.data.model.SendCodeRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.weatherstation.data.model.VerifyCodeRequest
import com.example.weatherstation.data.model.VerifyCodeResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiService {
    @GET("api/app/auth/verify-token")
    suspend fun verifyToken(
        @Header("Authorization") bearerToken: String
    ): Response<Unit>

    @POST("api/app/auth/send-code")
    suspend fun sendCode(
        @Body request: SendCodeRequest
    ): Response<Unit>

    @POST("api/app/auth/verify-code")
    suspend fun verifyCode(
        @Body request: VerifyCodeRequest
    ): Response<VerifyCodeResponse>

    @GET("api/app/stations")
    suspend fun getStations(
        @Header("Authorization") bearerToken: String
    ): Response<ListStationsResponse>

    @GET("api/app/measurements")
    suspend fun getMeasurements(
        @Header("Authorization") bearerToken: String,
        @Query("station_uids") stationUids: String
    ): Response<ListMeasurementsResponse>
}

object NetworkClient {
    private const val BASE_URL = "http://192.168.1.139:8080"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
