# Project Requirements Questionnaire
## Comprehensive Discovery Questions for Software Projects

**Version:** 1.0  
**Date:** December 2024  
**Purpose:** Master checklist of questions to ask when starting a new project or adding features to an existing system

---

## How to Use This Document

This questionnaire is organized by domain/aspect. For each new project or feature:
1. Review applicable sections based on project scope
2. Ask relevant questions during discovery/planning
3. Document answers in your specification
4. Identify gaps that need further clarification
5. Use answers to inform architecture and design decisions

---

# 1. Third-Party API Integration

## 1.1 API Discovery & Documentation

| # | Question | Why It Matters |
|---|----------|----------------|
| 1 | What is the primary purpose of the API and how does it align with our requirements? | Ensures fit for purpose |
| 2 | What API type is offered (REST, SOAP, GraphQL, gRPC)? | Determines integration approach |
| 3 | Is comprehensive API documentation available? Where? | Critical for development speed |
| 4 | Are there code samples, SDKs, or client libraries available? | Reduces development time |
| 5 | Is there a sandbox/staging environment for testing? | Essential for safe development |
| 6 | What authentication method is used (OAuth, API keys, JWT, mTLS)? | Security implementation |
| 7 | Is the API versioned? What is the versioning strategy (URL, header, query)? | Long-term maintainability |
| 8 | What data formats are supported (JSON, XML, CSV)? | Data handling requirements |

## 1.2 Rate Limits & Quotas

| # | Question | Why It Matters |
|---|----------|----------------|
| 9 | What are the rate limits (requests per second/minute/hour/day)? | Capacity planning |
| 10 | Are rate limits different per endpoint type? | Granular planning |
| 11 | Are rate limits negotiable based on usage/subscription tier? | Cost vs. capacity tradeoff |
| 12 | What HTTP headers indicate rate limit status (X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset)? | Implementation details |
| 13 | What happens when rate limits are exceeded (429 response, throttling, queuing)? | Error handling strategy |
| 14 | Is there a burst allowance or token bucket mechanism? | Peak load handling |
| 15 | Are there concurrent request limits? | Parallel processing constraints |

## 1.3 SLA & Reliability

| # | Question | Why It Matters |
|---|----------|----------------|
| 16 | What is the documented SLA (99.9%, 99.99%)? | Reliability expectations |
| 17 | Are there penalties/credits for SLA breaches? | Business protection |
| 18 | What is the scheduled maintenance window? | Planned downtime handling |
| 19 | How are unscheduled outages communicated (status page, email, webhook)? | Incident response |
| 20 | What was the historical uptime over the last 12 months? | Track record validation |
| 21 | Is there geographic redundancy/failover? | Disaster recovery |

## 1.4 Beta Features & Limitations

| # | Question | Why It Matters |
|---|----------|----------------|
| 22 | Which features are in beta/preview status? | Production readiness |
| 23 | What features are deprecated or scheduled for removal? | Future-proofing |
| 24 | Are there known limitations or bugs? | Risk assessment |
| 25 | What is the roadmap for new features? | Strategic alignment |
| 26 | Are breaking changes communicated in advance? How much notice? | Change management |

## 1.5 Troubleshooting & Support

| # | Question | Why It Matters |
|---|----------|----------------|
| 27 | What support channels are available (email, chat, phone, ticketing)? | Issue resolution |
| 28 | What is the response time SLA for different severity levels? | Incident planning |
| 29 | Is 24/7 support available? | Critical issue coverage |
| 30 | Are detailed error codes and messages documented? | Debugging efficiency |
| 31 | Is there a community forum or knowledge base? | Self-service support |
| 32 | Are there diagnostic/health endpoints available? | Monitoring capability |

## 1.6 Logging & Monitoring

| # | Question | Why It Matters |
|---|----------|----------------|
| 33 | Does the API provide request/response logging? | Audit trail |
| 34 | Is there a usage analytics dashboard? | Consumption visibility |
| 35 | Can we receive webhook notifications for events/errors? | Proactive monitoring |
| 36 | What tracing/correlation ID mechanisms exist? | Distributed tracing |
| 37 | How long are API logs retained by the provider? | Historical analysis |

## 1.7 Scaling & Performance

| # | Question | Why It Matters |
|---|----------|----------------|
| 38 | What is the expected response time for different endpoint types? | Performance budgeting |
| 39 | Does performance degrade under high load? | Peak capacity planning |
| 40 | Is there auto-scaling or do we need to request capacity increases? | Growth planning |
| 41 | Are there batch/bulk operation endpoints? | Efficiency optimization |
| 42 | Is pagination supported? What is max page size? | Large dataset handling |
| 43 | Is there response compression (gzip, brotli)? | Bandwidth optimization |

## 1.8 Automation & Integration

| # | Question | Why It Matters |
|---|----------|----------------|
| 44 | Are there existing integrations with our tech stack (Zapier, Make, native connectors)? | Build vs. buy decision |
| 45 | Is there webhook support for real-time updates? | Push vs. poll architecture |
| 46 | Can API access be restricted by IP/location? | Security controls |
| 47 | Does the API support idempotency keys? | Safe retries |
| 48 | Is there support for long-polling or streaming? | Real-time data needs |

