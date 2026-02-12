package com.unrotapp.post.controller

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import com.unrotapp.post.dto.*
import com.unrotapp.post.service.PostService
import java.util.UUID

@RestController
@RequestMapping("/api/posts")
class PostController(private val postService: PostService) {

    @GetMapping
    fun findAll(pageable: Pageable): Page<PostResponse> =
        postService.findAll(pageable).map { it.toResponse() }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): PostResponse =
        postService.findById(id).toResponse()

    @GetMapping("/author/{authorId}")
    fun findByAuthor(@PathVariable authorId: UUID, pageable: Pageable): Page<PostResponse> =
        postService.findByAuthorId(authorId, pageable).map { it.toResponse() }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreatePostRequest): PostResponse =
        postService.create(request.authorId, request.type, request.content).toResponse()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID) =
        postService.delete(id)

    // Comments

    @GetMapping("/{postId}/comments")
    fun findComments(@PathVariable postId: UUID, pageable: Pageable): Page<PostCommentResponse> =
        postService.findComments(postId, pageable).map { it.toResponse() }

    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    fun addComment(@PathVariable postId: UUID, @RequestBody request: CreateCommentRequest): PostCommentResponse =
        postService.addComment(postId, request.userId, request.content).toResponse()

    @DeleteMapping("/{postId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteComment(@PathVariable postId: UUID, @PathVariable commentId: UUID) =
        postService.deleteComment(postId, commentId)

    // Likes

    @PostMapping("/{postId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun likePost(@PathVariable postId: UUID, @RequestBody request: LikeRequest) =
        postService.likePost(postId, request.userId)

    @DeleteMapping("/{postId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unlikePost(@PathVariable postId: UUID, @RequestBody request: LikeRequest) =
        postService.unlikePost(postId, request.userId)

    // Bookmarks

    @PostMapping("/{postId}/bookmarks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun bookmarkPost(@PathVariable postId: UUID, @RequestBody request: BookmarkRequest) =
        postService.bookmarkPost(postId, request.userId)

    @DeleteMapping("/{postId}/bookmarks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unbookmarkPost(@PathVariable postId: UUID, @RequestBody request: BookmarkRequest) =
        postService.unbookmarkPost(postId, request.userId)
}
