import * as vscode from 'vscode';

// =============================================================
// CONSTANTS
// =============================================================

// Key used to store history in VS Code's globalState
const HISTORY_STORAGE_KEY = 'contextBuilder.history';

// Maximum number of history entries to keep
const MAX_HISTORY_ENTRIES = 10;

// =============================================================
// INTERFACES
// =============================================================

/**
 * Represents a single history entry (one generation)
 */
export interface HistoryEntry {
    id: string;              // Unique ID (timestamp-based)
    timestamp: number;       // When it was generated (ms since epoch)
    fileCount: number;       // Number of files included
    totalLines: number;      // Total lines across all files
    files: string[];         // List of relative file paths
    projectDescription: string;  // Project description (if any)
    content: string;         // The actual generated content
}

/**
 * Summary of a history entry (for display, without full content)
 */
export interface HistorySummary {
    id: string;
    timestamp: number;
    fileCount: number;
    totalLines: number;
    files: string[];         // First few files for preview
    projectDescription: string;
}

// =============================================================
// HISTORY MANAGER CLASS
// =============================================================

/**
 * Manages history storage using VS Code's globalState.
 *
 * globalState is VS Code's built-in persistent storage that:
 * - Survives extension restarts
 * - Is specific to the workspace (or global if using globalState)
 * - Stores data as JSON
 */
export class HistoryManager {

    // VS Code's extension context (needed for globalState access)
    private context: vscode.ExtensionContext;

    constructor(context: vscode.ExtensionContext) {
        this.context = context;
    }

    // ---------------------------------------------------------
    // ADD: Save a new generation to history
    // ---------------------------------------------------------
    async addEntry(entry: Omit<HistoryEntry, 'id' | 'timestamp'>): Promise<HistoryEntry> {
        // Get existing history
        const history = this.getAll();

        // Create new entry with ID and timestamp
        const newEntry: HistoryEntry = {
            ...entry,
            id: this.generateId(),
            timestamp: Date.now()
        };

        // Add to beginning of array (most recent first)
        history.unshift(newEntry);

        // Keep only the last N entries
        const trimmedHistory = history.slice(0, MAX_HISTORY_ENTRIES);

        // Save back to storage
        await this.context.globalState.update(HISTORY_STORAGE_KEY, trimmedHistory);

        return newEntry;
    }

    // ---------------------------------------------------------
    // GET ALL: Retrieve all history entries
    // ---------------------------------------------------------
    getAll(): HistoryEntry[] {
        return this.context.globalState.get<HistoryEntry[]>(HISTORY_STORAGE_KEY, []);
    }

    // ---------------------------------------------------------
    // GET SUMMARIES: Get entries without full content (for UI)
    // ---------------------------------------------------------
    getSummaries(): HistorySummary[] {
        const history = this.getAll();
        return history.map(entry => ({
            id: entry.id,
            timestamp: entry.timestamp,
            fileCount: entry.fileCount,
            totalLines: entry.totalLines,
            files: entry.files.slice(0, 3),  // First 3 files for preview
            projectDescription: entry.projectDescription
        }));
    }

    // ---------------------------------------------------------
    // GET BY ID: Retrieve a specific entry by its ID
    // ---------------------------------------------------------
    getById(id: string): HistoryEntry | undefined {
        const history = this.getAll();
        return history.find(entry => entry.id === id);
    }

    // ---------------------------------------------------------
    // DELETE: Remove a specific entry
    // ---------------------------------------------------------
    async deleteEntry(id: string): Promise<void> {
        const history = this.getAll();
        const filtered = history.filter(entry => entry.id !== id);
        await this.context.globalState.update(HISTORY_STORAGE_KEY, filtered);
    }

    // ---------------------------------------------------------
    // CLEAR: Remove all history
    // ---------------------------------------------------------
    async clearAll(): Promise<void> {
        await this.context.globalState.update(HISTORY_STORAGE_KEY, []);
    }

    // ---------------------------------------------------------
    // HELPER: Generate unique ID
    // ---------------------------------------------------------
    private generateId(): string {
        // Use timestamp + random suffix for uniqueness
        return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
    }

    // ---------------------------------------------------------
    // HELPER: Format timestamp for display
    // ---------------------------------------------------------
    static formatTimestamp(timestamp: number): string {
        const date = new Date(timestamp);
        const now = new Date();

        // If today, show time only
        if (date.toDateString() === now.toDateString()) {
            return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        }

        // If this year, show date without year
        if (date.getFullYear() === now.getFullYear()) {
            return date.toLocaleDateString([], { month: 'short', day: 'numeric' });
        }

        // Otherwise show full date
        return date.toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' });
    }
}
