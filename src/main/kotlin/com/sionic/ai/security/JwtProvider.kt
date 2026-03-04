package com.sionic.ai.security

import com.sionic.ai.domain.Role
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import java.util.UUID

@Component
class JwtProvider(
    private val props: JwtProperties,
) {
    private val key = Keys.hmacShaKeyFor(props.secret.toByteArray(StandardCharsets.UTF_8))

    fun generate(userId: UUID, email: String, role: Role): String {
        val now = Instant.now()
        val expiry = now.plusSeconds(props.expirationMinutes * 60)
        return Jwts.builder()
            .subject(userId.toString())
            .issuer(props.issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .claim("email", email)
            .claim("role", role.name)
            .signWith(key)
            .compact()
    }

    fun parse(token: String): Claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}
