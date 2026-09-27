package com.workly.hp657.domain.task.service

import com.workly.hp657.domain.project.repository.ProjectRepository
import com.workly.hp657.domain.task.dto.TaskCreateRequest
import com.workly.hp657.domain.task.dto.TaskResponse
import com.workly.hp657.domain.task.entity.Task
import com.workly.hp657.domain.task.entity.TaskStatus
import com.workly.hp657.domain.task.repository.TaskRepository
import com.workly.hp657.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TaskService(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository
) {

    fun getTasks(projectId: Long? = null): List<TaskResponse> {
        val tasks = projectId?.let(taskRepository::findAllByProjectId)
            ?: taskRepository.findAll()
        return tasks.map(TaskResponse::from)
    }

    fun getTask(id: Long): TaskResponse {
        val task = taskRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Task를 찾을 수 없습니다.") }
        return TaskResponse.from(task)
    }

    @Transactional
    fun create(request: TaskCreateRequest): TaskResponse {
        val project = projectRepository.findById(request.projectId)
            .orElseThrow { IllegalArgumentException("Project를 찾을 수 없습니다.") }
        val assignee = request.assigneeId?.let {
            userRepository.findById(it)
                .orElseThrow { IllegalArgumentException("담당자를 찾을 수 없습니다.") }
        }
        val task = Task(
            title = request.title,
            description = request.description,
            project = project,
            assignee = assignee,
            priority = request.priority,
            startAt = request.startAt,
            dueAt = request.dueAt
        )
        return TaskResponse.from(taskRepository.save(task))
    }

    @Transactional
    fun updateStatus(id: Long, status: TaskStatus): TaskResponse {
        val task = taskRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Task를 찾을 수 없습니다.") }
        task.status = status
        return TaskResponse.from(taskRepository.save(task))
    }

    @Transactional
    fun update(id: Long, request: com.workly.hp657.domain.task.dto.TaskUpdateRequest): TaskResponse {
        val task = taskRepository.findById(id).orElseThrow { IllegalArgumentException("Task not found") }
        task.title = request.title.trim()
        task.description = request.description
        task.status = request.status
        task.priority = request.priority
        task.assignee = request.assigneeId?.let { userRepository.findById(it).orElseThrow { IllegalArgumentException("Assignee not found") } }
        task.startAt = request.startAt
        task.dueAt = request.dueAt
        return TaskResponse.from(taskRepository.save(task))
    }
}
