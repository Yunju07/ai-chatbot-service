package com.sionic.ai.service

import com.sionic.ai.domain.Role
import com.sionic.ai.domain.User
import com.sionic.ai.dto.AuthResponse
import com.sionic.ai.dto.LoginRequest
import com.sionic.ai.dto.SignupRequest
import com.sionic.ai.repository.UserRepository
import com.sionic.ai.security.JwtProperties
import com.sionic.ai.security.JwtProvider
import com.sionic.ai.util.BadRequestException
import com.sionic.ai.util.UnauthorizedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val jwtProperties: JwtProperties,
) {
    @Transactional
    fun signup(request: SignupRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw BadRequestException("Email already registered")
        }
        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            name = request.name,
            role = Role.MEMBER,
        )
        val saved = userRepository.save(user)
        val token = jwtProvider.generate(saved.id, saved.email, saved.role)
        return AuthResponse(token = token, expiresInMinutes = jwtProperties.expirationMinutes)
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { UnauthorizedException("Invalid credentials") }
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw UnauthorizedException("Invalid credentials")
        }
        val token = jwtProvider.generate(user.id, user.email, user.role)
        return AuthResponse(token = token, expiresInMinutes = jwtProperties.expirationMinutes)
    }
}
