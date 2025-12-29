---
name: testcontainers
description: Testcontainers best practices for integration testing. Use when writing integration tests with databases, message queues, or other services.
---

# Testcontainers

> **Domain Experts**: Richard North (Creator), Sergei Egorov (Maintainer), Kevin Wittek (Co-maintainer)

## Never Use Fixed Ports

Testcontainers dynamically assigns ports to avoid conflicts.

```java
// Good: Dynamic port binding
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
    .withExposedPorts(5432);

String jdbcUrl = postgres.getJdbcUrl();  // Includes dynamic port

// Bad: Fixed port (causes conflicts in CI)
.withFixedExposedPort(5432, 5432)
```

## Use @DynamicPropertySource for Spring

Configure Spring to use container-provided connection details.

```java
@SpringBootTest
@Testcontainers
class IntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

## Avoid Static Names for Resources

Let Docker assign random names to prevent clashes.

```java
// Good: Let Docker name it
new GenericContainer<>("redis:7");

// Bad: Static name (conflicts across parallel tests)
new GenericContainer<>("redis:7").withCreateContainerCmdModifier(
    cmd -> cmd.withName("my-redis")
);
```

## Use Container Hostname, Not localhost

Use `container.getHost()` instead of hardcoding `localhost`.

```java
String host = container.getHost();  // Works in all environments
int port = container.getMappedPublicPort(6379);
```

## Keep Resource Reaper Enabled

Never disable the Resource Reaper; it cleans up orphaned containers.

## Use Wait Strategies

Ensure containers are ready before tests run.

```java
new GenericContainer<>("my-image")
    .waitingFor(Wait.forHttp("/health").forPort(8080))
    .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1));
```

## Share Containers Across Tests

Use static containers with `@Container` for expensive resources.

```java
@Testcontainers
class SharedContainerTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:15");

    // Container starts once, reused across all tests
}
```

## Quick Reference

| Practice | Rule |
|----------|------|
| Ports | Never fixed, always dynamic |
| Spring | Use @DynamicPropertySource |
| Names | Let Docker assign randomly |
| Hostname | Use getHost(), not localhost |
| Reaper | Keep enabled |
| Readiness | Use wait strategies |
| Performance | Share containers with static |
