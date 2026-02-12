package com.example.app.post.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import com.example.app.post.model.Post
import java.util.UUID

interface PostRepository : JpaRepository<Post, UUID> {
    fun findByAuthorId(authorId: UUID, pageable: Pageable): Page<Post>
}
