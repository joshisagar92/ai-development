---
name: docker
description: Docker best practices for containerization. Use when writing Dockerfiles, building images, or configuring containers.
---

# Docker

> **Domain Experts**: Nigel Poulton (Docker Captain, "Docker Deep Dive"), Solomon Hykes (Docker Creator)

## Use Official Base Images

Start from trusted, maintained images.

```dockerfile
# Good: Official image with specific tag
FROM python:3.11-slim

# Bad: Unknown or untagged image
FROM some-random-image
FROM python:latest
```

## Use Multi-Stage Builds

Reduce final image size by separating build and runtime.

```dockerfile
# Build stage
FROM maven:3.9 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Order Commands by Change Frequency

Put frequently changing instructions last to maximize cache hits.

```dockerfile
# Good: Dependencies first, code last
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package

# Bad: Invalidates cache on any code change
COPY . .
RUN mvn package
```

## Use .dockerignore

Exclude unnecessary files from build context.

```
# .dockerignore
.git
node_modules
*.md
Dockerfile
.env
```

## Run as Non-Root User

Security best practice: never run as root.

```dockerfile
RUN useradd --create-home appuser
USER appuser
```

## Use COPY, Not ADD

`COPY` is explicit; `ADD` has hidden behaviors.

```dockerfile
# Good: Explicit copy
COPY requirements.txt .

# Avoid ADD unless you need its special features
ADD https://example.com/file.tar.gz /app/  # Avoid
```

## One Process Per Container

Containers should run a single process.

```dockerfile
# Good: Single responsibility
CMD ["java", "-jar", "app.jar"]

# Bad: Multiple processes
CMD service nginx start && java -jar app.jar
```

## Use Health Checks

Docker can monitor container health.

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s \
    CMD curl -f http://localhost:8080/health || exit 1
```

## Quick Reference

| Practice | Rule |
|----------|------|
| Base Image | Official with specific tag |
| Builds | Multi-stage for small images |
| Layers | Frequently changing last |
| Ignore | Use .dockerignore |
| Security | Non-root user |
| Copy | COPY, not ADD |
| Process | One per container |
| Health | HEALTHCHECK directive |
