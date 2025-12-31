# Context Builder

IDE plugins for generating LLM context files from your codebase. Similar to gitingest/repo2txt but with IDE integration, per-file descriptions, and history tracking.

## Features

| Feature | Description |
|---------|-------------|
| **File Selection** | Right-click files in explorer to add to context |
| **Project Description** | Add optional description for your project |
| **Per-File Descriptions** | Annotate individual files with context |
| **Large File Warnings** | Warns when files exceed 100KB |
| **Copy & Save** | Copy to clipboard or save to file |
| **History** | Last 10 generations stored persistently |
| **Re-use History** | Copy or save previous generations |

## Plugins

| Plugin | Location | Status |
|--------|----------|--------|
| VS Code | `vscode-extension/` | ✅ Complete |
| JetBrains | `jetbrains-plugin/` | ✅ Complete |

## Output Format (Gitingest Style)

```
================================================
PROJECT DESCRIPTION
================================================
Building a React dashboard with authentication

================================================
FILE: src/components/Login.tsx
DESCRIPTION: Main login form with OAuth support
================================================
import React from 'react';
import { useAuth } from '../hooks/useAuth';

export function Login() {
  // ... file contents ...
}

================================================
FILE: src/hooks/useAuth.ts
================================================
// ... file contents (no description provided) ...
```

## Quick Start

### VS Code Extension

```bash
cd vscode-extension
npm install
npm run compile
# Press F5 to launch Extension Development Host
```

### JetBrains Plugin (IntelliJ IDEA, WebStorm, PyCharm, etc.)

```bash
# Option 1: Open in IntelliJ IDEA
1. Open IntelliJ IDEA
2. File → Open → Select jetbrains-plugin folder
3. Wait for Gradle sync
4. Run → Run Plugin (or green play button)

# Option 2: Build from command line (requires Gradle)
cd jetbrains-plugin
./gradlew buildPlugin
# Install from build/distributions/context-builder-1.0.0.zip
```

### Usage (Both Plugins)

1. **Add files**: Right-click file(s) in Explorer/Project View → "Add to Context"
2. **Add descriptions** (optional): Type in the description field for each file
3. **Add project description** (optional): Type in the top textarea
4. **Generate**: Click "Generate Context" button
5. **Choose output**: Select Copy/Save/Both from the dialog
6. **Re-use**: Click Copy/Save on history items to re-copy/re-save

## Architecture

### VS Code Extension (TypeScript)

```
┌─────────────────────────────────────────────────────────────┐
│                     extension.ts                             │
│                    (Orchestrator)                            │
│  - Registers commands and panel                              │
│  - Wires up callbacks between components                     │
│  - Handles clipboard/file operations                         │
└──────────────┬────────────────────────────┬─────────────────┘
               │                            │
               ▼                            ▼
┌──────────────────────────┐  ┌──────────────────────────────┐
│       panel.ts           │  │       generator.ts            │
│   (UI & State)           │  │   (Content Generation)        │
│  - WebviewViewProvider   │  │  - Read file contents         │
│  - HTML/CSS/JS UI        │  │  - Format output              │
│  - Message handling      │  │  - Check file sizes           │
└──────────────────────────┘  └──────────────────────────────┘
               │
               ▼
┌──────────────────────────┐
│      history.ts          │
│   (Persistence)          │
│  - globalState storage   │
│  - Last 10 generations   │
└──────────────────────────┘
```

### JetBrains Plugin (Java)

```
┌─────────────────────────────────────────────────────────────┐
│                  ContextBuilderService                       │
│                    (State Manager)                           │
│  - Holds selected files                                      │
│  - Project description                                       │
│  - Change listeners for UI updates                           │
└──────────────┬────────────────────────────┬─────────────────┘
               │                            │
               ▼                            ▼
┌──────────────────────────┐  ┌──────────────────────────────┐
│  ContextBuilderPanel     │  │    ContextGenerator          │
│   (UI - Swing)           │  │   (Content Generation)        │
│  - JPanel with components│  │  - Read file contents         │
│  - Event handlers        │  │  - Format output              │
│  - History display       │  │  - Check file sizes           │
└──────────────────────────┘  └──────────────────────────────┘
               │
               ▼
┌──────────────────────────┐    ┌──────────────────────────┐
│  ContextBuilderState     │    │      Actions             │
│   (Persistence)          │    │  - AddToContextAction    │
│  - PersistentStateComponent   │  - ShowPanelAction       │
│  - XML serialization     │    │  - ClearFilesAction      │
└──────────────────────────┘    └──────────────────────────┘
```

