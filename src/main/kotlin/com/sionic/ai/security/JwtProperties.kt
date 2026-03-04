package com.sionic.ai.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
class JwtProperties(
    val issuer: String,
    val secret: String,
    val expirationMinutes: Long,
)
