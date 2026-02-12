package com.example.app.post.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "post_likes",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["post_id", "user_id"])
    ]
)
class PostLike(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "post_id", nullable = false)
    val postId: UUID,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)
