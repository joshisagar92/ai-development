# Context Builder JetBrains Plugin - Technical Documentation

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Entry Points & Execution Flow](#entry-points--execution-flow)
3. [ID Matching in plugin.xml](#id-matching-in-pluginxml)
4. [IntelliJ Platform APIs Used](#intellij-platform-apis-used)
5. [Configuration File Links](#configuration-file-links)
6. [File Structure](#file-structure)
7. [VS Code Comparison](#vs-code-comparison)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    IntelliJ Platform Host                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────┐    ┌───────────────────┐    ┌──────────────┐  │
│  │  plugin.xml  │───►│ContextBuilder     │───►│ContextBuilder│  │
│  │  (manifest)  │    │    Service        │    │    Panel     │  │
│  └──────────────┘    │ (state manager)   │    │  (Swing UI)  │  │
│                      └─────────┬─────────┘    └──────┬───────┘  │
│                                │                      │          │
│                    ┌───────────┴───────────┐         │          │
│                    │                       │         │          │
│                    ▼                       ▼         ▼          │
│          ┌─────────────────┐    ┌─────────────────────────┐     │
│          │  Actions        │    │  ContextGenerator       │     │
│          │ - AddToContext  │    │  (content generation)   │     │
│          │ - ShowPanel     │    └─────────────────────────┘     │
│          │ - ClearFiles    │                                    │
│          └─────────────────┘    ┌─────────────────────────┐     │
│                                 │  ContextBuilderState    │     │
│                                 │  (persistent storage)   │     │
│                                 └─────────────────────────┘     │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Entry Points & Execution Flow

### 1. Plugin Loading

```
IDE starts up
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ IntelliJ reads plugin.xml from META-INF                     │
│ - Registers services (@Service annotations)                 │
│ - Registers actions (<action> elements)                     │
│ - Registers tool window (<toolWindow> element)              │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ Services are lazily instantiated on first use               │
│ - ContextBuilderService: first project.getService() call    │
│ - ContextBuilderState: first project.getService() call      │
└─────────────────────────────────────────────────────────────┘
```

### 2. Tool Window Creation

```
User clicks "Context Builder" in tool window bar
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ IntelliJ looks up plugin.xml                                │
│ Finds: <toolWindow factoryClass="...ToolWindowFactory" />   │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ Calls: createToolWindowContent(project, toolWindow)         │
│                                                              │
│ 1. Creates ContextBuilderPanel (JPanel)                     │
│ 2. Wraps in Content object                                  │
│ 3. Adds to toolWindow.getContentManager()                   │
└─────────────────────────────────────────────────────────────┘
```

### 3. Add to Context Flow

```
User right-clicks file(s) → "Context Builder" → "Add to Context"
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ IntelliJ calls AddToContextAction.update()                  │
│ - Checks if action should be visible/enabled                │
│ - Returns based on file selection                           │
└─────────────────────────────────────────────────────────────┘
         │
         ▼ (user clicks)
┌─────────────────────────────────────────────────────────────┐
│ IntelliJ calls AddToContextAction.actionPerformed()         │
│                                                              │
│ 1. Gets selected files from CommonDataKeys.VIRTUAL_FILE_ARRAY│
│ 2. Filters to only include files (not directories)          │
│ 3. Calls service.addFiles(filesToAdd)                       │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ ContextBuilderService.addFiles()                            │
│                                                              │
│ 1. Checks for duplicates                                    │
│ 2. Adds to selectedFiles list                               │
│ 3. Calls notifyListeners()                                  │
│ 4. Panel's change listener calls refreshUI()                │
└─────────────────────────────────────────────────────────────┘
```

### 4. Generate Context Flow

```
User clicks "Generate Context" button
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│ Button's ActionListener calls onGenerate()                  │
│                                                              │
│ 1. Check for large files (ContextGenerator.checkForLargeFiles)│
│ 2. Show warning if large files found (JOptionPane)          │
│ 3. Generate content (ContextGenerator.generate)             │
│ 4. Show copy/save dialog (JOptionPane)                      │
│ 5. Execute copy/save as needed                              │
│ 6. Save to history (ContextBuilderState.addHistoryEntry)    │
│ 7. Refresh history UI                                       │
└─────────────────────────────────────────────────────────────┘
```

---

## ID Matching in plugin.xml

### Tool Window Registration

```xml
plugin.xml                             Java Code
─────────────────────────────────────────────────────────────────

<toolWindow
    id="Context Builder"          ←── Used to find tool window
    factoryClass="...ToolWindowFactory"
    anchor="bottom"
    .../>

─────────────────────────────────────────────────────────────────

// In ShowPanelAction.java:
ToolWindow toolWindow = manager.getToolWindow("Context Builder");
                                              ↑
                                    Must match id in plugin.xml
```

### Action Registration

```xml
plugin.xml                             Java Code
─────────────────────────────────────────────────────────────────

<action
    id="ContextBuilder.AddToContext"  ←── Unique action ID
    class="...AddToContextAction"     ←── Fully qualified class name
    text="Add to Context"             ←── Menu text
    description="...">
</action>

<add-to-group
    group-id="ProjectViewPopupMenu"   ←── Standard IntelliJ group
    anchor="last"/>                   ←── Position in menu

─────────────────────────────────────────────────────────────────

// AddToContextAction.java must:
// 1. Extend AnAction
// 2. Implement actionPerformed()
// 3. Optionally implement update()
```

### Service Registration

```xml
plugin.xml                             Java Code
─────────────────────────────────────────────────────────────────

<projectService
    serviceImplementation="...ContextBuilderService"/>
                          ↑
                Fully qualified class name

─────────────────────────────────────────────────────────────────

// ContextBuilderService.java must:
@Service(Service.Level.PROJECT)    ←── Annotation required
public final class ContextBuilderService {

    // Static helper to get instance
    public static ContextBuilderService getInstance(Project project) {
        return project.getService(ContextBuilderService.class);
    }                               ↑
                          Class must match plugin.xml
}
```

---

## IntelliJ Platform APIs Used

### Services & Components

| API | Purpose | Used In |
|-----|---------|---------|
| `@Service(Service.Level.PROJECT)` | Project-scoped singleton service | ContextBuilderService, ContextBuilderState |
| `project.getService(Class)` | Get service instance | Throughout |
| `PersistentStateComponent<T>` | Auto-save/restore state | ContextBuilderState |
| `@State` + `@Storage` | Configure persistence location | ContextBuilderState |

### UI Components

| API | Purpose | Used In |
|-----|---------|---------|
| `ToolWindowFactory` | Create tool window content | ContextBuilderToolWindowFactory |
| `ContentFactory` | Wrap panels in Content objects | ContextBuilderToolWindowFactory |
| `JBLabel`, `JBList`, `JBTextArea` | IntelliJ-styled Swing components | ContextBuilderPanel |
| `JBUI.Borders` | HiDPI-aware borders/spacing | ContextBuilderPanel |

### Actions

| API | Purpose | Used In |
|-----|---------|---------|
| `AnAction` | Base class for all actions | All Action classes |
| `AnActionEvent` | Context for action execution | All Action classes |
| `CommonDataKeys` | Standard data keys (files, project) | AddToContextAction |
| `Presentation` | Control action visibility/enabled | All Action classes |

### File System

| API | Purpose | Used In |
|-----|---------|---------|
| `VirtualFile` | File abstraction | ContextBuilderService, ContextGenerator |
| `VirtualFile.contentsToByteArray()` | Read file content | ContextGenerator |
| `VirtualFile.getLength()` | Get file size | ContextGenerator |
| `FileSaverDialog` | Native save dialog | ContextBuilderPanel |
| `FileChooserFactory` | Create file dialogs | ContextBuilderPanel |

### Utilities

| API | Purpose | Used In |
|-----|---------|---------|
| `Toolkit.getSystemClipboard()` | System clipboard | ContextBuilderPanel |
| `StringSelection` | Clipboard data wrapper | ContextBuilderPanel |

---

## Configuration File Links

### build.gradle.kts ↔ plugin.xml

```
build.gradle.kts                       plugin.xml
─────────────────────────────────────────────────────────────────

intellij {
    version.set("2024.1")         // IDE version to build against
    type.set("IC")                // IC = Community, IU = Ultimate
}

tasks {
    patchPluginXml {
        sinceBuild.set("231")     ←── <idea-plugin> compatibility
        untilBuild.set("243.*")       (auto-patched during build)
    }
}
```

### build.gradle.kts ↔ Java Source

```
build.gradle.kts                       Java Files
─────────────────────────────────────────────────────────────────

group = "com.contextbuilder"      ←── package com.contextbuilder;

java {
    sourceCompatibility = VERSION_17  ←── Must match your JDK
    targetCompatibility = VERSION_17
}

// Source location convention:
// src/main/java/     ←── Java source files
// src/main/resources/ ←── Resources (plugin.xml, icons)
```

### plugin.xml ↔ Resources

```
plugin.xml                            File System
─────────────────────────────────────────────────────────────────

<toolWindow
    icon="/icons/toolwindow.svg"  ←── src/main/resources/icons/toolwindow.svg
    .../>                              (path relative to resources root)
```

---

## File Structure

```
jetbrains-plugin/
├── build.gradle.kts              # Build configuration (Kotlin DSL)
├── settings.gradle.kts           # Project name
├── gradlew.bat                   # Windows build script
├── gradle/wrapper/
│   └── gradle-wrapper.properties # Gradle version
├── src/main/
│   ├── java/com/contextbuilder/
│   │   ├── ContextBuilderService.java      # State management
│   │   ├── ContextBuilderPanel.java        # UI (Swing)
│   │   ├── ContextBuilderState.java        # Persistence
│   │   ├── ContextBuilderToolWindowFactory.java  # Panel factory
│   │   ├── ContextGenerator.java           # Content generation
│   │   └── actions/
│   │       ├── AddToContextAction.java     # Right-click action
│   │       ├── ShowPanelAction.java        # Show tool window
│   │       └── ClearFilesAction.java       # Clear files
│   └── resources/
│       ├── META-INF/
│       │   └── plugin.xml                  # Plugin manifest
│       └── icons/
│           └── toolwindow.svg              # Tool window icon
└── TECHNICAL.md                  # This file
```

### File Responsibilities

| File | Responsibility |
|------|----------------|
| `ContextBuilderService` | Holds selected files, project description, change listeners |
| `ContextBuilderPanel` | Swing UI, event handlers, history display |
| `ContextBuilderState` | Persistent history storage (PersistentStateComponent) |
| `ContextBuilderToolWindowFactory` | Creates panel when tool window opens |
| `ContextGenerator` | Reads files, formats output, checks file sizes |
| `AddToContextAction` | Handles right-click "Add to Context" |
| `ShowPanelAction` | Opens the tool window |
| `ClearFilesAction` | Clears all files from context |

---

## VS Code Comparison

### Equivalent Components

| VS Code | JetBrains | Notes |
|---------|-----------|-------|
| `package.json` | `plugin.xml` | Plugin manifest |
| `extension.ts` activate() | Services + Actions | No single entry point |
| `WebviewViewProvider` | `ToolWindowFactory` | Panel creation |
| HTML/CSS/JS | Swing (JPanel, JButton) | UI technology |
| `registerCommand()` | `AnAction` class | Command/action |
| `globalState` | `PersistentStateComponent` | Persistence |
| `vscode.workspace.fs` | `VirtualFile` | File system |
| `Uri` | `VirtualFile` | File reference |
| `postMessage()` | Direct method calls | Communication |

### Key Differences

| Aspect | VS Code | JetBrains |
|--------|---------|-----------|
| **UI Updates** | Re-render HTML | `revalidate()` + `repaint()` |
| **Communication** | postMessage (async) | Direct method calls (sync) |
| **Hot Reload** | Supported | Must restart sandbox |
| **Threading** | Single-threaded model | EDT + background threads |
| **File Reading** | Async (`readFile()`) | Sync (`contentsToByteArray()`) |

---

## Debugging Tips

### 1. Plugin Not Loading?
- Check `plugin.xml` syntax (use IDE validation)
- Verify all referenced classes exist with correct package names
- Check Gradle sync completed successfully

### 2. Tool Window Not Appearing?
- Verify `<toolWindow>` element in plugin.xml
- Check `factoryClass` fully qualified name is correct
- Look for exceptions in IDE log (Help → Show Log)

### 3. Action Not in Menu?
- Verify `<action>` and `<add-to-group>` in plugin.xml
- Check action class extends `AnAction`
- Verify `update()` returns `setEnabledAndVisible(true)`

### 4. Service Not Found?
- Ensure `@Service` annotation is present
- Verify `<projectService>` in plugin.xml
- Check class is `final` (required for @Service)

### 5. State Not Persisting?
- Verify `@State` and `@Storage` annotations
- Check `State` class has public fields or proper getters/setters
- Look for `.idea/contextBuilder.xml` in project folder

---

## Building for Distribution

```bash
# Build the plugin
./gradlew buildPlugin

# Output location
build/distributions/context-builder-1.0.0.zip

# Install in any JetBrains IDE:
# Settings → Plugins → ⚙️ → Install Plugin from Disk
```
