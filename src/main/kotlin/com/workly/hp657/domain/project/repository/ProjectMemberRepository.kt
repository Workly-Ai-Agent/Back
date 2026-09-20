package com.workly.hp657.domain.project.repository

import com.workly.hp657.domain.project.entity.ProjectMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectMemberRepository : JpaRepository<ProjectMember, Long> {
    fun findAllByProjectId(projectId: Long): List<ProjectMember>
    fun findByProjectIdAndUserId(
        projectId: Long,
        userId: Long
    ): ProjectMember?
    fun existsByProjectIdAndUserId(
        projectId: Long,
        userId: Long
    ): Boolean
}