package com.workly.hp657.domain.workspace.service

import com.workly.hp657.domain.user.repository.UserRepository
import com.workly.hp657.domain.workspace.dto.WorkspaceCreateRequest
import com.workly.hp657.domain.workspace.dto.WorkspaceResponse
import com.workly.hp657.domain.workspace.dto.WorkspaceUpdateRequest
import com.workly.hp657.domain.workspace.entity.Workspace
import com.workly.hp657.domain.workspace.entity.WorkspaceMember
import com.workly.hp657.domain.workspace.entity.WorkspaceMemberRole
import com.workly.hp657.domain.workspace.repository.WorkspaceMemberRepository
import com.workly.hp657.domain.workspace.repository.WorkspaceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class WorkspaceService(
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceMemberRepository: WorkspaceMemberRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun create(
        email: String,
        request: WorkspaceCreateRequest
    ): WorkspaceResponse {

        val admin = userRepository.findByEmail(email)
            .orElseThrow {
                IllegalArgumentException("사용자를 찾을 수 없습니다.")
            }

        val workspace = Workspace(
            name = request.name,
            description = request.description,
            admin = admin
        )

        val savedWorkspace = workspaceRepository.save(workspace)

        workspaceMemberRepository.save(
            WorkspaceMember(
                workspace = savedWorkspace,
                user = admin,
                role = WorkspaceMemberRole.ADMIN
            )
        )

        return savedWorkspace.toResponse()
    }

    fun get(workspaceId: Long): WorkspaceResponse {

        val workspace = findWorkspace(workspaceId)

        return workspace.toResponse()
    }

    fun getAll(): List<WorkspaceResponse> {

        return workspaceRepository.findAll()
            .map { it.toResponse() }
    }

    fun getByAdmin(adminId: Long): List<WorkspaceResponse> {

        return workspaceRepository
            .findAllByAdminId(adminId)
            .map { it.toResponse() }
    }

    @Transactional
    fun update(
        workspaceId: Long,
        request: WorkspaceUpdateRequest
    ): WorkspaceResponse {

        val workspace = findWorkspace(workspaceId)

        workspace.name = request.name
        workspace.description = request.description
        workspace.updatedAt = LocalDateTime.now()

        return workspace.toResponse()
    }

    @Transactional
    fun delete(workspaceId: Long) {

        val workspace = findWorkspace(workspaceId)

        workspaceRepository.delete(workspace)
    }

    private fun findWorkspace(workspaceId: Long): Workspace {

        return workspaceRepository.findById(workspaceId)
            .orElseThrow {
                IllegalArgumentException(
                    "워크스페이스를 찾을 수 없습니다. id=$workspaceId"
                )
            }
    }

    private fun Workspace.toResponse(): WorkspaceResponse {

        return WorkspaceResponse(
            id = id!!,
            name = name,
            description = description,
            adminId = admin.id!!,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}