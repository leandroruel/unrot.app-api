package com.unrotapp.storage

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(SeaweedFsProperties::class)
class StorageConfig {

    @Bean
    fun seaweedFsRestClient(properties: SeaweedFsProperties): RestClient =
        RestClient.builder()
            .baseUrl(properties.masterUrl)
            .build()
}
