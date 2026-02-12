package com.unrotapp.post.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.unrotapp.post.model.Post
import com.unrotapp.post.model.PostComment
import com.unrotapp.post.model.PostBookmark
import com.unrotapp.post.model.PostLike
import com.unrotapp.post.model.PostType
import com.unrotapp.post.repository.CategoryRepository
import com.unrotapp.post.repository.PostBookmarkRepository
import com.unrotapp.post.repository.PostCommentRepository
import com.unrotapp.post.repository.PostLikeRepository
import com.unrotapp.post.repository.PostRepository
import java.util.UUID

@Service
class PostService(
    private val postRepository: PostRepository,
    private val categoryRepository: CategoryRepository,
    private val commentRepository: PostCommentRepository,
    private val likeRepository: PostLikeRepository,
    private val bookmarkRepository: PostBookmarkRepository
) {

    fun findAll(pageable: Pageable): Page<Post> =
        postRepository.findAll(pageable)

    fun findById(id: UUID): Post =
        postRepository.findById(id).orElseThrow { NoSuchElementException("Post not found: $id") }

    fun findByAuthorId(authorId: UUID, pageable: Pageable): Page<Post> =
        postRepository.findByAuthorId(authorId, pageable)

    fun findByCategorySlug(slug: String, pageable: Pageable): Page<Post> =
        postRepository.findByCategorySlug(slug, pageable)

    fun create(authorId: UUID, type: PostType, content: String?, categorySlug: String?): Post {
        if (type == PostType.NOTE) {
            requireNotNull(content) { "Note content is required" }
            require(content.length <= 250) { "Note content must not exceed 250 characters" }
        }
        val category = if (type == PostType.ARTICLE) {
            requireNotNull(categorySlug) { "Article category is required" }
            categoryRepository.findBySlug(categorySlug)
                ?: throw NoSuchElementException("Category not found: $categorySlug")
        } else {
            require(categorySlug == null) { "Category is only allowed for articles" }
            null
        }
        return postRepository.save(Post(authorId = authorId, type = type, content = content, category = category))
    }

    fun delete(id: UUID) =
        postRepository.deleteById(id)

    // Comments

    fun findComments(postId: UUID, pageable: Pageable): Page<PostComment> =
        commentRepository.findByPostId(postId, pageable)

    @Transactional
    fun addComment(postId: UUID, userId: UUID, content: String): PostComment {
        val post = findById(postId)
        val comment = commentRepository.save(PostComment(postId = postId, userId = userId, content = content))
        post.commentCount++
        postRepository.save(post)
        return comment
    }

    @Transactional
    fun deleteComment(postId: UUID, commentId: UUID) {
        val post = findById(postId)
        commentRepository.deleteById(commentId)
        post.commentCount = maxOf(0, post.commentCount - 1)
        postRepository.save(post)
    }

    // Likes

    @Transactional
    fun likePost(postId: UUID, userId: UUID) {
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) return
        likeRepository.save(PostLike(postId = postId, userId = userId))
        val post = findById(postId)
        post.likeCount++
        postRepository.save(post)
    }

    @Transactional
    fun unlikePost(postId: UUID, userId: UUID) {
        if (!likeRepository.existsByPostIdAndUserId(postId, userId)) return
        likeRepository.deleteByPostIdAndUserId(postId, userId)
        val post = findById(postId)
        post.likeCount = maxOf(0, post.likeCount - 1)
        postRepository.save(post)
    }

    // Shares

    @Transactional
    fun sharePost(postId: UUID) {
        val post = findById(postId)
        post.shareCount++
        postRepository.save(post)
    }

    // Bookmarks

    @Transactional
    fun bookmarkPost(postId: UUID, userId: UUID) {
        if (bookmarkRepository.existsByPostIdAndUserId(postId, userId)) return
        bookmarkRepository.save(PostBookmark(postId = postId, userId = userId))
        val post = findById(postId)
        post.bookmarkCount++
        postRepository.save(post)
    }

    @Transactional
    fun unbookmarkPost(postId: UUID, userId: UUID) {
        if (!bookmarkRepository.existsByPostIdAndUserId(postId, userId)) return
        bookmarkRepository.deleteByPostIdAndUserId(postId, userId)
        val post = findById(postId)
        post.bookmarkCount = maxOf(0, post.bookmarkCount - 1)
        postRepository.save(post)
    }
}
