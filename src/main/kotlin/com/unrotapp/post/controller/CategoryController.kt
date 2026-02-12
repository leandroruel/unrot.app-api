package com.unrotapp.post.controller

import org.springframework.web.bind.annotation.*
import com.unrotapp.post.model.Category
import com.unrotapp.post.repository.CategoryRepository

@RestController
@RequestMapping("/api/categories")
class CategoryController(private val categoryRepository: CategoryRepository) {

    @GetMapping
    fun findAll(): List<Category> =
        categoryRepository.findAll()
}
