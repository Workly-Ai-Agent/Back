package com.workly.hp657.domain.skill.dto

import com.workly.hp657.domain.skill.entity.UserSkill

data class UserSkillCreateRequest(
    val skillId: Long
)

data class UserSkillResponse(
    val id: Long,
    val skillId: Long,
    val skillName: String
) {
    companion object {
        fun from(userSkill: UserSkill): UserSkillResponse {
            return UserSkillResponse(
                id = userSkill.id!!,
                skillId = userSkill.skill.id!!,
                skillName = userSkill.skill.name
            )
        }
    }
}