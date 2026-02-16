# Stage 1: Build native image
FROM ghcr.io/graalvm/native-image-community:21 AS builder
RUN microdnf install -y findutils && microdnf clean all
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew .
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
COPY src/ src/
RUN ./gradlew nativeCompile --no-daemon -x test

# Stage 2: Run
FROM debian:bookworm-slim
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
COPY --from=builder /app/build/native/nativeCompile/unrotapp app
RUN chown appuser:appgroup app
USER appuser
EXPOSE 8080
ENTRYPOINT ["./app"]
