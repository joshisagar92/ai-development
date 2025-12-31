package com.contextbuilder;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================
 * CONTEXT GENERATOR
 * =============================================================
 * Generates LLM context from selected files in gitingest style.
 *
 * Equivalent to VS Code's generator.ts.
 *
 * OUTPUT FORMAT:
 * ================================================
 * PROJECT DESCRIPTION
 * ================================================
 * [description text]
 *
 * ================================================
 * FILE: path/to/file.java
 * DESCRIPTION: User's description
 * ================================================
 * [file contents]
 * =============================================================
 */
public class ContextGenerator {

    // ---------------------------------------------------------
    // CONSTANTS
    // ---------------------------------------------------------

    /** Separator line for gitingest-style output */
    private static final String SEPARATOR = "================================================";

    /** Large file threshold (100KB) */
    public static final long LARGE_FILE_THRESHOLD = 100 * 1024;

    // ---------------------------------------------------------
    // RESULT CLASSES
    // ---------------------------------------------------------

    /**
     * Result of context generation.
     */
    public static class GeneratorResult {
        public final String content;
        public final int fileCount;
        public final int totalLines;

        public GeneratorResult(String content, int fileCount, int totalLines) {
            this.content = content;
            this.fileCount = fileCount;
            this.totalLines = totalLines;
        }
    }

    /**
     * Information about a large file.
     */
    public static class LargeFileInfo {
        public final String path;
        public final long size;
        public final String sizeFormatted;

        public LargeFileInfo(String path, long size) {
            this.path = path;
            this.size = size;
            this.sizeFormatted = formatFileSize(size);
        }
    }

    // ---------------------------------------------------------
    // MAIN GENERATION METHOD
    // ---------------------------------------------------------

    /**
     * Generate context from selected files.
     *
     * @param project            The current project
     * @param files              List of selected files
     * @param projectDescription Optional project description
     * @return GeneratorResult with content and statistics
     */
    public static GeneratorResult generate(
            Project project,
            List<ContextBuilderService.SelectedFile> files,
            String projectDescription
    ) {
        StringBuilder output = new StringBuilder();
        int totalLines = 0;

        // ---------------------------------------------------------
        // Add project description if provided
        // ---------------------------------------------------------
        if (projectDescription != null && !projectDescription.trim().isEmpty()) {
            output.append(SEPARATOR).append("\n");
            output.append("PROJECT DESCRIPTION").append("\n");
            output.append(SEPARATOR).append("\n");
            output.append(projectDescription.trim()).append("\n\n");
        }

        // ---------------------------------------------------------
        // Process each file
        // ---------------------------------------------------------
        for (ContextBuilderService.SelectedFile selectedFile : files) {
            VirtualFile file = selectedFile.getFile();
            String relativePath = selectedFile.getRelativePath(project);
            String description = selectedFile.getDescription();

            // Add file header
            output.append(SEPARATOR).append("\n");
            output.append("FILE: ").append(relativePath).append("\n");

            // Add description if present
            if (description != null && !description.trim().isEmpty()) {
                output.append("DESCRIPTION: ").append(description.trim()).append("\n");
            }

            output.append(SEPARATOR).append("\n");

            // Read and add file content
            try {
                String content = readFileContent(file);
                output.append(content);

                // Count lines
                int lines = content.split("\n", -1).length;
                totalLines += lines;

                // Ensure newline at end
                if (!content.endsWith("\n")) {
                    output.append("\n");
                }
            } catch (IOException e) {
                output.append("// Error reading file: ").append(e.getMessage()).append("\n");
            }

            output.append("\n");
        }

        return new GeneratorResult(output.toString(), files.size(), totalLines);
    }

    // ---------------------------------------------------------
    // LARGE FILE DETECTION
    // ---------------------------------------------------------

    /**
     * Check for files exceeding the size threshold.
     *
     * @param files List of selected files
     * @param project The current project
     * @return List of large files with their info
     */
    public static List<LargeFileInfo> checkForLargeFiles(
            List<ContextBuilderService.SelectedFile> files,
            Project project
    ) {
        List<LargeFileInfo> largeFiles = new ArrayList<>();

        for (ContextBuilderService.SelectedFile selectedFile : files) {
            VirtualFile file = selectedFile.getFile();
            long size = file.getLength();

            if (size > LARGE_FILE_THRESHOLD) {
                String relativePath = selectedFile.getRelativePath(project);
                largeFiles.add(new LargeFileInfo(relativePath, size));
            }
        }

        return largeFiles;
    }

    // ---------------------------------------------------------
    // HELPER METHODS
    // ---------------------------------------------------------

    /**
     * Read file content as string.
     *
     * VirtualFile provides contentsToByteArray() which is more efficient
     * than using InputStreams for small-medium files.
     */
    private static String readFileContent(VirtualFile file) throws IOException {
        byte[] bytes = file.contentsToByteArray();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Format file size in human-readable form.
     *
     * @param bytes File size in bytes
     * @return Formatted string (e.g., "1.5 MB")
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
}
