package com.unrotapp.auth

import java.time.Instant
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import com.unrotapp.security.JwtProperties
import com.unrotapp.user.User

@Service
class AuthTokenService(
    private val jwtEncoder: JwtEncoder,
    private val jwtProperties: JwtProperties
) {

    fun generateToken(user: User): AuthResponse {
        val userId = requireNotNull(user.id) { "User id is null" }
        val now = Instant.now()
        val expiresAt = now.plus(jwtProperties.ttl)
        val claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer)
            .issuedAt(now)
            .expiresAt(expiresAt)
            .subject(userId.toString())
            .claim("email", user.email)
            .claim("displayName", user.displayName)
            .build()

        val headers = JwsHeader.with(MacAlgorithm.HS256).build()
        val tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).tokenValue
        return AuthResponse(
            accessToken = tokenValue,
            tokenType = "Bearer",
            expiresInSeconds = jwtProperties.ttl.seconds
        )
    }
}
