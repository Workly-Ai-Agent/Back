package com.workly.hp657.domain.agent.entity

import com.workly.hp657.domain.project.entity.Project
import com.workly.hp657.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "agent_proposals")
class AgentProposal(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "project_id", nullable = false) val project: Project,
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by_id", nullable = false) val createdBy: User,
    @Column(nullable = false, length = 30) @Enumerated(EnumType.STRING) var status: ProposalStatus = ProposalStatus.PENDING,
    @Column(nullable = false, columnDefinition = "text") val resultJson: String,
    @Column(nullable = false, columnDefinition = "text") val requestText: String,
    // Nullable for rows created before proposal modes were introduced.
    @Column(name = "proposal_mode", nullable = true, length = 30) @Enumerated(EnumType.STRING) var mode: ProposalMode? = null,
    var requirementsStructuredCorrectly: Boolean? = null,
    var skillMatchingCorrect: Boolean? = null,
    var impactDetectionCorrect: Boolean? = null,
    var replanningSuccessful: Boolean? = null,
    @Column(nullable = false) val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false) var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ProposalStatus { PENDING, APPROVED, REJECTED }
enum class ProposalMode { REPLAN, ADD_TASKS, CHAT_UPDATE }