## Technical Documentation

| Plugin | Documentation |
|--------|---------------|
| VS Code | [vscode-extension/TECHNICAL.md](vscode-extension/TECHNICAL.md) |
| JetBrains | [jetbrains-plugin/TECHNICAL.md](jetbrains-plugin/TECHNICAL.md) |

## Configuration

### Large File Threshold

Files over 100KB trigger a warning before generation.

| Plugin | File | Constant |
|--------|------|----------|
| VS Code | `generator.ts` | `LARGE_FILE_THRESHOLD = 100 * 1024` |
| JetBrains | `ContextGenerator.java` | `LARGE_FILE_THRESHOLD = 100 * 1024` |

### History Limit

Maximum 10 history entries are stored.

| Plugin | File | Constant |
|--------|------|----------|
| VS Code | `history.ts` | `MAX_HISTORY_ENTRIES = 10` |
| JetBrains | `ContextBuilderState.java` | `MAX_HISTORY = 10` |

## APIs Used

### VS Code APIs

| API | Purpose |
|-----|---------|
| `WebviewViewProvider` | Custom panel in bottom area |
| `vscode.commands.registerCommand` | Right-click menu actions |
| `vscode.window.showQuickPick` | Copy/Save selection dialog |
| `vscode.env.clipboard` | System clipboard access |
| `vscode.workspace.fs` | File read/write operations |
| `context.globalState` | Persistent history storage |

### JetBrains/IntelliJ APIs

| API | Purpose |
|-----|---------|
| `ToolWindowFactory` | Custom panel in bottom area |
| `AnAction` | Right-click menu actions |
| `FileSaverDialog` | Save file dialog |
| `Toolkit.getSystemClipboard()` | System clipboard access |
| `VirtualFile` | File abstraction and reading |
| `PersistentStateComponent` | Persistent history storage |

## File Structure

```
context-builder/
├── README.md                     # This file
├── vscode-extension/
│   ├── src/
│   │   ├── extension.ts          # Entry point
│   │   ├── panel.ts              # UI provider
│   │   ├── generator.ts          # Content generation
│   │   └── history.ts            # Persistence
│   ├── package.json              # Extension manifest
│   ├── tsconfig.json             # TypeScript config
│   └── TECHNICAL.md              # Technical documentation
└── jetbrains-plugin/
    ├── src/main/
    │   ├── java/com/contextbuilder/
    │   │   ├── ContextBuilderService.java    # State management
    │   │   ├── ContextBuilderPanel.java      # UI (Swing)
    │   │   ├── ContextBuilderState.java      # Persistence
    │   │   ├── ContextBuilderToolWindowFactory.java
    │   │   ├── ContextGenerator.java         # Content generation
    │   │   └── actions/
    │   │       ├── AddToContextAction.java
    │   │       ├── ShowPanelAction.java
    │   │       └── ClearFilesAction.java
    │   └── resources/META-INF/
    │       └── plugin.xml                    # Plugin manifest
    ├── build.gradle.kts          # Build configuration
    └── TECHNICAL.md              # Technical documentation
```

## Platform Comparison

| Aspect | VS Code | JetBrains |
|--------|---------|-----------|
| Language | TypeScript | Java |
| UI Framework | HTML/CSS (Webview) | Swing (JPanel) |
| Manifest | package.json | plugin.xml |
| Build Tool | npm | Gradle |
| Commands | registerCommand() | AnAction classes |
| Persistence | globalState (JSON) | PersistentStateComponent (XML) |
| Test Method | F5 (Extension Host) | Run Plugin (Sandbox IDE) |
| Distribution | .vsix file | .zip file |

## License

MIT
