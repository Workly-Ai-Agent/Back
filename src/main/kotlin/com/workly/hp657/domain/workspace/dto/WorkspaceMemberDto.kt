package com.workly.hp657.domain.workspace.dto

import com.workly.hp657.domain.workspace.entity.WorkspaceMemberRole
import java.time.LocalDateTime

data class WorkspaceMemberAddRequest(
    val email: String,
    val role: WorkspaceMemberRole = WorkspaceMemberRole.MEMBER
)

data class WorkspaceMemberUpdateRequest(
    val role: WorkspaceMemberRole
)

data class WorkspaceMemberResponse(
    val id: Long,
    val workspaceId: Long,
    val userId: Long,
    val role: WorkspaceMemberRole,
    val joinedAt: LocalDateTime
)