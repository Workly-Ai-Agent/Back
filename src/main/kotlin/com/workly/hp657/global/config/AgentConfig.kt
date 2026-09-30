package com.workly.hp657.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

@Configuration
class AgentConfig {
    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    @Bean
    fun agentRestClient(@Value("\${agent.base-url:http://localhost:8000}") baseUrl: String): RestClient =
        RestClient.builder().baseUrl(baseUrl).build()
}
