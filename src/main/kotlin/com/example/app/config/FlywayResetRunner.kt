package com.example.app.config

import org.flywaydb.core.Flyway
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class FlywayResetRunner {

    @Bean
    @Profile("dev")
    fun flywayCleanAndMigrate(flyway: Flyway): ApplicationRunner = ApplicationRunner {
        flyway.clean()
        flyway.migrate()
    }
}
