package com.workly.hp657.domain.workspace.dto

import java.time.LocalDateTime

data class WorkspaceCreateRequest(
    val name: String,
    val description: String? = null
)

data class WorkspaceUpdateRequest(
    val name: String,
    val description: String? = null
)

data class WorkspaceResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val adminId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)