## 1.9 Cost & Billing

| # | Question | Why It Matters |
|---|----------|----------------|
| 49 | What is the pricing model (per request, per user, tiered, flat)? | Budget planning |
| 50 | Are there overage charges? What are the rates? | Cost control |
| 51 | Is there a free tier? What are the limits? | Development/testing costs |
| 52 | How are costs tracked and reported? | FinOps visibility |
| 53 | Are there volume discounts or committed use discounts? | Cost optimization |
| 54 | What happens if we exceed our plan limits? | Business continuity |

## 1.10 Security & Compliance

| # | Question | Why It Matters |
|---|----------|----------------|
| 55 | What encryption standards are used (TLS 1.2+, at-rest encryption)? | Security compliance |
| 56 | Does the API vendor have SOC 2/ISO 27001/GDPR compliance? | Regulatory requirements |
| 57 | How is sensitive data handled and protected? | Data protection |
| 58 | Is there a vulnerability disclosure program? | Security posture |
| 59 | How quickly are security patches applied? | Risk management |
| 60 | Does the API rely on other third-party services (supply chain risk)? | Dependency assessment |

---

# 2. Data Retention, Archiving & Purging

## 2.1 Regulatory Requirements

| # | Question | Why It Matters |
|---|----------|----------------|
| 1 | What regulations apply to our data (GDPR, HIPAA, SOX, PCI-DSS, CCPA)? | Compliance mandates |
| 2 | What are the minimum retention periods required by regulation? | Legal compliance |
| 3 | What are the maximum retention periods allowed? | GDPR storage limitation |
| 4 | Are there industry-specific retention requirements? | Sector compliance |
| 5 | How do retention requirements vary by data type (financial, medical, PII)? | Granular policy |
| 6 | What are the penalties for non-compliance? | Risk assessment |

## 2.2 Data Classification

| # | Question | Why It Matters |
|---|----------|----------------|
| 7 | What data categories exist in the system (PII, financial, operational, logs)? | Policy granularity |
| 8 | How is data classified and tagged? | Automation capability |
| 9 | Who owns each data category? | Accountability |
| 10 | What is the sensitivity level of each data type? | Security requirements |
| 11 | Is there cross-border data transfer? Which jurisdictions apply? | International compliance |

## 2.3 Retention Periods

| # | Question | Why It Matters |
|---|----------|----------------|
| 12 | What is the retention period for each data category? | Policy definition |
| 13 | Is retention based on creation date, last modified, or last accessed? | Retention logic |
| 14 | Should any data be retained indefinitely? Why? | Exception handling |
| 15 | How do contractual obligations affect retention? | Business requirements |
| 16 | What triggers the start of the retention period? | Implementation clarity |

## 2.4 Archiving Strategy

| # | Question | Why It Matters |
|---|----------|----------------|
| 17 | What data should be archived vs. immediately deleted? | Cost vs. accessibility |
| 18 | Where should archived data be stored (cold storage, tape, cloud archive)? | Infrastructure planning |
| 19 | How quickly must archived data be retrievable if needed? | Recovery time objectives |
| 20 | What format should archived data be stored in? | Long-term accessibility |
| 21 | Should archived data be compressed/encrypted? | Storage optimization/security |
| 22 | How long should archived data be retained before final deletion? | Full lifecycle |

## 2.5 Purging & Deletion

| # | Question | Why It Matters |
|---|----------|----------------|
| 23 | How should data be securely deleted (logical delete, crypto-shredding, physical destruction)? | Security compliance |
| 24 | How do we handle the "right to be forgotten" requests? | GDPR Article 17 |
| 25 | What is the SLA for processing deletion requests? | Legal compliance |
| 26 | How is deletion verified and certified? | Audit trail |
| 27 | How do we handle data in backups after deletion? | Complete erasure |
| 28 | What about data shared with third parties? | Data processor obligations |

## 2.6 Legal Hold

| # | Question | Why It Matters |
|---|----------|----------------|
| 29 | How do we implement legal hold to pause automated deletion? | Litigation readiness |
| 30 | Who can initiate and release a legal hold? | Process governance |
| 31 | How do we track data under legal hold? | Audit requirements |
| 32 | How long might legal holds last? | Storage planning |

## 2.7 Implementation & Automation

| # | Question | Why It Matters |
|---|----------|----------------|
| 33 | Will retention/archiving/purging be automated or manual? | Operational efficiency |
| 34 | What tools/systems will enforce retention policies? | Technology selection |
| 35 | How often should retention jobs run? | Scheduling |
| 36 | How will policy violations be detected and reported? | Compliance monitoring |
| 37 | How often should the retention policy be reviewed and updated? | Governance |

---

# 3. Resource Capacity Planning

## 3.1 Current State Assessment

| # | Question | Why It Matters |
|---|----------|----------------|
| 1 | What is the current resource utilization (CPU, memory, storage, network)? | Baseline measurement |
| 2 | What are the peak usage times and patterns? | Demand forecasting |
| 3 | What is the current user base size? | Scale baseline |
| 4 | What is the current transaction/request volume? | Throughput baseline |
| 5 | What is the current data growth rate? | Storage forecasting |
| 6 | Are there seasonal or cyclical patterns in usage? | Demand modeling |

