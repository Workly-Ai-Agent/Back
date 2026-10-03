package com.workly.hp657.domain.skill.service

import com.workly.hp657.domain.skill.dto.UserSkillCreateRequest
import com.workly.hp657.domain.skill.dto.UserSkillResponse
import com.workly.hp657.domain.skill.entity.UserSkill
import com.workly.hp657.domain.skill.repository.SkillRepository
import com.workly.hp657.domain.skill.repository.UserSkillRepository
import com.workly.hp657.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.access.AccessDeniedException

@Service
@Transactional(readOnly = true)
class UserSkillService(
    private val userRepository: UserRepository,
    private val skillRepository: SkillRepository,
    private val userSkillRepository: UserSkillRepository
) {

    fun getUserSkills(email: String, userId: Long): List<UserSkillResponse> {
        requireOwner(email, userId)
        return userSkillRepository.findAllByUserId(userId)
            .map(UserSkillResponse::from)
    }

    @Transactional
    fun addUserSkill(
        email: String,
        userId: Long,
        request: UserSkillCreateRequest
    ): UserSkillResponse {

        requireOwner(email, userId)
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
    fun deleteUserSkill(email: String, userId: Long, userSkillId: Long) {
        requireOwner(email, userId)
        val userSkill = userSkillRepository.findById(userSkillId)
            .orElseThrow { IllegalArgumentException("UserSkill을 찾을 수 없습니다.") }
        if (userSkill.user.id != userId) throw AccessDeniedException("자신의 Skill만 삭제할 수 있습니다.")
        userSkillRepository.delete(userSkill)
    }

    private fun requireOwner(email: String, userId: Long) {
        val current = userRepository.findByEmail(email).orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다.") }
        if (current.id != userId) throw AccessDeniedException("자신의 Skill Profile만 관리할 수 있습니다.")
    }
}
