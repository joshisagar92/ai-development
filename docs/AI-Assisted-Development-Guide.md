# The Developer's Guide to AI-Assisted Development

> AI-assisted development is transforming how we build software, but it's not a free pass to abandon rigor, review, or craftsmanship.

---

## The Hard Truth: Quality Isn't Automatic

Unchecked AI-generated code can massively amplify technical debt. As Simon Willison aptly says, think of an LLM pair programmer as *"over-confident and prone to mistakes"*.

**No matter how much AI you use, you remain the accountable engineer.**

This guide walks you through a structured approach to AI-assisted development—from initial thought to production-ready code.

---

## Phase 1: From Idea to Specification

Don't just throw wishes at the LLM. Begin by **defining the problem and planning a solution**.

### Brainstorm with AI

Start by brainstorming a detailed specification with the AI. Ask the LLM to iteratively ask you questions until you've fleshed out requirements and edge cases.

**Example Brainstorming Prompt:**

```
I've got an idea I want to talk through with you. I'd like you to help me
turn it into a fully formed design and spec (and eventually an implementation plan).

Check out the current state of the project in our working directory to
understand where we're starting off. Also read the @SPEC.md file if it exists
(or prepare to create one).

## Interview Process
Interview me in detail using the AskUserQuestionTool. Ask me questions, one
at a time, to help refine the idea. Cover literally everything:
- Technical implementation details and architectural decisions
- UI & UX considerations and user flows
- Concerns, risks, and potential failure modes
- Tradeoffs between different approaches
- Edge cases, scaling considerations, and security implications
- Integration points and dependencies

**Important:** Make sure questions are NOT obvious—dig deep, challenge my
assumptions, and probe for hidden requirements. Ask about what I DON'T want
as much as what I do want.

Ideally, questions would be multiple choice (with an "Other" option), but
open-ended questions are OK for complex topics. Don't forget: only one
question per message.

## Verification & Documentation
Once you believe you understand a section of what we're doing, stop and
describe the design to me in sections of maybe 200-300 words at a time,
asking after each section whether it looks right so far.

## Completion
Continue interviewing me until the specification is truly complete—don't
stop early. When the interview is exhaustive and you fully understand the
requirements, write the complete specification to the SPEC.md file including:
- Requirements and acceptance criteria
- Architecture decisions with rationale
- Data models and API contracts
- UI/UX specifications
- Error handling and testing strategy
```

### Create a Comprehensive Spec

Your spec should contain:
- Requirements
- Architecture decisions
- Data models
- Testing strategy

---

## Phase 2: Planning

Feed the spec into a reasoning-capable model and prompt it to generate a project plan. Break the implementation into logical, bite-sized tasks or milestones.

**Example Planning Prompt:**

```
Great. I need your help to write out a comprehensive implementation plan.

Assume that the engineer has zero context for our codebase and questionable
taste. Document everything they need to know: which files to touch for each
task, code, testing, docs they might need to check, how to test it.

Give them the whole plan as bite-sized tasks. DRY. YAGNI. TDD. Frequent commits.

Assume they are a skilled developer, but know almost nothing about our
toolset or problem domain. Assume they don't know good test design very well.

Please write out this plan, in full detail, into docs/plans/
```

### Iterate on the Plan

Often iterate on this plan—editing and asking the AI to critique or refine it—until it's coherent and complete. Think of it as *waterfall in 15 minutes*.

**Key Principle:** Scope management is everything. Feed the LLM manageable tasks, not the whole codebase at once. Implement one function, fix one bug, add one feature at a time.

---

## Phase 2.5: Implementation Mode (Learning-First Approach)

Once planning is complete, switch to implementation mode with a learning-first approach. This prompt ensures structured execution with documentation and knowledge capture.

**Example Implementation Prompt:**

