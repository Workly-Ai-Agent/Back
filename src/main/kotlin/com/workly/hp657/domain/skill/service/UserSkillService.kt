package com.workly.hp657.domain.skill.service

import com.workly.hp657.domain.skill.dto.UserSkillCreateRequest
import com.workly.hp657.domain.skill.dto.UserSkillResponse
import com.workly.hp657.domain.skill.entity.UserSkill
import com.workly.hp657.domain.skill.repository.SkillRepository
import com.workly.hp657.domain.skill.repository.UserSkillRepository
import com.workly.hp657.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserSkillService(
    private val userRepository: UserRepository,
    private val skillRepository: SkillRepository,
    private val userSkillRepository: UserSkillRepository
) {

    fun getUserSkills(userId: Long): List<UserSkillResponse> {
        return userSkillRepository.findAllByUserId(userId)
            .map(UserSkillResponse::from)
    }

    @Transactional
    fun addUserSkill(
        userId: Long,
        request: UserSkillCreateRequest
    ): UserSkillResponse {

        val user = userRepository.findById(userId)
            .orElseThrow {
                IllegalArgumentException("사용자를 찾을 수 없습니다.")
            }

        val skill = skillRepository.findById(request.skillId)
            .orElseThrow {
                IllegalArgumentException("Skill을 찾을 수 없습니다.")
            }

        if (userSkillRepository.existsByUserIdAndSkillId(
                userId,
                request.skillId
            )
        ) {
            throw IllegalArgumentException("이미 등록된 Skill입니다.")
        }

        val userSkill = UserSkill(
            user = user,
            skill = skill,
        )

        return UserSkillResponse.from(
            userSkillRepository.save(userSkill)
        )
    }

    @Transactional
    fun deleteUserSkill(userSkillId: Long) {
        if (!userSkillRepository.existsById(userSkillId)) {
            throw IllegalArgumentException("UserSkill을 찾을 수 없습니다.")
        }

        userSkillRepository.deleteById(userSkillId)
    }
}