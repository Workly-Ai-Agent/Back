package com.workly.hp657.domain.agent.dto

import com.fasterxml.jackson.annotation.JsonAlias

data class AgentWorkflowRequest(val planText: String)

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
data class AgentMessengerIntentResponse(
    val intent: String,
    val taskReference: String? = null,
    val requestedChange: String? = null,
    val requiresReplanning: Boolean = false,
    val confidence: Double = 0.0
)
