package com.unrotapp.post.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "post_media")
class PostMedia(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "post_id", nullable = false)
    val postId: UUID,

    @Column(nullable = false)
    val fileId: String,

    @Column(nullable = false)
    val url: String,

    val originalFilename: String? = null,

    @Column(nullable = false)
    val mimeType: String,

    @Column(nullable = false)
    val fileSizeBytes: Long,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)
