package com.contextbuilder;

import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * =============================================================
 * CONTEXT BUILDER PANEL
 * =============================================================
 * The main UI panel for the Context Builder tool window.
 *
 * Equivalent to VS Code's _getHtmlContent() in panel.ts.
 *
 * KEY DIFFERENCE FROM VS CODE:
 * - VS Code uses HTML/CSS/JavaScript in a webview
 * - JetBrains uses Swing components (JPanel, JButton, JList, etc.)
 * - IntelliJ provides JB* variants (JBLabel, JBList) with better styling
 *
 * LAYOUT:
 * ┌─────────────────────────────────────────┐
 * │ Context Builder          [X files]      │  ← Header
 * ├─────────────────────────────────────────┤
 * │ Project Description                     │  ← Label
 * │ ┌─────────────────────────────────────┐ │
 * │ │ [textarea]                          │ │  ← JBTextArea
 * │ └─────────────────────────────────────┘ │
 * ├─────────────────────────────────────────┤
 * │ Selected Files                          │  ← Label
 * │ ┌─────────────────────────────────────┐ │
 * │ │ file1.java                      [x] │ │  ← JBList
 * │ │ file2.java                      [x] │ │
 * │ └─────────────────────────────────────┘ │
 * ├─────────────────────────────────────────┤
 * │ [Generate Context]  [Clear All]         │  ← Buttons
 * └─────────────────────────────────────────┘
 * =============================================================
 */
public class ContextBuilderPanel extends JPanel {

    // ---------------------------------------------------------
    // FIELDS
    // ---------------------------------------------------------
    private final Project project;
    private final ContextBuilderService service;

    // UI Components
    private final JBTextArea projectDescriptionArea;
    private final JBLabel fileCountLabel;
    private final DefaultListModel<FileListItem> fileListModel;
    private final JBList<FileListItem> fileList;
    private final JButton generateButton;
    private final JButton clearButton;

    // History UI Components
    private final JBLabel historyCountLabel;
    private final JPanel historyListPanel;

