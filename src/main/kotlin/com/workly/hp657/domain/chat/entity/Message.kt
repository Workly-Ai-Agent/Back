package com.workly.hp657.domain.chat.entity
import com.workly.hp657.domain.user.entity.User
import com.workly.hp657.domain.workspace.entity.Workspace
import com.workly.hp657.domain.project.entity.Project
import jakarta.persistence.*
import java.time.LocalDateTime
@Entity
@Table(name = "messages")
class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    val workspace: Workspace,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id")
    val sender: User,
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "receiver_id", nullable = true)
    val receiver: User? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    val project: Project? = null,
    @Column(nullable = false, length = 4000)
    val content: String,
    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
