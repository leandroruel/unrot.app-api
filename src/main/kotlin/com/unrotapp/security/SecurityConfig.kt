package com.unrotapp.security

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.web.SecurityFilterChain
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.OctetSequenceKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import java.util.Base64
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/auth/**", "/actuator/health").permitAll()
                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }
        return http.build()
    }

    @Bean
    fun jwtEncoder(props: JwtProperties): JwtEncoder {
        val secretBytes = Base64.getDecoder().decode(props.secret)
        require(secretBytes.size >= 32) { "JWT secret must be at least 32 bytes" }
        val jwk = OctetSequenceKey.Builder(secretBytes)
            .algorithm(JWSAlgorithm.HS256)
            .build()
        val jwkSet = JWKSet(jwk)
        return NimbusJwtEncoder(ImmutableJWKSet(jwkSet))
    }

    @Bean
    fun jwtDecoder(props: JwtProperties): JwtDecoder {
        val secretBytes = Base64.getDecoder().decode(props.secret)
        require(secretBytes.size >= 32) { "JWT secret must be at least 32 bytes" }
        val key = SecretKeySpec(secretBytes, "HmacSHA256")
        return NimbusJwtDecoder.withSecretKey(key)
            .macAlgorithm(MacAlgorithm.HS256)
            .build()
    }
}
