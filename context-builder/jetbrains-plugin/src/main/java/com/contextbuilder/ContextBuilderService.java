package com.contextbuilder;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * =============================================================
 * CONTEXT BUILDER SERVICE
 * =============================================================
 * This is a PROJECT SERVICE - one instance per open project.
 *
 * Equivalent to VS Code's panel.ts state management.
 * Holds the list of selected files and project description.
 *
 * KEY CONCEPTS:
 * - @Service annotation marks this as an IntelliJ service
 * - Service.Level.PROJECT means one instance per project
 * - Services are obtained via project.getService(Class)
 * =============================================================
 */
@Service(Service.Level.PROJECT)
public final class ContextBuilderService {

    // ---------------------------------------------------------
    // STATE: Selected files and descriptions
    // ---------------------------------------------------------

    /**
     * Represents a file added to context with optional description.
     * Similar to VS Code's SelectedFile interface.
     */
    public static class SelectedFile {
        private final VirtualFile file;      // IntelliJ's file abstraction
        private String description;           // User's description

        public SelectedFile(VirtualFile file) {
            this.file = file;
            this.description = "";
        }

        public VirtualFile getFile() {
            return file;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * Get relative path from project root.
         * Similar to VS Code's workspace.asRelativePath()
         */
        public String getRelativePath(Project project) {
            String basePath = project.getBasePath();
            if (basePath != null && file.getPath().startsWith(basePath)) {
                return file.getPath().substring(basePath.length() + 1);
            }
            return file.getPath();
        }
    }

    // ---------------------------------------------------------
    // FIELDS
    // ---------------------------------------------------------

    // Thread-safe list (UI and background threads may access)
    private final List<SelectedFile> selectedFiles = new CopyOnWriteArrayList<>();

    // Project description (like VS Code's project description textarea)
    private String projectDescription = "";

    // Listeners for state changes (to update UI)
    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();

    // ---------------------------------------------------------
    // FILE MANAGEMENT
    // ---------------------------------------------------------

    /**
     * Add files to the context.
     * Checks for duplicates by file path.
     */
    public void addFiles(List<VirtualFile> files) {
        for (VirtualFile file : files) {
            // Skip if already added (check by path)
            boolean exists = selectedFiles.stream()
                    .anyMatch(sf -> sf.getFile().getPath().equals(file.getPath()));

            if (!exists) {
                selectedFiles.add(new SelectedFile(file));
            }
        }
        notifyListeners();
    }

    /**
     * Remove a file at the given index.
     */
    public void removeFile(int index) {
        if (index >= 0 && index < selectedFiles.size()) {
            selectedFiles.remove(index);
            notifyListeners();
        }
    }

    /**
     * Clear all files.
     */
    public void clearFiles() {
        selectedFiles.clear();
        notifyListeners();
    }

    /**
     * Get all selected files.
     */
    public List<SelectedFile> getSelectedFiles() {
        return new ArrayList<>(selectedFiles);
    }

    /**
     * Get file count.
     */
    public int getFileCount() {
        return selectedFiles.size();
    }

    // ---------------------------------------------------------
    // PROJECT DESCRIPTION
    // ---------------------------------------------------------

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String description) {
        this.projectDescription = description;
        notifyListeners();
    }

    // ---------------------------------------------------------
    // CHANGE LISTENERS (Observer Pattern)
    // ---------------------------------------------------------

    /**
     * Add a listener to be notified when state changes.
     * Used by the Tool Window to refresh UI.
     */
    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    private void notifyListeners() {
        for (Runnable listener : changeListeners) {
            listener.run();
        }
    }

    // ---------------------------------------------------------
    // STATIC HELPER: Get service instance
    // ---------------------------------------------------------

    /**
     * Get the service instance for a project.
     * This is the standard way to access services in IntelliJ.
     *
     * Usage: ContextBuilderService service = ContextBuilderService.getInstance(project);
     */
    public static ContextBuilderService getInstance(Project project) {
        return project.getService(ContextBuilderService.class);
    }
}
