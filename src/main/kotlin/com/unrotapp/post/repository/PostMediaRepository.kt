package com.unrotapp.post.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.unrotapp.post.model.PostMedia
import java.util.UUID

interface PostMediaRepository : JpaRepository<PostMedia, UUID> {
    fun findByPostId(postId: UUID): List<PostMedia>
    fun findByPostIdIn(postIds: List<UUID>): List<PostMedia>
    fun findByFileId(fileId: String): PostMedia?
}
