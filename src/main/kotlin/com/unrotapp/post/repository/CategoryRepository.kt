package com.unrotapp.post.repository

import org.springframework.data.jpa.repository.JpaRepository
import com.unrotapp.post.model.Category
import java.util.UUID

interface CategoryRepository : JpaRepository<Category, UUID> {
    fun findBySlug(slug: String): Category?
}
