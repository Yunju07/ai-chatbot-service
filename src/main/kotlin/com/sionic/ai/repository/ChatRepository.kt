package com.sionic.ai.repository

import com.sionic.ai.domain.Chat
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ChatRepository : JpaRepository<Chat, UUID> {
    fun findTopByUserIdOrderByCreatedAtDesc(userId: UUID): Chat?

    fun findByThreadIdInOrderByCreatedAtAsc(threadIds: List<UUID>): List<Chat>

    fun deleteByThreadId(threadId: UUID)
}
