package com.workly.hp657.domain.agent.repository

import com.workly.hp657.domain.agent.entity.AgentProposal
import org.springframework.data.jpa.repository.JpaRepository

interface AgentProposalRepository : JpaRepository<AgentProposal, Long> {
    fun findAllByProjectIdOrderByCreatedAtDesc(projectId: Long): List<AgentProposal>
}
