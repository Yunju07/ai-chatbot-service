package com.sionic.ai.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "chats")
class Chat(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    val threadId: UUID,

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false, columnDefinition = "text")
    var question: String,

    @Column(nullable = false, columnDefinition = "text")
    var answer: String,

    @Column(nullable = false)
    var createdAt: OffsetDateTime? = null,

    @Column
    var deletedAt: OffsetDateTime? = null,
) {
    @PrePersist
    fun prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now()
        }
    }
}
