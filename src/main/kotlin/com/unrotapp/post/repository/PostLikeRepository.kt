package com.unrotapp.post.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.unrotapp.post.model.PostLike
import java.util.UUID

interface PostLikeRepository : JpaRepository<PostLike, UUID> {
    fun existsByPostIdAndUserId(postId: UUID, userId: UUID): Boolean
    fun deleteByPostIdAndUserId(postId: UUID, userId: UUID)
    fun findByUserId(userId: UUID): List<PostLike>
}
