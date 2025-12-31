package com.contextbuilder.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

/**
 * =============================================================
 * SHOW PANEL ACTION
 * =============================================================
 * Opens the Context Builder tool window.
 *
 * Equivalent to VS Code's "contextBuilder.showFiles" command.
 * =============================================================
 */
public class ShowPanelAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // ---------------------------------------------------------
        // Get the tool window by ID (matches plugin.xml)
        // ---------------------------------------------------------
        ToolWindowManager manager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = manager.getToolWindow("Context Builder");

        if (toolWindow != null) {
            // Show and activate the tool window
            toolWindow.show();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        // Always visible if there's a project
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }
}
