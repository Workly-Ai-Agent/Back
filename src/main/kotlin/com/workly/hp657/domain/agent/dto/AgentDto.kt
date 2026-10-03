package com.workly.hp657.domain.agent.dto

import com.fasterxml.jackson.annotation.JsonAlias
import com.workly.hp657.domain.agent.entity.ProposalMode

data class AgentWorkflowRequest(
    val planText: String,
    val mode: ProposalMode = ProposalMode.REPLAN
)

data class AgentWorkflowResponse(
    val status: String,
    @JsonAlias("project_name")
    val projectName: String,
    val tasks: List<Map<String, Any?>> = emptyList(),
    val assignments: List<Map<String, Any?>> = emptyList(),
    val violations: List<String> = emptyList(),
    val monitoring: List<String> = emptyList(),
    val approved: Boolean = false,
    val log: List<String> = emptyList()
)

data class AgentMessengerIntentRequest(val message: String)
data class AgentSkillExtractionRequest(val profileText: String)
data class AgentExtractedSkill(val name: String, val evidence: String = "")
data class AgentSkillExtractionResponse(val skills: List<AgentExtractedSkill> = emptyList())
data class AgentMessengerIntentResponse(
    val intent: String,
    @JsonAlias("task_reference")
    val taskReference: String? = null,
    @JsonAlias("requested_change")
    val requestedChange: String? = null,
    @JsonAlias("requires_replanning")
    val requiresReplanning: Boolean = false,
    val confidence: Double = 0.0
)

data class AgentProposalResponse(
    val id: Long,
    val projectId: Long,
    val status: String,
    val requestText: String,
    val mode: String = "REPLAN",
    val result: AgentWorkflowResponse,
    val createdAt: java.time.LocalDateTime,
    val requirementsStructuredCorrectly: Boolean?,
    val skillMatchingCorrect: Boolean?,
    val impactDetectionCorrect: Boolean?,
    val replanningSuccessful: Boolean?
)

data class AgentProposalEvaluationRequest(
    val requirementsStructuredCorrectly: Boolean,
    val skillMatchingCorrect: Boolean,
    val impactDetectionCorrect: Boolean,
    val replanningSuccessful: Boolean
)

data class MetricRate(val successful: Long, val evaluated: Long, val ratePercent: Double?)
data class ProjectAgentMetricsResponse(
    val requirementStructuring: MetricRate,
    val taskCreation: MetricRate,
    val skillMatching: MetricRate,
    val changeImpactDetection: MetricRate,
    val replanning: MetricRate
)
