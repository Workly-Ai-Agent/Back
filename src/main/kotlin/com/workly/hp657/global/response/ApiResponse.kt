package com.workly.hp657.global.response

data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String? = null,
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResponse<T> {
            return ApiResponse(data = data, message = message)
        }

        fun success(message: String): ApiResponse<Unit> {
            return ApiResponse(message = message)
        }
    }
}
