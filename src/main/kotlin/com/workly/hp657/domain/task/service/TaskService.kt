package com.workly.hp657.domain.task.service

import com.workly.hp657.domain.project.repository.ProjectRepository
import com.workly.hp657.domain.project.repository.ProjectMemberRepository
import com.workly.hp657.domain.task.dto.TaskCreateRequest
import com.workly.hp657.domain.task.dto.TaskResponse
import com.workly.hp657.domain.task.dto.TaskMonitorResponse
import com.workly.hp657.domain.task.dto.DependencyBlockResponse
import com.workly.hp657.domain.task.dto.WorkspaceTaskSummary
import com.workly.hp657.domain.task.dto.ProjectStatusSummary
import com.workly.hp657.domain.task.entity.Task
import com.workly.hp657.domain.task.entity.TaskStatus
import com.workly.hp657.domain.task.repository.TaskRepository
import com.workly.hp657.domain.user.repository.UserRepository
import com.workly.hp657.domain.workspace.repository.WorkspaceMemberRepository
import org.springframework.stereotype.Service
import org.springframework.security.access.AccessDeniedException
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TaskService(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val workspaceMemberRepository: WorkspaceMemberRepository
) {

    fun getTasks(email: String, projectId: Long? = null): List<TaskResponse> {
        require(projectId != null) { "프로젝트 범위로 Task를 조회해야 합니다." }
        requireMember(email, projectId)
        val tasks = taskRepository.findAllByProjectId(projectId)
        return tasks.map(TaskResponse::from)
    }

    fun getTask(email: String, id: Long): TaskResponse {
        val task = taskRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Task를 찾을 수 없습니다.") }
        requireMember(email, task.project.id!!)
        return TaskResponse.from(task)
    }

    fun getMonitor(email: String, projectId: Long): TaskMonitorResponse {
        requireMember(email, projectId)
        val tasks = taskRepository.findAllByProjectId(projectId)
            .filter { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED }
        return TaskMonitorResponse(
            overdue = tasks.filter { it.dueAt?.isBefore(java.time.LocalDateTime.now()) == true }.map(TaskResponse::from),
            unassigned = tasks.filter { it.assignee == null }.map(TaskResponse::from),
            dependencyBlocked = tasks.mapNotNull { task ->
                val blockers = task.dependencies.filter { it.status != TaskStatus.COMPLETED }.map { it.title }
                if (blockers.isEmpty()) null else DependencyBlockResponse(task.id!!, task.title, blockers)
            }
        )
    }

    fun getWorkspaceSummary(email: String, workspaceId: Long): WorkspaceTaskSummary {
        val user = userRepository.findByEmail(email).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, user.id!!)) {
            throw AccessDeniedException("Workspace 멤버만 업무 현황을 조회할 수 있습니다.")
        }
        val now = java.time.LocalDateTime.now()
        val summaries = projectRepository.findAllByWorkspaceId(workspaceId).filter {
            projectMemberRepository.existsByProjectIdAndUserId(it.id!!, user.id!!)
        }.map { project ->
            val tasks = taskRepository.findAllByProjectId(project.id!!)
            ProjectStatusSummary(
                projectId = project.id!!,
                projectName = project.name,
                active = tasks.count { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED },
                completed = tasks.count { it.status == TaskStatus.COMPLETED },
                overdue = tasks.count { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED && it.dueAt?.isBefore(now) == true },
                unassigned = tasks.count { it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED && it.assignee == null }
            )
        }
        return WorkspaceTaskSummary(
            workspaceId = workspaceId,
            active = summaries.sumOf { it.active },
            completed = summaries.sumOf { it.completed },
            overdue = summaries.sumOf { it.overdue },
            unassigned = summaries.sumOf { it.unassigned },
            projects = summaries
        )
    }

    @Transactional
    fun create(email: String, request: TaskCreateRequest): TaskResponse {
        val project = projectRepository.findById(request.projectId)
            .orElseThrow { IllegalArgumentException("Project를 찾을 수 없습니다.") }
        requireMember(email, project.id!!)
        val assignee = request.assigneeId?.let {
            val user = userRepository.findById(it)
                .orElseThrow { IllegalArgumentException("담당자를 찾을 수 없습니다.") }
            require(projectMemberRepository.existsByProjectIdAndUserId(project.id!!, user.id!!)) {
                "담당자는 프로젝트 멤버여야 합니다."
            }
            user
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
    fun updateStatus(email: String, id: Long, status: TaskStatus): TaskResponse {
        val task = taskRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Task를 찾을 수 없습니다.") }
        requireMember(email, task.project.id!!)
        task.status = status
        return TaskResponse.from(taskRepository.save(task))
    }

    @Transactional
    fun update(email: String, id: Long, request: com.workly.hp657.domain.task.dto.TaskUpdateRequest): TaskResponse {
        val task = taskRepository.findById(id).orElseThrow { IllegalArgumentException("Task not found") }
        requireMember(email, task.project.id!!)
        task.title = request.title.trim()
        task.description = request.description
        task.status = request.status
        task.priority = request.priority
        task.assignee = request.assigneeId?.let {
            val user = userRepository.findById(it).orElseThrow { IllegalArgumentException("Assignee not found") }
            require(projectMemberRepository.existsByProjectIdAndUserId(task.project.id!!, user.id!!)) {
                "담당자는 프로젝트 멤버여야 합니다."
            }
            user
        }
        task.startAt = request.startAt
        task.dueAt = request.dueAt
        return TaskResponse.from(taskRepository.save(task))
    }

    private fun requireMember(email: String, projectId: Long) {
        val user = userRepository.findByEmail(email).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, user.id!!)) {
            throw AccessDeniedException("프로젝트 멤버만 Task에 접근할 수 있습니다.")
        }
    }
}