## 3.2 Growth Projections

| # | Question | Why It Matters |
|---|----------|----------------|
| 7 | What is the expected user growth rate (monthly, yearly)? | Capacity forecasting |
| 8 | What is the expected data growth rate? | Storage planning |
| 9 | Are there any planned features that will increase load? | Demand anticipation |
| 10 | Are there any planned marketing campaigns or launches? | Spike planning |
| 11 | What is the 1-year, 3-year, 5-year growth projection? | Long-term planning |

## 3.3 Performance Requirements

| # | Question | Why It Matters |
|---|----------|----------------|
| 12 | What is the target response time for key operations? | Performance SLA |
| 13 | What is the maximum acceptable latency (P50, P95, P99)? | Performance targets |
| 14 | What is the target throughput (requests per second)? | Capacity targets |
| 15 | What is the target availability (99.9%, 99.99%)? | Reliability requirements |
| 16 | What is the acceptable error rate? | Quality thresholds |

## 3.4 Resource Limits

| # | Question | Why It Matters |
|---|----------|----------------|
| 17 | What are the limits of current infrastructure? | Headroom assessment |
| 18 | What are the database connection pool limits? | Connection management |
| 19 | What are the storage limits (disk, object storage, database)? | Storage planning |
| 20 | What are the network bandwidth limits? | Network planning |
| 21 | What are the API/service rate limits (internal and external)? | Integration planning |

## 3.5 Scaling Strategy

| # | Question | Why It Matters |
|---|----------|----------------|
| 22 | Will scaling be horizontal (more instances) or vertical (bigger instances)? | Architecture decisions |
| 23 | Is auto-scaling available and configured? | Automated response |
| 24 | What triggers scaling events (CPU, memory, queue depth, custom metrics)? | Scaling policies |
| 25 | What is the minimum and maximum scale? | Cost and capacity bounds |
| 26 | How quickly can new capacity be provisioned? | Response time |
| 27 | Are there any components that cannot scale? | Bottleneck identification |

## 3.6 Cost Considerations

| # | Question | Why It Matters |
|---|----------|----------------|
| 28 | What is the current infrastructure cost? | Budget baseline |
| 29 | How does cost scale with usage? | Cost modeling |
| 30 | Are there reserved instance or committed use discounts available? | Cost optimization |
| 31 | What is the budget for infrastructure growth? | Financial constraints |
| 32 | How are costs attributed/charged back to business units? | FinOps |

## 3.7 Capacity Thresholds & Alerts

| # | Question | Why It Matters |
|---|----------|----------------|
| 33 | At what utilization level should warnings be triggered (70%, 80%)? | Proactive management |
| 34 | At what level should critical alerts be triggered (90%, 95%)? | Incident prevention |
| 35 | Who should receive capacity alerts? | Incident response |
| 36 | How far in advance should capacity additions be planned? | Lead time |
| 37 | What is the process for requesting additional capacity? | Governance |

---

# 4. Network Architecture & Security

## 4.1 Network Topology

| # | Question | Why It Matters |
|---|----------|----------------|
| 1 | What is the overall network architecture (hub-spoke, mesh, tiered)? | Design understanding |
| 2 | How many network zones/segments exist (DMZ, internal, management)? | Security zones |
| 3 | What VLANs/subnets are defined? | Network segmentation |
| 4 | Is there network segmentation between environments (dev, staging, prod)? | Environment isolation |
| 5 | What is the IP addressing scheme? | Network planning |
| 6 | Is IPv6 supported/required? | Future-proofing |

## 4.2 Port & Protocol Requirements

| # | Question | Why It Matters |
|---|----------|----------------|
| 7 | What ports need to be open for the application (HTTP:80, HTTPS:443, etc.)? | Firewall rules |
| 8 | What database ports are required (MySQL:3306, MSSQL:1433, PostgreSQL:5432)? | Database access |
| 9 | What ports are needed for messaging/queues (RabbitMQ:5672, Kafka:9092)? | Integration requirements |
| 10 | What ports are needed for caching (Redis:6379, Memcached:11211)? | Cache access |
| 11 | What ports are needed for monitoring/management (SSH:22, RDP:3389)? | Administrative access |
| 12 | Are any non-standard ports required? | Custom configuration |

## 4.3 DNS Requirements

| # | Question | Why It Matters |
|---|----------|----------------|
| 13 | What DNS records need to be created (A, CNAME, MX, TXT)? | DNS configuration |
| 14 | What is the TTL strategy for DNS records? | Failover/migration speed |
| 15 | Is internal DNS resolution required? | Service discovery |
| 16 | Are there split-horizon DNS requirements? | Internal vs. external |
| 17 | What DNS provider will be used? | Infrastructure decision |
| 18 | Is DNSSEC required? | Security requirement |

## 4.4 Inbound Traffic Rules

