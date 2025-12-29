---
name: api-documentation
description: API documentation best practices using OpenAPI/Swagger. Use when documenting APIs, writing OpenAPI specs, or designing API contracts.
---

# API Documentation (OpenAPI/Swagger)

> **Domain Experts**: Darrel Miller (OpenAPI TSC), Tony Tam (Swagger Creator), OpenAPI Initiative

## Use OpenAPI Specification (OAS) 3.x

```yaml
openapi: 3.0.3
info:
  title: User Management API
  description: API for managing user accounts
  version: 1.0.0
  contact:
    name: API Support
    email: api-support@example.com
```

## Design-First, Not Code-First

Write your OpenAPI specification before implementing:
- Early stakeholder review
- Parallel frontend/backend development
- Contract testing
- Better API design

## Provide Comprehensive Info Section

```yaml
info:
  title: Payment Processing API
  description: |
    # Overview
    This API handles payment processing for e-commerce transactions.

    ## Authentication
    All endpoints require Bearer token authentication.

    ## Rate Limits
    - Standard tier: 100 requests/minute
    - Premium tier: 1000 requests/minute
  version: 2.1.0
  termsOfService: https://example.com/terms
  contact:
    name: API Team
    email: api@example.com
```

## Use Semantic Versioning

```yaml
info:
  version: 2.1.0  # major.minor.patch
servers:
  - url: https://api.example.com/v2
    description: Production server
```

## Document All Response Codes

```yaml
responses:
  '200':
    description: Successful response
    content:
      application/json:
        schema:
          $ref: '#/components/schemas/User'
  '400':
    description: Invalid user ID format
  '401':
    description: Authentication required
  '403':
    description: Insufficient permissions
  '404':
    description: User not found
  '500':
    description: Internal server error
```

## Use Components for Reusability

```yaml
components:
  schemas:
    User:
      type: object
      required:
        - id
        - email
      properties:
        id:
          type: integer
          format: int64
        email:
          type: string
          format: email

    Error:
      type: object
      required:
        - code
        - message
      properties:
        code:
          type: string
        message:
          type: string

  parameters:
    UserIdParam:
      name: userId
      in: path
      required: true
      schema:
        type: integer
```

## Provide Realistic Examples

```yaml
example:
  orderId: "ORD-2024-001234"
  items:
    - productId: "PROD-001"
      quantity: 2
      price: 29.99
  total: 109.97
```

## Document Authentication

```yaml
security:
  - BearerAuth: []

paths:
  /public/health:
    get:
      security: []  # Override: no auth required
```

## Use Tags for Organization

```yaml
tags:
  - name: Users
    description: User management operations
  - name: Orders
    description: Order processing operations
```

## Quick Reference

| Practice | Rule |
|----------|------|
| Spec Version | OpenAPI 3.x |
| Approach | Design-first |
| Versioning | Semantic (major.minor.patch) |
| Responses | Document all status codes |
| Reuse | Use components |
| Examples | Provide realistic data |
| Auth | Document security schemes |
| Organization | Use tags |
