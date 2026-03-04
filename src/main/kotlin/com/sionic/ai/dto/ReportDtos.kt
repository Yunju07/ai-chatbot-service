package com.sionic.ai.dto

data class ActivityReportResponse(
    val from: String,
    val to: String,
    val signupCount: Long,
    val loginCount: Long,
    val chatCreatedCount: Long,
)
