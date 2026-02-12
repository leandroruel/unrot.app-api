package com.example.app.post.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "posts")
class Post(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false)
    val authorId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: PostType,

    @Column(columnDefinition = "TEXT")
    val content: String? = null,

    @Column(nullable = false)
    var likeCount: Long = 0,

    @Column(nullable = false)
    var commentCount: Long = 0,

    @Column(nullable = false)
    var bookmarkCount: Long = 0,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)
