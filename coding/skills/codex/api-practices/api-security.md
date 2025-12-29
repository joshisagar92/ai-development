---
name: api-security
description: API security best practices based on OWASP API Top 10. Use when securing APIs, implementing authentication, authorization, or rate limiting.
---

# API Security (OWASP API Top 10)

> **Domain Expert**: OWASP API Security Project

## API1: BOLA - Broken Object Level Authorization

The #1 API vulnerability (~40% of attacks).

```java
// VULNERABLE
@GetMapping("/api/v1/orders/{orderId}")
public Order getOrder(@PathVariable Long orderId) {
    return orderRepository.findById(orderId);
}

// SECURE: Query by user ID
@GetMapping("/api/v1/orders/{orderId}")
public Order getOrder(@PathVariable Long orderId, @AuthenticationPrincipal User user) {
    return orderRepository.findByIdAndUserId(orderId, user.getId())
        .orElseThrow(() -> new NotFoundException());
}
```

### Use Non-Guessable IDs

```java
// BAD: Sequential IDs are enumerable
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;  // 1, 2, 3, 4...

// BETTER: Use UUIDs
@Id
@GeneratedValue(generator = "UUID")
private UUID id;
```

## API3: Mass Assignment Prevention

```java
// VULNERABLE: Mass assignment
@PutMapping("/api/v1/users/{userId}")
public User updateUser(@RequestBody User user) {
    return userRepository.save(user);  // Attacker can modify isAdmin!
}

// SECURE: Use DTOs with explicit field mapping
public class UserUpdateRequest {
    @NotBlank
    private String name;

    @Email
    private String email;
    // No isAdmin, roles, or other sensitive fields
}
```

## API4: Rate Limiting

```java
@Configuration
public class RateLimitConfig {
    @Bean
    public Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}

// Limit response sizes
@GetMapping("/api/v1/items")
public Page<Item> getItems(
        @RequestParam(defaultValue = "20") @Max(100) int size) {
    return itemRepository.findAll(PageRequest.of(0, Math.min(size, 100)));
}
```

## API5: Function Level Authorization

```python
class Role(str, Enum):
    USER = "user"
    MODERATOR = "moderator"
    ADMIN = "admin"

def require_role(required_role: Role):
    def decorator(func):
        @wraps(func)
        async def wrapper(*args, current_user = Depends(get_current_user), **kwargs):
            if not has_role(current_user, required_role):
                raise HTTPException(status_code=403, detail="Insufficient permissions")
            return await func(*args, current_user=current_user, **kwargs)
        return wrapper
    return decorator

@app.delete("/api/admin/users/{user_id}")
@require_role(Role.ADMIN)
async def delete_user(user_id: int):
    await db.delete_user(user_id)
```

## API7: SSRF Prevention

```python
ALLOWED_HOSTS = {"api.trusted-partner.com", "cdn.example.com"}
BLOCKED_RANGES = [
    ipaddress.ip_network("10.0.0.0/8"),
    ipaddress.ip_network("172.16.0.0/12"),
    ipaddress.ip_network("192.168.0.0/16"),
    ipaddress.ip_network("127.0.0.0/8"),
]

async def fetch_external_resource(url: str):
    parsed = urlparse(url)

    if parsed.scheme not in ("http", "https"):
        raise ValueError("Invalid URL scheme")

    if parsed.hostname not in ALLOWED_HOSTS:
        raise ValueError("Host not allowed")

    ip = socket.gethostbyname(parsed.hostname)
    ip_addr = ipaddress.ip_address(ip)

    for blocked_range in BLOCKED_RANGES:
        if ip_addr in blocked_range:
            raise ValueError("Access to internal networks blocked")
```

## Security Headers

```python
@app.middleware("http")
async def add_security_headers(request: Request, call_next):
    response = await call_next(request)
    response.headers["X-Content-Type-Options"] = "nosniff"
    response.headers["X-Frame-Options"] = "DENY"
    response.headers["X-XSS-Protection"] = "1; mode=block"
    response.headers["Strict-Transport-Security"] = "max-age=31536000; includeSubDomains"
    response.headers["Content-Security-Policy"] = "default-src 'self'"
    return response
```

## Quick Reference

| Vulnerability | Prevention |
|--------------|------------|
| BOLA | Verify object ownership in queries |
| Mass Assignment | Use DTOs with explicit fields |
| Rate Limiting | Implement per-endpoint limits |
| Function Auth | Role-based access control |
| SSRF | Allowlist hosts, block internal IPs |
| Misconfiguration | Set security headers |
