package com.example.weatherstation.data.network

import com.example.weatherstation.data.model.SendCodeRequest
import com.example.weatherstation.data.model.SendCodeResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.weatherstation.data.model.TokenVerifyRequest
import com.example.weatherstation.data.model.TokenVerifyResponse
import com.example.weatherstation.data.model.VerifyCodeRequest
import com.example.weatherstation.data.model.VerifyCodeResponse
import retrofit2.http.GET

interface ApiService {
    @POST("api/auth/verify-token")
    suspend fun verifyToken(
        @Body request: TokenVerifyRequest
    ): TokenVerifyResponse

    @POST("api/auth/send-code")
    suspend fun sendCode(
        @Body request: SendCodeRequest
    ): SendCodeResponse

    @POST("api/auth/verify-code")
    suspend fun verifyCode(
        @Body request: VerifyCodeRequest
    ): VerifyCodeResponse
}

object NetworkClient {
    private const val BASE_URL = "http://192.168.1.139"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
