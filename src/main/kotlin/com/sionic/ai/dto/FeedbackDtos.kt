package com.sionic.ai.dto

import com.sionic.ai.domain.FeedbackStatus
import jakarta.validation.constraints.NotNull


data class FeedbackCreateRequest(
    @field:NotNull
    val chatId: String,

    @field:NotNull
    val isPositive: Boolean,
)

data class FeedbackUpdateStatusRequest(
    @field:NotNull
    val status: FeedbackStatus,
)

data class FeedbackItem(
    val id: String,
    val userId: String,
    val chatId: String,
    val isPositive: Boolean,
    val status: FeedbackStatus,
    val createdAt: String,
)

data class FeedbackListResponse(
    val items: List<FeedbackItem>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
)
