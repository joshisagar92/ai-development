// =============================================================
// IMPORTS
// =============================================================
import * as vscode from 'vscode';
import { ContextBuilderPanel } from './panel';
import { generateContext, checkForLargeFiles, formatFileSize } from './generator';
import { HistoryManager, HistoryEntry } from './history';

// =============================================================
// EXTENSION STATE
// =============================================================
// Panel instance - stored globally so commands can access it
let panel: ContextBuilderPanel;

// History manager - stored globally for access in handleGeneratedContent
let historyManager: HistoryManager;

// =============================================================
// ACTIVATE FUNCTION
// =============================================================
export function activate(context: vscode.ExtensionContext) {

    console.log('Context Builder extension is now active!');

    // ---------------------------------------------------------
    // STEP 1: CREATE PANEL AND HISTORY MANAGER
    // ---------------------------------------------------------
    // Pass extensionUri so panel can load local resources if needed
    panel = new ContextBuilderPanel(context.extensionUri);

    // Create history manager (uses context for globalState access)
    historyManager = new HistoryManager(context);

    // ---------------------------------------------------------
    // STEP 2: REGISTER PANEL WITH VS CODE
    // ---------------------------------------------------------
    // This tells VS Code: "When you need to show the view with ID
    // 'contextBuilder.panelView', use our panel to provide the content"
    const panelRegistration = vscode.window.registerWebviewViewProvider(
        ContextBuilderPanel.viewType,  // The view ID from package.json
        panel                          // Our panel instance
    );

    // Load existing history into panel
    panel.updateHistory(historyManager.getSummaries());

    // ---------------------------------------------------------
    // STEP 3: SET UP HISTORY ACTION CALLBACK
    // ---------------------------------------------------------
    // When user clicks copy/save on a history item
    panel.onHistoryAction(async (id: string, action: 'copy' | 'save') => {
        // Get the full history entry (with content)
        const entry = historyManager.getById(id);
        if (!entry) {
            vscode.window.showErrorMessage('History entry not found');
            return;
        }

        if (action === 'copy') {
            // Copy to clipboard
            await vscode.env.clipboard.writeText(entry.content);
            vscode.window.showInformationMessage(
                `Copied to clipboard (${entry.fileCount} files, ${entry.totalLines} lines)`
            );
        } else if (action === 'save') {
            // Save to file
            const uri = await vscode.window.showSaveDialog({
                defaultUri: vscode.Uri.file('context.txt'),
                filters: {
                    'Text files': ['txt'],
                    'Markdown files': ['md'],
                    'All files': ['*']
                },
                saveLabel: 'Save Context',
                title: 'Save Context from History'
            });

            if (uri) {
                const contentBytes = new TextEncoder().encode(entry.content);
                await vscode.workspace.fs.writeFile(uri, contentBytes);
                vscode.window.showInformationMessage(
                    `Saved to ${vscode.workspace.asRelativePath(uri)}`
                );
            }
        }
    });

    // ---------------------------------------------------------
    // STEP 4: SET UP GENERATE CALLBACK
    // ---------------------------------------------------------
    // When user clicks "Generate Context" button in the panel,
    // this function will be called
    panel.onGenerate(async () => {
        const files = panel.getFiles();
        if (files.length === 0) {
            vscode.window.showWarningMessage('No files selected');
            return;
        }

        // ---------------------------------------------------------
        // STEP 3a: Check for large files FIRST
        // ---------------------------------------------------------
        const largeFiles = await checkForLargeFiles(files);

        if (largeFiles.length > 0) {
            // Build warning message
            const fileList = largeFiles
                .map(f => `• ${f.path} (${f.sizeFormatted})`)
                .join('\n');

            const message = `${largeFiles.length} file(s) are over 100KB:\n${fileList}\n\nContinue anyway?`;

            // Show warning and ask user
            const choice = await vscode.window.showWarningMessage(
                message,
                { modal: true },
                'Continue',
                'Cancel'
            );

            if (choice !== 'Continue') {
                return;  // User cancelled
            }
        }

        // ---------------------------------------------------------
        // STEP 3b: Generate context
        // ---------------------------------------------------------
        try {
            // Show progress while generating
            await vscode.window.withProgress(
                {
                    location: vscode.ProgressLocation.Notification,
                    title: 'Generating context...',
                    cancellable: false
                },
                async () => {
                    const projectDescription = panel.getProjectDescription();
                    const result = await generateContext({
                        files: files,
                        projectDescription: projectDescription
                    });

                    // Get relative file paths for history
                    const filePaths = files.map(f => vscode.workspace.asRelativePath(f.uri));

                    // Handle the generated content (clipboard/save + history)
                    await handleGeneratedContent(
                        result.content,
                        result.fileCount,
                        result.totalLines,
                        filePaths,
                        projectDescription
                    );
                }
            );
        } catch (error) {
            vscode.window.showErrorMessage(`Failed to generate context: ${error}`);
        }
    });

    // ---------------------------------------------------------
    // REGISTER COMMAND: Add to Context
    // ---------------------------------------------------------
    // CHANGED: Now uses panel.addFiles() instead of local array
    const addToContextCommand = vscode.commands.registerCommand(
        'contextBuilder.addToContext',
        (uri: vscode.Uri, uris: vscode.Uri[]) => {
            // Handle both single and multi-select
            const filesToAdd = uris && uris.length > 0 ? uris : [uri];

            // Filter out undefined
            const validFiles = filesToAdd.filter(f => f !== undefined);

            if (validFiles.length === 0) {
                vscode.window.showWarningMessage('No files selected');
                return;
            }

            // Add to panel (instead of local array)
            const beforeCount = panel.getFiles().length;
            panel.addFiles(validFiles);
            const afterCount = panel.getFiles().length;
            const addedCount = afterCount - beforeCount;

            // Show feedback
            if (addedCount > 0) {
                vscode.window.showInformationMessage(
                    `Added ${addedCount} file(s) to context. Total: ${afterCount}`
                );
            } else {
                vscode.window.showInformationMessage(
                    'Files already in context'
                );
            }
        }
    );

    // ---------------------------------------------------------
    // REGISTER COMMAND: Show Selected Files
    // ---------------------------------------------------------
    // CHANGED: Now reads from panel.getFiles()
    const showFilesCommand = vscode.commands.registerCommand(
        'contextBuilder.showFiles',
        () => {
            const files = panel.getFiles();
            if (files.length === 0) {
                vscode.window.showInformationMessage('No files selected yet');
            } else {
                const fileNames = files.map(f =>
                    vscode.workspace.asRelativePath(f.uri)
                ).join('\n');

                vscode.window.showInformationMessage(
                    `Selected files:\n${fileNames}`,
                    { modal: true }
                );
            }
        }
    );

    // ---------------------------------------------------------
    // REGISTER COMMAND: Clear Selection
    // ---------------------------------------------------------
    const clearCommand = vscode.commands.registerCommand(
        'contextBuilder.clear',
        () => {
            const count = panel.getFiles().length;
            panel.clear();  // Clear all files and descriptions
            vscode.window.showInformationMessage(`Cleared ${count} file(s)`);
        }
    );

    // ---------------------------------------------------------
    // ADD ALL TO SUBSCRIPTIONS
    // ---------------------------------------------------------
    // IMPORTANT: panelRegistration must be added too!
    context.subscriptions.push(panelRegistration);  // NEW
    context.subscriptions.push(addToContextCommand);
    context.subscriptions.push(showFilesCommand);
    context.subscriptions.push(clearCommand);
}

