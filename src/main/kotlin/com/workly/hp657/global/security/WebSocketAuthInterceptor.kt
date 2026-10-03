package com.workly.hp657.global.security

import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.security.access.AccessDeniedException
import com.workly.hp657.domain.project.repository.ProjectMemberRepository
import com.workly.hp657.domain.project.repository.ProjectRepository
import com.workly.hp657.domain.user.repository.UserRepository
import com.workly.hp657.domain.workspace.repository.WorkspaceMemberRepository

@Component
class WebSocketAuthInterceptor(
    private val tokenProvider: JwtTokenProvider,
    private val users: UserRepository,
    private val workspaceMembers: WorkspaceMemberRepository,
    private val projectMembers: ProjectMemberRepository,
    private val projects: ProjectRepository
) : ChannelInterceptor {
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        // Mutate the accessor attached to the message so the authenticated
        // Principal is retained for later SUBSCRIBE and SEND frames.
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: StompHeaderAccessor.wrap(message)
        if (accessor.command == StompCommand.CONNECT) {
            val raw = accessor.getFirstNativeHeader("Authorization")
                ?.removePrefix("Bearer ")?.trim()
                ?: throw IllegalArgumentException("WebSocket authentication required")
            if (!tokenProvider.validateToken(raw)) throw IllegalArgumentException("Invalid access token")
            accessor.user = UsernamePasswordAuthenticationToken(tokenProvider.getEmail(raw), null, emptyList())
        }
        if (accessor.command == StompCommand.SUBSCRIBE) {
            authorizeSubscription(accessor.user?.name, accessor.destination)
        }
        return message
    }

    private fun authorizeSubscription(email: String?, destination: String?) {
        val user = email?.let { users.findByEmail(it).orElse(null) }
            ?: throw AccessDeniedException("Authenticated user is required")
        val path = destination ?: throw AccessDeniedException("Subscription destination is required")
        val workspacePattern = Regex("^/topic/workspace/(\\d+)$")
        val projectPattern = Regex("^/topic/workspace/(\\d+)/project/(\\d+)$")
        val directPattern = Regex("^/topic/workspace/(\\d+)/direct/(\\d+)/(\\d+)$")
        workspacePattern.matchEntire(path)?.let { match ->
            requireWorkspaceMember(match.groupValues[1].toLong(), user.id!!)
            return
        }
        projectPattern.matchEntire(path)?.let { match ->
            val workspaceId = match.groupValues[1].toLong()
            val projectId = match.groupValues[2].toLong()
            val project = projects.findById(projectId).orElseThrow { AccessDeniedException("Project channel does not exist") }
            if (project.workspace.id != workspaceId || !projectMembers.existsByProjectIdAndUserId(projectId, user.id!!)) {
                throw AccessDeniedException("Project members only")
            }
            return
        }
        directPattern.matchEntire(path)?.let { match ->
            val workspaceId = match.groupValues[1].toLong()
            val participants = listOf(match.groupValues[2].toLong(), match.groupValues[3].toLong())
            requireWorkspaceMember(workspaceId, user.id!!)
            if (user.id !in participants || participants.any { !workspaceMembers.existsByWorkspaceIdAndUserId(workspaceId, it) }) {
                throw AccessDeniedException("Direct channel participants only")
            }
            return
        }
        throw AccessDeniedException("Subscription destination is not permitted")
    }

    private fun requireWorkspaceMember(workspaceId: Long, userId: Long) {
        if (!workspaceMembers.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw AccessDeniedException("Workspace members only")
        }
    }
}
