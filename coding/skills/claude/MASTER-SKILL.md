---
name: development-mastery
description: Meta-skill that orchestrates all development practices. Use this for all coding tasks to ensure consistent output format and reasoning.
---

# Development Mastery - Meta Skill

This meta-skill ensures consistent, high-quality code output with complete reasoning.

---

## Non-Technical Mode

When the user indicates they are non-technical, apply these rules:

> **User Request**: "I'm not technical at all so please summarize it in a simple way for me. Use the explore agent to summarize how things work (that the other agent is working on) so I can learn while I do this. If the background agent runs into any errors, please stop and tell me but guide me in how I might be able to fix it. Remember I am non-technical, so any technical language at all is not useful to me."

### Non-Technical Communication Rules

1. **Use everyday language** - No jargon, no acronyms without explanation
2. **Use analogies** - Compare technical concepts to everyday things
3. **Explain the "why"** - Focus on purpose and benefits, not implementation details
4. **Step-by-step guidance** - Break down any actions into simple numbered steps
5. **Proactive error handling** - Stop immediately on errors, explain in plain terms, guide through fixes
6. **Learning mode** - Use explore agent to summarize what's happening in simple terms
7. **Visual cues** - Use formatting to highlight important points

### Example Non-Technical Explanations

| Technical | Non-Technical |
|-----------|---------------|
| "The API returned a 401 error" | "The system said 'you're not logged in' - like trying to enter a building without your keycard" |
| "We need to refactor this function" | "We need to reorganize this code - like cleaning out a messy closet so things are easier to find" |
| "The database query is slow" | "The system is taking too long to find the information - like searching through a huge filing cabinet" |

---

## Output Format Requirements

For EVERY code suggestion or implementation, include:

```
## Decision: [What was decided]

### Why This Approach
- Reasoning for the choice
- Alternatives considered
- Trade-offs made

### Requirement Questions Asked
- Questions that clarified the need
- Assumptions made

### Test Case
- How to verify this works
- Edge cases to test

### Documentation
- What to document for future reference
```

---

## Development Phase Skill Loading

### Phase 1: Planning & Design

Load these skills when planning:
- `@coding/skills/codex/programming/SKILL.md` - TDD, YAGNI principles
- `@coding/skills/codex/code/SKILL.md` - Design patterns, SOLID
- `@coding/skills/codex/integration/SKILL.md` - API design patterns

### Phase 2: Implementation

Load based on technology:

**Java/Spring Projects:**
```
@coding/skills/codex/technologies/java-spring.md
@coding/skills/codex/technologies/jdbc-pooling.md
@coding/skills/codex/technologies/testcontainers.md
@coding/skills/codex/technologies/mockito.md
```

**Python/FastAPI Projects:**
```
@coding/skills/codex/technologies/python-fastapi.md
@coding/skills/codex/technologies/jinja2.md
```

**Frontend Projects:**
```
@coding/skills/codex/technologies/javascript-angular.md
@coding/skills/codex/technologies/html-css.md
```

**Infrastructure:**
```
@coding/skills/codex/technologies/docker.md
@coding/skills/codex/technologies/kubernetes-aks.md
```

### Phase 3: Security Review

Always load:
```
@coding/skills/codex/security/SKILL.md
@coding/skills/codex/api-practices/api-security.md
```

### Phase 4: Testing

Load these skills:
```
@coding/skills/codex/testing/SKILL.md
@coding/skills/codex/technologies/testcontainers.md
@coding/skills/codex/technologies/mockito.md
@coding/skills/codex/api-practices/postman-newman.md
```

### Phase 5: Deployment & Monitoring

Load these skills:
```
@coding/skills/codex/api-practices/api-logging.md
@coding/skills/codex/api-practices/api-monitoring.md
@coding/skills/codex/technologies/observability.md
```

---

## Skill Reference Quick Guide

### Core Skills (Always Applicable)

