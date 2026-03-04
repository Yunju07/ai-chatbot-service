package com.sionic.ai.repository

import com.sionic.ai.domain.ActivityLog
import com.sionic.ai.domain.ActivityType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.OffsetDateTime
import java.util.UUID

interface ActivityLogRepository : JpaRepository<ActivityLog, UUID> {
    @Query(
        """
        select count(a) from ActivityLog a
        where a.type = :type and a.createdAt >= :from and a.createdAt < :to
        """
    )
    fun countByTypeBetween(
        @Param("type") type: ActivityType,
        @Param("from") from: OffsetDateTime,
        @Param("to") to: OffsetDateTime,
    ): Long
}
