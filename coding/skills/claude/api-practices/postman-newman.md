---
name: postman-newman
description: Postman and Newman best practices for API testing. Use when creating collections, writing tests, or setting up CI/CD API testing.
---

# Postman & Newman

> **Domain Expert**: Postman Official Documentation, Newman GitHub Repository

## Structure Collections by Domain/Feature

```
E-Commerce API
|-- Authentication
|   |-- POST Login
|   |-- POST Register
|   |-- POST Refresh Token
|-- Users
|   |-- GET Current User
|   |-- PUT Update Profile
|-- Orders
|   |-- GET List Orders
|   |-- POST Create Order
```

## Use Environments for Different Stages

```json
// Development.postman_environment.json
{
  "name": "Development",
  "values": [
    { "key": "baseUrl", "value": "http://localhost:8080/api" },
    { "key": "authToken", "value": "", "type": "secret" }
  ]
}

// Production.postman_environment.json
{
  "name": "Production",
  "values": [
    { "key": "baseUrl", "value": "https://api.example.com" },
    { "key": "authToken", "value": "", "type": "secret" }
  ]
}
```

## Use Variables Effectively

```javascript
// Request URL
{{baseUrl}}/users/{{userId}}

// Local variables in pre-request scripts
pm.variables.set("timestamp", Date.now());
pm.variables.set("randomEmail", `test_${Date.now()}@example.com`);
```

## Authentication Pre-Request Script

```javascript
const tokenExpiry = pm.environment.get("tokenExpiry");
const currentTime = Date.now();

if (!tokenExpiry || currentTime > tokenExpiry) {
    pm.sendRequest({
        url: pm.environment.get("baseUrl") + "/auth/login",
        method: "POST",
        header: { "Content-Type": "application/json" },
        body: {
            mode: "raw",
            raw: JSON.stringify({
                email: pm.environment.get("testEmail"),
                password: pm.environment.get("testPassword")
            })
        }
    }, (err, response) => {
        const jsonResponse = response.json();
        pm.environment.set("authToken", jsonResponse.accessToken);
        pm.environment.set("tokenExpiry", currentTime + (jsonResponse.expiresIn * 1000) - 60000);
    });
}
```

## Comprehensive Response Validation

```javascript
// Test status code
pm.test("Status code is 200", () => {
    pm.response.to.have.status(200);
});

// Test response time
pm.test("Response time is acceptable", () => {
    pm.expect(pm.response.responseTime).to.be.below(500);
});

// Test response structure
pm.test("Response has required fields", () => {
    const response = pm.response.json();
    pm.expect(response).to.have.property("id");
    pm.expect(response).to.have.property("email");
});

// Test data types
pm.test("Data types are correct", () => {
    const response = pm.response.json();
    pm.expect(response.id).to.be.a("number");
    pm.expect(response.email).to.be.a("string");
});
```

## JSON Schema Validation

```javascript
const userSchema = {
    type: "object",
    required: ["id", "email", "name"],
    properties: {
        id: { type: "integer" },
        email: { type: "string", format: "email" },
        name: { type: "string", minLength: 1 }
    }
};

pm.test("Response matches schema", () => {
    const response = pm.response.json();
    pm.expect(tv4.validate(response, userSchema)).to.be.true;
});
```

## Newman CLI Integration

```bash
# Run collection with environment
newman run collection.json -e environment.json

# Generate HTML report
newman run collection.json -e environment.json \
    -r htmlextra \
    --reporter-htmlextra-export ./reports/report.html

# Fail on any test failure (for CI)
newman run collection.json -e environment.json --bail
```

## CI/CD Integration

```yaml
# GitHub Actions
- name: Install Newman
  run: npm install -g newman newman-reporter-htmlextra

- name: Run API Tests
  run: |
    newman run ./postman/collection.json \
      -e ./postman/staging.environment.json \
      -r cli,htmlextra \
      --reporter-htmlextra-export ./reports/api-test-report.html \
      --bail

- name: Upload Test Report
  uses: actions/upload-artifact@v3
  if: always()
  with:
    name: api-test-report
    path: ./reports/api-test-report.html
```

## Quick Reference

| Practice | Rule |
|----------|------|
| Organization | By feature/domain, not HTTP method |
| Environments | Separate for each stage |
| Variables | Environment for secrets, collection for constants |
| Auth | Pre-request script for token refresh |
| Testing | Status, time, structure, schema |
| CI/CD | Newman with --bail flag |
| Reports | HTML for visibility |
