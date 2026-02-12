package com.unrotapp.post.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import com.unrotapp.post.model.PostComment
import java.util.UUID

interface PostCommentRepository : JpaRepository<PostComment, UUID> {
    fun findByPostId(postId: UUID, pageable: Pageable): Page<PostComment>
}
