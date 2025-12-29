---
name: api-monitoring
description: API monitoring best practices based on Google SRE. Use when implementing health checks, metrics, alerting, or distributed tracing.
---

# API Monitoring

> **Domain Experts**: Google SRE Book, Datadog Documentation, Prometheus Best Practices

## Monitor the Four Golden Signals

Per Google SRE:

| Signal | Description | Example Metrics |
|--------|-------------|-----------------|
| Latency | Time to service a request | p50, p95, p99 response times |
| Traffic | Demand on the system | Requests per second |
| Errors | Rate of failed requests | 5xx rate, error percentage |
| Saturation | System capacity usage | CPU, memory, connection pool |

```yaml
# Prometheus metrics example
api_request_duration_seconds:
  type: histogram
  buckets: [0.01, 0.05, 0.1, 0.25, 0.5, 1, 2.5, 5, 10]
  labels: [method, endpoint, status]

api_requests_total:
  type: counter
  labels: [method, endpoint, status]
```

## Implement Health Check Endpoints

```python
@app.get("/health/live")
async def liveness():
    """Basic liveness check - is the process running?"""
    return {"status": "alive"}

@app.get("/health/ready")
async def readiness(db: Session = Depends(get_db)):
    """Readiness check - can the service handle requests?"""
    checks = {
        "database": await check_database(db),
        "cache": await check_redis(),
        "external_api": await check_external_service()
    }

    all_healthy = all(checks.values())
    status_code = 200 if all_healthy else 503

    return JSONResponse(
        status_code=status_code,
        content={"status": "ready" if all_healthy else "not_ready", "checks": checks}
    )
```

## Define and Monitor SLIs/SLOs

```yaml
availability:
  target: 99.9%
  measurement: |
    1 - (error_requests / total_requests)

latency:
  target: 95% of requests < 200ms
  measurement: |
    histogram_quantile(0.95, api_request_duration_seconds)
```

## Use Distributed Tracing

```python
from opentelemetry import trace
from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor

tracer = trace.get_tracer(__name__)
FastAPIInstrumentor.instrument_app(app)

@app.post("/orders")
async def create_order(order: OrderRequest):
    with tracer.start_as_current_span("create_order") as span:
        span.set_attribute("order.customer_id", order.customer_id)

        with tracer.start_as_current_span("save_to_database"):
            result = await db.save(order)

        return result
```

## Set Up Meaningful Alerts

Alert on symptoms, not causes.

```yaml
# Good: Alert on user-facing symptoms
- alert: HighErrorRate
  expr: |
    sum(rate(api_requests_total{status=~"5.."}[5m]))
    /
    sum(rate(api_requests_total[5m])) > 0.01
  for: 5m
  annotations:
    summary: "API error rate exceeds 1%"
    runbook: https://wiki.example.com/runbooks/high-error-rate

# Bad: Alert on every single error
- alert: AnyError  # Too noisy!
  expr: increase(api_errors_total[1m]) > 0
```

## Create Actionable Dashboards

```
Dashboard: API Health Overview
|-- Row 1: Key Metrics
|   |-- Request Rate (current vs 24h ago)
|   |-- Error Rate (%)
|   |-- P95 Latency
|   |-- Active Users
|-- Row 2: Traffic Breakdown
|   |-- Requests by Endpoint
|   |-- Requests by Status Code
|-- Row 3: Dependencies
    |-- Database Query Time
    |-- External API Latency
    |-- Cache Hit Rate
```

## Quick Reference

| Practice | Rule |
|----------|------|
| Signals | Four Golden: Latency, Traffic, Errors, Saturation |
| Health | Liveness and Readiness endpoints |
| SLOs | Define availability and latency targets |
| Tracing | Distributed tracing across services |
| Alerts | Symptom-based, not cause-based |
| Dashboards | Actionable, answering specific questions |
