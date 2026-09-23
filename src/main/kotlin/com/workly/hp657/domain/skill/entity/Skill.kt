package com.workly.hp657.domain.skill.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "skills",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_skill_name",
            columnNames = ["name"]
        )
    ]
)
class Skill(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(length = 500)
    var description: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)