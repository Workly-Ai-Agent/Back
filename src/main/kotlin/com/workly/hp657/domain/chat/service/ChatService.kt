package com.workly.hp657.domain.chat.service

import com.workly.hp657.domain.chat.dto.MessageResponse
import com.workly.hp657.domain.chat.dto.SendMessageRequest
import com.workly.hp657.domain.chat.entity.Message
import com.workly.hp657.domain.chat.repository.MessageRepository
import com.workly.hp657.domain.project.repository.ProjectRepository
import com.workly.hp657.domain.project.repository.ProjectMemberRepository
import com.workly.hp657.domain.user.repository.UserRepository
import com.workly.hp657.domain.workspace.repository.WorkspaceMemberRepository
import com.workly.hp657.domain.workspace.repository.WorkspaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.access.AccessDeniedException

@Service
@Transactional(readOnly = true)
class ChatService(private val messages: MessageRepository, private val users: UserRepository, private val workspaces: WorkspaceRepository, private val members: WorkspaceMemberRepository, private val projects: ProjectRepository, private val projectMembers: ProjectMemberRepository) {
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
        if (project != null) {
            requireProjectMember(project.id!!, sender.id!!)
            receiver?.let { requireProjectMember(project.id!!, it.id!!) }
        }
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
        projectId?.let {
            val project = projects.findById(it).orElseThrow { IllegalArgumentException("Project not found") }
            require(project.workspace.id == workspaceId)
            requireProjectMember(it, user.id!!)
            receiverId?.let { receiver -> requireProjectMember(it, receiver) }
        }
        return messages.findChannel(workspaceId, projectId, receiverId, user.id!!).map(MessageResponse::from)
    }

    private fun validateMember(workspaceId: Long, userId: Long) {
        require(members.existsByWorkspaceIdAndUserId(workspaceId, userId)) { "Workspace member access required" }
    }

    private fun requireProjectMember(projectId: Long, userId: Long) {
        if (!projectMembers.existsByProjectIdAndUserId(projectId, userId)) {
            throw AccessDeniedException("프로젝트 멤버만 프로젝트 채팅에 접근할 수 있습니다.")
        }
    }
}
