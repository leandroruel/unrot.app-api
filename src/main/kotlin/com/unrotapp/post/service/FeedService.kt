package com.unrotapp.post.service

import org.springframework.stereotype.Service
import com.unrotapp.post.model.Post
import com.unrotapp.post.model.PostType
import com.unrotapp.post.repository.PostBookmarkRepository
import com.unrotapp.post.repository.PostLikeRepository
import com.unrotapp.post.repository.PostRepository
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.pow

@Service
class FeedService(
    private val postRepository: PostRepository,
    private val likeRepository: PostLikeRepository,
    private val bookmarkRepository: PostBookmarkRepository
) {

    companion object {
        private const val SHARE_WEIGHT = 20.0
        private const val COMMENT_WEIGHT = 13.5
        private const val BOOKMARK_WEIGHT = 10.0
        private const val LIKE_WEIGHT = 0.5
        private const val TIME_DECAY_HALF_LIFE_HOURS = 6.0
        private const val CATEGORY_AFFINITY_BOOST = 1.5
        private const val POST_TYPE_PREFERENCE_BOOST = 1.3
        private const val CANDIDATE_DAYS = 7L
    }

    fun getFeed(userId: UUID, page: Int, size: Int): List<Post> {
        val since = Instant.now().minus(CANDIDATE_DAYS, ChronoUnit.DAYS)
        val candidates = postRepository.findByCreatedAtAfter(since)

        if (candidates.isEmpty()) return emptyList()

        val userStats = getUserInteractionStats(userId, candidates)

        val scored = candidates.map { post ->
            ScoredPost(post, scorePost(post, userStats))
        }

        return scored
            .sortedByDescending { it.score }
            .drop(page * size)
            .take(size)
            .map { it.post }
    }

    private fun scorePost(post: Post, userStats: UserInteractionStats): Double {
        val engagement = engagementScore(post)
        val decay = timeDecay(post.createdAt)
        val personalization = personalizationBoost(post, userStats)
        return engagement * decay * personalization
    }

    private fun engagementScore(post: Post): Double {
        return (post.shareCount * SHARE_WEIGHT) +
            (post.commentCount * COMMENT_WEIGHT) +
            (post.bookmarkCount * BOOKMARK_WEIGHT) +
            (post.likeCount * LIKE_WEIGHT) +
            1.0 // base score so new posts with 0 engagement aren't invisible
    }

    private fun timeDecay(createdAt: Instant): Double {
        val ageHours = Duration.between(createdAt, Instant.now()).toSeconds() / 3600.0
        return 0.5.pow(ageHours / TIME_DECAY_HALF_LIFE_HOURS)
    }

    private fun personalizationBoost(post: Post, userStats: UserInteractionStats): Double {
        var boost = 1.0

        val categoryId = post.category?.id
        if (categoryId != null && categoryId in userStats.topCategoryIds) {
            boost *= CATEGORY_AFFINITY_BOOST
        }

        if (post.type in userStats.topPostTypes) {
            boost *= POST_TYPE_PREFERENCE_BOOST
        }

        return boost
    }

    private fun getUserInteractionStats(userId: UUID, candidates: List<Post>): UserInteractionStats {
        val postById = candidates.associateBy { it.id }

        val likedPosts = likeRepository.findByUserId(userId)
            .mapNotNull { postById[it.postId] ?: postRepository.findById(it.postId).orElse(null) }

        val bookmarkedPosts = bookmarkRepository.findByUserId(userId)
            .mapNotNull { postById[it.postId] ?: postRepository.findById(it.postId).orElse(null) }

        val allInteractedPosts = likedPosts + bookmarkedPosts

        val topCategoryIds = allInteractedPosts
            .mapNotNull { it.category?.id }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }
            .toSet()

        val topPostTypes = likedPosts
            .map { it.type }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }
            .toSet()

        return UserInteractionStats(topCategoryIds, topPostTypes)
    }

    private data class ScoredPost(val post: Post, val score: Double)

    private data class UserInteractionStats(
        val topCategoryIds: Set<UUID>,
        val topPostTypes: Set<PostType>
    )
}
