# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser
EXPOSE 8080

ENV JAVA_OPTS="-Xmx200m \
  -XX:MaxMetaspaceSize=100m \
  -XX:MaxDirectMemorySize=32m \
  -Xss256k \
  -XX:+UseSerialGC \
  -XX:ReservedCodeCacheSize=32m \
  -XX:+UseCompressedOops \
  -XX:+UseCompressedClassPointers \
  -XX:CompressedClassSpaceSize=32m"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
