package com.sionic.ai.repository

import com.sionic.ai.domain.Thread
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface ThreadRepository : JpaRepository<Thread, UUID> {
    fun findTopByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId: UUID): Thread?

    fun findByUserIdAndDeletedAtIsNull(userId: UUID, pageable: Pageable): Page<Thread>

    fun findByDeletedAtIsNull(pageable: Pageable): Page<Thread>
}
