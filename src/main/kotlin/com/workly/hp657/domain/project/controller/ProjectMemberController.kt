package com.workly.hp657.domain.project.controller

import com.workly.hp657.domain.project.dto.AddProjectMemberRequest
import com.workly.hp657.domain.project.dto.ProjectMemberResponse
import com.workly.hp657.domain.project.dto.UpdateProjectMemberRequest
import com.workly.hp657.domain.project.service.ProjectMemberService
import com.workly.hp657.global.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/projects/{projectId}/members")
class ProjectMemberController(
    private val projectMemberService: ProjectMemberService
) {

    @PostMapping
    fun addMember(
        authentication: Authentication,
        @PathVariable projectId: Long,
        @Valid @RequestBody request: AddProjectMemberRequest
    ): ResponseEntity<ApiResponse<ProjectMemberResponse>> {

        val response = projectMemberService.addMember(
            authentication.name,
            projectId,
            request
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    @GetMapping
    fun getMembers(
        authentication: Authentication,
        @PathVariable projectId: Long
    ): ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> {

        return ResponseEntity.ok(
            ApiResponse.success(
                projectMemberService.getMembers(
                    authentication.name,
                    projectId
                )
            )
        )
    }

    @PatchMapping("/{userId}")
    fun updateMember(
        authentication: Authentication,
        @PathVariable projectId: Long,
        @PathVariable userId: Long,
        @Valid @RequestBody request: UpdateProjectMemberRequest
    ): ResponseEntity<ApiResponse<ProjectMemberResponse>> {

        return ResponseEntity.ok(
            ApiResponse.success(
                projectMemberService.updateMember(
                    authentication.name,
                    projectId,
                    userId,
                    request
                )
            )
        )
    }

    @DeleteMapping("/{userId}")
    fun removeMember(
        authentication: Authentication,
        @PathVariable projectId: Long,
        @PathVariable userId: Long
    ): ResponseEntity<ApiResponse<Unit>> {

        projectMemberService.removeMember(
            authentication.name,
            projectId,
            userId
        )

        return ResponseEntity.ok(
            ApiResponse.success("프로젝트 멤버가 삭제되었습니다.")
        )
    }
}