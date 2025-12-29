---
name: design-practices
description: Design patterns and architecture principles. Use when making design decisions, structuring code, or reviewing architecture.
---

# Design Practices Skill

Apply these design principles for clean, maintainable code.

## SOLID Principles

### Single Responsibility
Each class should have one reason to change.

```python
# BAD: Multiple responsibilities
class UserService:
    def create_user(self, data): ...
    def send_email(self, user): ...
    def generate_report(self): ...

# GOOD: Single responsibility each
class UserService:
    def create_user(self, data): ...

class EmailService:
    def send_email(self, user): ...

class ReportService:
    def generate_report(self): ...
```

### Open/Closed
Open for extension, closed for modification.

### Dependency Inversion
Depend on abstractions, not concretions.

```python
# BAD: Depends on concrete class
class OrderService:
    def __init__(self):
        self.repo = MySQLOrderRepository()  # Hard dependency

# GOOD: Depends on abstraction
class OrderService:
    def __init__(self, repo: OrderRepository):
        self.repo = repo  # Injected, can be any implementation
```

## Tell, Don't Ask

```python
# BAD: Asking for data and deciding
if customer.get_balance() > amount:
    customer.set_balance(customer.get_balance() - amount)

# GOOD: Telling the object what to do
customer.withdraw(amount)  # Object handles its own logic
```

## Law of Demeter

Only talk to immediate friends:

```python
# BAD: Train wreck
customer.get_wallet().get_card().charge(amount)

# GOOD: One level deep
customer.charge(amount)
```

## Four Rules of Simple Design (Priority Order)

1. **Passes tests** - It works
2. **Reveals intention** - It's clear
3. **No duplication** - DRY
4. **Fewest elements** - YAGNI

## Common Patterns

### Repository Pattern
```python
class UserRepository:
    def find_by_id(self, id: int) -> User: ...
    def find_by_email(self, email: str) -> User: ...
    def save(self, user: User) -> User: ...
    def delete(self, id: int) -> None: ...
```

### Factory Pattern
```python
class NotificationFactory:
    @staticmethod
    def create(type: str) -> Notification:
        if type == "email":
            return EmailNotification()
        elif type == "sms":
            return SMSNotification()
        raise ValueError(f"Unknown type: {type}")
```

### Strategy Pattern
```python
class PaymentProcessor:
    def __init__(self, strategy: PaymentStrategy):
        self.strategy = strategy

    def process(self, amount):
        return self.strategy.pay(amount)

# Usage
processor = PaymentProcessor(CreditCardStrategy())
processor.process(100)
```

## Anti-Patterns to Avoid

### Anemic Domain Model
Objects with data but no behavior. Put behavior where data is.

### God Class
Class that does everything. Split by responsibility.

### Premature Abstraction
Abstracting before you have 3 concrete examples.

### Speculative Generality
"We might need this someday." YAGNI.

## Questions to Ask

For design decisions:
1. "What's the single responsibility?"
2. "Is this the simplest solution?"
3. "Can this be extended without modification?"

For abstractions:
1. "Do I have 3 concrete examples?"
2. "Is this solving a real problem?"
3. "What's the cost of NOT abstracting?"

For dependencies:
1. "Am I depending on abstractions?"
2. "Can this be injected?"
3. "What's the coupling level?"

## Design Checklist

- [ ] Each class has one responsibility
- [ ] Dependencies are injected, not created
- [ ] Abstractions are based on real needs
- [ ] Code tells, doesn't ask
- [ ] No train wrecks (Law of Demeter)
- [ ] No premature optimization
- [ ] No speculative features
