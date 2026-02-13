package com.unrotapp.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.storage.seaweedfs")
data class SeaweedFsProperties(
    val masterUrl: String
)
