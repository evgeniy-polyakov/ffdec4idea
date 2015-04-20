package com.epolyakov.ffdec4idea.actions;

import com.epolyakov.ffdec4idea.vfs.DecompiledSwfFileSystem;
import com.intellij.ide.projectView.impl.ProjectViewTree;
import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.jpexs.decompiler.flash.SWF;

import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author epolyakov
 */
public class DecompileAction extends AnAction {

    private static ResourceBundle resources = ResourceBundle.getBundle("com.epolyakov.ffdec4idea.resources.ffdec4idea");

    @Override
    public void actionPerformed(AnActionEvent event) {
        VirtualFile swfFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (isSwfFile(swfFile)) {
            try {
                String[] definitions = getDefinitions(event.getData(PlatformDataKeys.CONTEXT_COMPONENT), swfFile);
                openDefinitions(definitions, swfFile, event.getProject());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void update(AnActionEvent event) {
        VirtualFile file = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        Boolean enabled = isSwfFile(file);
        event.getPresentation().setEnabled(enabled);
        event.getPresentation().setVisible(enabled);
    }

    /**
     * Gets AS3 definitions selected in the project view.
     *
     * @param component The project view component.
     * @param swfFile   The selected swf file.
     * @return An array of definitions.
     */
    private String[] getDefinitions(Component component, VirtualFile swfFile) {
        if (component instanceof ProjectViewTree) {

            ProjectViewTree projectViewTree = (ProjectViewTree) component;
            TreePath[] selectionPaths = projectViewTree.getSelectionPaths();

            if (selectionPaths != null) {
                String[] definitions = new String[selectionPaths.length];
                for (int i = 0; i < selectionPaths.length; i++) {
                    definitions[i] = getDefinitionPath(selectionPaths[i], swfFile.getName());
                }
                return definitions;
            }
        }
        return new String[0];
    }

    /**
     * Opens the selected AS3 definitions
     *
     * @param definitions The list of definitions.
     * @param swfFile     The selected swf file
     * @param project     The current project.
     * @throws IOException
     * @throws InterruptedException
     */
    private void openDefinitions(String[] definitions, VirtualFile swfFile, Project project)
            throws IOException, InterruptedException {

        SWF swf = DecompiledSwfFileSystem.getInstance().getSwf(swfFile);

        for (String definition : definitions) {

            // The empty definition path means that the swf file itself is selected
            // Try to get a document class in this case.
            if (definition == null || definition.isEmpty()) {
                definition = swf.getDocumentClass();
                if (definition != null) {
                    definition = definition.replace('.', '/');
                }
            }

            // There is no document class. Show an appropriate message.
            if (definition == null || definition.isEmpty()) {
                // todo
                continue;
            }

            String path = swfFile.getPath() + DecompiledSwfFileSystem.PATH_SEPARATOR + definition;
            VirtualFile file = DecompiledSwfFileSystem.getInstance().findFileByPath(path);

            if (file != null) {
                OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, file, 0);
                FileEditorManager.getInstance(project).openTextEditor(openFileDescriptor, true);
            }
        }
    }

    /**
     * Checks if the file is of swf type.
     *
     * @param file The given virtual file.
     * @return True if the file is swf.
     */
    private boolean isSwfFile(VirtualFile file) {
        return file != null && file.getFileType() == FlexApplicationComponent.SWF_FILE_TYPE;
    }

    /**
     * Gets the path to AS3 definition in the swf file.
     *
     * @param treePath The path to the selected node in the project view component.
     * @param fileName The name of the swf file which should be an ancestor node.
     * @return The definition string like "com/mypackage/MyClass".
     */
    private String getDefinitionPath(TreePath treePath, String fileName) {

        StringBuilder sb = new StringBuilder();
        Object[] path = treePath.getPath();

        boolean fileNameFound = false;
        for (int i = path.length - 1; i >= 0; i--) {
            if (fileName.equals(path[i].toString())) {
                fileNameFound = true;
                break;
            } else {
                if (sb.length() > 0) {
                    sb.insert(0, '/');
                }
                sb.insert(0, path[i].toString());
            }
        }
        return fileNameFound ? sb.toString() : "";
    }

    /**
     * Adds a notification panel at the top of the editor window.
     *
     * @param editor The editor.
     * @param swf    The swf object.
     */
    private void addEditorNotificationPanel(Editor editor, SWF swf) {
        if (editor != null) {
            EditorNotificationPanel editorNotificationPanel = new EditorNotificationPanel();
            editorNotificationPanel.setText(MessageFormat.format(resources.getString("editor.notification"), swf.version));
            editor.setHeaderComponent(editorNotificationPanel);
        }
    }
}
