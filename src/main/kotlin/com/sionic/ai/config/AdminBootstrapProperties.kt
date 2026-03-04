package com.sionic.ai.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.bootstrap-admin")
class AdminBootstrapProperties(
    val enabled: Boolean = false,
    val email: String = "",
    val password: String = "",
    val name: String = "Administrator",
)
