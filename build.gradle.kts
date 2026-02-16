import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	kotlin("plugin.jpa") version "2.2.21"
	id("org.springframework.boot") version "4.0.2"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.graalvm.buildtools.native") version "0.10.6"
}

group = "com.unrotapp"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
}

dependencyManagement {
	imports {
		mavenBom(SpringBootPlugin.BOM_COORDINATES)
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.springframework:spring-webflux")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("org.hibernate.orm:hibernate-graalvm")
	testImplementation("com.h2database:h2")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

graalvmNative {
	binaries {
		named("main") {
			buildArgs.add("-H:Features=org.hibernate.graalvm.internal.GraalVMStaticFeature")
		}
	}
}
