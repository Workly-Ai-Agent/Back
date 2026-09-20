package com.workly.hp657.domain.workspace.controller

import com.workly.hp657.domain.workspace.dto.WorkspaceMemberAddRequest
import com.workly.hp657.domain.workspace.dto.WorkspaceMemberResponse
import com.workly.hp657.domain.workspace.dto.WorkspaceMemberUpdateRequest
import com.workly.hp657.domain.workspace.service.WorkspaceMemberService
import com.workly.hp657.global.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/members")
class WorkspaceMemberController(
    private val workspaceMemberService: WorkspaceMemberService
) {

    @PostMapping
    fun addMember(
        @PathVariable workspaceId: Long,
        @Valid @RequestBody request: WorkspaceMemberAddRequest
    ): ResponseEntity<ApiResponse<WorkspaceMemberResponse>> {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                ApiResponse.success(
                    workspaceMemberService.addMember(
                        workspaceId,
                        request
                    )
                )
            )
    }

    @GetMapping
    fun getMembers(
        @PathVariable workspaceId: Long
    ): ResponseEntity<ApiResponse<List<WorkspaceMemberResponse>>> {

        return ResponseEntity.ok(
            ApiResponse.success(
                workspaceMemberService.getMembers(workspaceId)
            )
        )
    }

    @GetMapping("/{memberId}")
    fun getMember(
        @PathVariable workspaceId: Long,
        @PathVariable memberId: Long
    ): ResponseEntity<ApiResponse<WorkspaceMemberResponse>> {

        return ResponseEntity.ok(
            ApiResponse.success(
                workspaceMemberService.getMember(
                    workspaceId,
                    memberId
                )
            )
        )
    }

    @PatchMapping("/{memberId}")
    fun updateRole(
        @PathVariable workspaceId: Long,
        @PathVariable memberId: Long,
        @Valid @RequestBody request: WorkspaceMemberUpdateRequest
    ): ResponseEntity<ApiResponse<WorkspaceMemberResponse>> {

        return ResponseEntity.ok(
            ApiResponse.success(
                workspaceMemberService.updateRole(
                    workspaceId,
                    memberId,
                    request
                )
            )
        )
    }

    @DeleteMapping("/{memberId}")
    fun removeMember(
        @PathVariable workspaceId: Long,
        @PathVariable memberId: Long
    ): ResponseEntity<ApiResponse<Unit>> {

        workspaceMemberService.removeMember(
            workspaceId,
            memberId
        )

        return ResponseEntity.ok(
            ApiResponse.success("멤버가 삭제되었습니다.")
        )
    }
}