---
name: programming-practices
description: Core programming practices including TDD, refactoring, and coding standards. Use when writing code, reviewing code, fixing bugs, or refactoring.
---

# Programming Practices Skill

Apply these practices when writing or reviewing code.

## TDD Workflow

```
1. RED:    Write a failing test first
2. GREEN:  Write minimum code to pass
3. REFACTOR: Clean up while tests stay green
```

### Before Writing Code
- Write a failing test that describes the expected behavior
- Ensure the test fails for the right reason
- Compilation failure counts as RED

### While Writing Code
- Write minimum code to make the test pass
- "Fake it" (hardcoded values) is acceptable temporarily
- Don't worry about elegance yet

### After Tests Pass
- Refactor to remove duplication
- Improve names for clarity
- Extract methods if > 10-15 lines
- Keep tests green throughout

## Code Quality Checklist

### Functions Should Be
- Small (one level of abstraction)
- Doing one thing
- Using descriptive names
- Having 0-3 parameters

### When I See These Smells, I Should Refactor

| Smell | Action |
|-------|--------|
| Method > 15 lines | Extract Method |
| > 3 parameters | Introduce Parameter Object |
| Duplicated code | Extract and reuse |
| Feature envy (using another class's data) | Move Method |
| Primitive obsession (strings for IDs, emails) | Replace with Value Object |
| Comments explaining "what" | Refactor code to be clearer |

## Questions I Should Ask

Before implementing:
1. "Can I write a test for this?"
2. "What's the simplest solution?"
3. "Is there existing code that does something similar?"

During implementation:
1. "Am I making this too complex?"
2. "Would someone else understand this?"
3. "Are there edge cases I'm missing?"

After implementation:
1. "Can I improve the names?"
2. "Is there duplication to remove?"
3. "Are the tests comprehensive?"

## Never Do

- Refactor without tests (except minimal changes to add tests)
- Mix refactoring with feature work in same commit
- Add features "just in case" (YAGNI)
- Commit code I can't explain
