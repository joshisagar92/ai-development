---
name: javascript-angular
description: JavaScript, Angular, and AngularJS best practices. Use when writing frontend code, Angular components, or AngularJS applications.
---

# JavaScript & Angular/AngularJS

> **Domain Experts**: Todd Motto (AngularJS Expert), John Papa (Angular Style Guide Author), Ward Bell (Angular Team)

## Angular (2+) Rules

### One Thing Per File

Each file should contain exactly one component, service, or module.

```typescript
// Good: one-component.component.ts
@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html'
})
export class UserProfileComponent { }

// Bad: Multiple components in one file
```

### Smart/Dumb Component Pattern

- **Container (Smart)**: Handle business logic, connect to services
- **Presentational (Dumb)**: Receive data via @Input, emit events via @Output

```typescript
// Container component
@Component({ ... })
export class UserListContainerComponent {
  users$ = this.userService.getUsers();
  constructor(private userService: UserService) {}
}

// Presentational component
@Component({ ... })
export class UserCardComponent {
  @Input() user: User;
  @Output() selected = new EventEmitter<User>();
}
```

### OnPush Change Detection

Improves performance by reducing change detection cycles.

```typescript
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  ...
})
export class PerformantComponent { }
```

### Unsubscribe from Observables

Prevent memory leaks.

```typescript
// Good: Use async pipe (auto-unsubscribes)
<div *ngFor="let user of users$ | async">{{ user.name }}</div>

// Good: takeUntil pattern
private destroy$ = new Subject<void>();

ngOnInit() {
  this.data$.pipe(takeUntil(this.destroy$)).subscribe();
}

ngOnDestroy() {
  this.destroy$.next();
  this.destroy$.complete();
}
```

## AngularJS (1.x) Rules

### Use Component Architecture

In AngularJS 1.5+, use `.component()` instead of `.directive()`.

```javascript
// Good: Component-based
angular.module('app').component('todoItem', {
  bindings: {
    todo: '<',
    onDelete: '&'
  },
  template: `<div>{{ $ctrl.todo.title }}</div>`,
  controller: function() { }
});
```

### One-Way Data Flow

Use one-way binding (`<`) and callbacks (`&`).

```javascript
bindings: {
  items: '<',        // One-way down
  onUpdate: '&'      // Callback up
}
```

### Use $onInit Lifecycle Hook

Initialize in `$onInit`, not the constructor.

```javascript
controller: function() {
  this.$onInit = function() {
    this.items = [];
    this.loadItems();
  };
}
```

### Folder-by-Feature Structure

```
// Good: Feature-based
app/
  users/
    user-list.component.js
    user-detail.component.js
    user.service.js
  orders/
    order-list.component.js
    order.service.js

// Bad: Type-based
app/
  controllers/
  services/
  directives/
```

## Quick Reference

| Framework | Practice | Rule |
|-----------|----------|------|
| Angular 2+ | Files | One thing per file |
| Angular 2+ | Components | Smart/Dumb pattern |
| Angular 2+ | Performance | OnPush change detection |
| Angular 2+ | Memory | Unsubscribe from observables |
| AngularJS | Components | Use .component() |
| AngularJS | Data | One-way binding |
| AngularJS | Lifecycle | Use $onInit |
| Both | Structure | Folder-by-feature |
