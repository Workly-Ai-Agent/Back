package com.workly.hp657.domain.chat.controller

import com.workly.hp657.domain.chat.dto.SendMessageRequest
import com.workly.hp657.domain.chat.dto.MessageResponse
import com.workly.hp657.domain.chat.service.ChatService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class ChatWebSocketController(
    private val chatService: ChatService,
    private val messaging: SimpMessagingTemplate
) {
    @MessageMapping("/chat.send")
    fun send(request: SendMessageRequest, principal: Principal) {
        val saved = chatService.sendMessage(principal.name, request)
        messaging.convertAndSend(destination(request, saved), saved)
    }

    private fun destination(request: SendMessageRequest, message: MessageResponse): String = when {
        request.projectId != null -> "/topic/workspace/${request.workspaceId}/project/${request.projectId}"
        request.receiverId != null -> {
            val low = minOf(message.senderId, request.receiverId)
            val high = maxOf(message.senderId, request.receiverId)
            "/topic/workspace/${request.workspaceId}/direct/$low/$high"
        }
        else -> "/topic/workspace/${request.workspaceId}"
    }
}
