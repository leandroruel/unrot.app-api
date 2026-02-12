package com.unrotapp.auth

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper
) {

    @Test
    fun registerReturnsToken() {
        val payload = mapOf(
            "email" to "user1@example.com",
            "password" to "password123",
            "displayName" to "User One"
        )

        mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.accessToken") { value(not("")) }
                jsonPath("$.tokenType") { value("Bearer") }
                jsonPath("$.expiresInSeconds") { value(3600) }
            }
    }

    @Test
    fun loginReturnsToken() {
        registerUser("user2@example.com")

        val payload = mapOf(
            "email" to "user2@example.com",
            "password" to "password123"
        )

        mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.accessToken") { value(not("")) }
            }
    }

    @Test
    fun postsRequireAuth() {
        mockMvc.get("/api/posts")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun postsReturnOkWithToken() {
        val token = registerUser("user3@example.com")

        mockMvc.get("/api/posts") {
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isOk() }
            }
    }

    private fun registerUser(email: String): String {
        val payload = mapOf(
            "email" to email,
            "password" to "password123",
            "displayName" to "User"
        )

        val result = mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect {
                status { isOk() }
            }
            .andReturn()

        val json = objectMapper.readTree(result.response.contentAsString)
        return json.get("accessToken").asText()
    }
}
