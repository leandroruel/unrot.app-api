package com.unrotapp.post

import com.unrotapp.post.model.Category
import com.unrotapp.post.repository.CategoryRepository
import com.unrotapp.storage.StorageService
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoryControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @MockitoBean
    private lateinit var storageService: StorageService

    @Test
    fun listAllCategories() {
        val slug = "cat-${System.nanoTime()}"
        categoryRepository.save(Category(name = "Test Category", slug = slug))

        val token = registerUser("cattest-${System.nanoTime()}@example.com")

        mockMvc.get("/api/categories") {
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(greaterThanOrEqualTo(1)) }
            }
    }

    @Test
    fun listCategoriesRequiresAuth() {
        mockMvc.get("/api/categories")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    private fun registerUser(email: String): String {
        val payload = mapOf(
            "email" to email,
            "password" to "password123",
            "displayName" to "Cat User"
        )

        val result = mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect { status { isOk() } }
            .andReturn()

        return objectMapper.readTree(result.response.contentAsString).get("accessToken").textValue()
    }
}
