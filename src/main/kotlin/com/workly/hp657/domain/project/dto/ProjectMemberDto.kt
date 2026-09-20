package com.workly.hp657.domain.project.dto

import com.workly.hp657.domain.project.entity.ProjectMember

data class AddProjectMemberRequest(
    val userId: Long,
    val role: String = "MEMBER"
)

data class UpdateProjectMemberRequest(
    val role: String
)

data class ProjectMemberResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val email: String,
    val role: String
) {
    companion object {
        fun from(member: ProjectMember): ProjectMemberResponse {
            return ProjectMemberResponse(
                id = member.id!!,
                userId = member.user.id!!,
                userName = member.user.name,
                email = member.user.email,
                role = member.role.name
            )
        }
    }
}