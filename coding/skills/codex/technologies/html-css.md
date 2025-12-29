---
name: html-css
description: HTML5 and CSS3 best practices including accessibility, semantic markup, and responsive design. Use when writing HTML, CSS, or frontend markup.
---

# HTML & CSS Best Practices

## Semantic HTML5

Use semantic elements for meaning, not just styling.

```html
<!-- Good: Semantic structure -->
<header>
  <nav aria-label="Main navigation">
    <ul>
      <li><a href="/">Home</a></li>
      <li><a href="/about">About</a></li>
    </ul>
  </nav>
</header>

<main>
  <article>
    <h1>Article Title</h1>
    <p>Content...</p>
  </article>
  <aside>
    <h2>Related Links</h2>
  </aside>
</main>

<footer>
  <p>&copy; 2024 Company Name</p>
</footer>

<!-- Bad: Non-semantic divs -->
<div class="header">
  <div class="nav">...</div>
</div>
```

## Semantic Elements Reference

| Element | Use Case |
|---------|----------|
| `<header>` | Introductory content, navigation |
| `<nav>` | Navigation links |
| `<main>` | Main content (one per page) |
| `<article>` | Self-contained content |
| `<section>` | Thematic grouping |
| `<aside>` | Tangentially related content |
| `<footer>` | Footer content |
| `<figure>` | Self-contained media |
| `<time>` | Dates and times |

## Accessibility (WCAG 2.1)

### Always Provide Alt Text

```html
<!-- Good: Descriptive alt text -->
<img src="chart.png" alt="Sales increased 25% from Q1 to Q2 2024">

<!-- Good: Decorative images -->
<img src="decorative-border.png" alt="" role="presentation">

<!-- Bad: Missing or unhelpful alt -->
<img src="chart.png">
<img src="chart.png" alt="image">
```

### Form Accessibility

```html
<!-- Good: Proper labels -->
<label for="email">Email Address</label>
<input type="email" id="email" name="email" required aria-describedby="email-hint">
<span id="email-hint">We'll never share your email</span>

<!-- Good: Error handling -->
<input type="email" id="email" aria-invalid="true" aria-describedby="email-error">
<span id="email-error" role="alert">Please enter a valid email address</span>

<!-- Bad: No label association -->
<label>Email</label>
<input type="email">
```

### Keyboard Navigation

```html
<!-- Good: Skip link for keyboard users -->
<a href="#main-content" class="skip-link">Skip to main content</a>

<!-- Good: Focus visible -->
<style>
  a:focus, button:focus {
    outline: 2px solid #005fcc;
    outline-offset: 2px;
  }
</style>

<!-- Good: Logical tab order -->
<button tabindex="0">First</button>
<button tabindex="0">Second</button>
```

### ARIA Landmarks

```html
<header role="banner">...</header>
<nav role="navigation" aria-label="Main">...</nav>
<main role="main">...</main>
<aside role="complementary">...</aside>
<footer role="contentinfo">...</footer>
```

## CSS Best Practices

### Use CSS Custom Properties

```css
:root {
  /* Colors */
  --color-primary: #005fcc;
  --color-secondary: #6c757d;
  --color-error: #dc3545;

  /* Typography */
  --font-family-base: system-ui, -apple-system, sans-serif;
  --font-size-base: 1rem;
  --line-height-base: 1.5;

  /* Spacing */
  --spacing-xs: 0.25rem;
  --spacing-sm: 0.5rem;
  --spacing-md: 1rem;
  --spacing-lg: 2rem;
}

.button {
  background-color: var(--color-primary);
  padding: var(--spacing-sm) var(--spacing-md);
}
```

### Mobile-First Responsive Design

```css
/* Base styles (mobile) */
.container {
  padding: var(--spacing-sm);
}

.grid {
  display: grid;
  gap: var(--spacing-md);
  grid-template-columns: 1fr;
}

/* Tablet and up */
@media (min-width: 768px) {
  .container {
    padding: var(--spacing-md);
  }

  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* Desktop and up */
@media (min-width: 1024px) {
  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: var(--spacing-lg);
  }

  .grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
```

### Use Flexbox and Grid

```css
/* Flexbox for 1D layouts */
.nav-list {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
}

.card {
  display: flex;
  flex-direction: column;
}

.card-body {
  flex: 1; /* Takes remaining space */
}

/* Grid for 2D layouts */
.page-layout {
  display: grid;
  grid-template-areas:
    "header header"
    "sidebar main"
    "footer footer";
  grid-template-columns: 250px 1fr;
  grid-template-rows: auto 1fr auto;
  min-height: 100vh;
}
```

### Avoid Common Mistakes

```css
/* Bad: Fixed pixel widths */
.container {
  width: 960px;
}

/* Good: Flexible with max-width */
.container {
  width: 100%;
  max-width: 1200px;
}

/* Bad: Using !important */
.button {
  color: red !important;
}

/* Good: Increase specificity if needed */
.sidebar .button {
  color: red;
}

/* Bad: Magic numbers */
.header {
  margin-top: 37px;
}

/* Good: Use variables or explain */
.header {
  margin-top: var(--header-offset);
}
```

## HTML Validation

```html
<!-- Required meta tags -->
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <meta name="description" content="Page description for SEO">
  <title>Page Title</title>
</head>
```

## Performance

```html
<!-- Preload critical resources -->
<link rel="preload" href="/fonts/main.woff2" as="font" type="font/woff2" crossorigin>
<link rel="preload" href="/css/critical.css" as="style">

<!-- Defer non-critical JavaScript -->
<script src="/js/analytics.js" defer></script>

<!-- Lazy load images below fold -->
<img src="image.jpg" loading="lazy" alt="Description">
```

## Quick Reference

| Practice | Rule |
|----------|------|
| Structure | Semantic HTML5 elements |
| Images | Always include alt text |
| Forms | Associate labels with inputs |
| Focus | Visible focus indicators |
| Colors | Use CSS custom properties |
| Layout | Flexbox for 1D, Grid for 2D |
| Responsive | Mobile-first approach |
| Performance | Preload, defer, lazy load |
