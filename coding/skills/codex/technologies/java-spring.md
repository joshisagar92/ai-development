---
name: java-spring
description: Java and Spring Framework best practices. Use when writing Java code, Spring Boot applications, or configuring Spring components.
---

# Java & Spring Framework

> **Domain Experts**: Josh Long (Spring Developer Advocate), Juergen Hoeller (Spring Framework Lead), Phil Webb (Spring Boot Co-founder)

## Spring Boot Starters

Use curated starter dependencies for tested, verified configurations.

```xml
<!-- Good: Use starters -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Bad: Cherry-pick individual JARs with version mismatches -->
```

## Constructor Injection

Prefer constructor injection over field injection.

```java
// Good: Constructor injection
@Service
public class OrderService {
    private final OrderRepository repository;
    private final PaymentService paymentService;

    public OrderService(OrderRepository repository, PaymentService paymentService) {
        this.repository = repository;
        this.paymentService = paymentService;
    }
}

// Bad: Field injection
@Service
public class OrderService {
    @Autowired
    private OrderRepository repository;
}
```

## Profile-Based Configuration

Use Spring profiles for environment-specific configuration.

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dev_db

# application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://prod-server:3306/prod_db
```

## Externalize Configuration

Never commit secrets or environment-specific values.

```java
@Value("${database.password}")
private String password;  // Injected from environment
```

## Actuator for Monitoring

Enable Spring Actuator for health checks and metrics.

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  endpoint:
    health:
      show-details: when_authorized
```

## Structured Logging

Use structured logging with correlation IDs.

```java
@Slf4j
public class PaymentService {
    public void processPayment(String correlationId, Payment payment) {
        MDC.put("correlationId", correlationId);
        log.info("Processing payment: {}", payment.getId());
    }
}
```

## Graceful Shutdown

Enable graceful shutdown in production.

```yaml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

## Quick Reference

| Practice | Rule |
|----------|------|
| Starters | Use spring-boot-starter-* dependencies |
| Injection | Constructor injection, not field injection |
| Config | Externalize with profiles |
| Secrets | Never in code or config files |
| Monitoring | Enable Actuator endpoints |
| Logging | Structured with correlation IDs |
