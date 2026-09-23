package com.workly.hp657.domain.task.controller

import com.workly.hp657.domain.task.dto.*
import com.workly.hp657.domain.task.service.TaskService
import com.workly.hp657.global.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskService: TaskService
) {

    @GetMapping
    fun getTasks(@RequestParam(required = false) projectId: Long?): ResponseEntity<ApiResponse<List<TaskResponse>>> {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTasks(projectId)))
    }

    @GetMapping("/{id}")
    fun getTask(@PathVariable id: Long): ResponseEntity<ApiResponse<TaskResponse>> {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTask(id)))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: TaskCreateRequest): ResponseEntity<ApiResponse<TaskResponse>> {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(taskService.create(request)))
    }
}
