package com.sionic.ai.config

import com.sionic.ai.domain.Role
import com.sionic.ai.domain.User
import com.sionic.ai.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class AdminBootstrapInitializer(
    private val props: AdminBootstrapProperties,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        if (!props.enabled) {
            return
        }
        if (props.email.isBlank() || props.password.isBlank()) {
            throw IllegalStateException("app.bootstrap-admin.email and password are required when enabled=true")
        }

        val existing = userRepository.findByEmail(props.email)
        if (existing.isPresent) {
            val user = existing.get()
            if (user.role != Role.ADMIN) {
                user.role = Role.ADMIN
                userRepository.save(user)
            }
            return
        }

        userRepository.save(
            User(
                email = props.email,
                passwordHash = passwordEncoder.encode(props.password),
                name = props.name,
                role = Role.ADMIN,
            )
        )
    }
}
