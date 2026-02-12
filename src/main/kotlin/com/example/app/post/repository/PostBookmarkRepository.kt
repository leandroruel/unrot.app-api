package com.example.app.post.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.example.app.post.model.PostBookmark
import java.util.UUID

interface PostBookmarkRepository : JpaRepository<PostBookmark, UUID> {
    fun existsByPostIdAndUserId(postId: UUID, userId: UUID): Boolean
    fun deleteByPostIdAndUserId(postId: UUID, userId: UUID)
}