| # | Question | Why It Matters |
|---|----------|----------------|
| 19 | What external traffic should be allowed (public internet, specific IPs, VPN only)? | Access control |
| 20 | Are there geographic restrictions on inbound traffic? | Geo-blocking |
| 21 | What load balancer configuration is needed? | Traffic distribution |
| 22 | Is SSL/TLS termination at load balancer or application? | Certificate management |
| 23 | What health check endpoints are required? | Load balancer configuration |
| 24 | Is DDoS protection required? | Security requirement |

## 4.5 Outbound Traffic Rules

| # | Question | Why It Matters |
|---|----------|----------------|
| 25 | What external services/APIs does the application call? | Outbound dependencies |
| 26 | Should outbound traffic be restricted to specific destinations? | Security posture |
| 27 | Is a proxy required for outbound traffic? | Traffic inspection |
| 28 | Are there compliance requirements for egress filtering? | Regulatory compliance |
| 29 | What DNS servers should be used for outbound resolution? | Network configuration |

## 4.6 Firewall Configuration

| # | Question | Why It Matters |
|---|----------|----------------|
| 30 | What is the default firewall policy (deny all, allow all)? | Security baseline |
| 31 | What firewall rules are needed between network zones? | Zone security |
| 32 | Are stateful inspection rules required? | Deep inspection |
| 33 | What logging level is required for firewall events? | Audit requirements |
| 34 | How are firewall changes reviewed and approved? | Change management |
| 35 | Is there a firewall rule expiration/review policy? | Rule hygiene |

## 4.7 VPN & Remote Access

| # | Question | Why It Matters |
|---|----------|----------------|
| 36 | Is VPN access required? What type (site-to-site, client VPN)? | Remote access |
| 37 | What authentication is required for VPN (MFA, certificates)? | VPN security |
| 38 | What resources are accessible via VPN? | Access scope |
| 39 | Are there split tunneling requirements? | Performance vs. security |
| 40 | What is the VPN capacity requirement (concurrent users)? | Sizing |

---

# 5. Web Application Firewall (WAF)

## 5.1 WAF Requirements

| # | Question | Why It Matters |
|---|----------|----------------|
| 1 | Is a WAF required for compliance (PCI-DSS, SOC 2)? | Regulatory mandate |
| 2 | What type of WAF is preferred (cloud, on-premise, CDN-integrated)? | Architecture decision |
| 3 | What deployment mode is needed (reverse proxy, inline, out-of-band)? | Implementation approach |
| 4 | What is the expected traffic volume? | Sizing |
| 5 | Is there a preference for WAF vendor (AWS WAF, Cloudflare, Imperva, F5, Azure WAF)? | Technology selection |

## 5.2 Protection Rules

| # | Question | Why It Matters |
|---|----------|----------------|
| 6 | Should OWASP Core Rule Set (CRS) be enabled? | Baseline protection |
| 7 | What OWASP Top 10 protections are required (SQL injection, XSS, SSRF, etc.)? | Vulnerability coverage |
| 8 | Are custom rules required for application-specific threats? | Tailored protection |
| 9 | What sensitivity level should rules operate at (paranoia level 1-4)? | False positive balance |
| 10 | Are bot detection/mitigation rules needed? | Bot protection |
| 11 | Is rate limiting/throttling required at WAF level? | DDoS protection |

## 5.3 Rule Management

| # | Question | Why It Matters |
|---|----------|----------------|
| 12 | Who is responsible for WAF rule management? | Ownership |
| 13 | What is the process for adding/modifying rules? | Change management |
| 14 | How often should rules be reviewed and updated? | Rule hygiene |
| 15 | Are automatic rule updates acceptable or must they be reviewed first? | Control level |
| 16 | How will false positives be identified and handled? | Operational process |
| 17 | What is the process for emergency rule deployment? | Incident response |

## 5.4 Logging & Monitoring

| # | Question | Why It Matters |
|---|----------|----------------|
| 18 | What WAF events should be logged? | Visibility requirements |
| 19 | How long should WAF logs be retained? | Compliance/forensics |
| 20 | Where should WAF logs be sent (SIEM, log aggregator)? | Integration |
| 21 | What alerts should be configured for WAF events? | Proactive monitoring |
| 22 | Are real-time dashboards required? | Operational visibility |
| 23 | What reporting is needed for compliance? | Audit requirements |

## 5.5 Exceptions & Allowlists

| # | Question | Why It Matters |
|---|----------|----------------|
| 24 | Are there known false positives that need permanent exceptions? | Operational stability |
| 25 | What IPs/ranges need to be allowlisted (monitoring, partners, office)? | Legitimate access |
| 26 | Are there specific URLs/endpoints that need different rules? | Granular control |
| 27 | How are exceptions documented and reviewed? | Exception governance |

## 5.6 Mode & Response

| # | Question | Why It Matters |
|---|----------|----------------|
| 28 | Should WAF operate in detection-only or blocking mode initially? | Deployment strategy |
| 29 | What is the transition plan from detection to blocking? | Risk management |
| 30 | What response should blocked requests receive (403, custom page)? | User experience |
| 31 | Should attackers be challenged with CAPTCHA before blocking? | Graduated response |

