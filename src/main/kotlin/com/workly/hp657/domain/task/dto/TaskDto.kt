package com.workly.hp657.domain.task.dto

import com.workly.hp657.domain.task.entity.Task
import com.workly.hp657.domain.task.entity.TaskPriority
import com.workly.hp657.domain.task.entity.TaskStatus
import java.time.LocalDateTime

data class TaskCreateRequest(
    val projectId: Long,
    val title: String,
    val description: String? = null,
    val assigneeId: Long? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val startAt: LocalDateTime? = null,
    val dueAt: LocalDateTime? = null
)

data class TaskUpdateRequest(
    val title: String,
    val description: String? = null,
    val assigneeId: Long? = null,
    val status: TaskStatus,
    val priority: TaskPriority,
    val startAt: LocalDateTime? = null,
    val dueAt: LocalDateTime? = null
)

data class TaskResponse(
    val id: Long,
    val projectId: Long,
    val title: String,
    val description: String?,
    val assigneeId: Long?,
    val status: TaskStatus,
    val priority: TaskPriority,
    val startAt: LocalDateTime?,
    val dueAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(task: Task): TaskResponse {
            return TaskResponse(
                id = task.id!!,
                projectId = task.project.id!!,
                title = task.title,
                description = task.description,
                assigneeId = task.assignee?.id,
                status = task.status,
                priority = task.priority,
                startAt = task.startAt,
                dueAt = task.dueAt,
                createdAt = task.createdAt,
                updatedAt = task.updatedAt
            )
        }
    }
}