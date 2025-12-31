package com.contextbuilder.actions;

import com.contextbuilder.ContextBuilderService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * =============================================================
 * CLEAR FILES ACTION
 * =============================================================
 * Clears all files from the Context Builder.
 *
 * Equivalent to VS Code's "contextBuilder.clear" command.
 * =============================================================
 */
public class ClearFilesAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        // Get service and clear files
        ContextBuilderService service = ContextBuilderService.getInstance(project);
        service.clearFiles();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        // Only enable if there are files to clear
        ContextBuilderService service = ContextBuilderService.getInstance(project);
        boolean hasFiles = service.getFileCount() > 0;

        e.getPresentation().setEnabled(hasFiles);
        e.getPresentation().setVisible(true);
    }
}
