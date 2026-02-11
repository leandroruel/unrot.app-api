# unrot.app

Backend API for unrot — a curated content feed that makes people smarter, not dumber. Think Instagram-style feed, but with substance over slop.

## Tech Stack

- **Runtime:** Java 21 LTS (Eclipse Temurin)
- **Framework:** Spring Boot 4.0.2, Spring Data JPA, Spring Actuator
- **Language:** Kotlin 2.2.21
- **Database:** PostgreSQL 17
- **Build:** Gradle 9.3
- **Container:** Docker (multi-stage build)
- **Orchestration:** microk8s with HPA autoscaling

## Running Locally

### Docker Compose (recommended)

```bash
docker compose up --build
```

App: `http://localhost:8080`
Health: `http://localhost:8080/actuator/health`

### Gradle (dev)

```bash
# Start PostgreSQL
docker compose up postgres

# Run the app
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## Testing

```bash
./gradlew test
```

Tests use H2 in-memory database — no PostgreSQL required.

## Deploy to microk8s

```bash
# Enable required addons
microk8s enable dns storage registry metrics-server

# Build and push image
docker build -t localhost:32000/demo-app:latest .
docker push localhost:32000/demo-app:latest

# Apply manifests
microk8s kubectl apply -f k8s/

# Verify
microk8s kubectl get pods -n demo
microk8s kubectl get hpa -n demo
```

## Kubernetes Features

- **Health Probes:** liveness, readiness, and startup probes via Spring Actuator
- **Autoscaling:** HPA with 2–8 replicas, targeting 70% CPU utilization
- **Secrets:** DB credentials managed via Kubernetes Secrets
- **Persistent Storage:** PostgreSQL data on PVC (1Gi)

## Spring Profiles

| Profile | Usage | Database |
|---------|-------|----------|
| *(default)* | Local development | PostgreSQL on localhost:5432 |
| `dev` | Docker Compose | PostgreSQL with `ddl-auto: update` |
| `prod` | Kubernetes | PostgreSQL via env vars, `ddl-auto: validate` |
| `test` | Tests | H2 in-memory |

## Project Structure

```
├── src/main/kotlin/com/example/app/   # Application source
├── src/main/resources/                 # Config (application*.yml)
├── src/test/                           # Tests
├── k8s/                                # Kubernetes manifests
├── Dockerfile                          # Multi-stage build
└── docker-compose.yml                  # Local dev environment
```
