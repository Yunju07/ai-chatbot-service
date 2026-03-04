package com.sionic.ai.controller

import com.sionic.ai.dto.ChatCreateRequest
import com.sionic.ai.dto.ThreadListResponse
import com.sionic.ai.security.UserPrincipal
import com.sionic.ai.service.ChatService
import com.sionic.ai.util.UnauthorizedException
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.UUID

@RestController
@RequestMapping("/chats")
class ChatController(
    private val chatService: ChatService,
) {
    @PostMapping
    fun create(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @Valid @RequestBody request: ChatCreateRequest,
    ): Any {
        val auth = principal ?: throw UnauthorizedException("Unauthorized")
        return if (request.isStreaming) {
            val emitter = SseEmitter(0L)
            chatService.streamChat(auth, request, emitter)
            emitter
        } else {
            chatService.createChat(auth, request)
        }
    }

    @GetMapping
    fun list(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "desc") sort: String,
    ): ThreadListResponse {
        val auth = principal ?: throw UnauthorizedException("Unauthorized")
        return chatService.listThreads(auth, page, size, sort)
    }

    @DeleteMapping("/threads/{threadId}")
    fun deleteThread(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @PathVariable threadId: UUID,
    ) {
        val auth = principal ?: throw UnauthorizedException("Unauthorized")
        chatService.deleteThread(auth, threadId)
    }
}
