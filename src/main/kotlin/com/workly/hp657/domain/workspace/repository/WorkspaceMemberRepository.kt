package com.workly.hp657.domain.workspace.repository

import com.workly.hp657.domain.workspace.entity.WorkspaceMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkspaceMemberRepository : JpaRepository<WorkspaceMember, Long> {
    fun findAllByWorkspaceId(workspaceId: Long): List<WorkspaceMember>
    fun findAllByUserId(userId: Long): List<WorkspaceMember>
    fun existsByWorkspaceIdAndUserId(
        workspaceId: Long,
        userId: Long
    ): Boolean
    fun findByWorkspaceIdAndUserId(
        workspaceId: Long,
        userId: Long
    ): WorkspaceMember?
}