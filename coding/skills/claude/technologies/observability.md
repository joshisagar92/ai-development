---
name: observability
description: Observability best practices for Elasticsearch, Splunk, and Datadog. Use when implementing logging, monitoring, or distributed tracing.
---

# Observability: Elasticsearch, Splunk & Datadog

## Elasticsearch

> **Domain Experts**: Clinton Gormley & Zachary Tong ("Elasticsearch: The Definitive Guide")

### Model Data for Queries

Denormalize for query performance.

```json
// Good: Denormalized for efficient queries
{
  "order_id": 123,
  "customer_name": "John Doe",
  "product_name": "Widget"
}
```

### Use Bulk API for Indexing

```python
from elasticsearch.helpers import bulk

actions = [
    {"_index": "products", "_source": doc}
    for doc in documents
]
bulk(es_client, actions)
```

### Set Explicit Mappings

Don't rely on dynamic mapping in production.

```json
PUT /products
{
  "mappings": {
    "properties": {
      "name": { "type": "text" },
      "price": { "type": "float" },
      "created_at": { "type": "date" }
    }
  }
}
```

### Use Aliases for Zero-Downtime Reindexing

```json
POST /_aliases
{
  "actions": [
    { "remove": { "index": "products_v1", "alias": "products" }},
    { "add": { "index": "products_v2", "alias": "products" }}
  ]
}
```

## Splunk

> **Domain Expert**: David Carasso (Splunk Chief Mind, SPL Creator)

### Optimize Queries with Time Constraints

Always include a time range.

```spl
index=main earliest=-24h latest=now
| stats count by source
```

### Filter Early in the Pipeline

```spl
// Good: Filter first
index=main sourcetype=access_combined status=500
| stats count by uri

// Bad: Filter after stats
index=main sourcetype=access_combined
| stats count by uri, status
| where status=500
```

### Use Indexed Fields for Filtering

```spl
// Efficient: Uses indexed field
index=web sourcetype=nginx

// Less efficient: Searches all events
... | where sourcetype="nginx"
```

## Datadog

> **Domain Expert**: Datadog Official Documentation

### Use Unified Tagging

Consistent tags across metrics, traces, and logs.

```yaml
DD_SERVICE: payment-service
DD_ENV: production
DD_VERSION: 1.2.3
```

### Implement APM Tracing

```python
from ddtrace import tracer

@tracer.wrap()
def process_payment(order_id):
    # Automatically traced
    pass
```

### Connect Logs to Traces

Inject trace IDs into logs.

```python
import logging
from ddtrace import tracer

FORMAT = '%(asctime)s %(levelname)s [dd.trace_id=%(dd.trace_id)s] %(message)s'
logging.basicConfig(format=FORMAT)
```

### Set Meaningful SLOs

```yaml
slo:
  name: "Payment Success Rate"
  type: metric
  numerator: sum:payment.success{*}
  denominator: sum:payment.requests{*}
  target: 99.9
```

## Quick Reference

| Tool | Practice | Rule |
|------|----------|------|
| Elasticsearch | Indexing | Use Bulk API |
| Elasticsearch | Mappings | Set explicit, not dynamic |
| Elasticsearch | Updates | Use aliases for zero-downtime |
| Splunk | Queries | Always include time range |
| Splunk | Performance | Filter early in pipeline |
| Datadog | Tags | Unified tagging across all data |
| Datadog | Tracing | APM with trace ID in logs |
| Datadog | SLOs | Define for critical paths |
