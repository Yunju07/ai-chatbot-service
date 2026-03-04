package com.sionic.ai.service

import com.sionic.ai.domain.ActivityType
import com.sionic.ai.domain.Role
import com.sionic.ai.dto.ActivityReportResponse
import com.sionic.ai.repository.ChatRepository
import com.sionic.ai.repository.ThreadRepository
import com.sionic.ai.repository.UserRepository
import com.sionic.ai.security.UserPrincipal
import com.sionic.ai.util.UnauthorizedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime

@Service
class ReportService(
    private val activityLogService: ActivityLogService,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun activityReport(principal: UserPrincipal): ActivityReportResponse {
        if (principal.role != Role.ADMIN) {
            throw UnauthorizedException("Not allowed")
        }
        val to = OffsetDateTime.now()
        val from = to.minusDays(1)
        return ActivityReportResponse(
            from = from.toString(),
            to = to.toString(),
            signupCount = activityLogService.count(ActivityType.SIGNUP, from, to),
            loginCount = activityLogService.count(ActivityType.LOGIN, from, to),
            chatCreatedCount = activityLogService.count(ActivityType.CHAT_CREATED, from, to),
        )
    }

    @Transactional(readOnly = true)
    fun dailyChatReportCsv(principal: UserPrincipal): ByteArray {
        if (principal.role != Role.ADMIN) {
            throw UnauthorizedException("Not allowed")
        }
        val to = OffsetDateTime.now()
        val from = to.minusDays(1)

        val chats = chatRepository.findAll().filter { chat ->
            val createdAt = chat.createdAt
            createdAt != null && !createdAt.isBefore(from) && createdAt.isBefore(to)
        }

        val userMap = userRepository.findAll().associateBy { it.id }
        val out = ByteArrayOutputStream()
        out.write("chat_id,user_id,user_email,question,answer,created_at\n".toByteArray(StandardCharsets.UTF_8))
        chats.forEach { chat ->
            val user = userMap[chat.userId]
            val row = listOf(
                chat.id.toString(),
                chat.userId.toString(),
                user?.email ?: "",
                csvEscape(chat.question),
                csvEscape(chat.answer),
                chat.createdAt.toString(),
            ).joinToString(",")
            out.write(row.toByteArray(StandardCharsets.UTF_8))
            out.write("\n".toByteArray(StandardCharsets.UTF_8))
        }
        return out.toByteArray()
    }

    private fun csvEscape(value: String?): String {
        val v = value ?: ""
        val needsQuote = v.contains(',') || v.contains('"') || v.contains('\n') || v.contains('\r')
        val escaped = v.replace("\"", "\"\"")
        return if (needsQuote) "\"$escaped\"" else escaped
    }
}
