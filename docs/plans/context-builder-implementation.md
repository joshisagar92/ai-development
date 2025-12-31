# Context Builder - Implementation Plan

> A plugin for VS Code and JetBrains IDEs that generates LLM-ready context files from selected code.

## Document Info

| Item | Value |
|------|-------|
| Created | 2024-12-30 |
| Status | Ready for Implementation |
| Platforms | VS Code (TypeScript), JetBrains (Java) |

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture](#2-architecture)
3. [VS Code Extension Tasks](#3-vscode-extension-tasks)
4. [JetBrains Plugin Tasks](#4-jetbrains-plugin-tasks)
5. [Testing Guide](#5-testing-guide)
6. [Definition of Done](#6-definition-of-done)

---

## 1. Overview

### What We're Building

A plugin that lets developers:
1. Select files/folders from the IDE file explorer
2. Add optional descriptions to each file
3. Generate a text file with all contents (for pasting into LLMs)

### Output Format

```
================================================
PROJECT DESCRIPTION
================================================
[User's project description text]

================================================
FILE: src/auth/LoginService.java
DESCRIPTION: Main login logic
================================================
[Complete file contents]

================================================
FILE: src/models/User.java
================================================
[Complete file contents]
```

### Key Features

| Feature | Description |
|---------|-------------|
| File Selection | Right-click → "Add to Context" (multi-select) |
| UI Location | Bottom panel (like terminal) |
| Descriptions | Optional per-file + overall project description |
| Output | Copy to clipboard OR save to file |
| Large Files | Warning at 100KB, offer chunking |
| History | Last 10 generations with reload capability |

---

## 2. Architecture

### Project Structure

```
context-builder/
├── README.md                    # Project overview
│
├── vscode-extension/            # VS Code plugin
│   ├── package.json             # Extension manifest
│   ├── tsconfig.json            # TypeScript config
│   ├── src/
│   │   ├── extension.ts         # Entry point
│   │   ├── contextBuilder.ts    # Core logic
│   │   ├── panel.ts             # Bottom panel UI
│   │   ├── history.ts           # History management
│   │   └── generator.ts         # Output generation
│   └── test/
│       └── suite/
│           ├── generator.test.ts
│           └── history.test.ts
│
└── jetbrains-plugin/            # IntelliJ plugin
    ├── build.gradle.kts         # Build config
    ├── src/main/
    │   ├── resources/
    │   │   └── META-INF/
    │   │       └── plugin.xml   # Plugin manifest
    │   └── java/com/contextbuilder/
    │       ├── ContextBuilderAction.java
    │       ├── ContextBuilderPanel.java
    │       ├── ContextGenerator.java
    │       └── HistoryManager.java
    └── src/test/java/com/contextbuilder/
        ├── ContextGeneratorTest.java
        └── HistoryManagerTest.java
```

### Data Flow

```
User right-clicks files
        ↓
Files added to ContextBuilder state
        ↓
User adds descriptions (optional)
        ↓
User clicks "Generate"
        ↓
Generator reads all files
        ↓
Output formatted as text
        ↓
Copy to clipboard OR save to file
        ↓
Entry added to history
```

---

## 3. VS Code Extension Tasks

### Prerequisites

Before starting, ensure you have:
- Node.js 18+ installed
- VS Code installed
- `yo` and `generator-code` installed globally:
  ```bash
  npm install -g yo generator-code
  ```

### Task 1: Project Scaffolding

**Goal:** Create the VS Code extension project structure.

**Steps:**

1. Navigate to the context-builder folder:
   ```bash
   cd C:/projects/ai/context/context-builder
   ```

2. Generate extension scaffold:
   ```bash
   yo code
   ```

   Answer the prompts:
   - Type: `New Extension (TypeScript)`
   - Name: `context-builder`
   - Identifier: `context-builder`
   - Description: `Generate LLM context from selected files`
   - Initialize git: `No` (we already have git)
   - Package manager: `npm`

3. Rename the generated folder:
   ```bash
   mv context-builder vscode-extension
   ```

4. Verify structure exists:
   ```
   vscode-extension/
   ├── package.json
   ├── tsconfig.json
   ├── src/
   │   └── extension.ts
   └── .vscode/
       └── launch.json
   ```

5. Test it works:
   ```bash
   cd vscode-extension
   npm install
   npm run compile
   ```
   Press F5 in VS Code to launch Extension Development Host.

**Commit:** `feat(vscode): scaffold extension project`

---

### Task 2: Register Context Menu Command

**Goal:** Add "Add to Context" to the file explorer right-click menu.

**Files to modify:**
- `vscode-extension/package.json`
- `vscode-extension/src/extension.ts`

**Steps:**

1. Open `package.json` and add to `contributes` section:

```json
{
  "contributes": {
    "commands": [
      {
        "command": "contextBuilder.addToContext",
        "title": "Add to Context"
      }
    ],
    "menus": {
      "explorer/context": [
        {
          "command": "contextBuilder.addToContext",
          "group": "navigation"
        }
      ]
    }
  }
}
```

2. Open `src/extension.ts` and register the command:

```typescript
import * as vscode from 'vscode';

// Store selected files (global state for now)
let selectedFiles: vscode.Uri[] = [];

export function activate(context: vscode.ExtensionContext) {

    const addToContext = vscode.commands.registerCommand(
        'contextBuilder.addToContext',
        (uri: vscode.Uri, uris: vscode.Uri[]) => {
            // Handle multi-select: uris contains all selected files
            const filesToAdd = uris || [uri];

            filesToAdd.forEach(file => {
                if (!selectedFiles.find(f => f.fsPath === file.fsPath)) {
                    selectedFiles.push(file);
                }
            });

            vscode.window.showInformationMessage(
                `Added ${filesToAdd.length} file(s) to context`
            );
        }
    );

    context.subscriptions.push(addToContext);
}

export function deactivate() {}
```

3. Test manually:
   - Press F5 to launch Extension Development Host
   - Right-click a file in explorer
   - You should see "Add to Context"
   - Click it and see the notification

**Commit:** `feat(vscode): add context menu command`

---

### Task 3: Create Bottom Panel UI

**Goal:** Create a webview panel in the bottom area showing selected files.

**Files to create:**
- `vscode-extension/src/panel.ts`

**Files to modify:**
- `vscode-extension/package.json`
- `vscode-extension/src/extension.ts`

**Steps:**

1. Add panel view to `package.json`:

```json
{
  "contributes": {
    "commands": [
      {
        "command": "contextBuilder.addToContext",
        "title": "Add to Context"
      },
      {
        "command": "contextBuilder.showPanel",
        "title": "Context Builder: Show Panel"
      }
    ],
    "viewsContainers": {
      "panel": [
        {
          "id": "contextBuilderPanel",
          "title": "Context Builder",
          "icon": "$(files)"
        }
      ]
    },
    "views": {
      "contextBuilderPanel": [
        {
          "type": "webview",
          "id": "contextBuilder.panelView",
          "name": "Context Builder"
        }
      ]
    }
  }
}
```

2. Create `src/panel.ts`:

```typescript
import * as vscode from 'vscode';
import * as path from 'path';

export interface SelectedFile {
    uri: vscode.Uri;
    description: string;
}

export class ContextBuilderPanel implements vscode.WebviewViewProvider {
    public static readonly viewType = 'contextBuilder.panelView';

    private _view?: vscode.WebviewView;
    private _files: SelectedFile[] = [];
    private _projectDescription: string = '';

    constructor(private readonly _extensionUri: vscode.Uri) {}

    public resolveWebviewView(
        webviewView: vscode.WebviewView,
        context: vscode.WebviewViewResolveContext,
        _token: vscode.CancellationToken
    ) {
        this._view = webviewView;

        webviewView.webview.options = {
            enableScripts: true,
            localResourceRoots: [this._extensionUri]
        };

        webviewView.webview.html = this._getHtmlContent();

        // Handle messages from webview
        webviewView.webview.onDidReceiveMessage(data => {
            switch (data.type) {
                case 'updateDescription':
                    this._updateFileDescription(data.index, data.description);
                    break;
                case 'updateProjectDescription':
                    this._projectDescription = data.description;
                    break;
                case 'removeFile':
                    this._removeFile(data.index);
                    break;
                case 'generate':
                    this._generate();
                    break;
                case 'clear':
                    this._clear();
                    break;
            }
        });
    }

    public addFiles(uris: vscode.Uri[]) {
        uris.forEach(uri => {
            if (!this._files.find(f => f.uri.fsPath === uri.fsPath)) {
                this._files.push({ uri, description: '' });
            }
        });
        this._updateView();
    }

    public getFiles(): SelectedFile[] {
        return this._files;
    }

    public getProjectDescription(): string {
        return this._projectDescription;
    }

    private _updateFileDescription(index: number, description: string) {
        if (this._files[index]) {
            this._files[index].description = description;
        }
    }

    private _removeFile(index: number) {
        this._files.splice(index, 1);
        this._updateView();
    }

    private _generate() {
        vscode.commands.executeCommand('contextBuilder.generate');
    }

    private _clear() {
        this._files = [];
        this._projectDescription = '';
        this._updateView();
    }

    private _updateView() {
        if (this._view) {
            this._view.webview.html = this._getHtmlContent();
        }
    }

    private _getHtmlContent(): string {
        const fileListHtml = this._files.map((file, index) => {
            const relativePath = vscode.workspace.asRelativePath(file.uri);
            return `
                <div class="file-item">
                    <div class="file-header">
                        <span class="file-path">${relativePath}</span>
                        <button class="remove-btn" onclick="removeFile(${index})">×</button>
                    </div>
                    <input
                        type="text"
                        class="description-input"
                        placeholder="Description (optional)"
                        value="${file.description}"
                        onchange="updateDescription(${index}, this.value)"
                    />
                </div>
            `;
        }).join('');

        return `<!DOCTYPE html>
        <html>
        <head>
            <style>
                body {
                    font-family: var(--vscode-font-family);
                    padding: 10px;
                    color: var(--vscode-foreground);
                }
                .project-desc {
                    width: 100%;
                    min-height: 60px;
                    margin-bottom: 15px;
                    background: var(--vscode-input-background);
                    color: var(--vscode-input-foreground);
                    border: 1px solid var(--vscode-input-border);
                    padding: 8px;
                    resize: vertical;
                }
                .file-item {
                    background: var(--vscode-editor-background);
                    border: 1px solid var(--vscode-panel-border);
                    padding: 8px;
                    margin-bottom: 8px;
                    border-radius: 4px;
                }
                .file-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                .file-path {
                    font-family: monospace;
                    font-size: 12px;
                }
                .remove-btn {
                    background: none;
                    border: none;
                    color: var(--vscode-errorForeground);
                    cursor: pointer;
                    font-size: 16px;
                }
                .description-input {
                    width: 100%;
                    margin-top: 5px;
                    background: var(--vscode-input-background);
                    color: var(--vscode-input-foreground);
                    border: 1px solid var(--vscode-input-border);
                    padding: 4px;
                }
                .actions {
                    margin-top: 15px;
                    display: flex;
                    gap: 10px;
                }
                button.primary {
                    background: var(--vscode-button-background);
                    color: var(--vscode-button-foreground);
                    border: none;
                    padding: 8px 16px;
                    cursor: pointer;
                }
                button.secondary {
                    background: var(--vscode-button-secondaryBackground);
                    color: var(--vscode-button-secondaryForeground);
                    border: none;
                    padding: 8px 16px;
                    cursor: pointer;
                }
                .footer {
                    margin-top: 15px;
                    font-size: 12px;
                    color: var(--vscode-descriptionForeground);
                }
            </style>
        </head>
        <body>
            <h3>Context Builder</h3>

            <label>Project Description:</label>
            <textarea
                class="project-desc"
                placeholder="Describe what you're working on..."
                onchange="updateProjectDescription(this.value)"
            >${this._projectDescription}</textarea>

            <label>Selected Files (${this._files.length}):</label>
            <div id="file-list">
                ${fileListHtml || '<p style="color: var(--vscode-descriptionForeground)">Right-click files in explorer and select "Add to Context"</p>'}
            </div>

            <div class="actions">
                <button class="primary" onclick="generate()">Generate</button>
                <button class="secondary" onclick="clearAll()">Clear</button>
            </div>

            <div class="footer">
                Total files: ${this._files.length}
            </div>

            <script>
                const vscode = acquireVsCodeApi();

                function updateDescription(index, description) {
                    vscode.postMessage({ type: 'updateDescription', index, description });
                }

                function updateProjectDescription(description) {
                    vscode.postMessage({ type: 'updateProjectDescription', description });
                }

                function removeFile(index) {
                    vscode.postMessage({ type: 'removeFile', index });
                }

                function generate() {
                    vscode.postMessage({ type: 'generate' });
                }

                function clearAll() {
                    vscode.postMessage({ type: 'clear' });
                }
            </script>
        </body>
        </html>`;
    }
}
```

3. Update `src/extension.ts` to use the panel:

```typescript
import * as vscode from 'vscode';
import { ContextBuilderPanel } from './panel';

let panel: ContextBuilderPanel;

export function activate(context: vscode.ExtensionContext) {

    // Create panel provider
    panel = new ContextBuilderPanel(context.extensionUri);

    context.subscriptions.push(
        vscode.window.registerWebviewViewProvider(
            ContextBuilderPanel.viewType,
            panel
        )
    );

    // Register add to context command
    const addToContext = vscode.commands.registerCommand(
        'contextBuilder.addToContext',
        (uri: vscode.Uri, uris: vscode.Uri[]) => {
            const filesToAdd = uris || [uri];
            panel.addFiles(filesToAdd);
            vscode.window.showInformationMessage(
                `Added ${filesToAdd.length} file(s) to context`
            );
        }
    );

    context.subscriptions.push(addToContext);
}

export function deactivate() {}
```

4. Test manually:
   - Press F5
   - Open the "Context Builder" panel (View → Open View → Context Builder)
   - Right-click files and add them
   - Verify they appear in the panel with description fields

**Commit:** `feat(vscode): add bottom panel UI with file list`

---

### Task 4: Implement Output Generator

**Goal:** Generate the text output in gitingest format.

**Files to create:**
- `vscode-extension/src/generator.ts`

**Files to modify:**
- `vscode-extension/src/extension.ts`

**Steps:**

1. Create `src/generator.ts`:

```typescript
import * as vscode from 'vscode';
import * as fs from 'fs';
import { SelectedFile } from './panel';

export interface GeneratorOptions {
    files: SelectedFile[];
    projectDescription: string;
}

export interface GeneratorResult {
    content: string;
    fileCount: number;
    totalLines: number;
    largeFiles: string[];  // Files over 100KB
}

const LARGE_FILE_THRESHOLD = 100 * 1024; // 100KB
const SEPARATOR = '================================================';

export async function generateContext(options: GeneratorOptions): Promise<GeneratorResult> {
    const { files, projectDescription } = options;

    let output = '';
    let totalLines = 0;
    const largeFiles: string[] = [];

    // Add project description if provided
    if (projectDescription.trim()) {
        output += `${SEPARATOR}\n`;
        output += `PROJECT DESCRIPTION\n`;
        output += `${SEPARATOR}\n`;
        output += `${projectDescription.trim()}\n\n`;
    }

    // Process each file
    for (const file of files) {
        const relativePath = vscode.workspace.asRelativePath(file.uri);

        try {
            const stat = await vscode.workspace.fs.stat(file.uri);

            // Check for large files
            if (stat.size > LARGE_FILE_THRESHOLD) {
                largeFiles.push(relativePath);
            }

            // Read file content
            const contentBytes = await vscode.workspace.fs.readFile(file.uri);
            const content = Buffer.from(contentBytes).toString('utf8');
            const lineCount = content.split('\n').length;
            totalLines += lineCount;

            // Add file header
            output += `${SEPARATOR}\n`;
            output += `FILE: ${relativePath}\n`;

            if (file.description.trim()) {
                output += `DESCRIPTION: ${file.description.trim()}\n`;
            }

            output += `${SEPARATOR}\n`;
            output += content;

            // Ensure file ends with newline
            if (!content.endsWith('\n')) {
                output += '\n';
            }
            output += '\n';

        } catch (error) {
            output += `${SEPARATOR}\n`;
            output += `FILE: ${relativePath}\n`;
            output += `${SEPARATOR}\n`;
            output += `[Error reading file: ${error}]\n\n`;
        }
    }

    return {
        content: output,
        fileCount: files.length,
        totalLines,
        largeFiles
    };
}
```

2. Update `src/extension.ts` to add generate command:

```typescript
import * as vscode from 'vscode';
import { ContextBuilderPanel } from './panel';
import { generateContext } from './generator';

let panel: ContextBuilderPanel;

export function activate(context: vscode.ExtensionContext) {

    panel = new ContextBuilderPanel(context.extensionUri);

    context.subscriptions.push(
        vscode.window.registerWebviewViewProvider(
            ContextBuilderPanel.viewType,
            panel
        )
    );

    // Add to context command
    const addToContext = vscode.commands.registerCommand(
        'contextBuilder.addToContext',
        (uri: vscode.Uri, uris: vscode.Uri[]) => {
            const filesToAdd = uris || [uri];
            panel.addFiles(filesToAdd);
            vscode.window.showInformationMessage(
                `Added ${filesToAdd.length} file(s) to context`
            );
        }
    );

    // Generate command
    const generate = vscode.commands.registerCommand(
        'contextBuilder.generate',
        async () => {
            const files = panel.getFiles();

            if (files.length === 0) {
                vscode.window.showWarningMessage('No files selected');
                return;
            }

            const result = await generateContext({
                files,
                projectDescription: panel.getProjectDescription()
            });

            // Warn about large files
            if (result.largeFiles.length > 0) {
                const proceed = await vscode.window.showWarningMessage(
                    `${result.largeFiles.length} file(s) are over 100KB. Continue?`,
                    'Yes', 'No'
                );
                if (proceed !== 'Yes') {
                    return;
                }
            }

            // Ask: copy or save?
            const action = await vscode.window.showQuickPick(
                ['Copy to Clipboard', 'Save to File'],
                { placeHolder: 'What do you want to do with the context?' }
            );

            if (action === 'Copy to Clipboard') {
                await vscode.env.clipboard.writeText(result.content);
                vscode.window.showInformationMessage(
                    `Copied ${result.fileCount} files (${result.totalLines} lines) to clipboard`
                );
            } else if (action === 'Save to File') {
                const uri = await vscode.window.showSaveDialog({
                    defaultUri: vscode.Uri.file('context.txt'),
                    filters: { 'Text files': ['txt'] }
                });

                if (uri) {
                    await vscode.workspace.fs.writeFile(
                        uri,
                        Buffer.from(result.content, 'utf8')
                    );
                    vscode.window.showInformationMessage(
                        `Saved ${result.fileCount} files to ${uri.fsPath}`
                    );
                }
            }
        }
    );

    context.subscriptions.push(addToContext, generate);
}

export function deactivate() {}
```

3. Test manually:
   - Add some files via right-click
   - Click "Generate"
   - Verify clipboard contains correct format
   - Try saving to file

**Commit:** `feat(vscode): implement context generation and output`

---

### Task 5: Add History Feature

**Goal:** Track last 10 generations and allow reloading them.

**Files to create:**
- `vscode-extension/src/history.ts`

**Files to modify:**
- `vscode-extension/src/extension.ts`
- `vscode-extension/src/panel.ts`

**Steps:**

1. Create `src/history.ts`:

```typescript
import * as vscode from 'vscode';

export interface HistoryEntry {
    timestamp: Date;
    files: string[];  // Relative paths
    outputDestination: 'clipboard' | string;  // 'clipboard' or file path
}

const MAX_HISTORY = 10;
const HISTORY_KEY = 'contextBuilder.history';

export class HistoryManager {
    constructor(private context: vscode.ExtensionContext) {}

    public getHistory(): HistoryEntry[] {
        const history = this.context.globalState.get<HistoryEntry[]>(HISTORY_KEY, []);
        // Convert date strings back to Date objects
        return history.map(entry => ({
            ...entry,
            timestamp: new Date(entry.timestamp)
        }));
    }

    public addEntry(entry: HistoryEntry): void {
        const history = this.getHistory();

        // Add new entry at the beginning
        history.unshift(entry);

        // Keep only last 10
        if (history.length > MAX_HISTORY) {
            history.pop();
        }

        this.context.globalState.update(HISTORY_KEY, history);
    }

    public clearHistory(): void {
        this.context.globalState.update(HISTORY_KEY, []);
    }
}
```

2. Update `src/extension.ts` to use history:

```typescript
// Add to imports
import { HistoryManager, HistoryEntry } from './history';

// Add after panel creation
let historyManager: HistoryManager;

export function activate(context: vscode.ExtensionContext) {
    historyManager = new HistoryManager(context);

    // ... existing code ...

    // In generate command, after successful output:
    // Add to history
    historyManager.addEntry({
        timestamp: new Date(),
        files: files.map(f => vscode.workspace.asRelativePath(f.uri)),
        outputDestination: action === 'Copy to Clipboard'
            ? 'clipboard'
            : uri?.fsPath || 'unknown'
    });
}
```

3. Update panel.ts to show history button and modal (add to HTML).

4. Test manually:
   - Generate context a few times
   - Click History button
   - Verify entries appear with timestamps
   - Click "Reload" and verify files are loaded

**Commit:** `feat(vscode): add generation history tracking`

---

### Task 6: Add Unit Tests

**Goal:** Add tests for the generator and history modules.

**Files to create:**
- `vscode-extension/src/test/suite/generator.test.ts`
- `vscode-extension/src/test/suite/history.test.ts`

**Testing Principles (READ THIS):**

1. **Test behavior, not implementation** - Test what it does, not how
2. **One assertion per test** - Each test checks one thing
3. **Use descriptive names** - `test_empty_files_returns_empty_output`
4. **AAA pattern** - Arrange, Act, Assert
5. **No external dependencies** - Mock file system, don't read real files

**Steps:**

1. Create `src/test/suite/generator.test.ts`:

```typescript
import * as assert from 'assert';
import { generateContext } from '../../generator';
import * as vscode from 'vscode';

suite('Generator Test Suite', () => {

    test('empty files returns project description only', async () => {
        // Arrange
        const options = {
            files: [],
            projectDescription: 'Test project'
        };

        // Act
        const result = await generateContext(options);

        // Assert
        assert.strictEqual(result.fileCount, 0);
        assert.ok(result.content.includes('PROJECT DESCRIPTION'));
        assert.ok(result.content.includes('Test project'));
    });

    test('empty project description is omitted', async () => {
        // Arrange
        const options = {
            files: [],
            projectDescription: ''
        };

        // Act
        const result = await generateContext(options);

        // Assert
        assert.ok(!result.content.includes('PROJECT DESCRIPTION'));
    });

    test('file without description omits DESCRIPTION line', async () => {
        // This test requires mocking vscode.workspace.fs
        // See mocking section below
    });

    test('large file is flagged in result', async () => {
        // Arrange - mock a file over 100KB
        // Act - generate
        // Assert - result.largeFiles includes the file
    });
});
```

2. Run tests:
   ```bash
   npm run test
   ```

**Commit:** `test(vscode): add generator unit tests`

---

## 4. JetBrains Plugin Tasks

### Prerequisites

Before starting, ensure you have:
- JDK 17+ installed
- IntelliJ IDEA (Community or Ultimate)
- Gradle 8+ (or use wrapper)

### Task 7: Project Scaffolding

**Goal:** Create the JetBrains plugin project structure.

**Steps:**

1. Create plugin directory:
   ```bash
   cd C:/projects/ai/context/context-builder
   mkdir jetbrains-plugin
   cd jetbrains-plugin
   ```

2. Create `build.gradle.kts`:

```kotlin
plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.16.1"
}

group = "com.contextbuilder"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

intellij {
    version.set("2023.2")
    type.set("IC") // IntelliJ Community
    plugins.set(listOf())
}

tasks {
    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("242.*")
    }

    test {
        useJUnitPlatform()
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:5.7.0")
}
```

3. Create `settings.gradle.kts`:

```kotlin
rootProject.name = "context-builder"
```

4. Create directory structure:
   ```bash
   mkdir -p src/main/java/com/contextbuilder
   mkdir -p src/main/resources/META-INF
   mkdir -p src/test/java/com/contextbuilder
   ```

5. Create `src/main/resources/META-INF/plugin.xml`:

```xml
<idea-plugin>
    <id>com.contextbuilder</id>
    <name>Context Builder</name>
    <vendor>Your Name</vendor>
    <description>Generate LLM context from selected files</description>

    <depends>com.intellij.modules.platform</depends>

    <actions>
        <action
            id="ContextBuilder.AddToContext"
            class="com.contextbuilder.AddToContextAction"
            text="Add to Context"
            description="Add selected files to context builder">
            <add-to-group group-id="ProjectViewPopupMenu" anchor="last"/>
        </action>
    </actions>

    <extensions defaultExtensionNs="com.intellij">
        <toolWindow
            id="Context Builder"
            anchor="bottom"
            factoryClass="com.contextbuilder.ContextBuilderToolWindowFactory"/>
    </extensions>
</idea-plugin>
```

6. Verify build works:
   ```bash
   ./gradlew build
   ```

**Commit:** `feat(jetbrains): scaffold plugin project`

---

### Task 8: Implement Add to Context Action

**Goal:** Add right-click action in Project view.

**Files to create:**
- `src/main/java/com/contextbuilder/AddToContextAction.java`
- `src/main/java/com/contextbuilder/ContextBuilderState.java`

**Steps:**

1. Create `ContextBuilderState.java` (holds selected files):

```java
package com.contextbuilder;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(Service.Level.PROJECT)
public final class ContextBuilderState {

    private final List<VirtualFile> selectedFiles = new ArrayList<>();
    private final Map<String, String> fileDescriptions = new HashMap<>();
    private String projectDescription = "";

    public static ContextBuilderState getInstance(Project project) {
        return project.getService(ContextBuilderState.class);
    }

    public void addFile(VirtualFile file) {
        if (!selectedFiles.contains(file)) {
            selectedFiles.add(file);
        }
    }

    public void addFiles(List<VirtualFile> files) {
        files.forEach(this::addFile);
    }

    public void removeFile(VirtualFile file) {
        selectedFiles.remove(file);
        fileDescriptions.remove(file.getPath());
    }

    public List<VirtualFile> getSelectedFiles() {
        return new ArrayList<>(selectedFiles);
    }

    public void setFileDescription(VirtualFile file, String description) {
        fileDescriptions.put(file.getPath(), description);
    }

    public String getFileDescription(VirtualFile file) {
        return fileDescriptions.getOrDefault(file.getPath(), "");
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String description) {
        this.projectDescription = description;
    }

    public void clear() {
        selectedFiles.clear();
        fileDescriptions.clear();
        projectDescription = "";
    }
}
```

2. Create `AddToContextAction.java`:

```java
package com.contextbuilder;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class AddToContextAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (files == null || files.length == 0) return;

        ContextBuilderState state = ContextBuilderState.getInstance(project);
        List<VirtualFile> fileList = Arrays.asList(files);
        state.addFiles(fileList);

        Messages.showInfoMessage(
            project,
            "Added " + files.length + " file(s) to context",
            "Context Builder"
        );
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Only show when files are selected
        VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        e.getPresentation().setEnabledAndVisible(files != null && files.length > 0);
    }
}
```

3. Test manually:
   - Run plugin (Gradle → runIde)
   - Right-click files in Project view
   - See "Add to Context" option
   - Click and see notification

**Commit:** `feat(jetbrains): add right-click action for files`

---

### Task 9: Create Tool Window Panel

**Goal:** Create bottom panel UI with file list.

**Files to create:**
- `src/main/java/com/contextbuilder/ContextBuilderToolWindowFactory.java`
- `src/main/java/com/contextbuilder/ContextBuilderPanel.java`

**Steps:**

1. Create `ContextBuilderToolWindowFactory.java`:

```java
package com.contextbuilder;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class ContextBuilderToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContextBuilderPanel panel = new ContextBuilderPanel(project);
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(panel.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
```

2. Create `ContextBuilderPanel.java`:

```java
package com.contextbuilder;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.table.JBTable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ContextBuilderPanel {

    private final Project project;
    private final JPanel mainPanel;
    private final JBTextArea projectDescriptionArea;
    private final DefaultTableModel tableModel;
    private final JBTable filesTable;

    public ContextBuilderPanel(Project project) {
        this.project = project;

        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Project description
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.add(new JLabel("Project Description:"), BorderLayout.NORTH);
        projectDescriptionArea = new JBTextArea(3, 40);
        projectDescriptionArea.setLineWrap(true);
        descPanel.add(new JBScrollPane(projectDescriptionArea), BorderLayout.CENTER);
        mainPanel.add(descPanel, BorderLayout.NORTH);

        // File table
        String[] columns = {"File", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only description is editable
            }
        };
        filesTable = new JBTable(tableModel);
        filesTable.getColumnModel().getColumn(0).setPreferredWidth(300);
        filesTable.getColumnModel().getColumn(1).setPreferredWidth(200);

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JLabel("Selected Files:"), BorderLayout.NORTH);
        tablePanel.add(new JBScrollPane(filesTable), BorderLayout.CENTER);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton generateBtn = new JButton("Generate");
        JButton refreshBtn = new JButton("Refresh");
        JButton clearBtn = new JButton("Clear");

        generateBtn.addActionListener(e -> generate());
        refreshBtn.addActionListener(e -> refresh());
        clearBtn.addActionListener(e -> clear());

        buttonPanel.add(generateBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(clearBtn);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Initial load
        refresh();
    }

    public JPanel getContent() {
        return mainPanel;
    }

    private void refresh() {
        tableModel.setRowCount(0);
        ContextBuilderState state = ContextBuilderState.getInstance(project);

        for (VirtualFile file : state.getSelectedFiles()) {
            String path = file.getPath();
            String desc = state.getFileDescription(file);
            tableModel.addRow(new Object[]{path, desc});
        }
    }

    private void generate() {
        // Save descriptions back to state
        saveDescriptions();

        ContextBuilderState state = ContextBuilderState.getInstance(project);
        state.setProjectDescription(projectDescriptionArea.getText());

        // Generate context
        ContextGenerator generator = new ContextGenerator(project);
        generator.generate();
    }

    private void saveDescriptions() {
        ContextBuilderState state = ContextBuilderState.getInstance(project);
        java.util.List<VirtualFile> files = state.getSelectedFiles();

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String desc = (String) tableModel.getValueAt(i, 1);
            if (i < files.size()) {
                state.setFileDescription(files.get(i), desc);
            }
        }
    }

    private void clear() {
        ContextBuilderState.getInstance(project).clear();
        projectDescriptionArea.setText("");
        refresh();
    }
}
```

3. Test manually:
   - Run plugin
   - Open Context Builder tool window (bottom)
   - Add files via right-click
   - See them appear in the panel

**Commit:** `feat(jetbrains): add tool window panel UI`

---

### Task 10: Implement Generator

**Goal:** Generate text output in gitingest format.

**Files to create:**
- `src/main/java/com/contextbuilder/ContextGenerator.java`

**Steps:**

1. Create `ContextGenerator.java`:

```java
package com.contextbuilder;

import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.ex.FileSaverDialogImpl;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;

import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ContextGenerator {

    private static final String SEPARATOR = "================================================";
    private static final long LARGE_FILE_THRESHOLD = 100 * 1024; // 100KB

    private final Project project;

    public ContextGenerator(Project project) {
        this.project = project;
    }

    public void generate() {
        ContextBuilderState state = ContextBuilderState.getInstance(project);
        List<VirtualFile> files = state.getSelectedFiles();

        if (files.isEmpty()) {
            Messages.showWarningDialog(project, "No files selected", "Context Builder");
            return;
        }

        // Check for large files
        List<String> largeFiles = new ArrayList<>();
        for (VirtualFile file : files) {
            if (file.getLength() > LARGE_FILE_THRESHOLD) {
                largeFiles.add(file.getName());
            }
        }

        if (!largeFiles.isEmpty()) {
            int result = Messages.showYesNoDialog(
                project,
                largeFiles.size() + " file(s) are over 100KB:\n" +
                    String.join(", ", largeFiles) + "\n\nContinue?",
                "Large Files Warning",
                Messages.getWarningIcon()
            );
            if (result != Messages.YES) {
                return;
            }
        }

        // Generate content
        String content = generateContent(state);

        // Ask: copy or save?
        int choice = Messages.showDialog(
            project,
            "What would you like to do?",
            "Generate Context",
            new String[]{"Copy to Clipboard", "Save to File", "Cancel"},
            0,
            Messages.getQuestionIcon()
        );

        if (choice == 0) {
            // Copy to clipboard
            CopyPasteManager.getInstance().setContents(new StringSelection(content));
            Messages.showInfoMessage(project,
                "Copied " + files.size() + " files to clipboard",
                "Context Builder");
        } else if (choice == 1) {
            // Save to file
            saveToFile(content);
        }
    }

    private String generateContent(ContextBuilderState state) {
        StringBuilder sb = new StringBuilder();

        // Project description
        String projectDesc = state.getProjectDescription();
        if (projectDesc != null && !projectDesc.trim().isEmpty()) {
            sb.append(SEPARATOR).append("\n");
            sb.append("PROJECT DESCRIPTION\n");
            sb.append(SEPARATOR).append("\n");
            sb.append(projectDesc.trim()).append("\n\n");
        }

        // Files
        for (VirtualFile file : state.getSelectedFiles()) {
            sb.append(SEPARATOR).append("\n");
            sb.append("FILE: ").append(getRelativePath(file)).append("\n");

            String desc = state.getFileDescription(file);
            if (desc != null && !desc.trim().isEmpty()) {
                sb.append("DESCRIPTION: ").append(desc.trim()).append("\n");
            }

            sb.append(SEPARATOR).append("\n");

            try {
                String content = new String(file.contentsToByteArray(), StandardCharsets.UTF_8);
                sb.append(content);
                if (!content.endsWith("\n")) {
                    sb.append("\n");
                }
            } catch (IOException e) {
                sb.append("[Error reading file: ").append(e.getMessage()).append("]\n");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    private String getRelativePath(VirtualFile file) {
        String basePath = project.getBasePath();
        if (basePath != null && file.getPath().startsWith(basePath)) {
            return file.getPath().substring(basePath.length() + 1);
        }
        return file.getPath();
    }

    private void saveToFile(String content) {
        FileSaverDescriptor descriptor = new FileSaverDescriptor(
            "Save Context",
            "Choose where to save the context file",
            "txt"
        );

        FileSaverDialogImpl dialog = new FileSaverDialogImpl(descriptor, project);
        VirtualFileWrapper wrapper = dialog.save((VirtualFile) null, "context.txt");

        if (wrapper != null) {
            try {
                java.io.File file = wrapper.getFile();
                java.nio.file.Files.writeString(file.toPath(), content);
                Messages.showInfoMessage(project,
                    "Saved to " + file.getPath(),
                    "Context Builder");
            } catch (IOException e) {
                Messages.showErrorDialog(project,
                    "Error saving file: " + e.getMessage(),
                    "Context Builder");
            }
        }
    }
}
```

2. Test manually:
   - Add files, add descriptions
   - Click Generate
   - Verify clipboard content matches format
   - Try saving to file

**Commit:** `feat(jetbrains): implement context generation`

---

### Task 11: Add History Feature

**Goal:** Track and display generation history.

**Files to create:**
- `src/main/java/com/contextbuilder/HistoryManager.java`

**Files to modify:**
- `src/main/java/com/contextbuilder/ContextBuilderPanel.java`
- `src/main/java/com/contextbuilder/ContextGenerator.java`

Follow similar pattern to VS Code history implementation.

**Commit:** `feat(jetbrains): add generation history`

---

### Task 12: Add Unit Tests

**Goal:** Add JUnit tests for generator and history.

**Files to create:**
- `src/test/java/com/contextbuilder/ContextGeneratorTest.java`
- `src/test/java/com/contextbuilder/HistoryManagerTest.java`

**Steps:**

1. Create `ContextGeneratorTest.java`:

```java
package com.contextbuilder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class ContextGeneratorTest {

    @Test
    void emptyFilesReturnsOnlyProjectDescription() {
        // Arrange
        String projectDesc = "Test project";

        // Act
        String content = generateTestContent(projectDesc, new String[]{});

        // Assert
        assertTrue(content.contains("PROJECT DESCRIPTION"));
        assertTrue(content.contains("Test project"));
    }

    @Test
    void emptyProjectDescriptionIsOmitted() {
        // Arrange
        String projectDesc = "";

        // Act
        String content = generateTestContent(projectDesc, new String[]{});

        // Assert
        assertFalse(content.contains("PROJECT DESCRIPTION"));
    }

    // Helper method for testing without full IntelliJ context
    private String generateTestContent(String projectDesc, String[] files) {
        StringBuilder sb = new StringBuilder();
        String SEPARATOR = "================================================";

        if (projectDesc != null && !projectDesc.trim().isEmpty()) {
            sb.append(SEPARATOR).append("\n");
            sb.append("PROJECT DESCRIPTION\n");
            sb.append(SEPARATOR).append("\n");
            sb.append(projectDesc.trim()).append("\n\n");
        }

        return sb.toString();
    }
}
```

2. Run tests:
   ```bash
   ./gradlew test
   ```

**Commit:** `test(jetbrains): add generator unit tests`

---

## 5. Testing Guide

### What Makes a Good Test

```
GOOD:
  test_empty_input_returns_empty_output
  test_single_file_includes_separator
  test_description_appears_after_file_path

BAD:
  test1
  testGenerate
  testItWorks
```

### Test Structure (AAA Pattern)

```java
@Test
void withdrawReducesBalance() {
    // ARRANGE - Set up the scenario
    Account account = new Account(100);

    // ACT - Do the thing
    account.withdraw(30);

    // ASSERT - Check the result
    assertEquals(70, account.getBalance());
}
```

### What to Test

| Component | Test Cases |
|-----------|------------|
| Generator | Empty files, single file, multiple files, with/without descriptions, large files |
| History | Add entry, max 10 entries, clear history, reload entry |
| File Selection | Add single, add multiple, remove file, clear all |

### What NOT to Test

- VS Code / IntelliJ APIs (they're tested by Microsoft/JetBrains)
- File system operations (mock them instead)
- UI rendering (test behavior, not appearance)

---

## 6. Definition of Done

A task is complete when:

- [ ] Code compiles without errors
- [ ] Feature works manually (you tested it yourself)
- [ ] Unit tests pass
- [ ] No console errors or warnings
- [ ] Code committed with descriptive message
- [ ] Works on both Windows and macOS (if applicable)

### Commit Message Format

```
type(scope): description

Examples:
feat(vscode): add context menu command
fix(jetbrains): handle empty file selection
test(vscode): add generator unit tests
docs: update README with usage instructions
```

---

## Quick Reference

### VS Code Extension Commands

```bash
cd vscode-extension
npm install          # Install dependencies
npm run compile      # Compile TypeScript
npm run watch        # Compile on change
npm run test         # Run tests
# Press F5 in VS Code to debug
```

### JetBrains Plugin Commands

```bash
cd jetbrains-plugin
./gradlew build      # Build plugin
./gradlew test       # Run tests
./gradlew runIde     # Run in test IDE
./gradlew buildPlugin # Create distributable
```

---

## Implementation Order

1. **VS Code: Tasks 1-6** (scaffolding → tests)
2. **JetBrains: Tasks 7-12** (scaffolding → tests)
3. **Polish: README, final testing**

Estimated total: 3-4 days for experienced developer.
