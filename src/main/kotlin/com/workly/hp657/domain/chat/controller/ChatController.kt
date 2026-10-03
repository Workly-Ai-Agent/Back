package com.workly.hp657.domain.chat.controller

import com.workly.hp657.domain.chat.dto.MessageResponse
import com.workly.hp657.domain.chat.dto.SendMessageRequest
import com.workly.hp657.domain.chat.service.ChatService
import com.workly.hp657.global.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService,
    private val messaging: SimpMessagingTemplate
) {

    @PostMapping("/messages")
    fun sendMessage(
        principal: Principal,
        @RequestBody request: SendMessageRequest
    ): ResponseEntity<ApiResponse<MessageResponse>> {
        val saved = chatService.sendMessage(principal.name, request)
        messaging.convertAndSend(destination(request, saved), saved)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(saved))
    }

    @GetMapping("/conversations/{userId}")
    fun getConversation(
        principal: Principal,
        @RequestParam workspaceId: Long,
        @PathVariable userId: Long
    ): ResponseEntity<ApiResponse<List<MessageResponse>>> {
        return ResponseEntity.ok(
            ApiResponse.success(
                chatService.getConversation(principal.name, workspaceId, userId)
            )
        )
    }

    @GetMapping("/channels")
    fun getChannel(principal: Principal, @RequestParam workspaceId: Long, @RequestParam(required = false) projectId: Long?, @RequestParam(required = false) receiverId: Long?): ResponseEntity<ApiResponse<List<MessageResponse>>> = ResponseEntity.ok(
        ApiResponse.success(chatService.getChannel(principal.name, workspaceId, projectId, receiverId))
    )

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
