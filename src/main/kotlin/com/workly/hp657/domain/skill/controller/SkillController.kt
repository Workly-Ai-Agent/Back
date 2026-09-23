package com.workly.hp657.domain.skill.controller

import com.workly.hp657.domain.skill.dto.SkillCreateRequest
import com.workly.hp657.domain.skill.dto.SkillResponse
import com.workly.hp657.domain.skill.service.SkillService
import com.workly.hp657.global.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/skills")
class SkillController(
    private val skillService: SkillService
) {

    @GetMapping
    fun getSkills(): ResponseEntity<ApiResponse<List<SkillResponse>>> {
        return ResponseEntity.ok(ApiResponse.success(skillService.getSkills()))
    }

    @GetMapping("/{skillId}")
    fun getSkill(
        @PathVariable skillId: Long
    ): ResponseEntity<ApiResponse<SkillResponse>> {
        return ResponseEntity.ok(ApiResponse.success(skillService.getSkill(skillId)))
    }

    @PostMapping
    fun createSkill(
        @RequestBody request: SkillCreateRequest
    ): ResponseEntity<ApiResponse<SkillResponse>> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(skillService.createSkill(request)))
    }
}
