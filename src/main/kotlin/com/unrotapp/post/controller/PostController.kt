package com.unrotapp.post.controller

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import com.unrotapp.post.dto.*
import com.unrotapp.post.service.PostService
import java.util.UUID

@RestController
@RequestMapping("/api/posts")
class PostController(private val postService: PostService) {

    @GetMapping
    fun findAll(pageable: Pageable): Page<PostResponse> {
        val page = postService.findAll(pageable)
        val mediaMap = postService.findMediaByPostIds(page.content.mapNotNull { it.id })
        return page.map { it.toResponse(mediaMap[it.id] ?: emptyList()) }
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): PostResponse {
        val post = postService.findById(id)
        return post.toResponse(postService.findMediaByPostId(id))
    }

    @GetMapping("/author/{authorId}")
    fun findByAuthor(@PathVariable authorId: UUID, pageable: Pageable): Page<PostResponse> {
        val page = postService.findByAuthorId(authorId, pageable)
        val mediaMap = postService.findMediaByPostIds(page.content.mapNotNull { it.id })
        return page.map { it.toResponse(mediaMap[it.id] ?: emptyList()) }
    }

    @GetMapping("/category/{slug}")
    fun findByCategory(@PathVariable slug: String, pageable: Pageable): Page<PostResponse> {
        val page = postService.findByCategorySlug(slug, pageable)
        val mediaMap = postService.findMediaByPostIds(page.content.mapNotNull { it.id })
        return page.map { it.toResponse(mediaMap[it.id] ?: emptyList()) }
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("#jwt.getClaimAsString('role') == 'ADMIN' or #jwt.getClaimAsString('role') == 'PARTNER'")
    fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestPart("post") request: CreatePostRequest,
        @RequestPart("file", required = false) file: MultipartFile?
    ): PostResponse {
        val post = postService.create(jwt.userId(), request.type, request.content, request.categorySlug, file)
        return post.toResponse(postService.findMediaByPostId(post.id!!))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#jwt.getClaimAsString('role') == 'ADMIN' or #jwt.getClaimAsString('role') == 'PARTNER'")
    fun delete(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: UUID) =
        postService.delete(id)

    // Comments

    @GetMapping("/{postId}/comments")
    fun findComments(@PathVariable postId: UUID, pageable: Pageable): Page<PostCommentResponse> =
        postService.findComments(postId, pageable).map { it.toResponse() }

    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    fun addComment(
        @PathVariable postId: UUID,
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CreateCommentRequest
    ): PostCommentResponse =
        postService.addComment(postId, jwt.userId(), request.content).toResponse()

    @DeleteMapping("/{postId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteComment(@PathVariable postId: UUID, @PathVariable commentId: UUID) =
        postService.deleteComment(postId, commentId)

    // Likes

    @PostMapping("/{postId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun likePost(@PathVariable postId: UUID, @AuthenticationPrincipal jwt: Jwt) =
        postService.likePost(postId, jwt.userId())

    @DeleteMapping("/{postId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unlikePost(@PathVariable postId: UUID, @AuthenticationPrincipal jwt: Jwt) =
        postService.unlikePost(postId, jwt.userId())

    // Shares

    @PostMapping("/{postId}/shares")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun sharePost(@PathVariable postId: UUID) =
        postService.sharePost(postId)

    // Bookmarks

    @PostMapping("/{postId}/bookmarks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun bookmarkPost(@PathVariable postId: UUID, @AuthenticationPrincipal jwt: Jwt) =
        postService.bookmarkPost(postId, jwt.userId())

    @DeleteMapping("/{postId}/bookmarks")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unbookmarkPost(@PathVariable postId: UUID, @AuthenticationPrincipal jwt: Jwt) =
        postService.unbookmarkPost(postId, jwt.userId())

    private fun Jwt.userId(): UUID =
        UUID.fromString(subject)
}
