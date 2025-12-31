package com.contextbuilder.actions;

import com.contextbuilder.ContextBuilderService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================
 * ADD TO CONTEXT ACTION
 * =============================================================
 * Action triggered from right-click menu in Project View or Editor tabs.
 *
 * Equivalent to VS Code's:
 * - Command: "contextBuilder.addToContext"
 * - Menu: "explorer/context"
 *
 * KEY CONCEPTS:
 * - AnAction: Base class for all actions in IntelliJ
 * - AnActionEvent: Contains context about where/how action was triggered
 * - CommonDataKeys: Standard keys to get data from context (files, project, etc.)
 *
 * REGISTRATION (in plugin.xml):
 * <action id="ContextBuilder.AddToContext"
 *         class="com.contextbuilder.actions.AddToContextAction" .../>
 * =============================================================
 */
public class AddToContextAction extends AnAction {

    /**
     * Called when the action is triggered (user clicks menu item).
     *
     * Equivalent to VS Code's command handler:
     * vscode.commands.registerCommand('contextBuilder.addToContext', (uri, uris) => {...})
     *
     * @param e The action event containing context
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // ---------------------------------------------------------
        // STEP 1: Get the project
        // ---------------------------------------------------------
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // ---------------------------------------------------------
        // STEP 2: Get selected files
        // ---------------------------------------------------------
        // CommonDataKeys.VIRTUAL_FILE_ARRAY gets all selected files
        // (handles multi-select in Project View)
        VirtualFile[] selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);

        if (selectedFiles == null || selectedFiles.length == 0) {
            // Fallback: try single file selection
            VirtualFile singleFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
            if (singleFile != null) {
                selectedFiles = new VirtualFile[]{singleFile};
            } else {
                return; // No files selected
            }
        }

        // ---------------------------------------------------------
        // STEP 3: Filter to only include actual files (not directories)
        // ---------------------------------------------------------
        List<VirtualFile> filesToAdd = new ArrayList<>();
        for (VirtualFile file : selectedFiles) {
            if (!file.isDirectory()) {
                filesToAdd.add(file);
            } else {
                // Optionally: recursively add files from directory
                // For now, we skip directories
                // TODO: Could add option to include directory contents
            }
        }

        if (filesToAdd.isEmpty()) {
            return;
        }

        // ---------------------------------------------------------
        // STEP 4: Add files to the service
        // ---------------------------------------------------------
        ContextBuilderService service = ContextBuilderService.getInstance(project);
        service.addFiles(filesToAdd);

        // Note: The panel will auto-refresh because it listens to service changes
    }

    /**
     * Called to determine if action should be visible/enabled.
     *
     * Equivalent to VS Code's "when" clause in package.json:
     * "when": "explorerResourceIsFile"
     *
     * @param e The action event
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        // ---------------------------------------------------------
        // Get project - hide if no project
        // ---------------------------------------------------------
        Project project = e.getProject();
        if (project == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        // ---------------------------------------------------------
        // Check if files are selected
        // ---------------------------------------------------------
        VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        VirtualFile singleFile = e.getData(CommonDataKeys.VIRTUAL_FILE);

        boolean hasFiles = (files != null && files.length > 0) || singleFile != null;

        // Enable and show only if files are selected
        e.getPresentation().setEnabledAndVisible(hasFiles);
    }
}
