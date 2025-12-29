---
name: jinja2
description: Jinja2 templating best practices. Use when writing Python templates, Flask/FastAPI templates, or HTML generation.
---

# Jinja2 Templating

> **Domain Expert**: Armin Ronacher (Creator of Jinja2 and Flask)

## Always Enable Autoescaping

Jinja2 escapes HTML by default - never disable this.

```python
from jinja2 import Environment, select_autoescape

env = Environment(
    autoescape=select_autoescape(['html', 'xml'])
)
```

## Use |safe Filter Cautiously

Only use when you're absolutely certain content is safe.

```html
<!-- Only use |safe for trusted, sanitized content -->
{{ trusted_html|safe }}

<!-- Default: escaped (XSS-safe) -->
{{ user_input }}
```

## Use Template Inheritance

DRY principle: base templates with blocks.

```html
<!-- base.html -->
<!DOCTYPE html>
<html>
<head><title>{% block title %}{% endblock %}</title></head>
<body>
    <header>{% include 'header.html' %}</header>
    {% block content %}{% endblock %}
    <footer>{% include 'footer.html' %}</footer>
</body>
</html>

<!-- page.html -->
{% extends 'base.html' %}
{% block title %}My Page{% endblock %}
{% block content %}
    <h1>Welcome</h1>
{% endblock %}
```

## Keep Logic Minimal in Templates

Templates should focus on presentation, not computation.

```html
<!-- Good: Simple display logic -->
{% if user.is_authenticated %}
    Welcome, {{ user.name }}
{% endif %}

<!-- Bad: Complex business logic in templates -->
{% set discount = calculate_complex_discount(items, user, promotions) %}
```

## Use Macros for Reusable Components

```html
<!-- macros.html -->
{% macro input(name, label, type='text') %}
    <div class="form-group">
        <label for="{{ name }}">{{ label }}</label>
        <input type="{{ type }}" name="{{ name }}" id="{{ name }}">
    </div>
{% endmacro %}

<!-- usage.html -->
{% from 'macros.html' import input %}
{{ input('email', 'Email Address', 'email') }}
```

## Use url_for() for URL Generation

Never hardcode URLs.

```html
<!-- Good: url_for is safe from URL structure changes -->
<a href="{{ url_for('user.profile', user_id=user.id) }}">Profile</a>

<!-- Bad: Hardcoded URL -->
<a href="/users/{{ user.id }}/profile">Profile</a>
```

## Use Default Filter for Missing Variables

```html
{{ user.nickname|default('Anonymous') }}
```

## Whitespace Control

Control whitespace with `-` in tags.

```html
<!-- With whitespace -->
{% if items %}
    <ul>
    {% for item in items %}
        <li>{{ item }}</li>
    {% endfor %}
    </ul>
{% endif %}

<!-- Trimmed whitespace -->
{%- if items -%}
<ul>
{%- for item in items -%}
<li>{{ item }}</li>
{%- endfor -%}
</ul>
{%- endif -%}
```

## Loop Utilities

```html
{% for item in items %}
    {% if loop.first %}<ul>{% endif %}
    <li class="{{ 'even' if loop.index is even else 'odd' }}">
        {{ loop.index }}. {{ item }}
    </li>
    {% if loop.last %}</ul>{% endif %}
{% else %}
    <p>No items found.</p>
{% endfor %}
```

| Property | Description |
|----------|-------------|
| `loop.index` | Current iteration (1-based) |
| `loop.index0` | Current iteration (0-based) |
| `loop.first` | True if first iteration |
| `loop.last` | True if last iteration |
| `loop.length` | Total number of items |

## Quick Reference

| Practice | Rule |
|----------|------|
| Autoescaping | Always enabled |
| |safe | Only for trusted content |
| Inheritance | Use base templates |
| Logic | Keep minimal in templates |
| Reuse | Use macros |
| URLs | Use url_for() |
| Defaults | Use |default filter |
