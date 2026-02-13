package com.unrotapp.post.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import com.unrotapp.post.model.Post
import java.time.Instant
import java.util.UUID

interface PostRepository : JpaRepository<Post, UUID> {
    fun findByAuthorId(authorId: UUID, pageable: Pageable): Page<Post>
    fun findByCategorySlug(slug: String, pageable: Pageable): Page<Post>
    fun findByCreatedAtAfter(since: Instant): List<Post>
}
