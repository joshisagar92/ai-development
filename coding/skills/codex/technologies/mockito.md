---
name: mockito
description: Mockito best practices for unit testing. Use when writing unit tests, mocking dependencies, or verifying interactions.
---

# Mockito

> **Domain Experts**: Szczepan Faber (Creator), Steve Freeman & Nat Pryce ("Growing Object-Oriented Software, Guided by Tests")

## Mock Roles, Not Objects

Mock collaborator interfaces, not concrete implementations.

```java
// Good: Mock the interface
@Mock
private PaymentGateway paymentGateway;

// Avoid: Mocking concrete classes when interfaces exist
```

## Don't Mock What You Don't Own

Use integration tests for third-party libraries; mock your own abstractions.

```java
// Bad: Mocking HttpClient directly
@Mock
private HttpClient httpClient;

// Good: Create an abstraction you control
interface WeatherService {
    Weather getWeather(String city);
}
@Mock
private WeatherService weatherService;
```

## Stub Queries, Verify Commands

Following CQS (Command-Query Separation):
- **Stub** methods that return values
- **Verify** methods that perform actions

```java
// Stub a query
when(repository.findById(1L)).thenReturn(Optional.of(user));

// Verify a command
verify(emailService).sendWelcomeEmail(user);
```

## Prefer State Verification Over Behavior

Test outcomes, not implementation details.

```java
// Good: State verification
User result = userService.createUser(request);
assertThat(result.getEmail()).isEqualTo("test@example.com");

// Avoid excessive behavior verification
verify(userMapper, times(1)).toEntity(any());
verify(repository, times(1)).save(any());
```

## Use @InjectMocks for Test Subject

Let Mockito inject mocks into the class under test.

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderService orderService;
}
```

## Use ArgumentCaptor for Complex Assertions

Capture arguments when you need to verify complex objects.

```java
@Captor
private ArgumentCaptor<Order> orderCaptor;

@Test
void shouldCreateOrderWithCorrectDetails() {
    orderService.placeOrder(request);

    verify(orderRepository).save(orderCaptor.capture());
    Order captured = orderCaptor.getValue();
    assertThat(captured.getTotal()).isEqualTo(expectedTotal);
}
```

## Use BDDMockito for Readability

Given-When-Then syntax improves test readability.

```java
import static org.mockito.BDDMockito.*;

// Given
given(userRepository.findById(1L)).willReturn(Optional.of(user));

// When
User result = userService.getUser(1L);

// Then
then(userRepository).should().findById(1L);
```

## Quick Reference

| Practice | Rule |
|----------|------|
| What to Mock | Interfaces (roles), not objects |
| Ownership | Don't mock what you don't own |
| Queries | Stub them |
| Commands | Verify them |
| Verification | Prefer state over behavior |
| Injection | Use @InjectMocks |
| Complex Args | Use ArgumentCaptor |
| Readability | Use BDDMockito |
