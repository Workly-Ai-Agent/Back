package com.workly.hp657.domain.agent.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.workly.hp657.domain.agent.dto.*
import com.workly.hp657.domain.project.repository.ProjectRepository
import com.workly.hp657.domain.project.repository.ProjectMemberRepository
import com.workly.hp657.domain.agent.entity.AgentProposal
import com.workly.hp657.domain.agent.entity.ProposalStatus
import com.workly.hp657.domain.agent.repository.AgentProposalRepository
import com.workly.hp657.domain.skill.repository.UserSkillRepository
import com.workly.hp657.domain.task.entity.Task
import com.workly.hp657.domain.task.entity.TaskStatus
import com.workly.hp657.domain.task.entity.TaskPriority
import com.workly.hp657.domain.task.dto.TaskResponse
import com.workly.hp657.domain.task.repository.TaskRepository
import com.workly.hp657.domain.user.repository.UserRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class AgentService(
    private val projectRepository: ProjectRepository,
    private val projectMemberRepository: ProjectMemberRepository,
    private val proposalRepository: AgentProposalRepository,
    private val userRepository: UserRepository,
    private val userSkillRepository: UserSkillRepository,
    private val taskRepository: TaskRepository,
    private val objectMapper: ObjectMapper,
    @Value("\${agent.base-url:http://localhost:8000}") private val agentBaseUrl: String
) {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    @Transactional
    fun createProposal(email: String, projectId: Long, request: AgentWorkflowRequest): AgentProposalResponse {
        val project = projectRepository.findById(projectId).orElseThrow { IllegalArgumentException("Project를 찾을 수 없습니다.") }
        val creator = userRepository.findByEmail(email).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, creator.id!!)) throw AccessDeniedException("프로젝트 멤버만 제안할 수 있습니다.")
        val initialWorkflow = runWorkflow(projectId, request)
        val existingTasks = taskRepository.findAllByProjectId(projectId).filter { it.status != TaskStatus.CANCELLED }
        val proposedTitles = initialWorkflow.tasks.mapNotNull { it["task"]?.toString()?.trim()?.lowercase() }.toSet()
        val assignmentByTitle = initialWorkflow.assignments.associateBy { it["task"]?.toString()?.trim()?.lowercase() }
        val impact = buildList {
            existingTasks.filter { it.status != TaskStatus.COMPLETED && it.title.trim().lowercase() !in proposedTitles }.forEach {
                add("기존 Task가 새 계획에 포함되지 않았습니다. 승인 시 삭제하지 않고 계획 제외 상태로 보존합니다: #${it.id} ${it.title}")
            }
            existingTasks.forEach { task ->
                val proposedAssignee = assignmentByTitle[task.title.trim().lowercase()]?.get("assignee")?.toString()
                if (proposedAssignee != null && proposedAssignee != task.assignee?.name) {
                    add("담당자 변경 제안: #${task.id} ${task.title} (${task.assignee?.name ?: "미배정"} → $proposedAssignee)")
                }
                val planned = initialWorkflow.tasks.firstOrNull { it["task"]?.toString()?.trim()?.equals(task.title.trim(), ignoreCase = true) == true }
                val proposedDueAt = planned?.get("due_at")?.toString()?.takeIf { it.isNotBlank() }
                if (proposedDueAt != null && parsePlanDate(proposedDueAt) != task.dueAt) {
                    add("기한 변경 제안: #${task.id} ${task.title} (${task.dueAt ?: "미정"} → $proposedDueAt)")
                }
                val proposedDependencies = (planned?.get("depends_on") as? List<*>)?.filterIsInstance<String>()?.toSet().orEmpty()
                val currentDependencies = task.dependencies.map { it.title }.toSet()
                if (proposedDependencies != currentDependencies) {
                    add("선행 업무 변경 제안: #${task.id} ${task.title} (${currentDependencies.ifEmpty { setOf("없음") }.joinToString()} → ${proposedDependencies.ifEmpty { setOf("없음") }.joinToString()})")
                }
            }
        }
        val workflow = initialWorkflow.copy(monitoring = initialWorkflow.monitoring + impact)
        require(workflow.violations.isEmpty()) { "검증 오류가 있어 승인 요청을 만들지 않았습니다: ${workflow.violations.joinToString()}" }
        val proposal = proposalRepository.save(AgentProposal(
            project = project, createdBy = creator, requestText = request.planText,
            resultJson = objectMapper.writeValueAsString(workflow)
        ))
        return proposalResponse(proposal)
    }

    @Transactional(readOnly = true)
    fun getProposals(email: String, projectId: Long): List<AgentProposalResponse> {
        val user = userRepository.findByEmail(email).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, user.id!!)) throw AccessDeniedException("프로젝트 멤버가 아닙니다.")
        return proposalRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId).map(::proposalResponse)
    }

    @Transactional
    fun approveProposal(email: String, proposalId: Long): List<TaskResponse> {
        val approver = userRepository.findByEmail(email).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        val proposal = proposalRepository.findById(proposalId).orElseThrow { IllegalArgumentException("제안을 찾을 수 없습니다.") }
        requireLeader(approver.id!!, proposal.project.id!!)
        check(proposal.status == ProposalStatus.PENDING) { "대기 중인 제안만 승인할 수 있습니다." }
        val result = objectMapper.readValue(proposal.resultJson, AgentWorkflowResponse::class.java)
        require(result.violations.isEmpty()) { "검증 오류가 있는 제안은 승인할 수 없습니다." }
        val members = projectMemberRepository.findAllByProjectId(proposal.project.id!!).associateBy { it.user.name }
        val assignments = result.assignments.associateBy { it["task"]?.toString() }
        val activeTasks = taskRepository.findAllByProjectId(proposal.project.id!!).filter { it.status != TaskStatus.CANCELLED }
        val existingByTitle = activeTasks.associateBy { it.title.trim().lowercase() }
        val plannedTasks = result.tasks.mapNotNull { data ->
            val title = data["task"]?.toString() ?: return@mapNotNull null
            val assigneeName = assignments[title]?.get("assignee")?.toString()
            val assignee = assigneeName?.let { members[it]?.user }
            require(assigneeName == null || assignee != null) { "프로젝트 외부 사용자가 담당자로 제안되었습니다: $assigneeName" }
            val description = buildString {
                data["description"]?.toString()?.takeIf(String::isNotBlank)?.let { append(it) }
                (data["required_skills"] as? List<*>)?.filterIsInstance<String>()?.takeIf { it.isNotEmpty() }?.let { append("\n필요 Skill: ").append(it.joinToString(", ")) }
                (data["depends_on"] as? List<*>)?.filterIsInstance<String>()?.takeIf { it.isNotEmpty() }?.let { append("\n선행 Task: ").append(it.joinToString(", ")) }
                assignments[title]?.get("reason")?.toString()?.takeIf(String::isNotBlank)?.let { append("\n배정 근거: ").append(it) }
            }
            val existing = existingByTitle[title.trim().lowercase()]
            val task = existing ?: Task(title = title, description = null, project = proposal.project)
            task.description = description.ifBlank { null }
            task.assignee = assignee
            (data["priority"] as? String)?.let { task.priority = TaskPriority.valueOf(it.uppercase()) }
            (data["start_at"] as? String)?.takeIf { it.isNotBlank() }?.let { task.startAt = parsePlanDate(it) }
            (data["due_at"] as? String)?.takeIf { it.isNotBlank() }?.let { task.dueAt = parsePlanDate(it) }
            task.updatedAt = java.time.LocalDateTime.now()
            title to taskRepository.save(task)
        }.toMap()
        val taskNames = plannedTasks.keys
        val normalizedPlannedTitles = taskNames.map { it.trim().lowercase() }.toSet()
        val retiredTasks = activeTasks.filter { it.status != TaskStatus.COMPLETED && it.title.trim().lowercase() !in normalizedPlannedTitles }.map { task ->
            task.status = TaskStatus.CANCELLED
            task.description = listOfNotNull(task.description, "Leader 승인으로 AI 재계획에서 제외되었습니다.").joinToString("\n")
            task.updatedAt = java.time.LocalDateTime.now()
            taskRepository.save(task)
        }
        result.tasks.forEach { data ->
            val title = data["task"]?.toString() ?: return@forEach
            val task = plannedTasks[title] ?: return@forEach
            val dependencyNames = (data["depends_on"] as? List<*>)?.filterIsInstance<String>().orEmpty()
            require(dependencyNames.all { it in taskNames }) { "제안에 현재 계획에 없는 선행 Task가 있습니다: $title" }
            require(title !in dependencyNames) { "Task가 자신을 선행 Task로 지정할 수 없습니다: $title" }
            task.dependencies.clear()
            task.dependencies.addAll(dependencyNames.mapNotNull(plannedTasks::get))
            taskRepository.save(task)
        }
        val created = (plannedTasks.values + retiredTasks).distinctBy { it.id }.map(TaskResponse::from)
        proposal.status = ProposalStatus.APPROVED
        proposal.updatedAt = java.time.LocalDateTime.now()
        return created
    }

    @Transactional
    fun rejectProposal(email: String, proposalId: Long) {
        val approver = userRepository.findByEmail(email).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        val proposal = proposalRepository.findById(proposalId).orElseThrow { IllegalArgumentException("제안을 찾을 수 없습니다.") }
        requireLeader(approver.id!!, proposal.project.id!!)
        check(proposal.status == ProposalStatus.PENDING) { "대기 중인 제안만 거절할 수 있습니다." }
        proposal.status = ProposalStatus.REJECTED
        proposal.updatedAt = java.time.LocalDateTime.now()
    }

    @Transactional
    fun evaluateProposal(email: String, proposalId: Long, request: AgentProposalEvaluationRequest) {
        val evaluator = userRepository.findByEmail(email).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        val proposal = proposalRepository.findById(proposalId).orElseThrow { IllegalArgumentException("제안을 찾을 수 없습니다.") }
        requireLeader(evaluator.id!!, proposal.project.id!!)
        check(proposal.status != ProposalStatus.PENDING) { "승인 또는 거절이 완료된 제안만 평가할 수 있습니다." }
        proposal.requirementsStructuredCorrectly = request.requirementsStructuredCorrectly
        proposal.skillMatchingCorrect = request.skillMatchingCorrect
        proposal.impactDetectionCorrect = request.impactDetectionCorrect
        proposal.replanningSuccessful = request.replanningSuccessful
        proposal.updatedAt = java.time.LocalDateTime.now()
    }

    @Transactional(readOnly = true)
    fun getMetrics(email: String, projectId: Long): ProjectAgentMetricsResponse {
        val user = userRepository.findByEmail(email).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        require(projectMemberRepository.existsByProjectIdAndUserId(projectId, user.id!!)) { "프로젝트 멤버가 아닙니다." }
        val proposals = proposalRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId)
        fun rate(values: List<Boolean?>): MetricRate {
            val evaluated = values.filterNotNull()
            return MetricRate(evaluated.count { it }.toLong(), evaluated.size.toLong(),
                if (evaluated.isEmpty()) null else evaluated.count { it }.toDouble() * 100 / evaluated.size)
        }
        val decided = proposals.filter { it.status != ProposalStatus.PENDING }
        return ProjectAgentMetricsResponse(
            requirementStructuring = rate(proposals.map { it.requirementsStructuredCorrectly }),
            taskCreation = rate(decided.map { it.status == ProposalStatus.APPROVED }),
            skillMatching = rate(proposals.map { it.skillMatchingCorrect }),
            changeImpactDetection = rate(proposals.map { it.impactDetectionCorrect }),
            replanning = rate(proposals.map { it.replanningSuccessful })
        )
    }

    private fun requireLeader(userId: Long, projectId: Long) {
        if (projectMemberRepository.findByProjectIdAndUserId(projectId, userId)?.role?.name != "LEADER") {
            throw AccessDeniedException("프로젝트 Leader만 승인할 수 있습니다.")
        }
    }

    private fun parsePlanDate(value: String): LocalDateTime =
        runCatching { LocalDateTime.parse(value) }.getOrElse {
            runCatching { LocalDate.parse(value).atStartOfDay() }.getOrElse {
                throw IllegalArgumentException("계획 날짜는 ISO 날짜 또는 날짜-시간 형식이어야 합니다: $value")
            }
        }

    private fun proposalResponse(proposal: AgentProposal) = AgentProposalResponse(
        id = proposal.id!!, projectId = proposal.project.id!!, status = proposal.status.name,
        requestText = proposal.requestText,
        result = objectMapper.readValue(proposal.resultJson, AgentWorkflowResponse::class.java),
        createdAt = proposal.createdAt,
        requirementsStructuredCorrectly = proposal.requirementsStructuredCorrectly,
        skillMatchingCorrect = proposal.skillMatchingCorrect,
        impactDetectionCorrect = proposal.impactDetectionCorrect,
        replanningSuccessful = proposal.replanningSuccessful
    )

    fun runWorkflow(projectId: Long, request: AgentWorkflowRequest): AgentWorkflowResponse {
        val project = projectRepository.findById(projectId)
            .orElseThrow { IllegalArgumentException("Project를 찾을 수 없습니다.") }
        val members = projectMemberRepository.findAllByProjectId(projectId).map { member ->
            val activeWorkload = taskRepository.findAllByAssigneeId(member.user.id!!)
                .count { it.project.id == projectId && it.status != TaskStatus.COMPLETED && it.status != TaskStatus.CANCELLED }
            mapOf(
                "name" to member.user.name,
                "skills" to userSkillRepository.findAllByUserId(member.user.id!!).map { it.skill.name },
                "current_workload" to activeWorkload
            )
        }
        val payload = mapOf(
            "project_name" to project.name,
            "plan_text" to request.planText,
            "employees" to members
        )
        return post("/api/agent/workflow", objectMapper.writeValueAsString(payload), AgentWorkflowResponse::class.java)
    }

    fun runWorkflow(email: String, projectId: Long, request: AgentWorkflowRequest): AgentWorkflowResponse {
        val user = userRepository.findByEmail(email).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, user.id!!)) throw AccessDeniedException("프로젝트 멤버만 Workflow에 접근할 수 있습니다.")
        return runWorkflow(projectId, request)
    }

    fun classifyMessage(request: AgentMessengerIntentRequest): AgentMessengerIntentResponse =
        post("/api/agent/messenger-intent", objectMapper.writeValueAsString(request), AgentMessengerIntentResponse::class.java)

    fun extractSkills(request: AgentSkillExtractionRequest): AgentSkillExtractionResponse =
        post("/api/agent/extract-skills", objectMapper.writeValueAsString(mapOf("profile_text" to request.profileText)), AgentSkillExtractionResponse::class.java)

    private fun <T> post(path: String, payload: String, responseType: Class<T>): T {
        val httpRequest = HttpRequest.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            // Planner와 Assignment가 여러 번 호출될 수 있으므로 브라우저 요청보다
            // 충분히 긴 서버-서버 타임아웃을 사용한다.
            .timeout(Duration.ofSeconds(240))
            .uri(URI.create(agentBaseUrl.trimEnd('/') + path))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload, Charsets.UTF_8))
            .build()
        val response = try {
            httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(Charsets.UTF_8))
        } catch (exception: java.net.http.HttpTimeoutException) {
            throw IllegalStateException(
                "AI Agent 응답 시간이 초과되었습니다. Agent 서버가 실행 중인지, OPENAI_API_KEY가 설정되었는지 확인해 주세요.",
                exception
            )
        }
        if (response.statusCode() !in 200..299) {
            throw IllegalStateException("Agent 요청 실패 (${response.statusCode()}): ${response.body()}")
        }
        return objectMapper.readValue(response.body(), responseType)
    }
}
