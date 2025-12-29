---
name: database-practices
description: Database design, queries, and optimization practices. Use when working with databases, writing SQL, or designing schemas.
---

# Database Practices Skill

Apply these practices for database work.

## Core Rules

1. **Version control all schema changes** - Migrations, not manual DDL
2. **Always use parameterized queries** - Never concatenate user input
3. **Index for common queries** - WHERE, JOIN, ORDER BY columns
4. **Use transactions for related operations** - All succeed or all fail
5. **Use connection pooling** - Don't create connections per request

## SQL Injection Prevention

```java
// NEVER: Concatenation
String sql = "SELECT * FROM users WHERE id = " + userId;

// ALWAYS: Parameterized
PreparedStatement stmt = conn.prepareStatement(
    "SELECT * FROM users WHERE id = ?");
stmt.setLong(1, userId);
```

## Migration Pattern

```sql
-- V001__create_users_table.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- V002__add_user_status.sql
ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'active';
```

## Index Strategy

### When to Index
- Foreign key columns
- Columns in WHERE clauses
- Columns in JOIN conditions
- Columns in ORDER BY

### Index Types
```sql
-- Single column
CREATE INDEX idx_users_email ON users(email);

-- Composite (order matters!)
CREATE INDEX idx_orders_user_date ON orders(user_id, created_at);

-- Partial (for filtered queries)
CREATE INDEX idx_active_users ON users(email) WHERE status = 'active';
```

## Transaction Patterns

```java
@Transactional
public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
    Account from = accountRepo.findById(fromId).orElseThrow();
    Account to = accountRepo.findById(toId).orElseThrow();

    from.setBalance(from.getBalance().subtract(amount));
    to.setBalance(to.getBalance().add(amount));

    accountRepo.save(from);
    accountRepo.save(to);
    // If exception thrown, both rolled back
}
```

## Connection Pool Configuration

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
```

## Query Optimization Checklist

- [ ] Using EXPLAIN to verify query plan
- [ ] Indexes on filtered columns
- [ ] Avoiding SELECT * (select needed columns)
- [ ] Limiting result sets with LIMIT
- [ ] Using pagination for large results
- [ ] Avoiding N+1 queries (use JOIN or batch)

## Questions to Ask

For schema design:
1. "What are the access patterns?"
2. "What are the relationships?"
3. "What needs to be indexed?"

For queries:
1. "Does EXPLAIN show index usage?"
2. "Is this query inside a loop? (N+1)"
3. "Should this be paginated?"

For migrations:
1. "Is this reversible?"
2. "What's the rollback plan?"
3. "Will this lock the table?"
