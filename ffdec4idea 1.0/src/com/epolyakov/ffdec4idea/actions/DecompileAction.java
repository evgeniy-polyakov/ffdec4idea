package com.epolyakov.ffdec4idea.actions;

import com.intellij.ide.projectView.impl.ProjectViewTree;
import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ClassPath;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;

import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author epolyakov
 */
public class DecompileAction extends AnAction {

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
                    definitions[i] = getDefinitionQualifiedName(selectionPaths[i], swfFile.getName());
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

        InputStream stream = swfFile.getInputStream();
        SWF swf = new SWF(stream, false);
        String tempDirectory = getSwfFileTempDirectory(project, swfFile);

        for (String definition : definitions) {

            // The empty definition path means that the swf file itself is selected
            // Try to get a document class in this case.
            if (definition == null || definition.isEmpty()) {
                definition = swf.getDocumentClass();
            }

            // There is no document class. Show an appropriate message.
            if (definition == null || definition.isEmpty()) {
                File file = ScriptExporter.export("// There is no document class in " + swfFile.getName() + ".", tempDirectory, "");
                openFileInEditor(file, swf, project);
                continue;
            }

            for (MyEntry<ClassPath, ScriptPack> scriptPack : swf.getAS3Packs()) {
                String classPath = scriptPack.getValue().getClassPath().toString();
                if (definition.equals(classPath)) {
                    File file = ScriptExporter.export(scriptPack.getValue(), tempDirectory);
                    openFileInEditor(file, swf, project);
                    break;
                }
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
     * Constructs a temp directory for the given swf file to put decompiled sources in.
     *
     * @param project The current project.
     * @param file    The selected swf file.
     * @return The path like "userTempDirectory/ffdec4idea/projectName/pathToSwf"
     */
    private String getSwfFileTempDirectory(Project project, VirtualFile file) {

        StringBuilder sb = new StringBuilder(FileUtil.getTempDirectory());
        sb.append(File.separatorChar);
        sb.append("ffdec4idea");
        sb.append(File.separatorChar);
        sb.append(project.getBaseDir().getName());

        String projectUrl = project.getBaseDir().getUrl();
        String fileUrl = file.getUrl();

        if (fileUrl.startsWith(projectUrl) && fileUrl.length() > projectUrl.length()) {
            // Use the relative path in project if the file is in the project directory
            String filePath = fileUrl.substring(projectUrl.length());
            if (filePath.charAt(0) != '/') {
                sb.append(File.separatorChar);
            }
            sb.append(filePath);
        } else {
            // Use the file name if the file is out of the project directory
            sb.append(File.separatorChar);
            sb.append(file.getName());
        }

        return sb.toString();
    }

    /**
     * Gets the qualified name of AS3 definition in the swf file.
     *
     * @param treePath The path to the selected node in the project view component.
     * @param fileName The name of the swf file which should be an ancestor node.
     * @return The definition string like "com.mypackage.MyClass".
     */
    private String getDefinitionQualifiedName(TreePath treePath, String fileName) {

        StringBuilder sb = new StringBuilder();
        Object[] path = treePath.getPath();

        boolean fileNameFound = false;
        for (int i = path.length - 1; i >= 0; i--) {
            if (fileName.equals(path[i].toString())) {
                fileNameFound = true;
                break;
            } else {
                if (sb.length() > 0) {
                    sb.insert(0, '.');
                }
                sb.insert(0, path[i].toString());
            }
        }
        return fileNameFound ? sb.toString() : "";
    }

    /**
     * Opens the specified file in the editor window.
     *
     * @param file    The file.
     * @param swf     The swf object.
     * @param project The current project.
     */
    private void openFileInEditor(File file, SWF swf, Project project) {
        if (file.exists()) {
            VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            if (virtualFile != null) {
                OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, virtualFile, 0);
                Editor editor = FileEditorManager.getInstance(project).openTextEditor(openFileDescriptor, true);
                addEditorNotificationPanel(editor, swf);
            }
        }
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
            editorNotificationPanel.setText("Decompiled by ffdec, swf version: " + swf.version);
            editor.setHeaderComponent(editorNotificationPanel);
        }
    }
}
