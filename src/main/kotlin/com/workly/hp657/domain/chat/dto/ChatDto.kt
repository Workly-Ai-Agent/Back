package com.workly.hp657.domain.chat.dto

data class ChatRequest(
    val message: String,
    val projectId: Long? = null
)

data class SendMessageRequest(
    val workspaceId: Long,
    val receiverId: Long? = null,
    val projectId: Long? = null,
    val content: String
)

data class MessageResponse(
    val id: Long,
    val workspaceId: Long,
    val senderId: Long,
    val senderName: String,
    val receiverId: Long?,
    val projectId: Long?,
    val content: String,
    val createdAt: java.time.LocalDateTime
) {
    companion object {
    fun from(message: com.workly.hp657.domain.chat.entity.Message) = MessageResponse(message.id!!, message.workspace.id!!, message.sender.id!!, message.sender.name, message.receiver?.id, message.project?.id, message.content, message.createdAt)
    }
}

data class ChatResponse(
    val reply: String,
    val intent: String,
    val requiresApproval: Boolean = false,
    val data: Any? = null
)
