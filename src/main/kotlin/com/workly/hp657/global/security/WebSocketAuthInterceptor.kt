package com.workly.hp657.global.security

import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component

@Component
class WebSocketAuthInterceptor(private val tokenProvider: JwtTokenProvider) : ChannelInterceptor {
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = StompHeaderAccessor.wrap(message)
        if (accessor.command == StompCommand.CONNECT) {
            val raw = accessor.getFirstNativeHeader("Authorization")
                ?.removePrefix("Bearer ")?.trim()
                ?: throw IllegalArgumentException("WebSocket authentication required")
            if (!tokenProvider.validateToken(raw)) throw IllegalArgumentException("Invalid access token")
            accessor.user = UsernamePasswordAuthenticationToken(tokenProvider.getEmail(raw), null, emptyList())
        }
        return message
    }
}
