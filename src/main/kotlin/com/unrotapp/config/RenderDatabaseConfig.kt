package com.unrotapp.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import java.net.URI
import javax.sql.DataSource

@Configuration
@Profile("prod")
class RenderDatabaseConfig {
	@Bean
	@Primary
	@ConditionalOnMissingBean(DataSource::class)
	fun dataSource(properties: DataSourceProperties, env: Environment): DataSource {
		val explicitSpringUrl = env.getProperty("SPRING_DATASOURCE_URL")?.trim().orEmpty()
		val databaseUrl = env.getProperty("DATABASE_URL")?.trim().orEmpty()
		val currentUrl = properties.url?.trim().orEmpty()

		if (databaseUrl.isNotEmpty() && explicitSpringUrl.isEmpty()) {
			val jdbcUrl = toJdbcUrl(databaseUrl)
			properties.url = jdbcUrl

			val (user, pass) = extractCredentials(databaseUrl)
			if (properties.username.isNullOrBlank() && !user.isNullOrBlank()) {
				properties.username = user
			}
			if (properties.password.isNullOrBlank() && !pass.isNullOrBlank()) {
				properties.password = pass
			}
		} else if (currentUrl.startsWith("postgres://") || currentUrl.startsWith("postgresql://")) {
			properties.url = toJdbcUrl(currentUrl)
		}

		return properties.initializeDataSourceBuilder().build()
	}

	private fun toJdbcUrl(raw: String): String {
		if (raw.startsWith("jdbc:")) {
			return raw
		}

		val uri = URI(raw)
		val scheme = uri.scheme?.lowercase()
		if (scheme != "postgres" && scheme != "postgresql") {
			return raw
		}

		val host = uri.host.orEmpty()
		val port = if (uri.port == -1) "" else ":${uri.port}"
		val database = uri.path?.removePrefix("/").orEmpty()
		val query = uri.rawQuery?.let { "?$it" }.orEmpty()

		return "jdbc:postgresql://$host$port/$database$query"
	}

	private fun extractCredentials(raw: String): Pair<String?, String?> {
		val normalized = if (raw.startsWith("jdbc:")) raw.removePrefix("jdbc:") else raw
		val uri = URI(normalized)
		val userInfo = uri.userInfo ?: return null to null
		val parts = userInfo.split(":", limit = 2)
		val user = parts.getOrNull(0)?.ifBlank { null }
		val pass = parts.getOrNull(1)?.ifBlank { null }
		return user to pass
	}
}
