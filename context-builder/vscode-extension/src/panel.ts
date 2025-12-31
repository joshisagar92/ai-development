import * as vscode from 'vscode';
import { HistorySummary, HistoryManager } from './history';

// =============================================================
// INTERFACE: Selected File
// =============================================================
// Represents a file added to context with its optional description
export interface SelectedFile {
    uri: vscode.Uri;        // File path
    description: string;    // User's description (can be empty)
}

// =============================================================
// CLASS: ContextBuilderPanel
// =============================================================
// This class implements WebviewViewProvider - VS Code's interface
// for creating custom HTML panels in the sidebar or bottom panel.
//
// WebviewViewProvider has ONE required method: resolveWebviewView()
// VS Code calls this when the panel needs to be displayed.

export class ContextBuilderPanel implements vscode.WebviewViewProvider {

    // This ID must match "id" in package.json views section
    public static readonly viewType = 'contextBuilder.panelView';

    // Store reference to the webview so we can update it later
    private _view?: vscode.WebviewView;

    // Our data: selected files and project description
    private _files: SelectedFile[] = [];
    private _projectDescription: string = '';

    // History data (summaries only, full content fetched on demand)
    private _history: HistorySummary[] = [];

    // Callback for when user clicks "Generate"
    private _onGenerateCallback?: () => void;

    // Callback for history actions (re-copy, re-save)
    private _onHistoryActionCallback?: (id: string, action: 'copy' | 'save') => void;

    // Constructor receives the extension URI (path to extension folder)
    // Needed for loading local resources like CSS/JS files
    constructor(private readonly _extensionUri: vscode.Uri) {}

    // ---------------------------------------------------------
    // resolveWebviewView (REQUIRED BY INTERFACE)
    // ---------------------------------------------------------
    // VS Code calls this when the panel is first shown or restored.
    // We set up the webview options and HTML content here.
    public resolveWebviewView(
        webviewView: vscode.WebviewView,
        context: vscode.WebviewViewResolveContext,
        _token: vscode.CancellationToken
    ) {
        // Save reference for later updates
        this._view = webviewView;

        // Configure webview options
        webviewView.webview.options = {
            // Allow JavaScript in the webview
            enableScripts: true,

            // Restrict which folders the webview can load resources from
            localResourceRoots: [this._extensionUri]
        };

        // Set initial HTML content
        webviewView.webview.html = this._getHtmlContent();

        // ---------------------------------------------------------
        // MESSAGE HANDLING: Receive messages from webview
        // ---------------------------------------------------------
        // When the webview calls vscode.postMessage(), we receive it here
        webviewView.webview.onDidReceiveMessage(message => {
            switch (message.type) {
                case 'updateDescription':
                    // User changed a file's description
                    this._handleUpdateDescription(message.index, message.description);
                    break;

                case 'updateProjectDescription':
                    // User changed the project description
                    this._projectDescription = message.description;
                    break;

                case 'removeFile':
                    // User clicked the X button on a file
                    this._handleRemoveFile(message.index);
                    break;

                case 'generate':
                    // User clicked "Generate Context"
                    if (this._onGenerateCallback) {
                        this._onGenerateCallback();
                    }
                    break;

                case 'clear':
                    // User clicked "Clear All"
                    this._handleClear();
                    break;

                case 'historyAction':
                    // User clicked copy/save on a history item
                    if (this._onHistoryActionCallback) {
                        this._onHistoryActionCallback(message.id, message.action);
                    }
                    break;
            }
        });
    }

    // ---------------------------------------------------------
    // PUBLIC: Set callback for generate button
    // ---------------------------------------------------------
    public onGenerate(callback: () => void): void {
        this._onGenerateCallback = callback;
    }

    // ---------------------------------------------------------
    // PUBLIC METHOD: Add files to the panel
    // ---------------------------------------------------------
    // Called from extension.ts when user right-clicks "Add to Context"
    public addFiles(uris: vscode.Uri[]): void {
        uris.forEach(uri => {
            // Avoid duplicates
            const exists = this._files.some(f => f.uri.fsPath === uri.fsPath);
            if (!exists) {
                this._files.push({ uri, description: '' });
            }
        });

        // Refresh the UI
        this._updateView();
    }

