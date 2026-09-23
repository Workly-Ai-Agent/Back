package com.workly.hp657.domain.skill.service

import com.workly.hp657.domain.skill.dto.SkillCreateRequest
import com.workly.hp657.domain.skill.dto.SkillResponse
import com.workly.hp657.domain.skill.entity.Skill
import com.workly.hp657.domain.skill.repository.SkillRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SkillService(
    private val skillRepository: SkillRepository
) {

    fun getSkills(): List<SkillResponse> {
        return skillRepository.findAll()
            .map(SkillResponse::from)
    }

    fun getSkill(skillId: Long): SkillResponse {
        val skill = skillRepository.findById(skillId)
            .orElseThrow {
                IllegalArgumentException("Skill을 찾을 수 없습니다.")
            }

        return SkillResponse.from(skill)
    }

    @Transactional
    fun createSkill(request: SkillCreateRequest): SkillResponse {

        if (skillRepository.existsByName(request.name)) {
            throw IllegalArgumentException("이미 존재하는 Skill입니다.")
        }

        val skill = Skill(
            name = request.name,
            description = request.description
        )

        return SkillResponse.from(
            skillRepository.save(skill)
        )
    }
}