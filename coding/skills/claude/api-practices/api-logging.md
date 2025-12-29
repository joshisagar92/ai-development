---
name: api-logging
description: API logging best practices following 12-Factor methodology. Use when implementing logging, configuring log formats, or setting up log aggregation.
---

# API Logging

> **Domain Experts**: 12-Factor App Methodology (Adam Wiggins), Serilog Best Practices (Nicholas Blumhardt)

## Treat Logs as Event Streams (12-Factor)

Write logs to stdout; let the environment handle routing.

```python
# Good: Write to stdout
import logging
import sys

logging.basicConfig(
    stream=sys.stdout,
    level=logging.INFO,
    format='%(message)s'
)

# Bad: Write to files in the application
logging.basicConfig(filename='/var/log/app.log')
```

## Use Structured Logging (JSON)

Structured logs are machine-parseable.

```json
{
  "timestamp": "2024-01-15T10:30:45.123Z",
  "level": "INFO",
  "service": "order-service",
  "traceId": "abc123def456",
  "spanId": "789xyz",
  "userId": "user-42",
  "message": "Order created successfully",
  "orderId": "ORD-2024-001234",
  "amount": 99.99,
  "duration_ms": 145
}
```

## Always Include Correlation IDs

Track requests across distributed services.

```python
@app.middleware("http")
async def add_correlation_id(request: Request, call_next):
    correlation_id = request.headers.get("X-Correlation-ID", str(uuid.uuid4()))

    with logging_context(correlation_id=correlation_id):
        response = await call_next(request)

    response.headers["X-Correlation-ID"] = correlation_id
    return response
```

## Use Appropriate Log Levels

| Level | Use Case | Production |
|-------|----------|------------|
| TRACE | Very detailed debugging | OFF |
| DEBUG | Debugging information | OFF |
| INFO | Normal operations, business events | ON |
| WARN | Unexpected but handled situations | ON |
| ERROR | Failures requiring attention | ON |
| FATAL | System-critical failures | ON |

## Log Entry and Exit Points

```java
public Order processOrder(OrderRequest request) {
    log.info("Processing order started",
        kv("customerId", request.getCustomerId()),
        kv("itemCount", request.getItems().size()));

    Stopwatch timer = Stopwatch.createStarted();
    try {
        Order order = orderService.create(request);

        log.info("Order processing completed",
            kv("orderId", order.getId()),
            kv("duration_ms", timer.elapsed(MILLISECONDS)),
            kv("status", "success"));

        return order;
    } catch (Exception e) {
        log.error("Order processing failed",
            kv("customerId", request.getCustomerId()),
            kv("duration_ms", timer.elapsed(MILLISECONDS)),
            kv("error", e.getMessage()),
            e);
        throw e;
    }
}
```

## Never Log Sensitive Data

```java
// Bad: Logging sensitive data
log.info("User login: email={}, password={}", email, password);

// Good: Mask sensitive data
log.info("User login attempt", kv("userId", user.getId()));
```

## Don't Use String Concatenation

```java
// Bad: String concatenation (always evaluated)
log.debug("Processing user: " + user.toString());

// Good: Parameterized (only evaluated if level enabled)
log.debug("Processing user: {}", user.getId());
```

## Quick Reference

| Practice | Rule |
|----------|------|
| Output | Write to stdout |
| Format | Structured JSON |
| Correlation | Always include trace IDs |
| Levels | INFO/WARN/ERROR in production |
| Entry/Exit | Log significant operations |
| Sensitive Data | Never log passwords/secrets |
| Performance | Parameterized, not concatenated |
