package com.contextbuilder;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * =============================================================
 * TOOL WINDOW FACTORY
 * =============================================================
 * Creates the Context Builder panel in the IDE.
 *
 * Equivalent to VS Code's WebviewViewProvider.
 *
 * KEY CONCEPTS:
 * - ToolWindowFactory: Interface to create tool window content
 * - Called by IntelliJ when user first opens the tool window
 * - createToolWindowContent(): Where we build the UI
 *
 * REGISTRATION:
 * This class is referenced in plugin.xml:
 * <toolWindow factoryClass="com.contextbuilder.ContextBuilderToolWindowFactory" .../>
 * =============================================================
 */
public class ContextBuilderToolWindowFactory implements ToolWindowFactory {

    /**
     * Called when the tool window is first opened.
     * This is where we create and add the UI content.
     *
     * @param project    The current project
     * @param toolWindow The tool window instance
     */
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // ---------------------------------------------------------
        // STEP 1: Create the panel UI component
        // ---------------------------------------------------------
        // ContextBuilderPanel is our custom JPanel with all the UI
        ContextBuilderPanel panel = new ContextBuilderPanel(project);

        // ---------------------------------------------------------
        // STEP 2: Wrap in Content object
        // ---------------------------------------------------------
        // IntelliJ tool windows use "Content" objects to manage tabs
        // ContentFactory creates these wrapper objects
        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(
                panel,           // The actual UI component (JPanel)
                "",              // Tab name (empty = no tab title)
                false            // isLockable - can user lock this tab?
        );

        // ---------------------------------------------------------
        // STEP 3: Add to tool window
        // ---------------------------------------------------------
        // getContentManager() manages tabs in the tool window
        toolWindow.getContentManager().addContent(content);
    }

    /**
     * Called to check if tool window should be available.
     * Return false to hide the tool window for certain projects.
     *
     * @param project The project to check
     * @return true if tool window should be shown
     */
    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        // Always available for all projects
        return true;
    }
}