    // ---------------------------------------------------------
    // CONSTRUCTOR
    // ---------------------------------------------------------
    public ContextBuilderPanel(Project project) {
        this.project = project;
        this.service = ContextBuilderService.getInstance(project);

        // Set layout - BorderLayout divides into NORTH, CENTER, SOUTH, etc.
        setLayout(new BorderLayout());
        // Add padding around the panel
        setBorder(JBUI.Borders.empty(10));

        // =================================================
        // HEADER SECTION (NORTH)
        // =================================================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(JBUI.Borders.emptyBottom(10));

        JBLabel titleLabel = new JBLabel("Context Builder");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));

        fileCountLabel = new JBLabel("0 file(s)");
        fileCountLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(fileCountLabel, BorderLayout.EAST);

        // =================================================
        // CENTER SECTION - Main content
        // =================================================
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // --- Project Description ---
        JBLabel descLabel = new JBLabel("Project Description");
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descLabel.setBorder(JBUI.Borders.emptyBottom(5));

        projectDescriptionArea = new JBTextArea(3, 20);
        projectDescriptionArea.setLineWrap(true);
        projectDescriptionArea.setWrapStyleWord(true);
        projectDescriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Listen for changes
        projectDescriptionArea.getDocument().addDocumentListener(new SimpleDocumentListener(() -> {
            service.setProjectDescription(projectDescriptionArea.getText());
        }));

        JBScrollPane descScrollPane = new JBScrollPane(projectDescriptionArea);
        descScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // --- Selected Files Label ---
        JBLabel filesLabel = new JBLabel("Selected Files");
        filesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filesLabel.setBorder(JBUI.Borders.empty(10, 0, 5, 0));

        // --- File List ---
        fileListModel = new DefaultListModel<>();
        fileList = new JBList<>(fileListModel);
        fileList.setCellRenderer(new FileListCellRenderer());
        fileList.setAlignmentX(Component.LEFT_ALIGNMENT);

        JBScrollPane fileScrollPane = new JBScrollPane(fileList);
        fileScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add to center panel
        centerPanel.add(descLabel);
        centerPanel.add(descScrollPane);
        centerPanel.add(filesLabel);
        centerPanel.add(fileScrollPane);

        // =================================================
        // BUTTON SECTION (SOUTH)
        // =================================================
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.setBorder(JBUI.Borders.emptyTop(10));

        generateButton = new JButton("Generate Context");
        generateButton.addActionListener(e -> onGenerate());

        clearButton = new JButton("Clear All");
        clearButton.addActionListener(e -> onClear());

        buttonPanel.add(generateButton);
        buttonPanel.add(clearButton);

        // =================================================
        // HISTORY SECTION
        // =================================================
        JPanel historySection = new JPanel(new BorderLayout());
        historySection.setBorder(JBUI.Borders.empty(10, 0, 0, 0));

        // History header
        JPanel historyHeader = new JPanel(new BorderLayout());
        JBLabel historyLabel = new JBLabel("Recent History");
        historyLabel.setFont(historyLabel.getFont().deriveFont(Font.BOLD));
        historyCountLabel = new JBLabel("0 generation(s)");
        historyCountLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        historyHeader.add(historyLabel, BorderLayout.WEST);
        historyHeader.add(historyCountLabel, BorderLayout.EAST);

        // History list (vertical panel with items)
        historyListPanel = new JPanel();
        historyListPanel.setLayout(new BoxLayout(historyListPanel, BoxLayout.Y_AXIS));

        JBScrollPane historyScrollPane = new JBScrollPane(historyListPanel);
        historyScrollPane.setPreferredSize(new Dimension(0, 120));

        historySection.add(historyHeader, BorderLayout.NORTH);
        historySection.add(historyScrollPane, BorderLayout.CENTER);

        // =================================================
        // ASSEMBLE LAYOUT (using nested panels for complex layout)
        // =================================================
        // Top section: header + center content + buttons
        JPanel topSection = new JPanel(new BorderLayout());
        topSection.add(headerPanel, BorderLayout.NORTH);
        topSection.add(centerPanel, BorderLayout.CENTER);
        topSection.add(buttonPanel, BorderLayout.SOUTH);

        // Main layout: top section + history at bottom
        add(topSection, BorderLayout.CENTER);
        add(historySection, BorderLayout.SOUTH);

        // =================================================
        // LISTEN FOR SERVICE CHANGES
        // =================================================
        service.addChangeListener(this::refreshUI);

        // Initial refresh
        refreshUI();
        refreshHistoryUI();
    }

    // ---------------------------------------------------------
    // HISTORY UI REFRESH
    // ---------------------------------------------------------
    /**
     * Refresh the history list UI from persistent storage.
     */
    private void refreshHistoryUI() {
        ContextBuilderState state = ContextBuilderState.getInstance(project);
        List<ContextBuilderState.HistoryEntry> history = state.getHistory();

        // Update count label
        historyCountLabel.setText(history.size() + " generation(s)");

        // Clear and rebuild history list
        historyListPanel.removeAll();

        if (history.isEmpty()) {
            JBLabel emptyLabel = new JBLabel("No history yet. Generate some context!");
            emptyLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
            emptyLabel.setFont(emptyLabel.getFont().deriveFont(Font.ITALIC));
            emptyLabel.setBorder(JBUI.Borders.empty(10));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            historyListPanel.add(emptyLabel);
        } else {
            for (int i = 0; i < history.size(); i++) {
                ContextBuilderState.HistoryEntry entry = history.get(i);
                JPanel itemPanel = createHistoryItemPanel(entry, i);
                historyListPanel.add(itemPanel);
            }
        }

        // Force UI update
        historyListPanel.revalidate();
        historyListPanel.repaint();
    }

    /**
     * Create a panel for a single history item.
     *
     * Layout:
     * ┌─────────────────────────────────────────┐
     * │ 10:30 AM          3 files, 450 lines    │
     * │ file1.java, file2.java +1 more          │
     * │ [📋 Copy] [💾 Save]                     │
     * └─────────────────────────────────────────┘
     */
    private JPanel createHistoryItemPanel(ContextBuilderState.HistoryEntry entry, int index) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(JBUI.Borders.empty(5, 0));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Row 1: Timestamp and stats ---
        JPanel row1 = new JPanel(new BorderLayout());
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);

        String timeStr = formatTimestamp(entry.timestamp);
        JBLabel timeLabel = new JBLabel(timeStr);
        timeLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        String statsStr = entry.fileCount + " files, " + entry.totalLines + " lines";
        JBLabel statsLabel = new JBLabel(statsStr);

        row1.add(timeLabel, BorderLayout.WEST);
        row1.add(statsLabel, BorderLayout.EAST);

        // --- Row 2: File preview ---
        String filesPreview = buildFilesPreview(entry.files);
        JBLabel filesLabel = new JBLabel(filesPreview);
        filesLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        filesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- Row 3: Action buttons ---
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton copyBtn = new JButton("Copy");
        copyBtn.setFont(copyBtn.getFont().deriveFont(11f));
        copyBtn.addActionListener(e -> onHistoryCopy(index));

        JButton saveBtn = new JButton("Save");
        saveBtn.setFont(saveBtn.getFont().deriveFont(11f));
        saveBtn.addActionListener(e -> onHistorySave(index));

        buttonsPanel.add(copyBtn);
        buttonsPanel.add(saveBtn);

        // Assemble
        panel.add(row1);
        panel.add(filesLabel);
        panel.add(buttonsPanel);

        // Add separator except for last item
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));

        return panel;
    }

    /**
     * Format timestamp for display.
     */
    private String formatTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        Date now = new Date();

        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");
        SimpleDateFormat fullFormat = new SimpleDateFormat("MMM d, yyyy");

        // Same day: show time only
        SimpleDateFormat dayCheck = new SimpleDateFormat("yyyyMMdd");
        if (dayCheck.format(date).equals(dayCheck.format(now))) {
            return timeFormat.format(date);
        }

        // Same year: show month and day
        SimpleDateFormat yearCheck = new SimpleDateFormat("yyyy");
        if (yearCheck.format(date).equals(yearCheck.format(now))) {
            return dateFormat.format(date);
        }

        // Different year: show full date
        return fullFormat.format(date);
    }

    /**
     * Build a preview string of files (first 2 + "more" count).
     */
    private String buildFilesPreview(List<String> files) {
        if (files.isEmpty()) {
            return "(no files)";
        }

        StringBuilder sb = new StringBuilder();
        int showCount = Math.min(2, files.size());

        for (int i = 0; i < showCount; i++) {
            if (i > 0) sb.append(", ");
            sb.append(files.get(i));
        }

        if (files.size() > 2) {
            sb.append(" +").append(files.size() - 2).append(" more");
        }

        return sb.toString();
    }

    /**
     * Handle copy from history.
     */
    private void onHistoryCopy(int index) {
        ContextBuilderState state = ContextBuilderState.getInstance(project);
        ContextBuilderState.HistoryEntry entry = state.getHistoryEntry(index);

        if (entry != null) {
            copyToClipboard(entry.content);
            JOptionPane.showMessageDialog(this,
                    "Copied to clipboard! (" + entry.fileCount + " files, " + entry.totalLines + " lines)",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Handle save from history.
     */
    private void onHistorySave(int index) {
        ContextBuilderState state = ContextBuilderState.getInstance(project);
        ContextBuilderState.HistoryEntry entry = state.getHistoryEntry(index);

        if (entry != null) {
            saveToFile(entry.content);
        }
    }

    // ---------------------------------------------------------
    // UI REFRESH
    // ---------------------------------------------------------
    /**
     * Refresh the UI to match the service state.
     * Called when files are added/removed.
     */
    private void refreshUI() {
        // Update file count
        int count = service.getFileCount();
        fileCountLabel.setText(count + " file(s)");

        // Update file list
        fileListModel.clear();
        List<ContextBuilderService.SelectedFile> files = service.getSelectedFiles();
        for (int i = 0; i < files.size(); i++) {
            ContextBuilderService.SelectedFile sf = files.get(i);
            fileListModel.addElement(new FileListItem(i, sf, project));
        }

        // Enable/disable buttons
        boolean hasFiles = count > 0;
        generateButton.setEnabled(hasFiles);
        clearButton.setEnabled(hasFiles);
    }

    // ---------------------------------------------------------
    // BUTTON HANDLERS
    // ---------------------------------------------------------
    private void onGenerate() {
        // ---------------------------------------------------------
        // STEP 1: Check if there are files
        // ---------------------------------------------------------
        List<ContextBuilderService.SelectedFile> files = service.getSelectedFiles();
        if (files.isEmpty()) {
            return;
        }

        // ---------------------------------------------------------
        // STEP 2: Check for large files
        // ---------------------------------------------------------
        List<ContextGenerator.LargeFileInfo> largeFiles =
                ContextGenerator.checkForLargeFiles(files, project);

        if (!largeFiles.isEmpty()) {
            // Build warning message
            StringBuilder warning = new StringBuilder();
            warning.append("The following files exceed 100KB:\n\n");
            for (ContextGenerator.LargeFileInfo info : largeFiles) {
                warning.append("• ").append(info.path)
                       .append(" (").append(info.sizeFormatted).append(")\n");
            }
            warning.append("\nContinue anyway?");

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    warning.toString(),
                    "Large Files Detected",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // ---------------------------------------------------------
        // STEP 3: Generate content
        // ---------------------------------------------------------
        ContextGenerator.GeneratorResult result = ContextGenerator.generate(
                project,
                files,
                service.getProjectDescription()
        );

        // ---------------------------------------------------------
        // STEP 4: Show result dialog (Step 5.5 will add clipboard/save)
        // ---------------------------------------------------------
        handleGeneratedContent(result);
    }

    /**
     * Handle the generated content - show options to copy/save.
     *
     * Uses IntelliJ's native dialogs:
     * - JOptionPane for the choice dialog
     * - FileSaverDialog for saving files
     * - Toolkit clipboard for copy
     */
    private void handleGeneratedContent(ContextGenerator.GeneratorResult result) {
        String message = String.format(
                "Generated context:\n• %d file(s)\n• %d lines\n• %d characters\n\nWhat would you like to do?",
                result.fileCount,
                result.totalLines,
                result.content.length()
        );

        // ---------------------------------------------------------
        // Show options dialog
        // ---------------------------------------------------------
        String[] options = {"Copy to Clipboard", "Save to File", "Both", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                this,
                message,
                "Context Generated",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        boolean shouldCopy = (choice == 0 || choice == 2);  // Copy or Both
        boolean shouldSave = (choice == 1 || choice == 2);  // Save or Both

        // ---------------------------------------------------------
        // Copy to clipboard
        // ---------------------------------------------------------
        if (shouldCopy) {
            copyToClipboard(result.content);
        }

        // ---------------------------------------------------------
        // Save to file using IntelliJ's FileSaverDialog
        // ---------------------------------------------------------
        if (shouldSave) {
            saveToFile(result.content);
        }

        // ---------------------------------------------------------
        // Show confirmation
        // ---------------------------------------------------------
        if (shouldCopy && !shouldSave) {
            JOptionPane.showMessageDialog(this,
                    "Copied to clipboard!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        // ---------------------------------------------------------
        // Save to history
        // ---------------------------------------------------------
        saveToHistory(result);
    }

    /**
     * Save the generated content to history for later retrieval.
     */
    private void saveToHistory(ContextGenerator.GeneratorResult result) {
        // Get the state service (persistent storage)
        ContextBuilderState state = ContextBuilderState.getInstance(project);

        // Build list of file paths
        List<String> filePaths = new ArrayList<>();
        for (ContextBuilderService.SelectedFile sf : service.getSelectedFiles()) {
            filePaths.add(sf.getRelativePath(project));
        }

        // Create history entry
        ContextBuilderState.HistoryEntry entry = new ContextBuilderState.HistoryEntry(
                System.currentTimeMillis(),           // timestamp
                result.fileCount,                     // fileCount
                result.totalLines,                    // totalLines
                filePaths,                            // files
                service.getProjectDescription(),      // projectDescription
                result.content                        // content
        );

        // Add to persistent state
        state.addHistoryEntry(entry);

        // Refresh history UI
        refreshHistoryUI();
    }

    /**
     * Save content to a file using IntelliJ's native file saver dialog.
     *
     * KEY CLASSES:
     * - FileSaverDescriptor: Configures the save dialog (title, extensions)
     * - FileSaverDialog: The actual dialog
     * - VirtualFileWrapper: Wraps the selected file path
     */
    private void saveToFile(String content) {
        // ---------------------------------------------------------
        // STEP 1: Create save dialog descriptor
        // ---------------------------------------------------------
        // FileSaverDescriptor(title, description, extensions...)
        FileSaverDescriptor descriptor = new FileSaverDescriptor(
                "Save Context",                    // Dialog title
                "Save generated context to file",  // Description
                "txt", "md"                        // Allowed extensions
        );

        // ---------------------------------------------------------
        // STEP 2: Create and show the dialog
        // ---------------------------------------------------------
        FileSaverDialog dialog = FileChooserFactory.getInstance()
                .createSaveFileDialog(descriptor, project);

        // show() returns VirtualFileWrapper or null if cancelled
        // Parameters: (parent component, default filename)
        VirtualFileWrapper fileWrapper = dialog.save("context.txt");

        if (fileWrapper == null) {
            // User cancelled
            return;
        }

        // ---------------------------------------------------------
        // STEP 3: Write content to selected file
        // ---------------------------------------------------------
        try {
            // VirtualFileWrapper.getFile() returns java.io.File
            Files.write(
                    fileWrapper.getFile().toPath(),
                    content.getBytes(StandardCharsets.UTF_8)
            );

            JOptionPane.showMessageDialog(this,
                    "Saved to: " + fileWrapper.getFile().getName(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save file: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Copy text to system clipboard.
     */
    private void copyToClipboard(String text) {
        java.awt.datatransfer.StringSelection selection =
                new java.awt.datatransfer.StringSelection(text);
        java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(selection, selection);
    }

    private void onClear() {
        service.clearFiles();
    }

    // ---------------------------------------------------------
    // INNER CLASS: File List Item
    // ---------------------------------------------------------
    /**
     * Represents an item in the file list.
     */
    private static class FileListItem {
        final int index;
        final ContextBuilderService.SelectedFile selectedFile;
        final String displayPath;

        FileListItem(int index, ContextBuilderService.SelectedFile sf, Project project) {
            this.index = index;
            this.selectedFile = sf;
            this.displayPath = sf.getRelativePath(project);
        }

        @Override
        public String toString() {
            return displayPath;
        }
    }

    // ---------------------------------------------------------
    // INNER CLASS: File List Cell Renderer
    // ---------------------------------------------------------
    /**
     * Custom renderer for file list items.
     * Shows file path with a remove button.
     */
    private class FileListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            if (value instanceof FileListItem) {
                FileListItem item = (FileListItem) value;
                label.setText(item.displayPath);
                label.setBorder(JBUI.Borders.empty(5));
            }

            return label;
        }
    }

    // ---------------------------------------------------------
    // INNER CLASS: Simple Document Listener
    // ---------------------------------------------------------
    /**
     * Simplified DocumentListener that calls a single callback.
     */
    private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable callback;

        SimpleDocumentListener(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            callback.run();
        }
    }
}