    // ---------------------------------------------------------
    // PUBLIC GETTERS
    // ---------------------------------------------------------
    public getFiles(): SelectedFile[] {
        return this._files;
    }

    public getProjectDescription(): string {
        return this._projectDescription;
    }

    // ---------------------------------------------------------
    // PUBLIC: Clear all files
    // ---------------------------------------------------------
    public clear(): void {
        this._handleClear();
    }

    // ---------------------------------------------------------
    // PUBLIC: Update history (called from extension.ts)
    // ---------------------------------------------------------
    public updateHistory(history: HistorySummary[]): void {
        this._history = history;
        this._updateView();
    }

    // ---------------------------------------------------------
    // PUBLIC: Set callback for history actions
    // ---------------------------------------------------------
    public onHistoryAction(callback: (id: string, action: 'copy' | 'save') => void): void {
        this._onHistoryActionCallback = callback;
    }

    // ---------------------------------------------------------
    // PRIVATE: Handle update description message
    // ---------------------------------------------------------
    private _handleUpdateDescription(index: number, description: string): void {
        if (index >= 0 && index < this._files.length) {
            this._files[index].description = description;
            // Note: No need to update view here since user is still typing
        }
    }

    // ---------------------------------------------------------
    // PRIVATE: Handle remove file message
    // ---------------------------------------------------------
    private _handleRemoveFile(index: number): void {
        if (index >= 0 && index < this._files.length) {
            this._files.splice(index, 1);  // Remove from array
            this._updateView();            // Refresh UI
        }
    }

    // ---------------------------------------------------------
    // PRIVATE: Handle clear all message
    // ---------------------------------------------------------
    private _handleClear(): void {
        this._files = [];
        this._projectDescription = '';
        this._updateView();
    }

    // ---------------------------------------------------------
    // PRIVATE: Update the webview HTML
    // ---------------------------------------------------------
    private _updateView(): void {
        if (this._view) {
            this._view.webview.html = this._getHtmlContent();
        }
    }

