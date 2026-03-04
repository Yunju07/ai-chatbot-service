package com.sionic.ai.repository

import com.sionic.ai.domain.Chat
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ChatRepository : JpaRepository<Chat, UUID> {
    fun findTopByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId: UUID): Chat?

    fun findByThreadIdInAndDeletedAtIsNullOrderByCreatedAtAsc(threadIds: List<UUID>): List<Chat>

    fun findByThreadId(threadId: UUID): List<Chat>
}
