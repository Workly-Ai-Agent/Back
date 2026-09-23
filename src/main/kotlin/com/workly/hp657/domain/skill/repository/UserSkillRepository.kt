package com.workly.hp657.domain.skill.repository

import com.workly.hp657.domain.skill.entity.UserSkill
import org.springframework.data.jpa.repository.JpaRepository

interface UserSkillRepository : JpaRepository<UserSkill, Long> {
    fun findAllByUserId(userId: Long): List<UserSkill>
    fun existsByUserIdAndSkillId(
        userId: Long,
        skillId: Long
    ): Boolean
}