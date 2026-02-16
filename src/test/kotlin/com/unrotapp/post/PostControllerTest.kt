package com.unrotapp.post

import tools.jackson.databind.ObjectMapper
import com.unrotapp.post.model.Category
import com.unrotapp.post.model.Post
import com.unrotapp.post.model.PostType
import com.unrotapp.post.repository.CategoryRepository
import com.unrotapp.post.repository.PostRepository
import com.unrotapp.storage.FileMetadata
import com.unrotapp.storage.StorageService
import com.unrotapp.user.Role
import com.unrotapp.user.User
import com.unrotapp.user.UserRepository
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.mock.web.MockPart
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @MockitoBean
    private lateinit var storageService: StorageService

    private lateinit var token: String
    private lateinit var userId: java.util.UUID

    @BeforeEach
    fun setUp() {
        val email = "posttest-${System.nanoTime()}@example.com"
        val user = userRepository.save(
            User(
                email = email,
                passwordHash = passwordEncoder.encode("password123")!!,
                displayName = "Admin User",
                role = Role.ADMIN
            )
        )
        userId = user.id!!
        token = login(email)
    }

    // --- Note posts ---

    @Test
    fun createNotePost() {
        val postJson = objectMapper.writeValueAsBytes(mapOf("type" to "NOTE", "content" to "Hello world"))

        mockMvc.multipart("/api/posts") {
            part(MockPart("post", null, postJson, MediaType.APPLICATION_JSON))
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isCreated() }
                jsonPath("$.type") { value("NOTE") }
                jsonPath("$.content") { value("Hello world") }
                jsonPath("$.media") { isEmpty() }
            }
    }

    @Test
    fun createNotePostTooLong() {
        val postJson = objectMapper.writeValueAsBytes(mapOf("type" to "NOTE", "content" to "x".repeat(251)))

        assertThrows<Exception> {
            mockMvc.multipart("/api/posts") {
                part(MockPart("post", null, postJson, MediaType.APPLICATION_JSON))
                header("Authorization", "Bearer $token")
            }.andReturn()
        }
    }

    // --- Image posts ---

    @Test
    fun createImagePostWithFile() {
        `when`(storageService.upload(any())).thenReturn(
            FileMetadata(fileId = "1,abc123", url = "http://fake:8080/1,abc123", fileSizeBytes = 100)
        )

        val postJson = objectMapper.writeValueAsBytes(mapOf("type" to "IMAGE", "content" to "My photo"))
        val file = MockMultipartFile("file", "photo.png", "image/png", "fake-image-data".toByteArray())

        mockMvc.multipart("/api/posts") {
            part(MockPart("post", null, postJson, MediaType.APPLICATION_JSON))
            file(file)
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isCreated() }
                jsonPath("$.type") { value("IMAGE") }
                jsonPath("$.media[0].url") { value("/api/media/1,abc123") }
                jsonPath("$.media[0].mimeType") { value("image/png") }
                jsonPath("$.media[0].originalFilename") { value("photo.png") }
            }
    }

    @Test
    fun createImagePostWithoutFileReturns500() {
        val postJson = objectMapper.writeValueAsBytes(mapOf("type" to "IMAGE", "content" to "No file"))

        assertThrows<Exception> {
            mockMvc.multipart("/api/posts") {
                part(MockPart("post", null, postJson, MediaType.APPLICATION_JSON))
                header("Authorization", "Bearer $token")
            }.andReturn()
        }
    }

    // --- CRUD ---

    @Test
    fun findAllPosts() {
        createNote("Post A")
        createNote("Post B")

        mockMvc.get("/api/posts") {
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(greaterThanOrEqualTo(2)) }
            }
    }

    @Test
    fun findPostById() {
        val id = createNote("Find me")

        mockMvc.get("/api/posts/$id") {
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.content") { value("Find me") }
            }
    }

    @Test
    fun deletePost() {
        val id = createNote("Delete me")

        mockMvc.delete("/api/posts/$id") {
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isNoContent() }
            }

        assertThrows<Exception> {
            mockMvc.get("/api/posts/$id") {
                header("Authorization", "Bearer $token")
            }.andReturn()
        }
    }

    // --- Likes ---

    @Test
    fun likeAndUnlikePost() {
        val id = createNote("Likeable")

        mockMvc.post("/api/posts/$id/likes") {
            header("Authorization", "Bearer $token")
        }.andExpect { status { isNoContent() } }

        mockMvc.get("/api/posts/$id") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            jsonPath("$.likeCount") { value(1) }
        }

        mockMvc.delete("/api/posts/$id/likes") {
            header("Authorization", "Bearer $token")
        }.andExpect { status { isNoContent() } }

        mockMvc.get("/api/posts/$id") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            jsonPath("$.likeCount") { value(0) }
        }
    }

    // --- Bookmarks ---

    @Test
    fun bookmarkAndUnbookmarkPost() {
        val id = createNote("Bookmarkable")

        mockMvc.post("/api/posts/$id/bookmarks") {
            header("Authorization", "Bearer $token")
        }.andExpect { status { isNoContent() } }

        mockMvc.get("/api/posts/$id") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            jsonPath("$.bookmarkCount") { value(1) }
        }

        mockMvc.delete("/api/posts/$id/bookmarks") {
            header("Authorization", "Bearer $token")
        }.andExpect { status { isNoContent() } }

        mockMvc.get("/api/posts/$id") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            jsonPath("$.bookmarkCount") { value(0) }
        }
    }

    // --- Comments ---

    @Test
    fun addAndDeleteComment() {
        val postId = createNote("Commentable")

        val commentResult = mockMvc.post("/api/posts/$postId/comments") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("content" to "Nice post!"))
        }
            .andExpect {
                status { isCreated() }
                jsonPath("$.content") { value("Nice post!") }
            }
            .andReturn()

        val commentId = objectMapper.readTree(commentResult.response.contentAsString).get("id").textValue()

        mockMvc.get("/api/posts/$postId/comments") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            jsonPath("$.content.length()") { value(1) }
        }

        mockMvc.delete("/api/posts/$postId/comments/$commentId") {
            header("Authorization", "Bearer $token")
        }.andExpect { status { isNoContent() } }
    }

    // --- Shares ---

    @Test
    fun sharePost() {
        val id = createNote("Shareable")

        mockMvc.post("/api/posts/$id/shares") {
            header("Authorization", "Bearer $token")
        }.andExpect { status { isNoContent() } }

        mockMvc.get("/api/posts/$id") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            jsonPath("$.shareCount") { value(1) }
        }
    }

    // --- Find by author ---

    @Test
    fun findPostsByAuthor() {
        createNote("Author post A")
        createNote("Author post B")

        mockMvc.get("/api/posts/author/$userId") {
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(greaterThanOrEqualTo(2)) }
            }
    }

    @Test
    fun findPostsByAuthorReturnsEmptyForUnknown() {
        mockMvc.get("/api/posts/author/${java.util.UUID.randomUUID()}") {
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(0) }
            }
    }

    // --- Find by category ---

    @Test
    fun findPostsByCategory() {
        val category = categoryRepository.save(Category(name = "Tech", slug = "tech-${System.nanoTime()}"))
        postRepository.save(Post(authorId = userId, type = PostType.ARTICLE, content = "Article 1", category = category))
        postRepository.save(Post(authorId = userId, type = PostType.ARTICLE, content = "Article 2", category = category))

        mockMvc.get("/api/posts/category/${category.slug}") {
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(2) }
                jsonPath("$.content[0].category.slug") { value(category.slug) }
            }
    }

    @Test
    fun findPostsByCategoryReturnsEmptyForUnknownSlug() {
        mockMvc.get("/api/posts/category/nonexistent-slug") {
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(0) }
            }
    }

    // --- Article posts ---

    @Test
    fun createArticlePostWithCategory() {
        val category = categoryRepository.save(Category(name = "Art", slug = "art-${System.nanoTime()}"))
        val postJson = objectMapper.writeValueAsBytes(
            mapOf("type" to "ARTICLE", "content" to "Full article here", "categorySlug" to category.slug)
        )

        mockMvc.multipart("/api/posts") {
            part(MockPart("post", null, postJson, MediaType.APPLICATION_JSON))
            header("Authorization", "Bearer $token")
        }
            .andExpect {
                status { isCreated() }
                jsonPath("$.type") { value("ARTICLE") }
                jsonPath("$.content") { value("Full article here") }
                jsonPath("$.category.slug") { value(category.slug) }
            }
    }

    @Test
    fun createArticlePostWithoutCategoryFails() {
        val postJson = objectMapper.writeValueAsBytes(mapOf("type" to "ARTICLE", "content" to "No category"))

        assertThrows<Exception> {
            mockMvc.multipart("/api/posts") {
                part(MockPart("post", null, postJson, MediaType.APPLICATION_JSON))
                header("Authorization", "Bearer $token")
            }.andReturn()
        }
    }

    // --- Regular USER can read posts ---

    @Test
    fun regularUserCanReadPosts() {
        createNote("Readable post")

        val email = "reader-${System.nanoTime()}@example.com"
        val payload = mapOf("email" to email, "password" to "password123", "displayName" to "Reader")
        val result = mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andReturn()
        val userToken = objectMapper.readTree(result.response.contentAsString).get("accessToken").textValue()

        mockMvc.get("/api/posts") {
            header("Authorization", "Bearer $userToken")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.content.length()") { value(greaterThanOrEqualTo(1)) }
            }
    }

    @Test
    fun regularUserCanReadFeed() {
        createNote("Feed visible post")

        val email = "feedreader-${System.nanoTime()}@example.com"
        val payload = mapOf("email" to email, "password" to "password123", "displayName" to "Feed Reader")
        val result = mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andReturn()
        val userToken = objectMapper.readTree(result.response.contentAsString).get("accessToken").textValue()

        mockMvc.get("/api/feed") {
            header("Authorization", "Bearer $userToken")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.length()") { value(greaterThanOrEqualTo(1)) }
            }
    }

    // --- Edge cases ---

    @Test
    fun findPostByIdNotFound() {
        assertThrows<Exception> {
            mockMvc.get("/api/posts/${java.util.UUID.randomUUID()}") {
                header("Authorization", "Bearer $token")
            }.andReturn()
        }
    }

    @Test
    fun doubleLikeIsIdempotent() {
        val id = createNote("Double like")

        mockMvc.post("/api/posts/$id/likes") {
            header("Authorization", "Bearer $token")
        }.andExpect { status { isNoContent() } }

        mockMvc.post("/api/posts/$id/likes") {
            header("Authorization", "Bearer $token")
        }.andExpect { status { isNoContent() } }

        mockMvc.get("/api/posts/$id") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            jsonPath("$.likeCount") { value(1) }
        }
    }

    @Test
    fun doubleBookmarkIsIdempotent() {
        val id = createNote("Double bookmark")

        mockMvc.post("/api/posts/$id/bookmarks") {
            header("Authorization", "Bearer $token")
        }.andExpect { status { isNoContent() } }

        mockMvc.post("/api/posts/$id/bookmarks") {
            header("Authorization", "Bearer $token")
        }.andExpect { status { isNoContent() } }

        mockMvc.get("/api/posts/$id") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            jsonPath("$.bookmarkCount") { value(1) }
        }
    }

    @Test
    fun partnerCanCreatePost() {
        val email = "partner-${System.nanoTime()}@example.com"
        userRepository.save(
            User(
                email = email,
                passwordHash = passwordEncoder.encode("password123")!!,
                displayName = "Partner User",
                role = Role.PARTNER
            )
        )
        val partnerToken = login(email)

        val postJson = objectMapper.writeValueAsBytes(mapOf("type" to "NOTE", "content" to "Partner note"))

        mockMvc.multipart("/api/posts") {
            part(MockPart("post", null, postJson, MediaType.APPLICATION_JSON))
            header("Authorization", "Bearer $partnerToken")
        }
            .andExpect {
                status { isCreated() }
                jsonPath("$.type") { value("NOTE") }
            }
    }

    // --- Auth ---

    @Test
    fun createPostRequiresAuth() {
        mockMvc.multipart("/api/posts") {
            part(MockPart("post", null, """{"type":"NOTE","content":"no auth"}""".toByteArray(), MediaType.APPLICATION_JSON))
        }
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun createPostForbiddenForRegularUser() {
        val email = "regular-${System.nanoTime()}@example.com"
        val payload = mapOf("email" to email, "password" to "password123", "displayName" to "Regular")
        val result = mockMvc.post("/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }.andReturn()
        val userToken = objectMapper.readTree(result.response.contentAsString).get("accessToken").textValue()

        mockMvc.multipart("/api/posts") {
            part(MockPart("post", null, """{"type":"NOTE","content":"forbidden"}""".toByteArray(), MediaType.APPLICATION_JSON))
            header("Authorization", "Bearer $userToken")
        }
            .andExpect {
                status { isForbidden() }
            }
    }

    // --- Helpers ---

    private fun createNote(content: String): String {
        val postJson = objectMapper.writeValueAsBytes(mapOf("type" to "NOTE", "content" to content))
        val result = mockMvc.multipart("/api/posts") {
            part(MockPart("post", null, postJson, MediaType.APPLICATION_JSON))
            header("Authorization", "Bearer $token")
        }
            .andExpect { status { isCreated() } }
            .andReturn()

        return objectMapper.readTree(result.response.contentAsString).get("id").textValue()
    }

    private fun login(email: String): String {
        val payload = mapOf("email" to email, "password" to "password123")
        val result = mockMvc.post("/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(payload)
        }
            .andExpect { status { isOk() } }
            .andReturn()

        return objectMapper.readTree(result.response.contentAsString).get("accessToken").textValue()
    }
}
