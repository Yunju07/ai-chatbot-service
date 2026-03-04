package com.sionic.ai.service

import com.sionic.ai.domain.ActivityLog
import com.sionic.ai.domain.ActivityType
import com.sionic.ai.repository.ActivityLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ActivityLogService(
    private val activityLogRepository: ActivityLogRepository,
) {
    @Transactional
    fun record(userId: UUID, type: ActivityType) {
        activityLogRepository.save(ActivityLog(userId = userId, type = type))
    }

    @Transactional(readOnly = true)
    fun count(type: ActivityType, from: OffsetDateTime, to: OffsetDateTime): Long =
        activityLogRepository.countByTypeBetween(type, from, to)
}
