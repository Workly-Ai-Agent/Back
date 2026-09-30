package com.workly.hp657.domain.agent.controller

import com.workly.hp657.domain.agent.dto.*
import com.workly.hp657.domain.agent.service.AgentService
import com.workly.hp657.global.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.workly.hp657.domain.task.dto.TaskResponse

@RestController
@RequestMapping("/api")
class AgentController(private val agentService: AgentService) {
    @PostMapping("/projects/{projectId}/agent/generate")
    fun generateAndCreate(@PathVariable projectId: Long, @RequestBody request: AgentWorkflowRequest): ResponseEntity<ApiResponse<List<TaskResponse>>> =
        ResponseEntity.ok(ApiResponse.success(agentService.generateAndCreate(projectId, request)))

    @PostMapping("/projects/{projectId}/agent/workflow")
    fun runWorkflow(@PathVariable projectId: Long, @RequestBody request: AgentWorkflowRequest): ResponseEntity<ApiResponse<AgentWorkflowResponse>> =
        ResponseEntity.ok(ApiResponse.success(agentService.runWorkflow(projectId, request)))

    @PostMapping("/chat/agent-intent")
    fun classifyMessage(@RequestBody request: AgentMessengerIntentRequest): ResponseEntity<ApiResponse<AgentMessengerIntentResponse>> =
        ResponseEntity.ok(ApiResponse.success(agentService.classifyMessage(request)))
}
