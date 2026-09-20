package com.workly.hp657.domain.project.repository

import com.workly.hp657.domain.project.entity.Project
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectRepository : JpaRepository<Project, Long> {
    fun findAllByWorkspaceId(workspaceId: Long): List<Project>
}