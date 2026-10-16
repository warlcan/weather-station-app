package com.example.weatherstation.data.model

data class SendCodeRequest(
    val email: String
)

data class VerifyCodeRequest(
    val email: String,
    val code: String
)
data class VerifyCodeResponse(
    val token: String?,
)
