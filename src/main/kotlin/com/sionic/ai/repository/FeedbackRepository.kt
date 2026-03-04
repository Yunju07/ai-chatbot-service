package com.sionic.ai.repository

import com.sionic.ai.domain.Feedback
import com.sionic.ai.domain.FeedbackStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface FeedbackRepository : JpaRepository<Feedback, UUID> {
    fun existsByChatIdAndUserId(chatId: UUID, userId: UUID): Boolean

    @Query(
        """
        select f from Feedback f
        where (:userId is null or f.userId = :userId)
          and (:isPositive is null or f.isPositive = :isPositive)
        """
    )
    fun findAllFiltered(
        @Param("userId") userId: UUID?,
        @Param("isPositive") isPositive: Boolean?,
        pageable: Pageable,
    ): Page<Feedback>
}
