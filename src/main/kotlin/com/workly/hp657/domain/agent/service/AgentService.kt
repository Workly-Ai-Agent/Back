package com.workly.hp657.domain.agent.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.workly.hp657.domain.agent.dto.*
import com.workly.hp657.domain.project.repository.ProjectRepository
import com.workly.hp657.domain.skill.repository.UserSkillRepository
import com.workly.hp657.domain.workspace.repository.WorkspaceMemberRepository
import com.workly.hp657.domain.task.entity.Task
import com.workly.hp657.domain.task.dto.TaskResponse
import com.workly.hp657.domain.task.repository.TaskRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Service
class AgentService(
    private val projectRepository: ProjectRepository,
    private val workspaceMemberRepository: WorkspaceMemberRepository,
    private val userSkillRepository: UserSkillRepository,
    private val taskRepository: TaskRepository,
    private val objectMapper: ObjectMapper,
    @Value("\${agent.base-url:http://localhost:8000}") private val agentBaseUrl: String
) {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()

    @Transactional
    fun generateAndCreate(projectId: Long, request: AgentWorkflowRequest): List<TaskResponse> {
        val project = projectRepository.findById(projectId)
            .orElseThrow { IllegalArgumentException("Project를 찾을 수 없습니다.") }
        val members = workspaceMemberRepository.findAllByWorkspaceId(project.workspace.id!!)
        val analysis = runWorkflow(projectId, request)
        val usersByName = members.associateBy { it.user.name }
        val assignments = analysis.assignments.associateBy { it["task"]?.toString() }
        return analysis.tasks.mapNotNull { taskData ->
            val title = taskData["task"]?.toString() ?: return@mapNotNull null
            val assignee = assignments[title]?.get("assignee")?.toString()?.let(usersByName::get)?.user
            val skills = (taskData["required_skills"] as? List<*>)
                ?.filterIsInstance<String>()
                ?.joinToString(", ")
            val dependency = (taskData["depends_on"] as? List<*>)
                ?.filterIsInstance<String>()
                ?.joinToString(", ")
            val taskDescription = taskData["description"]?.toString()?.trim()
            val description = buildString {
                if (!taskDescription.isNullOrBlank()) append(taskDescription)
                else append("AI가 생성한 Task입니다.")
                if (!skills.isNullOrBlank()) append("\n필요 Skill: ").append(skills)
                if (!dependency.isNullOrBlank()) append("\n선행 Task: ").append(dependency)
            }
            taskRepository.save(Task(title = title, description = description, project = project, assignee = assignee))
        }.map(TaskResponse::from)
    }

    fun runWorkflow(projectId: Long, request: AgentWorkflowRequest): AgentWorkflowResponse {
        val project = projectRepository.findById(projectId)
            .orElseThrow { IllegalArgumentException("Project를 찾을 수 없습니다.") }
        val members = workspaceMemberRepository.findAllByWorkspaceId(project.workspace.id!!).map { member ->
            mapOf(
                "name" to member.user.name,
                "skills" to userSkillRepository.findAllByUserId(member.user.id!!).map { it.skill.name }
            )
        }
        val payload = mapOf(
            "project_name" to project.name,
            "plan_text" to request.planText,
            "employees" to members
        )
        return post("/api/agent/workflow", objectMapper.writeValueAsString(payload), AgentWorkflowResponse::class.java)
    }

    fun classifyMessage(request: AgentMessengerIntentRequest): AgentMessengerIntentResponse =
        post("/api/agent/messenger-intent", objectMapper.writeValueAsString(request), AgentMessengerIntentResponse::class.java)

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
