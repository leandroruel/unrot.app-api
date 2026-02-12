package com.unrotapp.user

import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun register(email: String, password: String, displayName: String): User {
        val normalizedEmail = email.trim().lowercase()
        if (userRepository.findByEmail(normalizedEmail) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email already in use")
        }
        val encodedPassword = requireNotNull(passwordEncoder.encode(password)) {
            "Password encoder returned null"
        }
        val user = User(
            email = normalizedEmail,
            passwordHash = encodedPassword,
            displayName = displayName.trim()
        )
        return userRepository.save(user)
    }

    fun authenticate(email: String, password: String): User {
        val normalizedEmail = email.trim().lowercase()
        val user = userRepository.findByEmail(normalizedEmail)
            ?: throw BadCredentialsException("Invalid credentials")
        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw BadCredentialsException("Invalid credentials")
        }
        return user
    }
}
