package com.unrotapp.auth

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.unrotapp.user.UserService

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService,
    private val authTokenService: AuthTokenService
) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): AuthResponse {
        val user = userService.register(request.email, request.password, request.displayName)
        return authTokenService.generateToken(user)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): AuthResponse {
        val user = userService.authenticate(request.email, request.password)
        return authTokenService.generateToken(user)
    }
}

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresInSeconds: Long
)
