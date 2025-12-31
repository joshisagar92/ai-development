package com.contextbuilder;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================
 * CONTEXT BUILDER STATE (Persistent Storage)
 * =============================================================
 * Stores history across IDE restarts.
 *
 * Equivalent to VS Code's context.globalState.
 *
 * KEY CONCEPTS:
 * - @State: Tells IntelliJ to persist this component
 * - @Storage: Where to save (project-level .xml file)
 * - PersistentStateComponent: Interface for serializable state
 *
 * HOW IT WORKS:
 * 1. IntelliJ calls getState() to serialize data on save
 * 2. IntelliJ calls loadState() to deserialize on startup
 * 3. Data is stored in .idea/contextBuilder.xml
 * =============================================================
 */
@State(
        name = "ContextBuilderState",
        storages = @Storage("contextBuilder.xml")
)
@Service(Service.Level.PROJECT)
public final class ContextBuilderState implements PersistentStateComponent<ContextBuilderState.State> {

    // ---------------------------------------------------------
    // INNER CLASS: Serializable state
    // ---------------------------------------------------------
    /**
     * The actual data to persist.
     * Must have public fields or getters/setters for serialization.
     */
    public static class State {
        // History of generated contexts
        public List<HistoryEntry> history = new ArrayList<>();
    }

    /**
     * Represents one history entry.
     */
    public static class HistoryEntry {
        public long timestamp;
        public int fileCount;
        public int totalLines;
        public List<String> files = new ArrayList<>();
        public String projectDescription = "";
        public String content = "";

        // Default constructor required for serialization
        public HistoryEntry() {}

        public HistoryEntry(long timestamp, int fileCount, int totalLines,
                            List<String> files, String projectDescription, String content) {
            this.timestamp = timestamp;
            this.fileCount = fileCount;
            this.totalLines = totalLines;
            this.files = files;
            this.projectDescription = projectDescription;
            this.content = content;
        }
    }

    // ---------------------------------------------------------
    // FIELDS
    // ---------------------------------------------------------
    private State state = new State();
    private static final int MAX_HISTORY = 10;

    // ---------------------------------------------------------
    // PERSISTENT STATE COMPONENT METHODS
    // ---------------------------------------------------------

    /**
     * Called by IntelliJ to get current state for saving.
     */
    @Override
    public @Nullable State getState() {
        return state;
    }

    /**
     * Called by IntelliJ to restore state on startup.
     */
    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    // ---------------------------------------------------------
    // HISTORY MANAGEMENT
    // ---------------------------------------------------------

    /**
     * Add a new history entry.
     */
    public void addHistoryEntry(HistoryEntry entry) {
        // Add to beginning (most recent first)
        state.history.add(0, entry);

        // Keep only last N entries
        while (state.history.size() > MAX_HISTORY) {
            state.history.remove(state.history.size() - 1);
        }
    }

    /**
     * Get all history entries.
     */
    public List<HistoryEntry> getHistory() {
        return new ArrayList<>(state.history);
    }

    /**
     * Get entry by index.
     */
    public HistoryEntry getHistoryEntry(int index) {
        if (index >= 0 && index < state.history.size()) {
            return state.history.get(index);
        }
        return null;
    }

    /**
     * Clear all history.
     */
    public void clearHistory() {
        state.history.clear();
    }

    // ---------------------------------------------------------
    // STATIC HELPER
    // ---------------------------------------------------------
    public static ContextBuilderState getInstance(Project project) {
        return project.getService(ContextBuilderState.class);
    }
}