---

# 6. Non-Functional Requirements (NFRs)

## 6.1 Performance

| # | Question | Why It Matters |
|---|----------|----------------|
| 1 | What is the target page load time (initial, subsequent)? | User experience |
| 2 | What is the target API response time (P50, P95, P99)? | Performance SLA |
| 3 | What is the target throughput (requests per second, transactions per minute)? | Capacity requirement |
| 4 | What is the maximum acceptable latency? | Performance ceiling |
| 5 | What is the target time to first byte (TTFB)? | Network performance |
| 6 | What is the page weight budget (HTML, CSS, JS, images)? | Frontend optimization |

## 6.2 Scalability

| # | Question | Why It Matters |
|---|----------|----------------|
| 7 | What is the expected concurrent user load (normal, peak)? | Capacity planning |
| 8 | What is the expected growth rate (users, data, transactions)? | Future planning |
| 9 | Should the system scale horizontally, vertically, or both? | Architecture decision |
| 10 | What components must scale independently? | Microservices design |
| 11 | What is the acceptable scaling response time? | Auto-scaling requirements |

## 6.3 Availability & Reliability

| # | Question | Why It Matters |
|---|----------|----------------|
| 12 | What is the target availability (99.9%, 99.95%, 99.99%)? | Uptime SLA |
| 13 | What is the maximum acceptable downtime per month/year? | Downtime budget |
| 14 | What is the RTO (Recovery Time Objective)? | Disaster recovery |
| 15 | What is the RPO (Recovery Point Objective)? | Data loss tolerance |
| 16 | Is geographic redundancy required? | High availability |
| 17 | What failover mechanisms are needed? | Resilience design |

## 6.4 Security

| # | Question | Why It Matters |
|---|----------|----------------|
| 18 | What authentication mechanisms are required (SSO, MFA, OAuth)? | Access control |
| 19 | What authorization model is needed (RBAC, ABAC, ACL)? | Permission structure |
| 20 | What encryption is required (TLS version, at-rest encryption)? | Data protection |
| 21 | Are there data residency requirements? | Compliance |
| 22 | What security certifications are required (SOC 2, ISO 27001, FedRAMP)? | Compliance mandates |
| 23 | Is penetration testing required? How often? | Security validation |

## 6.5 Usability

| # | Question | Why It Matters |
|---|----------|----------------|
| 24 | What accessibility standards must be met (WCAG 2.1 AA, Section 508)? | Compliance |
| 25 | What browsers/devices must be supported? | Compatibility |
| 26 | What is the target audience's technical proficiency? | UX design |
| 27 | Are internationalization (i18n) and localization (l10n) required? | Global readiness |
| 28 | Is offline capability required? | Resilience |

## 6.6 Maintainability

| # | Question | Why It Matters |
|---|----------|----------------|
| 29 | What is the target code coverage for automated tests? | Quality assurance |
| 30 | What deployment frequency is expected (daily, weekly, monthly)? | DevOps planning |
| 31 | What is the maximum acceptable deployment downtime? | Release planning |
| 32 | What documentation standards are required? | Knowledge transfer |
| 33 | What is the expected system lifespan? | Technical debt management |

## 6.7 Compliance & Audit

| # | Question | Why It Matters |
|---|----------|----------------|
| 34 | What regulations apply (GDPR, HIPAA, PCI-DSS, SOX)? | Compliance scope |
| 35 | What audit logging is required? | Audit trail |
| 36 | How long must audit logs be retained? | Compliance retention |
| 37 | What compliance reports are needed and how often? | Reporting requirements |
| 38 | Are there industry-specific compliance requirements? | Sector regulations |

---

# 7. Logging & Monitoring

## 7.1 Logging Requirements

| # | Question | Why It Matters |
|---|----------|----------------|
| 1 | What events must be logged (access, errors, security, business, audit)? | Log scope |
| 2 | What log format is required (JSON, structured, syslog)? | Standardization |
| 3 | What fields must be included in each log entry? | Log schema |
| 4 | What log levels are needed (DEBUG, INFO, WARN, ERROR, FATAL)? | Verbosity control |
| 5 | How should sensitive data be handled in logs (masking, redaction)? | Security/compliance |
| 6 | What is the log retention period (7 days, 30 days, 1 year)? | Storage/compliance |

## 7.2 Log Aggregation & Storage

| # | Question | Why It Matters |
|---|----------|----------------|
| 7 | What log aggregation tool will be used (ELK, Splunk, Datadog, CloudWatch)? | Technology selection |
| 8 | What is the expected daily log volume (GB/day)? | Capacity planning |
| 9 | How quickly must logs be searchable after generation? | Real-time requirements |
| 10 | Is log archiving to cold storage required? | Cost optimization |
| 11 | Are there geographic restrictions on log storage? | Data residency |
| 12 | What is the log ingestion rate limit? | Throughput planning |

## 7.3 Monitoring & Observability

