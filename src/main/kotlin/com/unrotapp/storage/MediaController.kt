package com.unrotapp.storage

import org.springframework.http.CacheControl
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import com.unrotapp.post.repository.PostMediaRepository
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/media")
class MediaController(
    private val storageService: StorageService,
    private val mediaRepository: PostMediaRepository
) {

    @GetMapping("/{fileId}")
    fun serve(@PathVariable fileId: String): ResponseEntity<ByteArray> {
        val media = mediaRepository.findByFileId(fileId)
            ?: throw NoSuchElementException("Media not found: $fileId")

        val bytes = storageService.fetch(fileId)

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(media.mimeType))
            .contentLength(bytes.size.toLong())
            .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
            .body(bytes)
    }
}
