package com.sionic.ai.service

import com.sionic.ai.config.ChatProperties
import com.sionic.ai.domain.ActivityType
import com.sionic.ai.domain.Chat
import com.sionic.ai.domain.Role
import com.sionic.ai.domain.Thread
import com.sionic.ai.dto.ChatCreateRequest
import com.sionic.ai.dto.ChatCreateResponse
import com.sionic.ai.dto.ChatItem
import com.sionic.ai.dto.ThreadItem
import com.sionic.ai.dto.ThreadListResponse
import com.sionic.ai.repository.ChatRepository
import com.sionic.ai.repository.ThreadRepository
import com.sionic.ai.security.UserPrincipal
import com.sionic.ai.util.BadRequestException
import com.sionic.ai.util.UnauthorizedException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val threadRepository: ThreadRepository,
    private val chatProperties: ChatProperties,
    private val aiClient: AiClient,
    private val activityLogService: ActivityLogService,
) {
    @Transactional
    fun createChat(principal: UserPrincipal, request: ChatCreateRequest): ChatCreateResponse {
        val thread = resolveThread(principal.id)
        val model = request.model ?: chatProperties.defaultModel
        val history = chatRepository.findByThreadIdInAndDeletedAtIsNullOrderByCreatedAtAsc(listOf(thread.id))
            .map { it.question to it.answer }

        val answer = aiClient.generateAnswer(model, request.question, history)
        val chat = Chat(
            threadId = thread.id,
            userId = principal.id,
            question = request.question,
            answer = answer,
        )
        val saved = chatRepository.save(chat)
        activityLogService.record(principal.id, ActivityType.CHAT_CREATED)
        return ChatCreateResponse(
            threadId = saved.threadId.toString(),
            chatId = saved.id.toString(),
            question = saved.question,
            answer = saved.answer,
            createdAt = saved.createdAt!!.toString(),
        )
    }

    fun streamChat(principal: UserPrincipal, request: ChatCreateRequest, emitter: SseEmitter) {
        val thread = resolveThread(principal.id)
        val model = request.model ?: chatProperties.defaultModel
        val history = chatRepository.findByThreadIdInAndDeletedAtIsNullOrderByCreatedAtAsc(listOf(thread.id))
            .map { it.question to it.answer }

        Thread {
            try {
                val answerBuilder = StringBuilder()
                aiClient.generateAnswerStream(model, request.question, history).forEach { chunk ->
                    answerBuilder.append(chunk)
                    emitter.send(SseEmitter.event().name("chunk").data(chunk))
                }
                val chat = Chat(
                    threadId = thread.id,
                    userId = principal.id,
                    question = request.question,
                    answer = answerBuilder.toString(),
                )
                val saved = chatRepository.save(chat)
                activityLogService.record(principal.id, ActivityType.CHAT_CREATED)
                emitter.send(
                    SseEmitter.event().name("done").data(
                        ChatCreateResponse(
                            threadId = saved.threadId.toString(),
                            chatId = saved.id.toString(),
                            question = saved.question,
                            answer = saved.answer,
                            createdAt = saved.createdAt!!.toString(),
                        )
                    )
                )
                emitter.complete()
            } catch (ex: Exception) {
                emitter.completeWithError(ex)
            }
        }.start()
    }

    @Transactional(readOnly = true)
    fun listThreads(
        principal: UserPrincipal,
        page: Int,
        size: Int,
        sort: String,
    ): ThreadListResponse {
        val sortDir = parseSortDirection(sort)
        val pageable = PageRequest.of(page, size, Sort.by(sortDir, "createdAt"))
        val threadsPage = if (principal.role == Role.ADMIN) {
            threadRepository.findByDeletedAtIsNull(pageable)
        } else {
            threadRepository.findByUserIdAndDeletedAtIsNull(principal.id, pageable)
        }

        val threadIds = threadsPage.content.map { it.id }
        val chats = if (threadIds.isEmpty()) emptyList() else chatRepository.findByThreadIdInAndDeletedAtIsNullOrderByCreatedAtAsc(threadIds)
        val chatsByThread = chats.groupBy { it.threadId }

        val threadItems = threadsPage.content.map { thread ->
            val threadChats = chatsByThread[thread.id].orEmpty()
            ThreadItem(
                id = thread.id.toString(),
                createdAt = thread.createdAt!!.toString(),
                chats = threadChats.map {
                    ChatItem(
                        id = it.id.toString(),
                        question = it.question,
                        answer = it.answer,
                        createdAt = it.createdAt!!.toString(),
                    )
                },
            )
        }

        return ThreadListResponse(
            items = threadItems,
            page = page,
            size = size,
            totalItems = threadsPage.totalElements,
        )
    }

    @Transactional
    fun deleteThread(principal: UserPrincipal, threadId: UUID) {
        val thread = threadRepository.findById(threadId)
            .orElseThrow { BadRequestException("Thread not found") }
        if (principal.role != Role.ADMIN && thread.userId != principal.id) {
            throw UnauthorizedException("Not allowed")
        }
        val now = OffsetDateTime.now()
        thread.deletedAt = now
        val chats = chatRepository.findByThreadId(threadId)
        chats.forEach { it.deletedAt = now }
    }

    private fun resolveThread(userId: UUID): Thread {
        val latestThread = threadRepository.findTopByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
        if (latestThread == null) {
            return threadRepository.save(Thread(userId = userId))
        }
        val lastChat = chatRepository.findTopByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
        if (lastChat == null) {
            return threadRepository.save(Thread(userId = userId))
        }
        val cutoff = OffsetDateTime.now().minusMinutes(chatProperties.threadExpireMinutes)
        return if (lastChat.createdAt!!.isBefore(cutoff)) {
            threadRepository.save(Thread(userId = userId))
        } else {
            latestThread
        }
    }

    private fun parseSortDirection(sort: String): Sort.Direction {
        return when (sort.lowercase()) {
            "asc" -> Sort.Direction.ASC
            "desc" -> Sort.Direction.DESC
            else -> throw BadRequestException("sort must be one of: asc, desc")
        }
    }
}
