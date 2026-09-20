package com.workly.hp657.domain.project.controller

import com.workly.hp657.domain.project.dto.ProjectCreateRequest
import com.workly.hp657.domain.project.dto.ProjectResponse
import com.workly.hp657.domain.project.dto.ProjectUpdateRequest
import com.workly.hp657.domain.project.service.ProjectService
import com.workly.hp657.global.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class ProjectController(
    private val projectService: ProjectService
) {

    @PostMapping("/workspaces/{workspaceId}/projects")
    fun create(
        authentication: Authentication,
        @PathVariable workspaceId: Long,
        @Valid @RequestBody request: ProjectCreateRequest
    ): ResponseEntity<ApiResponse<ProjectResponse>> {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                ApiResponse.success(
                    projectService.create(
                        authentication.name,
                        workspaceId,
                        request
                    )
                )
            )
    }

    @GetMapping("/workspaces/{workspaceId}/projects")
    fun getProjects(
        authentication: Authentication,
        @PathVariable workspaceId: Long
    ): ResponseEntity<ApiResponse<List<ProjectResponse>>> {

        return ResponseEntity.ok(
            ApiResponse.success(
                projectService.getProjects(
                    authentication.name,
                    workspaceId
                )
            )
        )
    }

    @GetMapping("/projects/{projectId}")
    fun getProject(
        authentication: Authentication,
        @PathVariable projectId: Long
    ): ResponseEntity<ApiResponse<ProjectResponse>> {

        return ResponseEntity.ok(
            ApiResponse.success(
                projectService.getProject(
                    authentication.name,
                    projectId
                )
            )
        )
    }

    @PatchMapping("/projects/{projectId}")
    fun update(
        authentication: Authentication,
        @PathVariable projectId: Long,
        @Valid @RequestBody request: ProjectUpdateRequest
    ): ResponseEntity<ApiResponse<ProjectResponse>> {

        return ResponseEntity.ok(
            ApiResponse.success(
                projectService.update(
                    authentication.name,
                    projectId,
                    request
                )
            )
        )
    }

    @DeleteMapping("/projects/{projectId}")
    fun delete(
        authentication: Authentication,
        @PathVariable projectId: Long
    ): ResponseEntity<ApiResponse<Unit>> {

        projectService.delete(
            authentication.name,
            projectId
        )

        return ResponseEntity.ok(
            ApiResponse.success("프로젝트가 삭제되었습니다.")
        )
    }
}