| # | Question | Why It Matters |
|---|----------|----------------|
| 13 | What metrics must be collected (CPU, memory, disk, network, custom)? | Observability scope |
| 14 | What is the monitoring tool (Datadog, New Relic, Prometheus, Dynatrace)? | Technology selection |
| 15 | What is the metrics retention period? | Historical analysis |
| 16 | Is distributed tracing required? What tool (Jaeger, Zipkin, X-Ray)? | Request flow visibility |
| 17 | What APM (Application Performance Monitoring) is needed? | Application insights |
| 18 | Is synthetic monitoring/uptime monitoring required? | Proactive detection |

## 7.4 Alerting

| # | Question | Why It Matters |
|---|----------|----------------|
| 19 | What conditions should trigger alerts (error rate, latency, availability)? | Alert definition |
| 20 | What are the alert thresholds (warning, critical)? | Severity levels |
| 21 | What notification channels are needed (email, Slack, PagerDuty, SMS)? | Alert routing |
| 22 | Who should receive which alerts (on-call rotation, escalation)? | Incident response |
| 23 | What is the alert de-duplication/aggregation strategy? | Alert fatigue prevention |
| 24 | Are there quiet hours or maintenance windows for alerts? | Operational efficiency |

## 7.5 Dashboards & Visualization

| # | Question | Why It Matters |
|---|----------|----------------|
| 25 | What dashboards are needed (system health, business KPIs, security)? | Visibility requirements |
| 26 | Who needs access to which dashboards? | Access control |
| 27 | Are real-time dashboards required? What refresh rate? | Data freshness |
| 28 | What visualization types are needed (time series, heatmaps, tables)? | Display requirements |
| 29 | Should dashboards be displayed on monitors (NOC, office TV)? | Physical display |

## 7.6 Log Security & Compliance

| # | Question | Why It Matters |
|---|----------|----------------|
| 30 | Are immutable/tamper-proof logs required? | Audit integrity |
| 31 | Is log encryption required (in transit, at rest)? | Security compliance |
| 32 | What access controls are needed for logs? | Least privilege |
| 33 | Are logs required for compliance audits? Which frameworks? | Regulatory evidence |
| 34 | How should security events be correlated (SIEM integration)? | Security operations |

---

# 8. Data Analytics & Business Intelligence

## 8.1 Analytics Requirements

| # | Question | Why It Matters |
|---|----------|----------------|
| 1 | What business questions need to be answered with data? | Purpose definition |
| 2 | What KPIs and metrics need to be tracked? | Success measurement |
| 3 | What reports are needed (daily, weekly, monthly, ad-hoc)? | Reporting cadence |
| 4 | Who are the consumers of analytics (executives, analysts, operations)? | Audience identification |
| 5 | What is the data freshness requirement (real-time, hourly, daily)? | Latency requirements |

## 8.2 Data Sources

| # | Question | Why It Matters |
|---|----------|----------------|
| 6 | What data sources need to be integrated (databases, APIs, files, streams)? | Data scope |
| 7 | What is the volume of data from each source? | Storage planning |
| 8 | How frequently is source data updated? | Refresh scheduling |
| 9 | Are there data quality issues in source systems? | Data cleansing needs |
| 10 | Is historical data available and needed? | Historical analysis |

## 8.3 Data Warehouse / Data Lake

| # | Question | Why It Matters |
|---|----------|----------------|
| 11 | Is a data warehouse/lake needed or can analytics run on operational DBs? | Architecture decision |
| 12 | What data warehouse platform (Snowflake, BigQuery, Redshift, Synapse)? | Technology selection |
| 13 | What is the data modeling approach (star schema, snowflake, data vault)? | Schema design |
| 14 | How long should data be retained in the warehouse? | Storage costs |
| 15 | What ETL/ELT tool will be used (dbt, Fivetran, Airbyte, custom)? | Data pipeline |

## 8.4 Usage & Cost Tracking

| # | Question | Why It Matters |
|---|----------|----------------|
| 16 | What usage metrics need to be tracked (API calls, storage, compute, users)? | Resource monitoring |
| 17 | How should costs be attributed (by department, project, customer)? | FinOps/chargeback |
| 18 | What cost alerts and thresholds are needed? | Budget management |
| 19 | How frequently should usage/cost reports be generated? | Reporting cadence |
| 20 | Is cloud cost optimization analysis needed (reserved instances, spot)? | Cost optimization |

## 8.5 BI Tool (Power BI, Tableau, Looker)

| # | Question | Why It Matters |
|---|----------|----------------|
| 21 | What BI tool will be used (Power BI, Tableau, Looker, Metabase)? | Technology selection |
| 22 | How many users need access (viewer, analyst, developer)? | Licensing costs |
| 23 | Is self-service analytics required for business users? | User empowerment |
| 24 | What data refresh frequency is needed in BI tool? | Data currency |
| 25 | Are embedded analytics required in applications? | Integration needs |

## 8.6 Power BI Specific Questions

| # | Question | Why It Matters |
|---|----------|----------------|
| 26 | Is Power BI Pro, Premium, or Embedded needed? | Licensing decision |
| 27 | What data sources will Power BI connect to (DirectQuery vs Import)? | Performance planning |
| 28 | Are dataflows needed for data preparation? | Data transformation |
| 29 | Is row-level security (RLS) required? | Data access control |
| 30 | What is the workspace/app structure? | Governance |
| 31 | Are scheduled refreshes sufficient or is real-time needed? | Data freshness |
| 32 | Is on-premises data gateway required? | Hybrid connectivity |