// =============================================================
// HELPER: Handle generated content (clipboard/save + history)
// =============================================================
async function handleGeneratedContent(
    content: string,
    fileCount: number,
    totalLines: number,
    files: string[],
    projectDescription: string
): Promise<void> {
    // ---------------------------------------------------------
    // STEP 1: Show quick pick with options
    // ---------------------------------------------------------
    // QuickPickItem allows us to show options with descriptions
    const options: vscode.QuickPickItem[] = [
        {
            label: '$(clippy) Copy to Clipboard',
            description: 'Copy the generated context to clipboard',
            detail: `${fileCount} file(s), ${totalLines} lines`
        },
        {
            label: '$(save) Save to File',
            description: 'Save the generated context to a file',
            detail: `${fileCount} file(s), ${totalLines} lines`
        },
        {
            label: '$(files) Both',
            description: 'Copy to clipboard AND save to file',
            detail: `${fileCount} file(s), ${totalLines} lines`
        }
    ];

    const selection = await vscode.window.showQuickPick(options, {
        placeHolder: 'What would you like to do with the generated context?',
        title: 'Context Generated Successfully'
    });

    // User cancelled the quick pick
    if (!selection) {
        return;
    }

    // ---------------------------------------------------------
    // STEP 2: Handle the selected option
    // ---------------------------------------------------------
    const copyToClipboard = selection.label.includes('Clipboard') || selection.label.includes('Both');
    const saveToFile = selection.label.includes('Save') || selection.label.includes('Both');

    // Track what we did for the final message
    const actions: string[] = [];

    // ---------------------------------------------------------
    // STEP 3: Copy to clipboard if requested
    // ---------------------------------------------------------
    if (copyToClipboard) {
        await vscode.env.clipboard.writeText(content);
        actions.push('copied to clipboard');
    }

    // ---------------------------------------------------------
    // STEP 4: Save to file if requested
    // ---------------------------------------------------------
    if (saveToFile) {
        // Show save dialog
        const uri = await vscode.window.showSaveDialog({
            defaultUri: vscode.Uri.file('context.txt'),
            filters: {
                'Text files': ['txt'],
                'Markdown files': ['md'],
                'All files': ['*']
            },
            saveLabel: 'Save Context',
            title: 'Save Generated Context'
        });

        // If user selected a location, write the file
        if (uri) {
            const contentBytes = new TextEncoder().encode(content);
            await vscode.workspace.fs.writeFile(uri, contentBytes);
            actions.push(`saved to ${vscode.workspace.asRelativePath(uri)}`);
        }
    }

    // ---------------------------------------------------------
    // STEP 5: Save to history
    // ---------------------------------------------------------
    // Save the generation to history (even if user cancelled the quick pick)
    await historyManager.addEntry({
        fileCount,
        totalLines,
        files,
        projectDescription,
        content
    });

    // Update the panel to show new history
    panel.updateHistory(historyManager.getSummaries());

    // ---------------------------------------------------------
    // STEP 6: Show confirmation message
    // ---------------------------------------------------------
    if (actions.length > 0) {
        vscode.window.showInformationMessage(
            `Context ${actions.join(' and ')} (${fileCount} files, ${totalLines} lines)`
        );
    }
}

// =============================================================
// DEACTIVATE FUNCTION
// =============================================================
export function deactivate() {
    console.log('Context Builder extension deactivated');
}
