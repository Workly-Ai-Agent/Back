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
    val assigneeName: String?,
    val dependsOn: List<Long>,
    val dependencyTitles: List<String>,
    val status: TaskStatus,
    val priority: TaskPriority,
    val startAt: LocalDateTime?,
    val dueAt: LocalDateTime?,
    val overdue: Boolean,
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
                assigneeName = task.assignee?.name,
                dependsOn = task.dependencies.mapNotNull { it.id },
                dependencyTitles = task.dependencies.map { it.title },
                status = task.status,
                priority = task.priority,
                startAt = task.startAt,
                dueAt = task.dueAt,
                overdue = task.dueAt?.isBefore(LocalDateTime.now()) == true &&
                    task.status != TaskStatus.COMPLETED && task.status != TaskStatus.CANCELLED,
                createdAt = task.createdAt,
                updatedAt = task.updatedAt
            )
        }
    }
}

data class DependencyBlockResponse(val taskId: Long, val taskTitle: String, val blockedBy: List<String>)
data class TaskMonitorResponse(
    val overdue: List<TaskResponse>,
    val unassigned: List<TaskResponse>,
    val dependencyBlocked: List<DependencyBlockResponse>
)

data class ProjectStatusSummary(val projectId: Long, val projectName: String, val active: Int, val completed: Int, val overdue: Int, val unassigned: Int)
data class WorkspaceTaskSummary(val workspaceId: Long, val active: Int, val completed: Int, val overdue: Int, val unassigned: Int, val projects: List<ProjectStatusSummary>)