    // ---------------------------------------------------------
    // PRIVATE: Generate HTML content
    // ---------------------------------------------------------
    // This is where we build the UI that appears in the panel
    private _getHtmlContent(): string {
        const fileCount = this._files.length;

        // Build file list HTML
        const fileListHtml = this._files.length === 0
            ? '<p class="empty-message">Right-click files in Explorer and select "Add to Context"</p>'
            : this._files.map((file, index) => {
                const relativePath = vscode.workspace.asRelativePath(file.uri);
                return `
                <div class="file-item">
                    <div class="file-header">
                        <span class="file-path" title="${file.uri.fsPath}">${relativePath}</span>
                        <button class="remove-btn" onclick="removeFile(${index})" title="Remove file">×</button>
                    </div>
                    <input
                        type="text"
                        class="description-input"
                        placeholder="Description (optional)"
                        value="${this._escapeHtml(file.description)}"
                        data-index="${index}"
                        onchange="updateDescription(${index}, this.value)"
                    />
                </div>`;
            }).join('');

        // Build history HTML
        const historyHtml = this._history.length === 0
            ? '<p class="history-empty">No history yet. Generate some context!</p>'
            : this._history.map(entry => {
                const formattedTime = HistoryManager.formatTimestamp(entry.timestamp);
                const filePreview = entry.files.slice(0, 2).join(', ') +
                    (entry.files.length > 2 ? ` +${entry.files.length - 2} more` : '');
                return `
                <div class="history-item">
                    <div class="history-item-header">
                        <span class="history-timestamp">${formattedTime}</span>
                        <span class="history-stats">${entry.fileCount} files, ${entry.totalLines} lines</span>
                    </div>
                    <div class="history-files">${this._escapeHtml(filePreview)}</div>
                    <div class="history-actions">
                        <button class="btn-small" onclick="historyAction('${entry.id}', 'copy')">📋 Copy</button>
                        <button class="btn-small" onclick="historyAction('${entry.id}', 'save')">💾 Save</button>
                    </div>
                </div>`;
            }).join('');

        return `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Context Builder</title>
    <style>
        /* ============================================= */
        /* BASE STYLES                                   */
        /* ============================================= */
        body {
            font-family: var(--vscode-font-family);
            font-size: var(--vscode-font-size);
            color: var(--vscode-foreground);
            background-color: var(--vscode-panel-background);
            padding: 12px;
            margin: 0;
        }

        /* ============================================= */
        /* HEADER                                        */
        /* ============================================= */
        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;
            padding-bottom: 8px;
            border-bottom: 1px solid var(--vscode-panel-border);
        }
        .header h3 {
            margin: 0;
        }
        .file-count {
            color: var(--vscode-descriptionForeground);
            font-size: 12px;
        }

        /* ============================================= */
        /* PROJECT DESCRIPTION                           */
        /* ============================================= */
        .section-label {
            font-weight: bold;
            margin-bottom: 6px;
            display: block;
            color: var(--vscode-foreground);
        }
        .project-desc {
            width: 100%;
            min-height: 50px;
            max-height: 100px;
            padding: 8px;
            margin-bottom: 16px;
            background: var(--vscode-input-background);
            color: var(--vscode-input-foreground);
            border: 1px solid var(--vscode-input-border);
            border-radius: 2px;
            resize: vertical;
            font-family: inherit;
            font-size: inherit;
            box-sizing: border-box;
        }
        .project-desc:focus {
            outline: 1px solid var(--vscode-focusBorder);
        }

        /* ============================================= */
        /* FILE LIST                                     */
        /* ============================================= */
        .file-list {
            margin-bottom: 16px;
            max-height: 300px;
            overflow-y: auto;
        }
        .file-item {
            background: var(--vscode-editor-background);
            border: 1px solid var(--vscode-panel-border);
            border-radius: 4px;
            padding: 8px;
            margin-bottom: 8px;
        }
        .file-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 6px;
        }
        .file-path {
            font-family: var(--vscode-editor-font-family), monospace;
            font-size: 12px;
            color: var(--vscode-textLink-foreground);
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            flex: 1;
            margin-right: 8px;
        }
        .remove-btn {
            background: transparent;
            border: none;
            color: var(--vscode-errorForeground);
            cursor: pointer;
            font-size: 16px;
            padding: 0 4px;
            line-height: 1;
        }
        .remove-btn:hover {
            color: var(--vscode-inputValidation-errorBorder);
        }
        .description-input {
            width: 100%;
            padding: 4px 8px;
            background: var(--vscode-input-background);
            color: var(--vscode-input-foreground);
            border: 1px solid var(--vscode-input-border);
            border-radius: 2px;
            font-family: inherit;
            font-size: 12px;
            box-sizing: border-box;
        }
        .description-input:focus {
            outline: 1px solid var(--vscode-focusBorder);
        }
        .empty-message {
            color: var(--vscode-descriptionForeground);
            font-style: italic;
            text-align: center;
            padding: 20px;
        }

        /* ============================================= */
        /* BUTTONS                                       */
        /* ============================================= */
        .button-row {
            display: flex;
            gap: 8px;
        }
        .btn {
            padding: 6px 14px;
            border: none;
            border-radius: 2px;
            cursor: pointer;
            font-family: inherit;
            font-size: 13px;
        }
        .btn-primary {
            background: var(--vscode-button-background);
            color: var(--vscode-button-foreground);
        }
        .btn-primary:hover {
            background: var(--vscode-button-hoverBackground);
        }
        .btn-primary:disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }
        .btn-secondary {
            background: var(--vscode-button-secondaryBackground);
            color: var(--vscode-button-secondaryForeground);
        }
        .btn-secondary:hover {
            background: var(--vscode-button-secondaryHoverBackground);
        }

        /* ============================================= */
        /* HISTORY SECTION                               */
        /* ============================================= */
        .history-section {
            margin-top: 20px;
            padding-top: 16px;
            border-top: 1px solid var(--vscode-panel-border);
        }
        .history-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;
        }
        .history-item {
            background: var(--vscode-editor-background);
            border: 1px solid var(--vscode-panel-border);
            border-radius: 4px;
            padding: 8px;
            margin-bottom: 6px;
        }
        .history-item-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 4px;
        }
        .history-timestamp {
            font-size: 11px;
            color: var(--vscode-descriptionForeground);
        }
        .history-stats {
            font-size: 11px;
            color: var(--vscode-foreground);
        }
        .history-files {
            font-size: 11px;
            color: var(--vscode-descriptionForeground);
            margin-top: 4px;
            font-family: var(--vscode-editor-font-family), monospace;
        }
        .history-actions {
            display: flex;
            gap: 4px;
            margin-top: 6px;
        }
        .btn-small {
            padding: 2px 8px;
            font-size: 11px;
            background: var(--vscode-button-secondaryBackground);
            color: var(--vscode-button-secondaryForeground);
            border: none;
            border-radius: 2px;
            cursor: pointer;
        }
        .btn-small:hover {
            background: var(--vscode-button-secondaryHoverBackground);
        }
        .history-empty {
            color: var(--vscode-descriptionForeground);
            font-style: italic;
            font-size: 12px;
            text-align: center;
            padding: 12px;
        }
    </style>
</head>
<body>
    <!-- HEADER -->
    <div class="header">
        <h3>Context Builder</h3>
        <span class="file-count">${fileCount} file(s)</span>
    </div>

    <!-- PROJECT DESCRIPTION -->
    <label class="section-label">Project Description</label>
    <textarea
        class="project-desc"
        placeholder="Describe what you're working on (optional)..."
        onchange="updateProjectDescription(this.value)"
    >${this._escapeHtml(this._projectDescription)}</textarea>

    <!-- FILE LIST -->
    <label class="section-label">Selected Files</label>
    <div class="file-list">
        ${fileListHtml}
    </div>

    <!-- BUTTONS -->
    <div class="button-row">
        <button class="btn btn-primary" onclick="generate()" ${fileCount === 0 ? 'disabled' : ''}>
            Generate Context
        </button>
        <button class="btn btn-secondary" onclick="clearAll()" ${fileCount === 0 ? 'disabled' : ''}>
            Clear All
        </button>
    </div>

    <!-- HISTORY SECTION -->
    <div class="history-section">
        <div class="history-header">
            <label class="section-label">Recent History</label>
            <span class="file-count">${this._history.length} generation(s)</span>
        </div>
        ${historyHtml}
    </div>

    <!-- JAVASCRIPT: Communication with extension -->
    <script>
        // Get VS Code API for sending messages to extension
        const vscode = acquireVsCodeApi();

        // Send message to update file description
        function updateDescription(index, description) {
            vscode.postMessage({
                type: 'updateDescription',
                index: index,
                description: description
            });
        }

        // Send message to update project description
        function updateProjectDescription(description) {
            vscode.postMessage({
                type: 'updateProjectDescription',
                description: description
            });
        }

        // Send message to remove a file
        function removeFile(index) {
            vscode.postMessage({
                type: 'removeFile',
                index: index
            });
        }

        // Send message to generate context
        function generate() {
            vscode.postMessage({ type: 'generate' });
        }

        // Send message to clear all files
        function clearAll() {
            vscode.postMessage({ type: 'clear' });
        }

        // Send message for history action (copy or save)
        function historyAction(id, action) {
            vscode.postMessage({
                type: 'historyAction',
                id: id,
                action: action
            });
        }
    </script>
</body>
</html>`;
    }

    // ---------------------------------------------------------
    // PRIVATE: Escape HTML to prevent XSS
    // ---------------------------------------------------------
    private _escapeHtml(text: string): string {
        return text
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }
}
