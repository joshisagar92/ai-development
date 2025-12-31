# AI Context Library

A comprehensive library of AI development skills and best practices for Claude Code and OpenAI Codex.

## Project Structure

```
ai-context/
├── website/           # Web interface for browsing skills
│   ├── server.js      # Node.js/Express server
│   ├── package.json   # Dependencies
│   ├── public/        # Static files (HTML, CSS, JS)
│   └── README.md      # Website documentation
│
├── skills/            # CLI skills for Claude/Codex
│   ├── claude/        # Skills for Claude Code
│   ├── codex/         # Skills for OpenAI Codex
│   └── README.md      # CLI usage documentation
│
├── docs/              # Reference documentation
│   ├── AI-Assisted-Development-Guide.md
│   ├── domain-experts-reference.md
│   └── project-requirements-questionnaire.md
│
└── README.md          # This file
```

## Quick Start

### Option 1: Web Interface

```bash
cd website
npm install
npm start
```

Open http://localhost:3000

### Option 2: CLI Usage (Claude Code)

```bash
# Load the master skill
Read @skills/claude/MASTER-SKILL.md and help me implement [feature]
```

### Option 3: CLI Usage (OpenAI Codex)

```
@skills/codex/MASTER-SKILL.md

Help me implement [feature]
```

## What's Included

### Core Skills
- **Programming** - TDD workflow, code quality, refactoring
- **Security** - OWASP, SQL injection, XSS prevention
- **Testing** - Test patterns, mocking, Playwright
- **Database** - JdbcTemplate, indexes, transactions
- **Integration** - REST API, circuit breaker, retry
- **Design** - SOLID principles, patterns
- **Performance** - N+1 detection, memory leaks, profiling

### Technology Skills
- Java & Spring Boot
- Python & FastAPI
- Angular & JavaScript
- HTML & CSS (Accessibility)
- Docker
- Kubernetes & AKS
- Observability (ELK, Splunk, Datadog)
- Testcontainers & Mockito

### API Practice Skills
- API Documentation (OpenAPI)
- API Logging (Structured, Correlation IDs)
- API Monitoring (Four Golden Signals)
- API Security (OWASP API Top 10)
- Postman & Newman Testing

## Based On

Skills are based on best practices from 35+ domain experts including:
- Kent Beck, Martin Fowler (TDD, Refactoring)
- Josh Long, Phil Webb (Spring Framework)
- Kelsey Hightower (Kubernetes)
- OWASP Foundation (Security)
- Google SRE Team (Observability)

## License

MIT
