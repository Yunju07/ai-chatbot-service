package com.sionic.ai.service

import com.sionic.ai.domain.Feedback
import com.sionic.ai.domain.FeedbackStatus
import com.sionic.ai.domain.Role
import com.sionic.ai.dto.FeedbackCreateRequest
import com.sionic.ai.dto.FeedbackItem
import com.sionic.ai.dto.FeedbackListResponse
import com.sionic.ai.dto.FeedbackUpdateStatusRequest
import com.sionic.ai.repository.ChatRepository
import com.sionic.ai.repository.FeedbackRepository
import com.sionic.ai.security.UserPrincipal
import com.sionic.ai.util.BadRequestException
import com.sionic.ai.util.UnauthorizedException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class FeedbackService(
    private val feedbackRepository: FeedbackRepository,
    private val chatRepository: ChatRepository,
) {
    @Transactional
    fun createFeedback(principal: UserPrincipal, request: FeedbackCreateRequest): FeedbackItem {
        val chatId = runCatching { UUID.fromString(request.chatId) }
            .getOrElse { throw BadRequestException("Invalid chatId") }
        val chat = chatRepository.findById(chatId)
            .orElseThrow { BadRequestException("Chat not found") }
        if (principal.role != Role.ADMIN && chat.userId != principal.id) {
            throw UnauthorizedException("Not allowed")
        }
        if (feedbackRepository.existsByChatIdAndUserId(chatId, principal.id)) {
            throw BadRequestException("Feedback already exists for this chat")
        }
        val feedback = Feedback(
            userId = principal.id,
            chatId = chatId,
            isPositive = request.isPositive,
            status = FeedbackStatus.PENDING,
        )
        val saved = feedbackRepository.save(feedback)
        return toItem(saved)
    }

    @Transactional(readOnly = true)
    fun listFeedbacks(
        principal: UserPrincipal,
        page: Int,
        size: Int,
        sort: String,
        isPositive: Boolean?,
    ): FeedbackListResponse {
        val sortDir = parseSortDirection(sort)
        val pageable = PageRequest.of(page, size, Sort.by(sortDir, "createdAt"))
        val userId = if (principal.role == Role.ADMIN) null else principal.id
        val pageResult = feedbackRepository.findAllFiltered(userId, isPositive, pageable)
        val items = pageResult.content.map { toItem(it) }
        return FeedbackListResponse(items, page, size, pageResult.totalElements)
    }

    @Transactional
    fun updateStatus(principal: UserPrincipal, feedbackId: UUID, request: FeedbackUpdateStatusRequest): FeedbackItem {
        if (principal.role != Role.ADMIN) {
            throw UnauthorizedException("Not allowed")
        }
        val feedback = feedbackRepository.findById(feedbackId)
            .orElseThrow { BadRequestException("Feedback not found") }
        feedback.status = request.status
        return toItem(feedback)
    }

    private fun toItem(feedback: Feedback): FeedbackItem =
        FeedbackItem(
            id = feedback.id.toString(),
            userId = feedback.userId.toString(),
            chatId = feedback.chatId.toString(),
            isPositive = feedback.isPositive,
            status = feedback.status,
            createdAt = feedback.createdAt!!.toString(),
        )

    private fun parseSortDirection(sort: String): Sort.Direction {
        return when (sort.lowercase()) {
            "asc" -> Sort.Direction.ASC
            "desc" -> Sort.Direction.DESC
            else -> throw BadRequestException("sort must be one of: asc, desc")
        }
    }
}
