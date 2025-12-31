import * as vscode from 'vscode';
import { SelectedFile } from './panel';

// =============================================================
// CONSTANTS
// =============================================================

// Separator line used between files (same as gitingest)
const SEPARATOR = '================================================';

// Files larger than this will trigger a warning (100KB)
export const LARGE_FILE_THRESHOLD = 100 * 1024;

// =============================================================
// LARGE FILE INFO
// =============================================================

/**
 * Information about a large file (for warning dialog)
 */
export interface LargeFileInfo {
    path: string;       // Relative path
    size: number;       // Size in bytes
    sizeFormatted: string;  // Human readable (e.g., "150 KB")
}

/**
 * Check files for size BEFORE generating.
 * This allows us to warn the user without reading all file contents.
 */
export async function checkForLargeFiles(files: SelectedFile[]): Promise<LargeFileInfo[]> {
    const largeFiles: LargeFileInfo[] = [];

    for (const file of files) {
        try {
            const stat = await vscode.workspace.fs.stat(file.uri);
            if (stat.size > LARGE_FILE_THRESHOLD) {
                largeFiles.push({
                    path: vscode.workspace.asRelativePath(file.uri),
                    size: stat.size,
                    sizeFormatted: formatFileSize(stat.size)
                });
            }
        } catch {
            // If we can't stat the file, skip it (error will show during generation)
        }
    }

    return largeFiles;
}

/**
 * Format bytes into human-readable string
 */
export function formatFileSize(bytes: number): string {
    if (bytes < 1024) {
        return `${bytes} B`;
    } else if (bytes < 1024 * 1024) {
        return `${(bytes / 1024).toFixed(1)} KB`;
    } else {
        return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
    }
}

// =============================================================
// INTERFACES
// =============================================================

/**
 * Options passed to the generator
 */
export interface GeneratorOptions {
    files: SelectedFile[];      // Files to include
    projectDescription: string; // Optional project description
}

/**
 * Result returned by the generator
 */
export interface GeneratorResult {
    content: string;        // The generated context text
    fileCount: number;      // Number of files included
    totalLines: number;     // Total lines across all files
    totalSize: number;      // Total size in bytes
    largeFiles: string[];   // List of files over 100KB (for warning)
}

// =============================================================
// MAIN GENERATOR FUNCTION
// =============================================================

/**
 * Generates context text from selected files.
 *
 * Output format (gitingest style):
 * ================================================
 * PROJECT DESCRIPTION
 * ================================================
 * [project description text]
 *
 * ================================================
 * FILE: path/to/file.ts
 * DESCRIPTION: user's description
 * ================================================
 * [file contents]
 */
export async function generateContext(options: GeneratorOptions): Promise<GeneratorResult> {
    const { files, projectDescription } = options;

    // We'll build the output string here
    let output = '';
    let totalLines = 0;
    let totalSize = 0;
    const largeFiles: string[] = [];

    // ---------------------------------------------------------
    // SECTION 1: Add project description (if provided)
    // ---------------------------------------------------------
    if (projectDescription && projectDescription.trim().length > 0) {
        output += SEPARATOR + '\n';
        output += 'PROJECT DESCRIPTION\n';
        output += SEPARATOR + '\n';
        output += projectDescription.trim() + '\n';
        output += '\n';  // Blank line after description
    }

    // ---------------------------------------------------------
    // SECTION 2: Process each file
    // ---------------------------------------------------------
    for (const file of files) {
        // Get relative path for display (e.g., "src/auth/login.ts" instead of full path)
        const relativePath = vscode.workspace.asRelativePath(file.uri);

        try {
            // Read file stats (size, etc.)
            const stat = await vscode.workspace.fs.stat(file.uri);
            const fileSize = stat.size;
            totalSize += fileSize;

            // Check if file is large (for warning later)
            if (fileSize > LARGE_FILE_THRESHOLD) {
                largeFiles.push(relativePath);
            }

            // Read file contents as bytes, then convert to string
            const contentBytes = await vscode.workspace.fs.readFile(file.uri);
            const content = new TextDecoder('utf-8').decode(contentBytes);

            // Count lines
            const lineCount = content.split('\n').length;
            totalLines += lineCount;

            // ---------------------------------------------------------
            // Build output for this file
            // ---------------------------------------------------------
            output += SEPARATOR + '\n';
            output += `FILE: ${relativePath}\n`;

            // Add description only if user provided one
            if (file.description && file.description.trim().length > 0) {
                output += `DESCRIPTION: ${file.description.trim()}\n`;
            }

            output += SEPARATOR + '\n';
            output += content;

            // Ensure file ends with newline
            if (!content.endsWith('\n')) {
                output += '\n';
            }
            output += '\n';  // Blank line between files

        } catch (error) {
            // If we can't read a file, include an error message instead
            output += SEPARATOR + '\n';
            output += `FILE: ${relativePath}\n`;
            output += SEPARATOR + '\n';
            output += `[Error reading file: ${error}]\n`;
            output += '\n';
        }
    }

    return {
        content: output,
        fileCount: files.length,
        totalLines,
        totalSize,
        largeFiles
    };
}
