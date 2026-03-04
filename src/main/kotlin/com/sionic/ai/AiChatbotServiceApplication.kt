package com.sionic.ai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class AiChatbotServiceApplication

fun main(args: Array<String>) {
    runApplication<AiChatbotServiceApplication>(*args)
}
