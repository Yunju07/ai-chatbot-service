package com.sionic.ai.controller

import com.sionic.ai.dto.AuthResponse
import com.sionic.ai.dto.LoginRequest
import com.sionic.ai.dto.SignupRequest
import com.sionic.ai.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): AuthResponse =
        authService.signup(request)

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): AuthResponse =
        authService.login(request)
}
