package com.unrotapp.storage

import java.net.URI
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.multipart.MultipartFile

data class FileMetadata(
    val fileId: String,
    val url: String,
    val fileSizeBytes: Long
)

@Service
class StorageService(
    private val seaweedFsRestClient: RestClient,
    private val seaweedFsProperties: SeaweedFsProperties
) {

    private val masterHost: String = URI.create(seaweedFsProperties.masterUrl).host

    fun upload(file: MultipartFile): FileMetadata {
        val assign = seaweedFsRestClient.get()
            .uri("/dir/assign")
            .retrieve()
            .body(AssignResponse::class.java)
            ?: throw IllegalStateException("Failed to get file assignment from SeaweedFS")

        val volumeUrl = resolveVolumeUrl(assign.url)

        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("file", object : ByteArrayResource(file.bytes) {
            override fun getFilename() = file.originalFilename ?: "upload"
        }).contentType(MediaType.parseMediaType(file.contentType ?: "application/octet-stream"))

        val uploadClient = RestClient.create(volumeUrl)
        uploadClient.post()
            .uri("/${assign.fid}")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(bodyBuilder.build())
            .retrieve()
            .body(UploadResponse::class.java)
            ?: throw IllegalStateException("Failed to upload file to SeaweedFS")

        return FileMetadata(
            fileId = assign.fid,
            url = "$volumeUrl/${assign.fid}",
            fileSizeBytes = file.size
        )
    }

    fun delete(fileId: String) {
        val lookup = seaweedFsRestClient.get()
            .uri("/dir/lookup?volumeId={volumeId}", fileId.substringBefore(","))
            .retrieve()
            .body(LookupResponse::class.java)
            ?: return

        val location = lookup.locations.firstOrNull() ?: return
        val volumeUrl = resolveVolumeUrl(location.url)
        RestClient.create(volumeUrl)
            .delete()
            .uri("/$fileId")
            .retrieve()
            .toBodilessEntity()
    }

    fun fetch(fileId: String): ByteArray {
        val lookup = seaweedFsRestClient.get()
            .uri("/dir/lookup?volumeId={volumeId}", fileId.substringBefore(","))
            .retrieve()
            .body(LookupResponse::class.java)
            ?: throw NoSuchElementException("File not found: $fileId")

        val location = lookup.locations.firstOrNull()
            ?: throw NoSuchElementException("No volume location for file: $fileId")

        val volumeUrl = resolveVolumeUrl(location.url)
        return RestClient.create(volumeUrl)
            .get()
            .uri("/$fileId")
            .retrieve()
            .body(ByteArray::class.java)
            ?: throw NoSuchElementException("File content not found: $fileId")
    }

    /**
     * Replaces the host in the volume URL with the master's host.
     * SeaweedFS may return internal/private IPs (e.g. 10.0.0.43:8080)
     * that are unreachable from external clients (like Docker containers).
     */
    private fun resolveVolumeUrl(volumeAddress: String): String {
        val port = volumeAddress.substringAfter(":", "8080")
        return "http://$masterHost:$port"
    }

    private data class AssignResponse(
        val fid: String,
        val url: String,
        val publicUrl: String?,
        val count: Int
    )

    private data class UploadResponse(
        val name: String?,
        val size: Long?
    )

    private data class LookupResponse(
        val locations: List<VolumeLocation>
    )

    private data class VolumeLocation(
        val url: String,
        val publicUrl: String?
    )
}