```
Now let's implement the plan we created. I want to use a learning-first approach
where I understand every decision we make. Please follow these guidelines:

## Cross-Question Before Acting
- Before implementing any step, ask clarifying questions if multiple approaches exist
- Present options with trade-offs (performance, maintainability, complexity)
- Don't assume - validate my preferences for architectural decisions

## Task Breakdown
- Break each task into small, testable steps
- Show me what you're about to do BEFORE doing it
- Wait for my "go ahead" before proceeding to the next step
- Track progress using a todo list visible to me

## Test After Each Step
- After completing each step, compile/run tests immediately
- Fix any issues before moving to the next step
- If a step works, explicitly confirm it before continuing

## Document Decisions & Assumptions
- Create a file `docs/decisions/[feature-name]-decisions.md` that captures:
  - Key architectural decisions and WHY we made them
  - Assumptions we're making
  - Alternative approaches we considered but rejected (and why)
  - Gotchas or tricky parts for future reference
- Update this file as we progress

## Insight Summaries
- After completing each major task, provide a brief "★ Insight" summary
- Highlight patterns, VS Code APIs, or techniques that are reusable
- Connect new concepts to broader programming principles

## Learning Mode Output
- Explain each significant code block before or after writing it
- Use diagrams (ASCII) for complex flows or architecture
- When using framework/library APIs, briefly explain what they do
- Format explanations clearly with headers and tables

## Final Deliverable
- At the end of implementation, create a summary suitable for NotebookLM:
  - Key concepts learned
  - Architecture decisions made
  - Code flow diagrams
  - API reference tables
  - This will be used to generate infographics and mind maps

Let's start with the first task. What do you need to know before we begin?
```

### Decision Log Template

Create `docs/decisions/[feature-name]-decisions.md`:

```markdown
# [Feature Name] - Decisions & Assumptions Log

## Overview
Brief description of what we're building.

## Key Decisions

### Decision 1: [Title]
- **Context:** What problem were we solving?
- **Options Considered:**
  1. Option A - [pros/cons]
  2. Option B - [pros/cons]
- **Decision:** We chose Option X because...
- **Consequences:** This means we need to...

### Decision 2: [Title]
...

## Assumptions
| Assumption | Risk if Wrong | Mitigation |
|------------|---------------|------------|
| Users have VS Code 1.85+ | Extension won't load | Add engine check |
| Files are UTF-8 encoded | Garbled output | Add encoding detection |

## Gotchas & Learnings
- **Gotcha 1:** IDs in package.json must match exactly in code
- **Learning 1:** WebviewViewProvider requires enableScripts for JS

## For Future Reference
- If extending this feature, consider...
- Related documentation: [links]
```

### NotebookLM Export Template

At project completion, create `docs/[feature-name]-summary.md` for NotebookLM:

```markdown
# [Feature Name] - Learning Summary

## Concepts Learned
1. **Concept A:** Brief explanation
2. **Concept B:** Brief explanation

## Architecture
[ASCII diagram of system architecture]

## Key Code Flows
### Flow 1: [Name]
Step 1 → Step 2 → Step 3

## API Quick Reference
| API | Purpose | Example |
|-----|---------|---------|
| ... | ... | ... |

## Decision Summary
| Decision | Why |
|----------|-----|
| Chose X over Y | Because... |

## Patterns Used
- Pattern 1: Description
- Pattern 2: Description

## Mind Map Structure
- Main Topic
  - Subtopic A
    - Detail 1
    - Detail 2
  - Subtopic B
    - Detail 1
```

---

## Phase 3: Context is King

LLMs are only as good as the context you provide. Show them the relevant code, docs, and constraints.

### What the Model Should Know Before Coding

- High-level goals and invariants
- Examples of good solutions
- Warnings about approaches to avoid
- Which naive solutions are too slow
- Reference implementations from elsewhere
- Official docs or READMEs

### Tools for Context Management

