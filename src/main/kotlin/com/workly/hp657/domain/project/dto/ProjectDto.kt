package com.workly.hp657.domain.project.dto

import com.workly.hp657.domain.project.entity.Project
import com.workly.hp657.domain.project.entity.ProjectMemberRole
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class ProjectCreateRequest(

    @field:NotBlank
    @field:Size(max = 100)
    val name: String,

    @field:Size(max = 1000)
    val description: String? = null,

    val leaderId: Long
)

data class ProjectUpdateRequest(

    @field:NotBlank
    @field:Size(max = 100)
    val name: String,

    @field:Size(max = 1000)
    val description: String? = null,

    val leaderId: Long
)

data class ProjectResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val workspaceId: Long,
    val createdById: Long,
    val leaderId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(project: Project): ProjectResponse {
            return ProjectResponse(
                id = project.id!!,
                name = project.name,
                description = project.description,
                workspaceId = project.workspace.id!!,
                createdById = project.createdBy.id!!,
                leaderId = project.leader.id!!,
                createdAt = project.createdAt,
                updatedAt = project.updatedAt
            )
        }
    }
}

data class ProjectMemberAddRequest(
    val userId: Long,
    val role: ProjectMemberRole = ProjectMemberRole.MEMBER
)

data class ProjectMemberUpdateRequest(
    val role: ProjectMemberRole
)