| Skill | When to Load | Key Topics |
|-------|--------------|------------|
| `programming/SKILL.md` | All coding | TDD, refactoring, code quality |
| `security/SKILL.md` | All coding | OWASP Top 10, input validation |
| `testing/SKILL.md` | All coding | Test pyramid, test doubles |
| `code/SKILL.md` | All coding | Code smells, patterns |

### Technology Skills (Load by Stack)

| Skill | Trigger Files | Key Topics |
|-------|---------------|------------|
| `technologies/java-spring.md` | `*.java`, `pom.xml` | Spring Boot, DI, profiles |
| `technologies/jdbc-pooling.md` | `*.java` + DB | HikariCP, PreparedStatements |
| `technologies/python-fastapi.md` | `*.py`, `requirements.txt` | Pydantic, async, Depends |
| `technologies/javascript-angular.md` | `*.ts`, `angular.json` | Components, RxJS |
| `technologies/html-css.md` | `*.html`, `*.css` | Semantic HTML, accessibility |
| `technologies/docker.md` | `Dockerfile` | Multi-stage, non-root |
| `technologies/kubernetes-aks.md` | `*.yaml` (k8s) | Probes, HPA, PDB |

### API Practice Skills

| Skill | When to Load | Key Topics |
|-------|--------------|------------|
| `api-practices/api-documentation.md` | API design | OpenAPI 3.x, examples |
| `api-practices/api-logging.md` | Logging setup | Structured JSON, correlation IDs |
| `api-practices/api-monitoring.md` | Monitoring setup | Four Golden Signals, SLOs |
| `api-practices/api-security.md` | API security | BOLA, rate limiting, SSRF |
| `api-practices/postman-newman.md` | API testing | Collections, Newman CI |

---

## Quick Reference Checklists

### Before Writing Code
- [ ] Write a failing test first
- [ ] Ask: "What's the simplest solution?"
- [ ] Check: "Does similar code exist?"
- [ ] Load relevant technology skills

### While Writing Code
- [ ] One thing at a time
- [ ] Keep tests green
- [ ] Commit frequently
- [ ] Apply security checklist

### After Writing Code
- [ ] Refactor if needed
- [ ] Verify edge cases
- [ ] Run security skill checks
- [ ] Update documentation

### Security Checklist
- [ ] Parameterized queries (SQL injection)
- [ ] Output encoding (XSS)
- [ ] Ownership verification (BOLA)
- [ ] Strong password hashing (bcrypt/Argon2)
- [ ] HTTPS enforced
- [ ] No secrets in code
- [ ] Rate limiting implemented
- [ ] Input validation on all user input
- [ ] Security headers configured
- [ ] Dependencies up to date

### Test Quality Checklist
- [ ] Test one thing per test
- [ ] Test name describes scenario
- [ ] Fast (< 100ms each)
- [ ] No external dependencies
- [ ] No test-to-test dependencies
- [ ] Deterministic (no flaky tests)

### Code Smell Quick Reference
| Smell | Primary Refactoring |
|-------|---------------------|
| Long Method | Extract Method |
| Long Parameter List | Introduce Parameter Object |
| Feature Envy | Move Method |
| Switch Statements | Replace Conditional with Polymorphism |
| Duplicated Code | Extract Method |
| Dead Code | Delete it |

---

## Related Documentation

For comprehensive guidance, also reference:
- `@domain-experts-reference.md` - List of domain experts by technology
- `@AI-Assisted-Development-Guide.md` - AI-assisted development workflow
- `@project-requirements-questionnaire.md` - Discovery questions for projects

---

## Sources

- Kent Beck - TDD, XP, Simple Design
- Martin Fowler - Refactoring, Patterns, Architecture
- Michael Feathers - Legacy Code, Seams
- OWASP - Web, API, LLM Security Standards
- 12-Factor App - Configuration, Logging
- Google SRE - Monitoring, Reliability
