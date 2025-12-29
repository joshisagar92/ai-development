---
name: jdbc-pooling
description: JDBC and connection pooling best practices. Use when working with databases, configuring HikariCP, or writing SQL queries.
---

# JDBC & Connection Pooling

> **Domain Experts**: Brett Wooldridge (HikariCP Creator), Vlad Mihalcea (Hibernate Expert)

## Always Use Connection Pooling

Connection creation is expensive (TCP handshake, SSL, auth).

```java
// Good: Use HikariCP (Spring Boot default since 2.0)
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
config.setMaximumPoolSize(10);
HikariDataSource dataSource = new HikariDataSource(config);

// Bad: DriverManager per request
Connection conn = DriverManager.getConnection(url, user, password);
```

## Don't Cache PreparedStatements at Pool Layer

Per Brett Wooldridge: "Using a statement cache at the pooling layer is an anti-pattern."

```properties
# Let the driver handle caching, not the pool
spring.datasource.hikari.data-source-properties.cachePrepStmts=true
spring.datasource.hikari.data-source-properties.prepStmtCacheSize=250
spring.datasource.hikari.data-source-properties.prepStmtCacheSqlLimit=2048
```

## Right-Size Your Pool

Formula: `connections = ((core_count * 2) + effective_spindle_count)`

For most applications, 10-20 connections is optimal.

```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

## Always Use PreparedStatements

Prevents SQL injection and improves performance.

```java
// Good: PreparedStatement
String sql = "SELECT * FROM users WHERE id = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setInt(1, userId);

// Bad: String concatenation (SQL injection vulnerability)
String sql = "SELECT * FROM users WHERE id = " + userId;
```

## Close Resources Properly

Use try-with-resources.

```java
try (Connection conn = dataSource.getConnection();
     PreparedStatement pstmt = conn.prepareStatement(sql);
     ResultSet rs = pstmt.executeQuery()) {
    while (rs.next()) {
        // Process results
    }
} // Resources automatically closed
```

## Use Batch Operations for Bulk Inserts

Reduces network round-trips dramatically.

```java
conn.setAutoCommit(false);
PreparedStatement pstmt = conn.prepareStatement("INSERT INTO items VALUES (?, ?)");
for (Item item : items) {
    pstmt.setInt(1, item.getId());
    pstmt.setString(2, item.getName());
    pstmt.addBatch();
}
pstmt.executeBatch();
conn.commit();
```

## Configure Connection Validation

Ensure connections are valid before use.

```properties
spring.datasource.hikari.connection-test-query=SELECT 1
spring.datasource.hikari.validation-timeout=5000
```

## Quick Reference

| Practice | Rule |
|----------|------|
| Pooling | Always use HikariCP |
| Pool Size | 10-20 connections typical |
| Statements | Always PreparedStatement |
| Resources | Always try-with-resources |
| Bulk Ops | Use batch operations |
| Caching | Let driver cache, not pool |
