package com.example.app.post.dto

import com.example.app.post.model.Post
import com.example.app.post.model.PostComment

fun Post.toResponse() = PostResponse(
    id = id!!,
    authorId = authorId,
    type = type,
    content = content,
    likeCount = likeCount,
    commentCount = commentCount,
    bookmarkCount = bookmarkCount,
    createdAt = createdAt
)

fun PostComment.toResponse() = PostCommentResponse(
    id = id!!,
    postId = postId,
    userId = userId,
    content = content,
    createdAt = createdAt
)
