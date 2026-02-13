package com.unrotapp.post.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import com.unrotapp.post.model.Post
import com.unrotapp.post.model.PostComment
import com.unrotapp.post.model.PostBookmark
import com.unrotapp.post.model.PostLike
import com.unrotapp.post.model.PostMedia
import com.unrotapp.post.model.PostType
import com.unrotapp.post.repository.CategoryRepository
import com.unrotapp.post.repository.PostBookmarkRepository
import com.unrotapp.post.repository.PostCommentRepository
import com.unrotapp.post.repository.PostLikeRepository
import com.unrotapp.post.repository.PostMediaRepository
import com.unrotapp.post.repository.PostRepository
import com.unrotapp.storage.StorageService
import java.util.UUID

@Service
class PostService(
    private val postRepository: PostRepository,
    private val categoryRepository: CategoryRepository,
    private val commentRepository: PostCommentRepository,
    private val likeRepository: PostLikeRepository,
    private val bookmarkRepository: PostBookmarkRepository,
    private val mediaRepository: PostMediaRepository,
    private val storageService: StorageService
) {

    fun findAll(pageable: Pageable): Page<Post> =
        postRepository.findAll(pageable)

    fun findById(id: UUID): Post =
        postRepository.findById(id).orElseThrow { NoSuchElementException("Post not found: $id") }

    fun findByAuthorId(authorId: UUID, pageable: Pageable): Page<Post> =
        postRepository.findByAuthorId(authorId, pageable)

    fun findByCategorySlug(slug: String, pageable: Pageable): Page<Post> =
        postRepository.findByCategorySlug(slug, pageable)

    fun create(authorId: UUID, type: PostType, content: String?, categorySlug: String?, file: MultipartFile?): Post {
        if (type == PostType.NOTE) {
            requireNotNull(content) { "Note content is required" }
            require(content.length <= 250) { "Note content must not exceed 250 characters" }
        }
        if (type == PostType.IMAGE || type == PostType.VIDEO) {
            requireNotNull(file) { "${type.name.lowercase()} file is required" }
        }
        val category = if (type == PostType.ARTICLE) {
            requireNotNull(categorySlug) { "Article category is required" }
            categoryRepository.findBySlug(categorySlug)
                ?: throw NoSuchElementException("Category not found: $categorySlug")
        } else {
            require(categorySlug == null) { "Category is only allowed for articles" }
            null
        }
        val post = postRepository.save(Post(authorId = authorId, type = type, content = content, category = category))

        if (file != null) {
            val metadata = storageService.upload(file)
            mediaRepository.save(
                PostMedia(
                    postId = post.id!!,
                    fileId = metadata.fileId,
                    url = metadata.url,
                    originalFilename = file.originalFilename,
                    mimeType = file.contentType ?: "application/octet-stream",
                    fileSizeBytes = metadata.fileSizeBytes
                )
            )
        }

        return post
    }

    @Transactional
    fun delete(id: UUID) {
        val mediaList = mediaRepository.findByPostId(id)
        mediaList.forEach { storageService.delete(it.fileId) }
        postRepository.deleteById(id)
    }

    fun findMediaByPostId(postId: UUID): List<PostMedia> =
        mediaRepository.findByPostId(postId)

    fun findMediaByPostIds(postIds: List<UUID>): Map<UUID, List<PostMedia>> =
        mediaRepository.findByPostIdIn(postIds).groupBy { it.postId }

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
