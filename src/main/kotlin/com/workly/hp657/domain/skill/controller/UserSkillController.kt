package com.workly.hp657.domain.skill.controller

import com.workly.hp657.domain.skill.dto.UserSkillCreateRequest
import com.workly.hp657.domain.skill.dto.UserSkillResponse
import com.workly.hp657.domain.skill.service.UserSkillService
import com.workly.hp657.global.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users/{userId}/skills")
class UserSkillController(
    private val userSkillService: UserSkillService
) {

    @GetMapping
    fun getUserSkills(
        @PathVariable userId: Long
    ): ResponseEntity<ApiResponse<List<UserSkillResponse>>> {
        return ResponseEntity.ok(ApiResponse.success(userSkillService.getUserSkills(userId)))
    }

    @PostMapping
    fun addUserSkill(
        @PathVariable userId: Long,
        @RequestBody request: UserSkillCreateRequest
    ): ResponseEntity<ApiResponse<UserSkillResponse>> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(userSkillService.addUserSkill(userId, request)))
    }

    @DeleteMapping("/{userSkillId}")
    fun deleteUserSkill(
        @PathVariable userId: Long,
        @PathVariable userSkillId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        userSkillService.deleteUserSkill(userSkillId)
        return ResponseEntity.ok(ApiResponse.success("사용자 스킬이 삭제되었습니다."))
    }
}
