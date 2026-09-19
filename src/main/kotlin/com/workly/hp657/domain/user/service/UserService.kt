package com.workly.hp657.domain.user.service

import com.workly.hp657.global.security.JwtTokenProvider
import com.workly.hp657.domain.user.dto.AuthResponse
import com.workly.hp657.domain.user.dto.AuthTokens
import com.workly.hp657.domain.user.dto.LoginRequest
import com.workly.hp657.domain.user.dto.SignUpRequest
import com.workly.hp657.domain.user.entity.User
import com.workly.hp657.domain.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Transactional
    fun signUp(request: SignUpRequest) {
        if (userRepository.existsByEmail(request.email.lowercase())) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다.")
        }

        val encodedPassword = passwordEncoder.encode(request.password)
            ?: throw IllegalStateException("비밀번호 암호화에 실패했습니다.")

        val user = User(
            email = request.email.lowercase(),
            password = encodedPassword,
            name = request.name.trim()
        )

        userRepository.save(user)
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthTokens {
        val user = userRepository.findByEmail(request.email.lowercase())
            .orElseThrow { IllegalArgumentException("존재하지 않는 사용자입니다.") }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("비밀번호가 일치하지 않습니다.")
        }

        val token = jwtTokenProvider.generateToken(user.id!!, user.email)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id!!, user.email)

        return AuthTokens(
            response = AuthResponse(token, user.email, user.name),
            refreshToken = refreshToken
        )
    }

    @Transactional(readOnly = true)
    fun refresh(refreshToken: String): AuthResponse {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw IllegalArgumentException("유효하지 않은 refresh token입니다.")
        }

        val email = jwtTokenProvider.getEmail(refreshToken)
        val user = userRepository.findByEmail(email)
            .orElseThrow { IllegalArgumentException("존재하지 않는 사용자입니다.") }
        val token = jwtTokenProvider.generateToken(user.id!!, user.email)

        return AuthResponse(token, user.email, user.name)
    }
}
