package com.unrotapp.storage

import com.unrotapp.post.model.Post
import com.unrotapp.post.model.PostMedia
import com.unrotapp.post.model.PostType
import com.unrotapp.post.repository.PostMediaRepository
import com.unrotapp.post.repository.PostRepository
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MediaControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var mediaRepository: PostMediaRepository

    @MockitoBean
    private lateinit var storageService: StorageService

    @Test
    fun serveMediaReturnsFileContent() {
        val post = postRepository.save(Post(authorId = UUID.randomUUID(), type = PostType.NOTE, content = "test"))
        mediaRepository.save(
            PostMedia(
                postId = post.id!!,
                fileId = "1,testfile123",
                url = "http://fake:8080/1,testfile123",
                originalFilename = "photo.png",
                mimeType = "image/png",
                fileSizeBytes = 4
            )
        )

        val fakeBytes = "PNG!".toByteArray()
        `when`(storageService.fetch("1,testfile123")).thenReturn(fakeBytes)

        mockMvc.get("/api/media/1,testfile123")
            .andExpect {
                status { isOk() }
                content { contentType("image/png") }
                content { bytes(fakeBytes) }
                header { string("Cache-Control", containsString("max-age")) }
            }
    }

    @Test
    fun serveMediaNotFoundThrowsException() {
        assertThrows<Exception> {
            mockMvc.get("/api/media/999,nonexistent").andReturn()
        }
    }

    @Test
    fun mediaEndpointIsPublic() {
        // No auth header — should throw (not found), not 401
        assertThrows<Exception> {
            mockMvc.get("/api/media/999,nonexistent").andReturn()
        }
    }
}
