---
name: kubernetes-aks
description: Kubernetes and Azure Kubernetes Service best practices. Use when deploying to Kubernetes, writing manifests, or configuring AKS.
---

# Kubernetes & Azure Kubernetes Service (AKS)

> **Domain Experts**: Kelsey Hightower (Kubernetes Evangelist), Brendan Burns (Kubernetes Co-founder), Nigel Poulton ("The Kubernetes Book")

## Use Namespaces for Isolation

Separate environments and teams.

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: production
---
apiVersion: v1
kind: Namespace
metadata:
  name: development
```

## Set Resource Requests and Limits

Prevent resource starvation and enable proper scheduling.

```yaml
resources:
  requests:
    memory: "256Mi"
    cpu: "100m"
  limits:
    memory: "512Mi"
    cpu: "500m"
```

## Use Liveness and Readiness Probes

Kubernetes can manage pod lifecycle correctly.

```yaml
livenessProbe:
  httpGet:
    path: /health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
readinessProbe:
  httpGet:
    path: /ready
    port: 8080
  initialDelaySeconds: 5
  periodSeconds: 5
```

## Use ConfigMaps and Secrets

Never hardcode configuration or credentials.

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  LOG_LEVEL: "INFO"
---
apiVersion: v1
kind: Secret
metadata:
  name: db-credentials
type: Opaque
data:
  password: cGFzc3dvcmQxMjM=  # base64 encoded
```

## Use Pod Disruption Budgets

Maintain availability during upgrades.

```yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: app-pdb
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: my-app
```

## Use Horizontal Pod Autoscaler

Scale based on actual demand.

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: app-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: my-app
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

## Use Network Policies

Limit pod-to-pod communication.

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: deny-all
spec:
  podSelector: {}
  policyTypes:
  - Ingress
  - Egress
```

## Quick Reference

| Practice | Rule |
|----------|------|
| Isolation | Use namespaces |
| Resources | Set requests and limits |
| Health | Liveness and readiness probes |
| Config | ConfigMaps and Secrets |
| Availability | Pod Disruption Budgets |
| Scaling | Horizontal Pod Autoscaler |
| Security | Network Policies |
