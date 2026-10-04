package com.example.weatherstation.data.model

data class TokenVerifyRequest(
    val token: String
)
data class TokenVerifyResponse(
    val isValid: Boolean
)

data class SendCodeRequest(
    val email: String
)
data class SendCodeResponse(
    val success: Boolean
)

data class VerifyCodeRequest(
    val email: String,
    val code: String
)
data class VerifyCodeResponse(
    val success: Boolean,
    val token: String?,
)
