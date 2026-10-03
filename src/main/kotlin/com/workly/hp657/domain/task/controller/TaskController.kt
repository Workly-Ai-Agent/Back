package com.workly.hp657.domain.task.controller

import com.workly.hp657.domain.task.dto.*
import com.workly.hp657.domain.task.service.TaskService
import com.workly.hp657.global.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import com.workly.hp657.domain.task.entity.TaskStatus
import org.springframework.security.core.Authentication

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService
) {

    @GetMapping
    fun getTasks(authentication: Authentication, @RequestParam(required = false) projectId: Long?): ResponseEntity<ApiResponse<List<TaskResponse>>> {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTasks(authentication.name, projectId)))
    }

    @GetMapping("/monitor")
    fun getMonitor(authentication: Authentication, @RequestParam projectId: Long): ResponseEntity<ApiResponse<com.workly.hp657.domain.task.dto.TaskMonitorResponse>> =
        ResponseEntity.ok(ApiResponse.success(taskService.getMonitor(authentication.name, projectId)))

    @GetMapping("/workspace-summary")
    fun getWorkspaceSummary(authentication: Authentication, @RequestParam workspaceId: Long): ResponseEntity<ApiResponse<com.workly.hp657.domain.task.dto.WorkspaceTaskSummary>> =
        ResponseEntity.ok(ApiResponse.success(taskService.getWorkspaceSummary(authentication.name, workspaceId)))

    @GetMapping("/{id}")
    fun getTask(authentication: Authentication, @PathVariable id: Long): ResponseEntity<ApiResponse<TaskResponse>> {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTask(authentication.name, id)))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(authentication: Authentication, @RequestBody request: TaskCreateRequest): ResponseEntity<ApiResponse<TaskResponse>> {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(taskService.create(authentication.name, request)))
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(
        authentication: Authentication,
        @PathVariable id: Long,
        @RequestParam status: TaskStatus
    ): ResponseEntity<ApiResponse<TaskResponse>> {
        return ResponseEntity.ok(ApiResponse.success(taskService.updateStatus(authentication.name, id, status)))
    }

    @PatchMapping("/{id}")
    fun update(authentication: Authentication, @PathVariable id: Long, @RequestBody request: TaskUpdateRequest): ResponseEntity<ApiResponse<TaskResponse>> =
        ResponseEntity.ok(ApiResponse.success(taskService.update(authentication.name, id, request)))
}
