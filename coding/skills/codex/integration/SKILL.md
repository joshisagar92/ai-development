---
name: integration-practices
description: API design and integration patterns. Use when designing APIs, integrating services, or working with microservices.
---

# Integration Practices Skill

Apply these practices for APIs and service integration.

## API Design Principles

### Richardson Maturity Model

| Level | Description | Target |
|-------|-------------|--------|
| 0 | Single endpoint, POST everything | Avoid |
| 1 | Multiple resources | Minimum |
| 2 | Proper HTTP verbs + status | **Recommended** |
| 3 | HATEOAS (hypermedia links) | When beneficial |

### RESTful Resource Design

```
GET    /orders          # List orders
GET    /orders/{id}     # Get one order
POST   /orders          # Create order
PUT    /orders/{id}     # Replace order
PATCH  /orders/{id}     # Update order
DELETE /orders/{id}     # Delete order
```

## HTTP Methods

| Method | Purpose | Idempotent |
|--------|---------|------------|
| GET | Retrieve | Yes |
| POST | Create | No |
| PUT | Full replace | Yes |
| PATCH | Partial update | No |
| DELETE | Remove | Yes |

## Error Response Format

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Human readable message",
    "details": [
      {"field": "email", "message": "Invalid format"}
    ],
    "traceId": "abc-123"
  }
}
```

## Integration Patterns

### Tolerant Reader

> "Be conservative in what you send, be liberal in what you accept."

- Ignore unknown fields when consuming
- Don't fail on extra data
- Only depend on fields you actually use

### Circuit Breaker

```java
@CircuitBreaker(name = "externalService", fallbackMethod = "fallback")
public String callExternalService() {
    return externalClient.call();
}

public String fallback(Exception e) {
    return "Default response";
}
```

### Retry with Backoff

```java
@Retry(name = "externalService", fallbackMethod = "fallback")
public String callExternalService() {
    return externalClient.call();
}
```

## Messaging Patterns

### Idempotent Receiver
Handle duplicate messages safely:

```java
public void handleMessage(OrderEvent event) {
    if (processedEvents.contains(event.getId())) {
        return;  // Already processed
    }
    processOrder(event);
    processedEvents.add(event.getId());
}
```

### Correlation ID
Track requests across services:

```java
@GetMapping("/orders/{id}")
public Order getOrder(@PathVariable Long id,
                      @RequestHeader("X-Correlation-ID") String correlationId) {
    log.info("Processing order {} with correlation {}", id, correlationId);
    // Pass correlationId to downstream calls
}
```

## API Versioning

```
/api/v1/orders     # Version in URL (recommended)
/api/v2/orders     # New version
```

Rules:
- Version from day one
- Support N-1 versions
- Deprecation headers before removal

## Questions to Ask

For API design:
1. "What resources are we exposing?"
2. "What actions can be performed?"
3. "How will this be versioned?"

For integration:
1. "What if the external service is down?"
2. "How do we handle duplicate messages?"
3. "What's the retry strategy?"

For consistency:
1. "Is eventual consistency acceptable?"
2. "What happens if part of the operation fails?"
3. "Do we need distributed transactions?"

## Anti-Patterns to Avoid

- **Shared Database**: Integrate via APIs, not database
- **Synchronous Everything**: Use async for resilience
- **Tight Coupling**: Services should be independently deployable
- **No Timeout**: Always set timeouts on external calls
