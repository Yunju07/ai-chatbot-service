package com.sionic.ai.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


data class SignupRequest(
    @field:Email
    @field:NotBlank
    val email: String,

    @field:NotBlank
    @field:Size(min = 8, max = 72)
    val password: String,

    @field:NotBlank
    val name: String,
)

data class LoginRequest(
    @field:Email
    @field:NotBlank
    val email: String,

    @field:NotBlank
    val password: String,
)

data class AuthResponse(
    val token: String,
    val tokenType: String = "Bearer",
    val expiresInMinutes: Long,
)
