package com.sionic.ai.security

import com.sionic.ai.domain.Role
import java.util.UUID

data class UserPrincipal(
    val id: UUID,
    val email: String,
    val role: Role,
)
