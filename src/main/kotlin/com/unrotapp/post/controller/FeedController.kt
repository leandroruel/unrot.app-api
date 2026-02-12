package com.unrotapp.post.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import com.unrotapp.post.dto.PostResponse
import com.unrotapp.post.dto.toResponse
import com.unrotapp.post.service.FeedService
import java.util.UUID

@RestController
@RequestMapping("/api/feed")
class FeedController(private val feedService: FeedService) {

    @GetMapping
    fun getFeed(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): List<PostResponse> {
        val userId = UUID.fromString(jwt.subject)
        return feedService.getFeed(userId, page, size).map { it.toResponse() }
    }
}
