package com.workly.hp657.domain.skill.repository

import com.workly.hp657.domain.skill.entity.Skill
import org.springframework.data.jpa.repository.JpaRepository

interface SkillRepository : JpaRepository<Skill, Long> {
    fun existsByName(name: String): Boolean
}