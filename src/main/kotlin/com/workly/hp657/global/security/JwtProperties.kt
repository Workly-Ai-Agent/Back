package com.workly.hp657.global.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    var secret: String = "workly-dev-secret-key-change-me-in-production",
    var expirationMs: Long = 900000L,
    var refreshExpirationMs: Long = 1209600000L,
    var cookieSecure: Boolean = false
)
