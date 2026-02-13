package com.unrotapp.post.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import com.unrotapp.post.dto.PostResponse
import com.unrotapp.post.dto.toResponse
import com.unrotapp.post.service.FeedService
import com.unrotapp.post.service.PostService
import java.util.UUID

@RestController
@RequestMapping("/api/feed")
class FeedController(
    private val feedService: FeedService,
    private val postService: PostService
) {

    @GetMapping
    fun getFeed(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): List<PostResponse> {
        val userId = UUID.fromString(jwt.subject)
        val posts = feedService.getFeed(userId, page, size)
        val mediaMap = postService.findMediaByPostIds(posts.mapNotNull { it.id })
        return posts.map { it.toResponse(mediaMap[it.id] ?: emptyList()) }
    }
}
