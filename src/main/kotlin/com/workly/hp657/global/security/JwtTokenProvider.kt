package com.workly.hp657.global.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())

    fun generateToken(userId: Long, email: String): String {
        return generateToken(userId, email, jwtProperties.expirationMs, "access")
    }

    fun generateRefreshToken(userId: Long, email: String): String {
        return generateToken(userId, email, jwtProperties.refreshExpirationMs, "refresh")
    }

    private fun generateToken(userId: Long, email: String, expirationMs: Long, type: String): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            getClaims(token).get("type", String::class.java) == "access"
        } catch (_: Exception) {
            false
        }
    }

    fun validateRefreshToken(token: String): Boolean {
        return try {
            getClaims(token).get("type", String::class.java) == "refresh"
        } catch (_: Exception) {
            false
        }
    }

    fun getEmail(token: String): String = getClaims(token).get("email", String::class.java)

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