| Tool | Purpose |
|------|---------|
| [Context7 MCP](https://github.com/anthropics/skills) | Managed context injection |
| gitingest / repo2txt | Dump relevant codebase parts into text |
| Chrome DevTools MCP | Bridge static analysis and live browser execution |

> **Warning:** Use tools like gitingest or repo2txt for public code only. For company code, develop your own plugin or prepare files manually.

Don't make the AI operate on partial information.

### Skills: Reusable AI Capabilities

Turn fragile repeated prompting into something durable and reusable. Skills package instructions, scripts, and domain-specific expertise into modular capabilities that tools automatically apply when a request matches.

**Skill Resources:**
- [Anthropic Skills](https://github.com/anthropics/skills/tree/main/skills)
- [OpenAI Skills](https://github.com/openai/skills/tree/main/skills/.system)
- [x-cmd Community Skills](https://www.x-cmd.com/skill/)
- [Skillz by intellectronica](https://github.com/intellectronica/skillz)

---

## Phase 4: Implementation

### Test-Driven Development with AI

This fits nicely with a TDD approach—write or generate tests for each piece as you go.

Instruct the LLM to run the test suite after implementing a task, and have it debug failures if any occur. This tight feedback loop (write code → run tests → fix) is something AI excels at—*as long as the tests exist*.

### Treat AI Output Like a Junior Developer's PR

Read and understand what the AI wrote, as if a junior dev on your team wrote it.

**Your Review Checklist:**

1. **Refactor into clean, modular parts** — AI output is often monolithic or messy. Break the blob into smaller, focused modules for clarity.

2. **Add missing edge-case handling** — AI often misses corner cases or error conditions. Insert null checks, input validation, etc.

3. **Strengthen types and interfaces** — If the AI used loose types or leaky abstractions, firm that up. Turn implicit assumptions into explicit contracts.

4. **Question the architecture** — Did the AI choose an inefficient approach? Maybe it brute-forced something that should use a more optimal algorithm, or introduced global state where a pure function would suffice.

5. **Write tests** — Treat AI code like any PR from a coworker: it doesn't go in until it's tested. If the AI wrote unit tests, double-check those tests aren't superficial.

---

## Phase 5: Code Review

AI-written code needs extra scrutiny because it can be superficially convincing while hiding flaws.

### Self-Review Prompt

```
Can you review this function for any errors or improvements?
```

### External Review Integration

Use tools like [CodeRabbit](https://github.com/obra/coderabbit-review-helper) for enhanced implementation reviews.

**Example Review Evaluation Prompt:**

```
A reviewer did some analysis of this PR. They're external, so reading
the codebase cold. This is their analysis of the changes and I'd like
you to evaluate the analysis and the reviewer carefully.

1) Should we hire this reviewer?
2) Which of the issues they've flagged should be fixed?
3) Are the fixes they propose the correct ones?

Anything we *should* fix, put on your todo list.
Anything we should skip, tell me about now.
```

### Quality Loop with Browser Tools

Use Chrome DevTools MCP for debugging and quality loops. Bridge the gap between static code analysis and live browser execution—inspect the DOM, get rich performance traces, console logs, or network traces.

---

## Phase 6: Version Control Best Practices

Commit often and use version control as a safety net. **Never commit code you can't explain.**

Frequent commits are your save points—they let you undo AI missteps and understand changes.

### Advanced Workflow: Git Worktrees

Don't be afraid to use branches or worktrees to isolate AI experiments:

1. Spin up a fresh git worktree for a new feature or sub-project
2. Run multiple AI coding sessions in parallel on the same repo without interference
3. Later merge the changes

It's like having each AI task in its own sandbox branch. If one experiment fails, throw away that worktree and nothing is lost in main. If it succeeds, merge it in.

---

## Configuring Your AI Assistant

Steer your AI assistant by providing style guides, examples, and rules files. A little upfront tuning yields much better outputs.

### Rules Files

Create files like `CLAUDE.md` or `GEMINI.md` containing:
- Process rules and preferences
- Code style requirements
- Lint rules to follow
- Functions to avoid
- Paradigm preferences (functional vs OOP)

### Provide In-Line Examples

Show the AI similar functions already in your codebase:

> *"Here's how we implemented X, use a similar approach for Y."*

### Reduce Hallucinations

Add prompts like:

```
If you are unsure about something or the codebase context is missing,
ask for clarification rather than making up an answer.
```

```
Always explain your reasoning briefly in comments when fixing a bug.
```

---

## Quick Reference: Key Prompts

| Stage | Prompt Purpose |
|-------|----------------|
| Starting a task | `Please read docs/plans/this-task-plan.md and <design doc>. Let me know if you have questions.` |
| Brainstorming | See Phase 1 example |
| Planning | See Phase 2 example |
| Implementation | See Phase 2.5 example (learning-first approach) |
| Review | See Phase 5 examples |

---

## Resources

### Official Skill Repositories
- [Anthropic Skills](https://github.com/anthropics/skills/tree/main/skills)
- [OpenAI Skills](https://github.com/openai/skills/tree/main/skills/.system)

### Community Resources
- [x-cmd Skill Collections](https://www.x-cmd.com/skill/)
- [Skillz](https://github.com/intellectronica/skillz)
- [CodeRabbit Review Helper](https://github.com/obra/coderabbit-review-helper)

---

## Summary

| Principle | Action |
|-----------|--------|
| Quality isn't automatic | Review all AI code thoroughly |
| Scope management is everything | One task at a time |
| Context is king | Provide relevant code, docs, constraints |
| You are accountable | Never commit code you can't explain |
| Tight feedback loops | Write code → Run tests → Fix |
| Version control is your safety net | Commit often, use worktrees |
| Document decisions | Create decision logs for future reference |
| Learning-first mindset | Capture insights for knowledge transfer |

---

*Remember: AI will happily produce plausible-looking code, but you are responsible for quality—always review and test thoroughly.*
