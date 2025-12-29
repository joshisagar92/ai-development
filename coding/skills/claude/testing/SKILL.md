---
name: testing-practices
description: Testing patterns and practices. Use when writing tests, debugging test failures, or improving test coverage.
---

# Testing Practices Skill

Apply these testing practices to ensure quality.

## Test Pyramid

```
           /\         E2E Tests (Few)
          /  \        Slow, Expensive
         /    \
        /──────\      Integration Tests (Some)
       /        \     Medium Speed
      /──────────\
     /            \   Unit Tests (Many)
    /______________\  Fast, Cheap
```

## Test Structure: AAA

```python
def test_withdraw_reduces_balance():
    # ARRANGE - Set up the scenario
    account = Account(balance=100)

    # ACT - Perform the action
    account.withdraw(30)

    # ASSERT - Verify the outcome
    assert account.balance == 70
```

## Good Test Characteristics

### Tests Must Be:
- **Fast**: < 100ms per unit test
- **Independent**: No test-to-test dependencies
- **Repeatable**: Same result every time
- **Self-validating**: Pass or fail, no manual inspection

### A Test Is NOT a Unit Test If It:
- Talks to a database
- Communicates across a network
- Touches the file system
- Requires special environment configuration

## Test Doubles

| Type | Purpose | Example Use |
|------|---------|-------------|
| **Stub** | Return canned answers | "Always return $10 as price" |
| **Mock** | Verify interactions | "Verify email was sent" |
| **Fake** | Working but simplified | In-memory database |
| **Spy** | Record what happened | "What methods were called?" |

### When to Use What

```python
# STUB: You need a predictable response
stub_price_service.get_price.return_value = 10.00

# MOCK: You need to verify behavior
mock_email_service.send.assert_called_once_with("user@example.com")

# FAKE: You need working behavior without real infrastructure
fake_repo = InMemoryRepository()
```

## Common Test Patterns

### Testing Exceptions
```python
def test_empty_account_rejects_withdrawal():
    account = Account(balance=0)

    with pytest.raises(InsufficientFunds):
        account.withdraw(10)
```

### Testing Edge Cases
Always test:
- Empty collections
- Null/None values
- Boundary values (0, 1, max)
- Invalid inputs

### Test Data Builders
```python
class OrderBuilder:
    def __init__(self):
        self.items = []
        self.customer = default_customer()

    def with_item(self, item):
        self.items.append(item)
        return self

    def build(self):
        return Order(self.customer, self.items)

# Usage
order = OrderBuilder().with_item(laptop).with_item(mouse).build()
```

## When Tests Are Hard to Write

Difficulty writing tests signals design problems:

| Problem | Solution |
|---------|----------|
| Too many dependencies | Extract interfaces, use DI |
| Hidden dependencies | Make explicit via constructor |
| Static methods | Wrap in instance methods |
| Global state | Pass as parameters |
| Long setup | Class doing too much; split it |

## Test Quality Checklist

- [ ] Test names describe behavior, not implementation
- [ ] One assertion per test (generally)
- [ ] No test-to-test dependencies
- [ ] No sleeps (use explicit waits if needed)
- [ ] Clear failure messages
- [ ] Tests run in any order
