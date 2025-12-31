# Context Builder VS Code Extension - Technical Documentation

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Entry Points & Execution Flow](#entry-points--execution-flow)
3. [ID Matching Gotchas](#id-matching-gotchas)
4. [VS Code APIs Used](#vs-code-apis-used)
5. [Configuration File Links](#configuration-file-links)
6. [File Structure](#file-structure)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        VS Code Host                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────────┐  │
│  │ package.json │───►│  extension.ts │───►│    panel.ts      │  │
│  │ (manifest)   │    │ (orchestrator)│    │ (UI + webview)   │  │
│  └──────────────┘    └───────┬───────┘    └────────┬─────────┘  │
│                              │                      │            │
│                              │                      │            │
│                    ┌─────────▼─────────┐   ┌───────▼────────┐   │
│                    │   generator.ts    │   │   history.ts   │   │
│                    │ (content builder) │   │ (persistence)  │   │
│                    └───────────────────┘   └────────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Entry Points & Execution Flow

### 1. Extension Activation

```
User opens VS Code with workspace
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ VS Code reads package.json                                   │
│ - Finds "main": "./out/extension.js"                        │
│ - Checks "activationEvents": [] (activates immediately)     │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ VS Code calls activate(context) in extension.ts             │
│                                                              │
│ 1. Creates ContextBuilderPanel instance                     │
│ 2. Creates HistoryManager instance                          │
│ 3. Registers WebviewViewProvider                            │
│ 4. Sets up callbacks (onGenerate, onHistoryAction)          │
│ 5. Registers commands (addToContext, showFiles, clear)      │
│ 6. Adds all to context.subscriptions for cleanup            │
└─────────────────────────────────────────────────────────────┘
```

### 2. Panel Display Flow

```
User clicks "Context Builder" tab in bottom panel
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ VS Code calls panel.resolveWebviewView()                    │
│                                                              │
│ 1. Saves webviewView reference                              │
│ 2. Configures webview options (scripts, resource roots)     │
│ 3. Sets HTML content via _getHtmlContent()                  │
│ 4. Sets up message listener for webview → extension         │
└─────────────────────────────────────────────────────────────┘
```

### 3. Add to Context Flow

```
User right-clicks file(s) → "Add to Context"
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ VS Code triggers command "contextBuilder.addToContext"      │
│ (defined in package.json menus.explorer/context)            │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ Command handler in extension.ts                             │
│                                                              │
│ 1. Receives (uri, uris) - single and multi-select           │
│ 2. Filters valid URIs                                       │
│ 3. Calls panel.addFiles(validFiles)                         │
│ 4. Shows feedback message                                   │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ panel.addFiles()                                            │
│                                                              │
│ 1. Checks for duplicates (by fsPath)                        │
│ 2. Adds new SelectedFile objects to _files array            │
│ 3. Calls _updateView() to refresh HTML                      │
└─────────────────────────────────────────────────────────────┘
```

### 4. Generate Context Flow

```
User clicks "Generate Context" button
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ Webview JavaScript calls:                                   │
│ vscode.postMessage({ type: 'generate' })                    │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ panel.ts message handler                                    │
│ → calls _onGenerateCallback()                               │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ extension.ts onGenerate callback                            │
│                                                              │
│ 1. Get files from panel.getFiles()                          │
│ 2. Check for large files (checkForLargeFiles)               │
│ 3. Show warning if large files exist                        │
│ 4. Show progress notification                               │
│ 5. Call generateContext() from generator.ts                 │
│ 6. Call handleGeneratedContent()                            │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ handleGeneratedContent()                                    │
│                                                              │
│ 1. Show QuickPick (Copy/Save/Both)                          │
│ 2. Execute clipboard/file operations                        │
│ 3. Save to history via historyManager.addEntry()            │
│ 4. Update panel history via panel.updateHistory()           │
│ 5. Show confirmation message                                │
└─────────────────────────────────────────────────────────────┘
```

---

## ID Matching Gotchas

### ⚠️ Critical: Three IDs Must Match for Panel to Work

```
package.json                          panel.ts
─────────────────────────────────────────────────────────────────

"viewsContainers": {
  "panel": [{
    "id": "contextBuilderPanel",  ←─┐
    ...                              │
  }]                                 │
},                                   │
                                     │  MUST MATCH
"views": {                           │
  "contextBuilderPanel": [{     ←────┘
    "type": "webview",
    "id": "contextBuilder.panelView",  ←──┐
    ...                                    │
  }]                                       │  MUST MATCH
}                                          │
                                           │
─────────────────────────────────────────  │
                                           │
public static readonly viewType =          │
  'contextBuilder.panelView';         ←────┘
```

**Visual Analogy:**
```
┌─────────────────────────────────────────────────────────────┐
│  HOTEL ANALOGY                                              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  viewsContainers.panel[].id     = "Floor Number"            │
│  views["floorNumber"]           = "Rooms on that floor"     │
│  views[...][].id                = "Room Number"             │
│  viewType in code               = "Key to that room"        │
│                                                              │
│  If any don't match → You can't get into your room!         │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Command ID Matching

```
package.json                          extension.ts
─────────────────────────────────────────────────────────────────

"commands": [{
  "command": "contextBuilder.addToContext",  ←──┐
  ...                                           │
}]                                              │  MUST MATCH
                                                │
"menus": {                                      │
  "explorer/context": [{                        │
    "command": "contextBuilder.addToContext" ←──┤
  }]                                            │
}                                               │
                                                │
─────────────────────────────────────────────── │
                                                │
vscode.commands.registerCommand(                │
  'contextBuilder.addToContext',           ←────┘
  (uri, uris) => { ... }
);
```

---

## VS Code APIs Used

### Window APIs (`vscode.window`)

| API | Purpose | Used In |
|-----|---------|---------|
| `registerWebviewViewProvider()` | Register custom panel UI | extension.ts:39 |
| `showInformationMessage()` | Success notifications | extension.ts:127 |
| `showWarningMessage()` | Large file warnings | extension.ts:63 |
| `showErrorMessage()` | Error notifications | extension.ts:97 |
| `showQuickPick()` | Copy/Save selection dialog | extension.ts:209 |
| `showSaveDialog()` | Native file save dialog | extension.ts:241 |
| `withProgress()` | Progress indicator | extension.ts:80 |

### Workspace APIs (`vscode.workspace`)

| API | Purpose | Used In |
|-----|---------|---------|
| `asRelativePath()` | Convert URI to relative path | panel.ts:214 |
| `fs.stat()` | Get file size | generator.ts:36 |
| `fs.readFile()` | Read file contents | generator.ts:146 |
| `fs.writeFile()` | Save generated content | extension.ts:255 |

### Environment APIs (`vscode.env`)

| API | Purpose | Used In |
|-----|---------|---------|
| `clipboard.writeText()` | Copy to system clipboard | extension.ts:232 |

### Commands APIs (`vscode.commands`)

| API | Purpose | Used In |
|-----|---------|---------|
| `registerCommand()` | Register command handlers | extension.ts:105,142,164 |

### Extension Context (`vscode.ExtensionContext`)

| Property | Purpose | Used In |
|----------|---------|---------|
| `extensionUri` | Path to extension folder | extension.ts:29 |
| `subscriptions` | Cleanup on deactivate | extension.ts:177-180 |
| `globalState` | Persistent key-value storage | history.ts:68 |

### URI APIs (`vscode.Uri`)

| API | Purpose | Used In |
|-----|---------|---------|
| `Uri.file()` | Create URI from path | extension.ts:241 |
| `uri.fsPath` | Get filesystem path | panel.ts:115 |

### Progress Location

| Constant | Purpose | Used In |
|----------|---------|---------|
| `ProgressLocation.Notification` | Show progress in notification | extension.ts:82 |

---

## Configuration File Links

### package.json ↔ launch.json

```
package.json                          .vscode/launch.json
─────────────────────────────────────────────────────────────────

"main": "./out/extension.js"     ────►  Tells launch.json where
                                        to find compiled code

"scripts": {
  "compile": "tsc -p ./"         ────►  "preLaunchTask": "npm: watch"
  "watch": "tsc -watch -p ./"           runs this before debugging
}

─────────────────────────────────────────────────────────────────

                                  .vscode/launch.json:
                                  {
                                    "type": "extensionHost",
                                    "request": "launch",
                                    "args": [
                                      "--extensionDevelopmentPath=${workspaceFolder}"
                                          ↑
                                          Points to folder containing package.json
                                    ],
                                    "outFiles": [
                                      "${workspaceFolder}/out/**/*.js"
                                          ↑
                                          Must match "main" path pattern
                                    ],
                                    "preLaunchTask": "npm: watch"
                                          ↑
                                          Runs "watch" script before launch
                                  }
```

### package.json ↔ tasks.json

```
package.json                          .vscode/tasks.json
─────────────────────────────────────────────────────────────────

"scripts": {                          "tasks": [{
  "watch": "tsc -watch -p ./"    ←────  "type": "npm",
}                                       "script": "watch",
                                        "problemMatcher": "$tsc-watch",
                                        "isBackground": true
                                      }]

"scripts": {                          "tasks": [{
  "compile": "tsc -p ./"         ←────  "type": "npm",
}                                       "script": "compile",
                                        "problemMatcher": "$tsc"
                                      }]
```

### package.json ↔ tsconfig.json

```
package.json                          tsconfig.json
─────────────────────────────────────────────────────────────────

"main": "./out/extension.js"     ←────  "outDir": "./out"
                                        (compiled JS goes here)

src/extension.ts                 ────►  "rootDir": "./src"
src/panel.ts                            (source files here)
src/generator.ts
src/history.ts
```

---

## File Structure

```
vscode-extension/
├── .vscode/
│   ├── launch.json          # Debug configuration
│   └── tasks.json           # Build tasks
├── src/
│   ├── extension.ts         # Entry point, orchestration
│   ├── panel.ts             # WebviewViewProvider, UI
│   ├── generator.ts         # Content generation logic
│   └── history.ts           # Persistent storage
├── out/                      # Compiled JavaScript (gitignored)
├── package.json              # Extension manifest
├── tsconfig.json             # TypeScript configuration
└── TECHNICAL.md              # This file
```

### File Responsibilities

| File | Responsibility | Key Exports |
|------|----------------|-------------|
| `extension.ts` | Entry point, command registration, callback wiring | `activate()`, `deactivate()` |
| `panel.ts` | Webview UI, message handling, state management | `ContextBuilderPanel`, `SelectedFile` |
| `generator.ts` | File reading, content formatting | `generateContext()`, `checkForLargeFiles()` |
| `history.ts` | Persistent storage, history CRUD | `HistoryManager`, `HistoryEntry` |

---

## Debugging Tips

### 1. Extension Not Loading?
- Check `"main"` path in package.json points to compiled JS
- Ensure `npm run compile` succeeded
- Check Developer Tools console (Help → Toggle Developer Tools)

### 2. Panel Not Showing?
- Verify ID matching (see [ID Matching Gotchas](#id-matching-gotchas))
- Check `resolveWebviewView` is being called (add console.log)
- Look for errors in Debug Console

### 3. Commands Not Working?
- Verify command ID in package.json matches registerCommand
- Check command is in menus section
- Ensure handler is added to subscriptions

### 4. Webview Messages Not Received?
- Verify `enableScripts: true` in webview options
- Check message type matches switch case
- Add console.log in message handler

---

## Output Format Reference

```
================================================
PROJECT DESCRIPTION
================================================
[User's project description text]

================================================
FILE: src/components/Button.tsx
DESCRIPTION: Reusable button component
================================================
import React from 'react';
// ... file contents ...

================================================
FILE: src/utils/helpers.ts
================================================
// ... file contents (no description) ...
```
