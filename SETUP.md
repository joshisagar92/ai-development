# AI Context Library - Complete Setup Guide

> How to install and use development skills with Claude Code, OpenAI Codex, ChatGPT, and GitHub Copilot

---

## Table of Contents

1. [What Are Skills?](#what-are-skills)
2. [Directory Structure](#directory-structure)
3. [Quick Start](#quick-start)
4. [Claude Code Setup](#claude-code-setup)
5. [OpenAI Codex / ChatGPT Setup](#openai-codex--chatgpt-setup)
6. [GitHub Copilot Setup](#github-copilot-setup)
7. [Skills Reference](#skills-reference)
8. [Creating Custom Skills](#creating-custom-skills)
9. [Verification](#verification)
10. [Troubleshooting](#troubleshooting)

---

## What Are Skills?

Skills are instruction files that teach AI assistants specific rules, patterns, and practices. When loaded, the AI applies these automatically to all code it generates.

**Benefits:**
- Consistent code quality across all outputs
- Built-in security best practices (OWASP)
- Automatic documentation of decisions
- Reduced need for repeated instructions
- Technology-specific guidance

**What's Inside MASTER-SKILL.md:**

| Section | Topics |
|---------|--------|
| **Non-Technical Mode** | Plain language communication for non-developers |
| **Development Phases** | Skill loading by phase (Planning, Implementation, Security, Testing, Deployment) |
| **TDD Practices** | Red-Green-Refactor, Kent Beck's laws |
| **OWASP Security** | Web Top 10, API Top 10, LLM Top 10 |
| **Code Quality** | Smells, refactoring patterns, SOLID |
| **Checklists** | Security, testing, code quality |

---

## Directory Structure

```
ai-context-library/
├── index.html                      # Interactive documentation hub
├── SETUP.md                        # This file
│
├── AI-Assisted-Development-Guide.md
├── domain-experts-reference.md
├── project-requirements-questionnaire.md
├── technology-rulebook.md
├── api-practices-rulebook.md
│
└── coding/
    └── skills/
        ├── claude/                 # Claude Code skills
        │   ├── MASTER-SKILL.md
        │   ├── programming/SKILL.md
        │   ├── security/SKILL.md
        │   ├── testing/SKILL.md
        │   ├── db/SKILL.md
        │   ├── integration/SKILL.md
        │   ├── code/SKILL.md
        │   ├── api-practices/
        │   │   ├── api-documentation.md
        │   │   ├── api-logging.md
        │   │   ├── api-monitoring.md
        │   │   ├── api-security.md
        │   │   └── postman-newman.md
        │   └── technologies/
        │       ├── java-spring.md
        │       ├── jdbc-pooling.md
        │       ├── javascript-angular.md
        │       ├── python-fastapi.md
        │       ├── docker.md
        │       ├── kubernetes-aks.md
        │       ├── observability.md
        │       ├── testcontainers.md
        │       ├── mockito.md
        │       ├── html-css.md
        │       └── jinja2.md
        │
        └── codex/                  # OpenAI Codex/ChatGPT skills
            └── (same structure as claude/)
```

---

## Quick Start

### Claude Code (Fastest)

```bash
# Copy master skill to your project
cp coding/skills/claude/MASTER-SKILL.md ./CLAUDE.md

# Claude Code automatically reads CLAUDE.md from project root
```

### ChatGPT (Custom Instructions)

1. Open ChatGPT → Profile → Settings → Personalization → Custom Instructions
2. Paste content from `coding/skills/codex/MASTER-SKILL.md`
3. Save

### OpenAI API

```python
with open("coding/skills/codex/MASTER-SKILL.md", "r") as f:
    system_prompt = f.read()

response = openai.chat.completions.create(
    model="gpt-4",
    messages=[
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": "Write a user registration function"}
    ]
)
```

---

## Claude Code Setup

### Method 1: CLAUDE.md File (Recommended)

Claude Code automatically reads `CLAUDE.md` files in your project root.

**Steps:**

1. **Copy the skill content** to your project's `CLAUDE.md`:

```bash
# From your project root
cp /path/to/coding/skills/claude/MASTER-SKILL.md ./CLAUDE.md
```

2. **Or append to existing CLAUDE.md**:

```bash
cat /path/to/coding/skills/claude/MASTER-SKILL.md >> ./CLAUDE.md
```

3. **Verify**: Claude Code will now apply these rules automatically.

### Method 2: Using Skills Directory

Claude Code can load skills from a dedicated directory.

**Steps:**

1. **Create skills directory** in your project:

```bash
mkdir -p .claude/skills
```

2. **Copy skill files**:

```bash
cp /path/to/coding/skills/claude/*.md .claude/skills/
cp -r /path/to/coding/skills/claude/technologies .claude/skills/
cp -r /path/to/coding/skills/claude/api-practices .claude/skills/
```

3. **Reference in CLAUDE.md**:

```markdown
# CLAUDE.md

Load skills from: .claude/skills/

## Project-Specific Rules
- Use TypeScript for all new files
- Follow existing patterns in src/
```

### Method 3: Global Skills (All Projects)

For skills that apply to ALL your projects:

```bash
# Create global config directory
mkdir -p ~/.claude/skills

# Copy master skill
cp /path/to/coding/skills/claude/MASTER-SKILL.md ~/.claude/skills/

# Claude Code will load these globally
```

### Method 4: In-Conversation Loading

Reference skills directly in your prompts:

```
Read @coding/skills/claude/MASTER-SKILL.md and apply those practices when reviewing this code.
```

Or load multiple skills:

```
I need to implement a REST API endpoint. Apply these skills:
- @coding/skills/claude/technologies/java-spring.md
- @coding/skills/claude/api-practices/api-documentation.md
- @coding/skills/claude/api-practices/api-security.md

Create a GET endpoint for fetching user orders.
```

### Method 5: Settings Configuration

Configure in `.claude/settings.json`:

```json
{
  "skills": {
    "enabled": true,
    "paths": [
      "./coding/skills/claude/MASTER-SKILL.md",
      "./coding/skills/claude/programming/SKILL.md",
      "./coding/skills/claude/security/SKILL.md"
    ],
    "autoLoad": {
      "*.java": ["./coding/skills/claude/technologies/java-spring.md"],
      "*.py": ["./coding/skills/claude/technologies/python-fastapi.md"],
      "*.ts": ["./coding/skills/claude/technologies/javascript-angular.md"],
      "Dockerfile": ["./coding/skills/claude/technologies/docker.md"]
    }
  }
}
```

---

## OpenAI Codex / ChatGPT Setup

### Method 1: Custom Instructions (ChatGPT)

**Step 1: Open Custom Instructions**
1. Open ChatGPT (chat.openai.com)
2. Click your profile icon (bottom left)
3. Select "Settings"
4. Click "Personalization"
5. Click "Custom Instructions"

**Step 2: Configure the Instructions**

In the **"How would you like ChatGPT to respond?"** section, paste:

```
When writing code, follow these practices:

## TDD
- Write failing test first (Red)
- Write minimum code to pass (Green)
- Refactor while tests pass

## Security (OWASP)
- Use parameterized queries (never concatenate SQL)
- Encode all output (XSS prevention)
- Verify object ownership (BOLA prevention)
- Use bcrypt/Argon2 for passwords

## Code Quality
- Methods under 15 lines
- Max 3 parameters per method
- Extract data clumps into objects
- Replace conditionals with polymorphism

## For Every Code Output
Include:
1. WHY this approach was chosen
2. Test case to verify it works
3. Edge cases to consider
```

**Step 3: Save** - Click "Save" - these rules now apply to all future conversations.

**For Full Rules**: Copy the entire content of `coding/skills/codex/MASTER-SKILL.md` into Custom Instructions (may need to summarize due to length limits).

### Method 2: System Prompt (API / Codex)

Use MASTER-SKILL.md as your system prompt:

```python
import openai

# Load the skill file as system prompt
with open("coding/skills/codex/MASTER-SKILL.md", "r") as f:
    system_prompt = f.read()

response = openai.chat.completions.create(
    model="gpt-4",
    messages=[
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": "Write a user registration function"}
    ]
)
```

**For production**, consider:
- Caching the loaded prompt
- Using a summarized version if token limits are a concern
- Selecting domain-specific skill files for focused tasks

### Method 3: Global Configuration

Add to your Codex configuration file (`~/.codex/config.yaml`):

```yaml
skills:
  # Master skill for all coding tasks
  - path: /path/to/coding/skills/codex/MASTER-SKILL.md
    auto_apply: true

  # Technology skills (loaded based on file type)
  - path: /path/to/coding/skills/codex/technologies/java-spring.md
    triggers: ["*.java", "pom.xml", "build.gradle"]
  - path: /path/to/coding/skills/codex/technologies/python-fastapi.md
    triggers: ["*.py", "requirements.txt", "pyproject.toml"]
  - path: /path/to/coding/skills/codex/technologies/docker.md
    triggers: ["Dockerfile", "docker-compose.yml"]
```

### Method 4: Project-Level Configuration

Create a `.codex/skills.yaml` in your project root:

```yaml
version: 1
skills:
  base:
    - ../coding/skills/codex/MASTER-SKILL.md

  context:
    java:
      - ../coding/skills/codex/technologies/java-spring.md
      - ../coding/skills/codex/technologies/jdbc-pooling.md
    python:
      - ../coding/skills/codex/technologies/python-fastapi.md
    frontend:
      - ../coding/skills/codex/technologies/javascript-angular.md
      - ../coding/skills/codex/technologies/html-css.md
    docker:
      - ../coding/skills/codex/technologies/docker.md
      - ../coding/skills/codex/technologies/kubernetes-aks.md
```

### Method 5: Command Line

Load skills dynamically when running Codex:

```bash
# Load master skill
codex --skill ./coding/skills/codex/MASTER-SKILL.md "your prompt"

# Load multiple skills
codex \
  --skill ./coding/skills/codex/MASTER-SKILL.md \
  --skill ./coding/skills/codex/technologies/java-spring.md \
  --skill ./coding/skills/codex/security/SKILL.md \
  "Review this Java code for security issues"
```

---

## GitHub Copilot Setup

Create `.github/copilot-instructions.md` in your repository:

```markdown
# Copilot Instructions

## Code Quality Rules
- All methods must have tests
- Use parameterized SQL queries only
- Encode all user output
- Maximum 15 lines per method

## Security Rules
- Never concatenate user input into SQL
- Always verify resource ownership
- Use bcrypt for password hashing
- Set security headers (CSP, HSTS)

## Testing Rules
- Write test before implementation
- Use Arrange-Act-Assert pattern
- No external dependencies in unit tests
```

---

## Skills Reference

### Core Development Skills

| Skill | Purpose | Key Topics |
|-------|---------|------------|
| `MASTER-SKILL.md` | Meta-skill with output rules | Phase loading, checklists, non-technical mode |
| `programming/SKILL.md` | TDD, refactoring | Red-Green-Refactor, code quality |
| `security/SKILL.md` | OWASP security | SQL injection, XSS, access control |
| `testing/SKILL.md` | Test patterns | Pyramid, AAA, test doubles |
| `db/SKILL.md` | Database practices | SQL, indexes, migrations, pooling |
| `integration/SKILL.md` | API & integration | REST, circuit breaker, correlation IDs |
| `code/SKILL.md` | Design patterns | SOLID, Tell Don't Ask, Law of Demeter |

### API Practice Skills

| Skill | Purpose | Key Topics |
|-------|---------|------------|
| `api-documentation.md` | OpenAPI specs | Design-first, examples |
| `api-logging.md` | Structured logging | JSON, correlation IDs, 12-Factor |
| `api-monitoring.md` | Health & metrics | Four Golden Signals, SLOs |
| `api-security.md` | API security | BOLA, rate limiting, SSRF |
| `postman-newman.md` | API testing | Collections, Newman CI |

### Technology Skills

| Skill | Trigger Files | Key Topics |
|-------|---------------|------------|
| `java-spring.md` | `*.java`, `pom.xml` | Spring Boot, DI, profiles |
| `jdbc-pooling.md` | `*.java` + DB | HikariCP, PreparedStatements |
| `python-fastapi.md` | `*.py`, `requirements.txt` | Pydantic, Depends, async |
| `javascript-angular.md` | `*.ts`, `angular.json` | Components, RxJS |
| `html-css.md` | `*.html`, `*.css` | Semantic HTML, accessibility |
| `docker.md` | `Dockerfile` | Multi-stage, non-root |
| `kubernetes-aks.md` | `*.yaml` (k8s) | Probes, HPA, PDB |
| `observability.md` | - | Elasticsearch, Splunk, Datadog |
| `testcontainers.md` | - | Container-based testing |
| `mockito.md` | `*.java` | Mocking best practices |
| `jinja2.md` | `*.html`, `*.j2` | Templating, autoescaping |

### Which to Use

| Scenario | Recommended Skill |
|----------|-------------------|
| Full project | `MASTER-SKILL.md` |
| Security review only | `security/SKILL.md` |
| Database work only | `db/SKILL.md` |
| API development | `MASTER-SKILL.md` + `api-practices/*` |
| Lightweight/fast responses | Individual domain skills |

---

## Creating Custom Skills

### Skill File Format

Each skill file follows this format:

```markdown
---
name: skill-name
description: When to use this skill. This appears in skill listings.
---

# Skill Title

## Section 1

Rules and examples...

## Quick Reference

| Practice | Rule |
|----------|------|
| ... | ... |
```

### Required Frontmatter

| Field | Description |
|-------|-------------|
| `name` | Unique identifier for the skill |
| `description` | When/how to use the skill |

### Optional Frontmatter

| Field | Description |
|-------|-------------|
| `triggers` | File patterns that activate this skill |
| `depends` | Other skills this skill depends on |
| `priority` | Loading priority (higher = loaded first) |

### Example: Custom Company Skill

```markdown
---
name: my-company-standards
description: Company-specific coding standards. Apply to all company projects.
---

# Company Coding Standards

## Naming Conventions

- Services: `{Domain}Service` (e.g., `OrderService`)
- Repositories: `{Entity}Repository`
- Controllers: `{Domain}Controller`

## Logging

All logs must include:
- Transaction ID
- User ID (if authenticated)
- Timestamp in ISO 8601 format

## Quick Reference

| Area | Standard |
|------|----------|
| Logs | JSON format, include txId |
| APIs | OpenAPI 3.x documentation |
| Tests | 80% coverage minimum |
```

### Example: PostgreSQL Skill

```markdown
---
name: postgresql-practices
description: PostgreSQL-specific database practices.
---

# PostgreSQL Best Practices

## Use EXPLAIN ANALYZE

Always analyze query plans before optimization.

```sql
EXPLAIN ANALYZE
SELECT * FROM users WHERE email = 'test@example.com';
```

## Indexing Rules

1. B-tree for equality and range queries
2. GIN for full-text search and arrays
3. BRIN for time-series data
```

---

## Verification

### Claude Code

Ask Claude:

```
What rules are you following for this project?
```

Expected response should reference your skill content.

### ChatGPT/Codex

Ask:

```
Before writing code, tell me what quality rules you'll apply.
```

### Codex CLI

```bash
# List loaded skills
codex --list-skills

# Test skill loading
codex --dry-run --skill ./MASTER-SKILL.md "test prompt"
```

---

## Troubleshooting

### Skills Not Loading

| Problem | Solution |
|---------|----------|
| Claude ignores CLAUDE.md | Check file is in project root |
| Rules partially applied | File may be too long; split into sections |
| Conflicting rules | Check for duplicate/contradicting instructions |
| Codex not finding skills | Verify file paths are correct |
| Frontmatter errors | Check YAML between `---` markers |

### Skills Not Triggering

| Problem | Solution |
|---------|----------|
| Auto-load not working | Verify trigger patterns match your files |
| Skill not registered | Add to configuration file |
| Wrong skill loaded | Check priority field for ordering |

### Performance Issues

| Problem | Solution |
|---------|----------|
| Slow responses | Use domain-specific skill instead of MASTER |
| Token limits exceeded | Split into smaller skill files |
| Context window full | Load only necessary skills |

### Conflicts Between Skills

1. Use priority field to control loading order
2. Check for contradictory rules between skills
3. Use more specific skills over general ones
4. Create a project skill to resolve conflicts

---

## Best Practices

1. **Start with MASTER-SKILL.md** - Ensures consistent output format
2. **Layer skills appropriately** - General → Specific
3. **Use triggers** - Auto-load skills based on file context
4. **Keep skills focused** - One concern per skill file
5. **Reference relevant skills** - Don't load everything, just what's needed
6. **Create project-specific skills** - Extend base skills for your needs
7. **Keep skills updated** - Review and update quarterly
8. **Version control skills** - Track changes like code

---

## Platform Summary

| Platform | Installation Method | Skill Location |
|----------|---------------------|----------------|
| **Claude Code** | Copy to `CLAUDE.md` | `skills/claude/` |
| **ChatGPT** | Custom Instructions | `skills/codex/` |
| **OpenAI API** | System prompt | `skills/codex/` |
| **Codex CLI** | Config file or `--skill` flag | `skills/codex/` |
| **GitHub Copilot** | `.github/copilot-instructions.md` | Summarized version |

---

## Sources

The skills in this library are based on the work of 35+ domain experts:

- **Kent Beck** - TDD, XP, Simple Design
- **Martin Fowler** - Refactoring, Patterns, Architecture
- **Michael Feathers** - Legacy Code, Seams
- **OWASP** - Web, API, LLM Security Standards
- **12-Factor App** - Configuration, Logging
- **Google SRE** - Monitoring, Reliability
- **Josh Long, Juergen Hoeller** - Spring Framework
- **Kelsey Hightower, Brendan Burns** - Kubernetes

See `domain-experts-reference.md` for the complete list.

---

## File Locations

```
# Main documentation
index.html              # Interactive documentation hub (open in browser)
SETUP.md                # This setup guide

# Skills
coding/skills/claude/   # Claude Code skills
coding/skills/codex/    # OpenAI Codex/ChatGPT skills

# Reference documents
AI-Assisted-Development-Guide.md
domain-experts-reference.md
project-requirements-questionnaire.md
technology-rulebook.md
api-practices-rulebook.md
```
