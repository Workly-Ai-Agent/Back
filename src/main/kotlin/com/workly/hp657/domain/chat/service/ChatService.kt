package com.workly.hp657.domain.chat.service

import com.workly.hp657.domain.chat.dto.MessageResponse
import com.workly.hp657.domain.chat.dto.SendMessageRequest
import com.workly.hp657.domain.chat.entity.Message
import com.workly.hp657.domain.chat.repository.MessageRepository
import com.workly.hp657.domain.project.repository.ProjectRepository
import com.workly.hp657.domain.user.repository.UserRepository
import com.workly.hp657.domain.workspace.repository.WorkspaceMemberRepository
import com.workly.hp657.domain.workspace.repository.WorkspaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ChatService(private val messages: MessageRepository, private val users: UserRepository, private val workspaces: WorkspaceRepository, private val members: WorkspaceMemberRepository, private val projects: ProjectRepository) {
    @Transactional
    fun sendMessage(email: String, request: SendMessageRequest): MessageResponse {
        require(request.content.isNotBlank()) { "Message content is required" }
        val sender = users.findByEmail(email).orElseThrow { IllegalArgumentException("User not found") }
        val receiver = request.receiverId?.let { users.findById(it).orElseThrow { IllegalArgumentException("Receiver not found") } }
        val workspace = workspaces.findById(request.workspaceId).orElseThrow { IllegalArgumentException("Workspace not found") }
        val project = request.projectId?.let { projects.findById(it).orElseThrow { IllegalArgumentException("Project not found") } }
        require(project == null || project.workspace.id == workspace.id) { "Project does not belong to workspace" }
        validateMember(workspace.id!!, sender.id!!)
        receiver?.let { validateMember(workspace.id!!, it.id!!) }
        return MessageResponse.from(messages.save(Message(workspace = workspace, sender = sender, receiver = receiver, project = project, content = request.content.trim())))
    }

    fun getConversation(email: String, workspaceId: Long, userId: Long): List<MessageResponse> {
        val user = users.findByEmail(email).orElseThrow { IllegalArgumentException("User not found") }
        validateMember(workspaceId, user.id!!)
        return messages.findConversation(workspaceId, user.id!!, userId).map(MessageResponse::from)
    }

    fun getChannel(email: String, workspaceId: Long, projectId: Long?, receiverId: Long?): List<MessageResponse> {
        val user = users.findByEmail(email).orElseThrow { IllegalArgumentException("User not found") }
        validateMember(workspaceId, user.id!!)
        receiverId?.let { validateMember(workspaceId, it) }
        projectId?.let { require(projects.findById(it).orElseThrow { IllegalArgumentException("Project not found") }.workspace.id == workspaceId) }
        return messages.findChannel(workspaceId, projectId, receiverId, user.id!!).map(MessageResponse::from)
    }

    private fun validateMember(workspaceId: Long, userId: Long) {
        require(members.existsByWorkspaceIdAndUserId(workspaceId, userId)) { "Workspace member access required" }
    }
}
