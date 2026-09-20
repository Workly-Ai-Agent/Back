package com.workly.hp657.domain.workspace.controller

import com.workly.hp657.domain.workspace.dto.WorkspaceCreateRequest
import com.workly.hp657.domain.workspace.dto.WorkspaceResponse
import com.workly.hp657.domain.workspace.dto.WorkspaceUpdateRequest
import com.workly.hp657.domain.workspace.service.WorkspaceService
import com.workly.hp657.global.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/workspaces")
class WorkspaceController(
    private val workspaceService: WorkspaceService
) {

    @PostMapping
    fun create(
        authentication: Authentication,
        @Valid @RequestBody request: WorkspaceCreateRequest
    ): ResponseEntity<ApiResponse<WorkspaceResponse>> {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                ApiResponse.success(
                    workspaceService.create(
                        authentication.name,
                        request
                    )
                )
            )
    }

    @GetMapping
    fun getAll(): ResponseEntity<ApiResponse<List<WorkspaceResponse>>> {

        return ResponseEntity.ok(
            ApiResponse.success(
                workspaceService.getAll()
            )
        )
    }

    @GetMapping("/{workspaceId}")
    fun get(
        @PathVariable workspaceId: Long
    ): ResponseEntity<ApiResponse<WorkspaceResponse>> {

        return ResponseEntity.ok(
            ApiResponse.success(
                workspaceService.get(workspaceId)
            )
        )
    }

    @GetMapping("/admin/{adminId}")
    fun getByAdmin(
        @PathVariable adminId: Long
    ): ResponseEntity<ApiResponse<List<WorkspaceResponse>>> {

        return ResponseEntity.ok(
            ApiResponse.success(
                workspaceService.getByAdmin(adminId)
            )
        )
    }

    @PatchMapping("/{workspaceId}")
    fun update(
        @PathVariable workspaceId: Long,
        @Valid @RequestBody request: WorkspaceUpdateRequest
    ): ResponseEntity<ApiResponse<WorkspaceResponse>> {

        return ResponseEntity.ok(
            ApiResponse.success(
                workspaceService.update(
                    workspaceId,
                    request
                )
            )
        )
    }

    @DeleteMapping("/{workspaceId}")
    fun delete(
        @PathVariable workspaceId: Long
    ): ResponseEntity<ApiResponse<Unit>> {

        workspaceService.delete(workspaceId)

        return ResponseEntity.ok(
            ApiResponse.success("워크스페이스가 삭제되었습니다.")
        )
    }
}