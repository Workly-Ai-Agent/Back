package com.workly.hp657.domain.workspace.repository

import com.workly.hp657.domain.workspace.entity.Workspace
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkspaceRepository : JpaRepository<Workspace, Long> {
    fun findAllByAdminId(adminId: Long): List<Workspace>
}