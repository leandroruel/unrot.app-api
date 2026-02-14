package com.unrotapp.post

import tools.jackson.databind.ObjectMapper
import com.unrotapp.post.model.Post
import com.unrotapp.post.model.PostType
import com.unrotapp.post.repository.PostRepository
import com.unrotapp.storage.StorageService
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
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
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FeedServiceTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var postRepository: PostRepository

    @MockitoBean
    private lateinit var storageService: StorageService

    private lateinit var token: String
    private lateinit var userId: UUID

    @BeforeEach
    fun setUp() {
        val email = "feed-${System.nanoTime()}@example.com"
        token = registerUser(email)
        userId = extractUserId(token)
    }

    @Test
    fun feedReturnsRecentPosts() {
        postRepository.save(Post(authorId = userId, type = PostType.NOTE, content = "Feed post 1"))
        postRepository.save(Post(authorId = userId, type = PostType.NOTE, content = "Feed post 2"))

        mockMvc.get("/api/feed") {
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(greaterThanOrEqualTo(2)) }
            }
    }

    @Test
    fun feedRanksEngagedPostsHigher() {
        postRepository.save(Post(authorId = userId, type = PostType.NOTE, content = "Low engagement"))
        val highEngagement = Post(authorId = userId, type = PostType.NOTE, content = "High engagement").apply {
            likeCount = 100
            shareCount = 50
            commentCount = 30
        }
        postRepository.save(highEngagement)

        mockMvc.get("/api/feed") {
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$[0].content") { value("High engagement") }
            }
    }

    @Test
    fun feedRequiresAuth() {
        mockMvc.get("/api/feed")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun feedSupportsPagination() {
        repeat(5) {
            postRepository.save(Post(authorId = userId, type = PostType.NOTE, content = "Page post $it"))
        }

        mockMvc.get("/api/feed?page=0&size=2") {
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(2) }
            }
    }

    // --- Helpers ---

    private fun registerUser(email: String): String {
        val payload = mapOf(
            "email" to email,
            "password" to "password123",
            "displayName" to "Feed User"
        )

        val result = mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect { status { isOk() } }
            .andReturn()

        return objectMapper.readTree(result.response.contentAsString).get("accessToken").textValue()
    }

    private fun extractUserId(token: String): UUID {
        val payload = String(java.util.Base64.getUrlDecoder().decode(token.split(".")[1]))
        return UUID.fromString(objectMapper.readTree(payload).get("sub").textValue())
    }
}
