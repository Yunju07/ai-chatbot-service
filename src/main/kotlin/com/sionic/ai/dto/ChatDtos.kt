package com.sionic.ai.dto

import jakarta.validation.constraints.NotBlank


data class ChatCreateRequest(
    @field:NotBlank
    val question: String,
    val isStreaming: Boolean = false,
    val model: String? = null,
)

data class ChatCreateResponse(
    val threadId: String,
    val chatId: String,
    val question: String,
    val answer: String,
    val createdAt: String,
)

data class ChatItem(
    val id: String,
    val question: String,
    val answer: String,
    val createdAt: String,
)

data class ThreadItem(
    val id: String,
    val createdAt: String,
    val chats: List<ChatItem>,
)

data class ThreadListResponse(
    val items: List<ThreadItem>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
)
