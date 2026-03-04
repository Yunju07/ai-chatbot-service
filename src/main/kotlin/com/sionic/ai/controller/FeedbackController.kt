package com.sionic.ai.controller

import com.sionic.ai.dto.FeedbackCreateRequest
import com.sionic.ai.dto.FeedbackItem
import com.sionic.ai.dto.FeedbackListResponse
import com.sionic.ai.dto.FeedbackUpdateStatusRequest
import com.sionic.ai.security.UserPrincipal
import com.sionic.ai.service.FeedbackService
import com.sionic.ai.util.UnauthorizedException
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/feedbacks")
class FeedbackController(
    private val feedbackService: FeedbackService,
) {
    @PostMapping
    fun create(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @Valid @RequestBody request: FeedbackCreateRequest,
    ): FeedbackItem {
        val auth = principal ?: throw UnauthorizedException("Unauthorized")
        return feedbackService.createFeedback(auth, request)
    }

    @GetMapping
    fun list(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "desc") sort: String,
        @RequestParam(required = false) isPositive: Boolean?,
    ): FeedbackListResponse {
        val auth = principal ?: throw UnauthorizedException("Unauthorized")
        return feedbackService.listFeedbacks(auth, page, size, sort, isPositive)
    }

    @PutMapping("/{feedbackId}/status")
    fun updateStatus(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @PathVariable feedbackId: UUID,
        @Valid @RequestBody request: FeedbackUpdateStatusRequest,
    ): FeedbackItem {
        val auth = principal ?: throw UnauthorizedException("Unauthorized")
        return feedbackService.updateStatus(auth, feedbackId, request)
    }
}
