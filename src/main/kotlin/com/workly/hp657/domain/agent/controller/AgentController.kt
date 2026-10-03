package com.workly.hp657.domain.agent.controller

import com.workly.hp657.domain.agent.dto.*
import com.workly.hp657.domain.agent.service.AgentService
import com.workly.hp657.global.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.workly.hp657.domain.task.dto.TaskResponse
import org.springframework.security.core.Authentication

@RestController
@RequestMapping("/api")
class AgentController(private val agentService: AgentService) {
    @PostMapping("/projects/{projectId}/agent/generate")
    fun generateAndCreate(authentication: Authentication, @PathVariable projectId: Long, @RequestBody request: AgentWorkflowRequest): ResponseEntity<ApiResponse<AgentProposalResponse>> =
        ResponseEntity.ok(ApiResponse.success(agentService.createProposal(authentication.name, projectId, request)))

    @GetMapping("/projects/{projectId}/agent/proposals")
    fun getProposals(authentication: Authentication, @PathVariable projectId: Long): ResponseEntity<ApiResponse<List<AgentProposalResponse>>> =
        ResponseEntity.ok(ApiResponse.success(agentService.getProposals(authentication.name, projectId)))

    @PostMapping("/agent/proposals/{proposalId}/approve")
    fun approveProposal(authentication: Authentication, @PathVariable proposalId: Long): ResponseEntity<ApiResponse<List<TaskResponse>>> =
        ResponseEntity.ok(ApiResponse.success(agentService.approveProposal(authentication.name, proposalId)))

    @PostMapping("/agent/proposals/{proposalId}/reject")
    fun rejectProposal(authentication: Authentication, @PathVariable proposalId: Long): ResponseEntity<ApiResponse<Unit>> {
        agentService.rejectProposal(authentication.name, proposalId)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }

    @PostMapping("/agent/proposals/{proposalId}/evaluation")
    fun evaluateProposal(authentication: Authentication, @PathVariable proposalId: Long, @RequestBody request: AgentProposalEvaluationRequest): ResponseEntity<ApiResponse<Unit>> {
        agentService.evaluateProposal(authentication.name, proposalId, request)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }

    @GetMapping("/projects/{projectId}/agent/metrics")
    fun getMetrics(authentication: Authentication, @PathVariable projectId: Long): ResponseEntity<ApiResponse<ProjectAgentMetricsResponse>> =
        ResponseEntity.ok(ApiResponse.success(agentService.getMetrics(authentication.name, projectId)))

    @PostMapping("/projects/{projectId}/agent/workflow")
    fun runWorkflow(authentication: Authentication, @PathVariable projectId: Long, @RequestBody request: AgentWorkflowRequest): ResponseEntity<ApiResponse<AgentWorkflowResponse>> =
        ResponseEntity.ok(ApiResponse.success(agentService.runWorkflow(authentication.name, projectId, request)))

    @PostMapping("/chat/agent-intent")
    fun classifyMessage(@RequestBody request: AgentMessengerIntentRequest): ResponseEntity<ApiResponse<AgentMessengerIntentResponse>> =
        ResponseEntity.ok(ApiResponse.success(agentService.classifyMessage(request)))

    @PostMapping("/agent/extract-skills")
    fun extractSkills(@RequestBody request: AgentSkillExtractionRequest): ResponseEntity<ApiResponse<AgentSkillExtractionResponse>> =
        ResponseEntity.ok(ApiResponse.success(agentService.extractSkills(request)))
}
