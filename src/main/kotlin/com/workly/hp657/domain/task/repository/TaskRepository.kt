package com.workly.hp657.domain.task.repository

import com.workly.hp657.domain.task.entity.Task
import org.springframework.data.jpa.repository.JpaRepository

interface TaskRepository : JpaRepository<Task, Long> {
    fun findAllByProjectId(projectId: Long): List<Task>
    fun findAllByAssigneeId(assigneeId: Long): List<Task>
}