package com.unrotapp.config

import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportRuntimeHints
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

@Configuration
@ImportRuntimeHints(HibernateRuntimeHints::class)
class NativeHintsConfig

class HibernateRuntimeHints : RuntimeHintsRegistrar {
    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        val resolver = PathMatchingResourcePatternResolver(classLoader)
        val resources = runCatching {
            resolver.getResources("classpath*:org/hibernate/**/*_\$logger.class")
        }.getOrDefault(emptyArray())

        if (resources.isEmpty()) {
            registerLoggerImplementation(hints, classLoader, "org.hibernate.jpa.internal.JpaLogger_\$logger")
            return
        }

        for (resource in resources) {
            val className = resourceClassName(resource.toString()) ?: continue
            registerLoggerImplementation(hints, classLoader, className)
        }
    }

    private fun registerLoggerImplementation(
        hints: RuntimeHints,
        classLoader: ClassLoader?,
        className: String
    ) {
        val clazz = runCatching { Class.forName(className, false, classLoader) }.getOrNull() ?: return
        hints.reflection().registerType(clazz, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
    }

    private fun resourceClassName(resourceDescription: String): String? {
        val marker = "org/hibernate/"
        val startIndex = resourceDescription.indexOf(marker)
        if (startIndex == -1) {
            return null
        }
        val path = resourceDescription.substring(startIndex)
        if (!path.endsWith(".class")) {
            return null
        }
        return path.removeSuffix(".class").replace('/', '.')
    }
}