## 8.7 SQL Queries & Performance

| # | Question | Why It Matters |
|---|----------|----------------|
| 33 | Are complex SQL queries needed for analytics? | Query complexity |
| 34 | What is the acceptable query response time? | Performance requirements |
| 35 | Are pre-aggregated tables/materialized views needed? | Query optimization |
| 36 | Is query caching available and configured? | Performance improvement |
| 37 | What indexes are needed for analytics queries? | Query optimization |

## 8.8 Data Governance

| # | Question | Why It Matters |
|---|----------|----------------|
| 38 | Who can access which data sets? | Access control |
| 39 | Is data lineage tracking required? | Data provenance |
| 40 | Is a data catalog/dictionary needed? | Data discovery |
| 41 | How is data quality monitored and enforced? | Data reliability |
| 42 | Who owns/stewards each data domain? | Accountability |

---

# 9. Disaster Recovery & Business Continuity

## 9.1 Recovery Objectives

| # | Question | Why It Matters |
|---|----------|----------------|
| 1 | What is the RTO (Recovery Time Objective) for each system? | Downtime tolerance |
| 2 | What is the RPO (Recovery Point Objective) for each system? | Data loss tolerance |
| 3 | Which systems are business-critical vs. non-critical? | Prioritization |
| 4 | What is the acceptable MTTR (Mean Time to Recovery)? | Recovery performance |

## 9.2 Backup Strategy

| # | Question | Why It Matters |
|---|----------|----------------|
| 5 | What data needs to be backed up? | Backup scope |
| 6 | What is the backup frequency (continuous, hourly, daily)? | RPO alignment |
| 7 | How long should backups be retained? | Recovery window |
| 8 | Where should backups be stored (same region, cross-region, off-site)? | Geographic redundancy |
| 9 | Are backups encrypted? | Security |
| 10 | How are backup restores tested? How often? | Recoverability validation |

## 9.3 Failover & High Availability

| # | Question | Why It Matters |
|---|----------|----------------|
| 11 | Is active-active or active-passive failover needed? | HA architecture |
| 12 | What is the failover time requirement? | RTO alignment |
| 13 | Is automatic or manual failover preferred? | Operational model |
| 14 | What triggers a failover? | Failover criteria |
| 15 | How is data replicated between primary and secondary? | Data consistency |

---

# 10. DevOps & CI/CD

## 10.1 Source Control

| # | Question | Why It Matters |
|---|----------|----------------|
| 1 | What source control platform (GitHub, GitLab, Bitbucket, Azure DevOps)? | Tool selection |
| 2 | What branching strategy (GitFlow, trunk-based, feature branches)? | Development workflow |
| 3 | What code review requirements exist? | Quality gates |
| 4 | Is signed commits or branch protection required? | Security |

## 10.2 CI/CD Pipeline

| # | Question | Why It Matters |
|---|----------|----------------|
| 5 | What CI/CD tool (Jenkins, GitHub Actions, GitLab CI, Azure Pipelines)? | Tool selection |
| 6 | What stages are needed (build, test, scan, deploy)? | Pipeline design |
| 7 | What test types must pass (unit, integration, E2E, performance)? | Quality gates |
| 8 | What code coverage threshold is required? | Quality standards |
| 9 | Are security scans required (SAST, DAST, SCA, secrets detection)? | Security gates |
| 10 | What is the deployment strategy (blue-green, canary, rolling)? | Release approach |

## 10.3 Environment Management

| # | Question | Why It Matters |
|---|----------|----------------|
| 11 | What environments are needed (dev, test, staging, prod)? | Environment strategy |
| 12 | How is infrastructure provisioned (Terraform, CloudFormation, Pulumi)? | IaC approach |
| 13 | How are environment configurations managed (secrets, feature flags)? | Configuration management |
| 14 | Is environment parity enforced? | Consistency |

---

# 11. Integration & Middleware

## 11.1 Integration Requirements

| # | Question | Why It Matters |
|---|----------|----------------|
| 1 | What systems need to integrate (internal, external, legacy)? | Integration scope |
| 2 | What integration pattern (sync, async, event-driven, batch)? | Architecture pattern |
| 3 | What message broker/queue (Kafka, RabbitMQ, SQS, Azure Service Bus)? | Technology selection |
| 4 | What data formats for integration (JSON, XML, Avro, Protobuf)? | Data standards |
| 5 | Are there existing ESB/iPaaS platforms to use? | Existing infrastructure |

## 11.2 API Management

| # | Question | Why It Matters |
|---|----------|----------------|
| 6 | Is an API gateway needed? Which one (Kong, Apigee, AWS API Gateway)? | API management |
| 7 | What API versioning strategy? | API lifecycle |
| 8 | Is API documentation required (OpenAPI/Swagger)? | Developer experience |
| 9 | What API authentication/authorization is needed? | Security |
| 10 | Are API rate limits and quotas needed? | Protection |

