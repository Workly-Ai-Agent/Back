package com.workly.hp657.domain.skill.dto

import com.workly.hp657.domain.skill.entity.Skill

data class SkillCreateRequest(
    val name: String,
    val description: String? = null
)

data class SkillResponse(
    val id: Long,
    val name: String,
    val description: String?
) {
    companion object {
        fun from(skill: Skill): SkillResponse {
            return SkillResponse(
                id = skill.id!!,
                name = skill.name,
                description = skill.description
            )
        }
    }
}