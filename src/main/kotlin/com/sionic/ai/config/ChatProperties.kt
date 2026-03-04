package com.sionic.ai.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.chat")
class ChatProperties(
    val threadExpireMinutes: Long = 30,
    val defaultModel: String = "gpt-4o-mini",
)
