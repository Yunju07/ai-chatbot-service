package com.sionic.ai.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.openai")
class OpenAiProperties(
    val enabled: Boolean = false,
    val apiKey: String = "",
    val baseUrl: String = "https://api.openai.com",
)
