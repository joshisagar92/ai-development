---
name: security-practices
description: Security implementation practices based on OWASP. Use when handling user input, authentication, authorization, or sensitive data.
---

# Security Practices Skill

Apply these security rules to all code.

## Golden Rules

1. **Never trust user input** - Validate everything
2. **Deny by default** - Explicitly grant access
3. **Defense in depth** - Multiple security layers
4. **Fail securely** - Errors shouldn't expose info

## SQL Injection Prevention

### Always Use Parameterized Queries

```java
// NEVER: String concatenation
String sql = "SELECT * FROM users WHERE id = " + userId;

// ALWAYS: Parameterized
PreparedStatement stmt = conn.prepareStatement(
    "SELECT * FROM users WHERE id = ?");
stmt.setLong(1, userId);
```

```python
# NEVER
cursor.execute(f"SELECT * FROM users WHERE name = '{name}'")

# ALWAYS
cursor.execute("SELECT * FROM users WHERE name = %s", (name,))
```

## XSS Prevention

### Encode Output Based on Context

- HTML body: HTML-encode
- HTML attribute: Attribute-encode
- JavaScript: JavaScript-encode
- URL: URL-encode

```java
// Use OWASP Encoder
Encode.forHtml(userInput)      // <div>${this}</div>
Encode.forHtmlAttribute(input) // <div title="${this}">
Encode.forJavaScript(input)    // <script>var x='${this}'</script>
```

## Access Control

### Verify Object Ownership

```java
// INSECURE: No ownership check
@GetMapping("/orders/{id}")
public Order getOrder(@PathVariable Long id) {
    return orderRepo.findById(id);  // Anyone can access any order!
}

// SECURE: Verify ownership
@GetMapping("/orders/{id}")
public Order getOrder(@PathVariable Long id, @AuthenticationPrincipal User user) {
    return orderRepo.findByIdAndUserId(id, user.getId())
        .orElseThrow(() -> new NotFoundException());
}
```

## Password Storage

### Use Strong Hashing

```java
// Use bcrypt with cost factor 12+
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

Never:
- Store plaintext passwords
- Use MD5 or SHA1 for passwords
- Use unsalted hashes

## Security Checklist

Before code review, verify:

- [ ] All SQL uses parameterized queries
- [ ] All user input is validated
- [ ] All output is encoded for context
- [ ] Access control checks on every endpoint
- [ ] No hardcoded secrets in code
- [ ] Sensitive data is encrypted
- [ ] Errors don't expose internal info
- [ ] Logging doesn't include sensitive data

## Questions to Ask

For new features:
1. "What data does this access?"
2. "Who should be able to use this?"
3. "What could go wrong if misused?"

For data handling:
1. "Is this data sensitive?"
2. "Do we really need to store this?"
3. "How long do we keep it?"