---

# 12. User & Access Management

## 12.1 Identity & Authentication

| # | Question | Why It Matters |
|---|----------|----------------|
| 1 | What identity provider (Azure AD, Okta, Auth0, custom)? | IdP selection |
| 2 | Is SSO required? What protocols (SAML, OIDC, OAuth)? | Authentication method |
| 3 | Is MFA required? What factors (SMS, TOTP, hardware key)? | Security level |
| 4 | What password policy is required? | Credential security |
| 5 | Is social login needed (Google, Facebook, Apple)? | User convenience |

## 12.2 Authorization

| # | Question | Why It Matters |
|---|----------|----------------|
| 6 | What authorization model (RBAC, ABAC, ACL)? | Permission structure |
| 7 | What roles are needed and what permissions do they have? | Role definition |
| 8 | Is fine-grained/field-level authorization needed? | Granularity |
| 9 | How are permissions provisioned/deprovisioned? | User lifecycle |
| 10 | Is just-in-time or time-limited access needed? | Least privilege |

---

# 13. Testing Requirements

## 13.1 Test Strategy

| # | Question | Why It Matters |
|---|----------|----------------|
| 1 | What testing types are required (unit, integration, E2E, performance, security)? | Test scope |
| 2 | What is the target code coverage? | Quality standards |
| 3 | What test frameworks/tools will be used? | Technology selection |
| 4 | Is TDD/BDD required? | Development approach |
| 5 | What test environments are needed? | Infrastructure |

## 13.2 Performance Testing

| # | Question | Why It Matters |
|---|----------|----------------|
| 6 | What load testing tool (JMeter, k6, Gatling, Locust)? | Tool selection |
| 7 | What is the target load profile (users, requests per second)? | Test scenarios |
| 8 | What are the performance pass/fail criteria? | Success criteria |
| 9 | Is soak/endurance testing required? | Stability validation |
| 10 | Is chaos engineering/resilience testing required? | Fault tolerance |

## 13.3 Security Testing

| # | Question | Why It Matters |
|---|----------|----------------|
| 11 | Is SAST (Static Application Security Testing) required? Which tool? | Code security |
| 12 | Is DAST (Dynamic Application Security Testing) required? Which tool? | Runtime security |
| 13 | Is SCA (Software Composition Analysis) required? | Dependency security |
| 14 | Is penetration testing required? How often? | Security validation |
| 15 | Is bug bounty program participation needed? | Crowdsourced security |

---

# 14. Documentation Requirements

## 14.1 Technical Documentation

| # | Question | Why It Matters |
|---|----------|----------------|
| 1 | What architecture documentation is required (C4, UML, diagrams)? | System understanding |
| 2 | What API documentation format (OpenAPI, AsyncAPI, GraphQL schema)? | Developer experience |
| 3 | What runbook/playbook documentation is needed? | Operations support |
| 4 | What database schema documentation is required? | Data understanding |
| 5 | Where will documentation be hosted (Confluence, GitHub, wiki)? | Accessibility |

## 14.2 User Documentation

| # | Question | Why It Matters |
|---|----------|----------------|
| 6 | What end-user documentation is needed (guides, FAQs, tutorials)? | User support |
| 7 | Is in-app help/tooltips required? | User assistance |
| 8 | What training materials are needed? | User onboarding |
| 9 | Are video tutorials/walkthroughs required? | Learning formats |

---

# Quick Reference: Questions by Role

## For Product Owner / Business Analyst
- Sections: 1 (API business needs), 6 (NFRs), 8 (Analytics)

## For Solution Architect
- Sections: 3 (Capacity), 4 (Network), 5 (WAF), 6 (NFRs), 9 (DR), 11 (Integration)

## For Security Engineer
- Sections: 1.10 (API Security), 2 (Retention), 4.4-4.6 (Firewall), 5 (WAF), 6.4 (Security NFRs), 7.6 (Log Security)

## For DevOps Engineer
- Sections: 7 (Logging), 10 (CI/CD), 4 (Network)

## For Data Engineer / BI Developer
- Sections: 8 (Analytics), 2 (Retention)

## For DBA
- Sections: 2 (Retention), 3 (Capacity), 8.7 (SQL Performance)

---

# Appendix: Quick Checklist Summary

## Critical Questions for ANY Project

| Category | Must-Answer Questions |
|----------|----------------------|
| **Performance** | Response time targets, throughput requirements, concurrent users |
| **Security** | Authentication method, encryption requirements, compliance mandates |
| **Availability** | Uptime SLA, RTO/RPO, failover requirements |
| **Scalability** | Growth projections, scaling strategy (horizontal/vertical) |
| **Integration** | External dependencies, API rate limits, data formats |
| **Data** | Retention periods, backup strategy, data classification |
| **Monitoring** | Log aggregation tool, alerting thresholds, dashboard needs |
| **Compliance** | Applicable regulations (GDPR, HIPAA, PCI), audit requirements |

---

**Document Version:** 1.0  
**Last Updated:** December 2024  
**Next Review:** Quarterly or upon major project initiation
