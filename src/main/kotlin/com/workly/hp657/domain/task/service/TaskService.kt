package com.workly.hp657.domain.task.service

import com.workly.hp657.domain.project.repository.ProjectRepository
import com.workly.hp657.domain.task.dto.*
import com.workly.hp657.domain.task.entity.Task
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
    fun getTasks(projectId: Long? = null): List<TaskResponse> =
        (projectId?.let(taskRepository::findAllByProjectId) ?: taskRepository.findAll())
            .map(TaskResponse::from)

    fun getTask(id: Long): TaskResponse = TaskResponse.from(taskRepository.findById(id).orElseThrow { IllegalArgumentException("Task를 찾을 수 없습니다.") })

    @Transactional
    fun create(request: TaskCreateRequest): TaskResponse {
        val project = projectRepository.findById(request.projectId).orElseThrow { IllegalArgumentException("Project를 찾을 수 없습니다.") }
        val assignee = request.assigneeId?.let { userRepository.findById(it).orElseThrow { IllegalArgumentException("담당자를 찾을 수 없습니다.") } }
        return TaskResponse.from(taskRepository.save(Task(title = request.title, description = request.description, project = project, assignee = assignee, priority = request.priority, startAt = request.startAt, dueAt = request.dueAt)))
    }

    @Transactional
    fun updateStatus(id: Long, status: com.workly.hp657.domain.task.entity.TaskStatus): TaskResponse {
        val task = taskRepository.findById(id).orElseThrow { IllegalArgumentException("Task를 찾을 수 없습니다.") }
        task.status = status
        return TaskResponse.from(taskRepository.save(task))
    }
}
