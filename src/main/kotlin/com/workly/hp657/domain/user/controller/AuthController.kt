package com.workly.hp657.domain.user.controller

import com.workly.hp657.domain.user.dto.AuthResponse
import com.workly.hp657.domain.user.dto.LoginRequest
import com.workly.hp657.domain.user.dto.SignUpRequest
import com.workly.hp657.domain.user.service.UserService
import com.workly.hp657.global.response.ApiResponse
import jakarta.validation.Valid
import com.workly.hp657.global.security.JwtProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api")
class AuthController(
    private val userService: UserService,
    private val jwtProperties: JwtProperties
) {

    @PostMapping("/auth/signup")
    fun signUp(@Valid @RequestBody request: SignUpRequest): ResponseEntity<ApiResponse<Unit>> {
        userService.signUp(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("회원가입이 완료되었습니다."))
    }

    @PostMapping("/auth/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val result = userService.login(request)
        return authResponse(HttpStatus.OK, result.response, result.refreshToken)
    }

    @PostMapping("/auth/refresh")
    fun refresh(@CookieValue(name = "refreshToken", required = false) refreshToken: String?): ResponseEntity<ApiResponse<AuthResponse>> {
        if (refreshToken.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token이 없습니다.")
        }

        return ResponseEntity.ok(ApiResponse.success(userService.refresh(refreshToken)))
    }

    @GetMapping("/auth/me")
    fun me(authentication: Authentication): ResponseEntity<ApiResponse<Map<String, Any>>> {
        return ResponseEntity.ok(ApiResponse.success(
            mapOf(
                "email" to authentication.name,
                "authenticated" to authentication.isAuthenticated
            )
        ))
    }

    private fun authResponse(
        status: HttpStatus,
        response: AuthResponse,
        refreshToken: String
    ): ResponseEntity<ApiResponse<AuthResponse>> {
        val cookie = ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(jwtProperties.cookieSecure)
            .sameSite("Lax")
            .path("/api/auth")
            .maxAge(jwtProperties.refreshExpirationMs / 1000)
            .build()

        return ResponseEntity.status(status)
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(ApiResponse.success(response))
    }
}
