package com.workly.hp657.global.controller

import com.workly.hp657.global.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class HealthController {

    @GetMapping("/health")
    fun health(): ApiResponse<Map<String, String>> = ApiResponse.success(mapOf("status" to "UP"))
}
