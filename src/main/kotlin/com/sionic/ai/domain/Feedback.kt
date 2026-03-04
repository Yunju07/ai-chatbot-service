package com.sionic.ai.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(
    name = "feedbacks",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_feedback_user_chat", columnNames = ["user_id", "chat_id"])
    ]
)
class Feedback(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, name = "user_id")
    val userId: UUID,

    @Column(nullable = false, name = "chat_id")
    val chatId: UUID,

    @Column(nullable = false)
    val isPositive: Boolean,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: FeedbackStatus = FeedbackStatus.PENDING,

    @Column(nullable = false)
    var createdAt: OffsetDateTime? = null,
) {
    @PrePersist
    fun prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now()
        }
    }
}
