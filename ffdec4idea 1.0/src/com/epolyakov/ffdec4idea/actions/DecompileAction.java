package com.epolyakov.ffdec4idea.actions;

import com.intellij.ide.projectView.impl.ProjectViewTree;
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
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;

import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.io.InputStream;

/**
 * @author epolyakov
 */
public class DecompileAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent _anActionEvent) {
        VirtualFile swfFile = _anActionEvent.getData(PlatformDataKeys.VIRTUAL_FILE);
        // todo use file type instead of extension
        if (swfFile != null && "swf".equals(swfFile.getExtension())) {
            try {
                InputStream stream = swfFile.getInputStream();
                SWF swf = new SWF(stream, false);

//                String documentClass = swf.getDocumentClass();
//                String[] documentPath = documentClass.split("\\.");
//                String documentTagName = String.join("/", documentPath);

                Project project = _anActionEvent.getProject();

                String tempDirectory = getTempDirectory(project, swfFile);

                Component component = _anActionEvent.getData(PlatformDataKeys.CONTEXT_COMPONENT);
                if (component instanceof ProjectViewTree) {

                    ProjectViewTree projectViewTree = (ProjectViewTree) component;
                    TreePath[] selectionPaths = projectViewTree.getSelectionPaths();

                    if (selectionPaths != null) {
                        for (TreePath treePath : selectionPaths) {
                            String definitionPath = getDefinitionPath(treePath, swfFile.getName());
                            for (MyEntry<ClassPath, ScriptPack> scriptPack : swf.getAS3Packs()) {
                                if (definitionPath.equals(scriptPack.getValue().getClassPath().toString())) {
                                    File file = scriptPack.getValue().export(tempDirectory, ScriptExportMode.AS, false);
                                    if (file.exists()) {
                                        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
                                        if (virtualFile != null) {
                                            Editor editor = FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, virtualFile, 0), true);
                                            EditorNotificationPanel editorNotificationPanel = new EditorNotificationPanel();
                                            editorNotificationPanel.setText("Decompiled by ffdec, swf version: " + swf.version);
                                            editor.setHeaderComponent(editorNotificationPanel);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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
                    sb.insert(0, '.');
                }
                sb.insert(0, path[i].toString());
            }
        }
        return fileNameFound ? sb.toString() : "";
    }

    @Override
    public void update(AnActionEvent e) {
        VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        Boolean enabled = file != null && "swf".equals(file.getExtension());
        e.getPresentation().setEnabled(enabled);
        e.getPresentation().setVisible(enabled);
    }

    private String getTempDirectory(Project project, VirtualFile file) {

        StringBuilder sb = new StringBuilder(FileUtil.getTempDirectory());
        sb.append(File.separatorChar);
        sb.append("ffdec4idea");
        sb.append(File.separatorChar);
        sb.append(project.getName());
        sb.append(File.separatorChar);
        sb.append(file.getName());

        return sb.toString();
    }
}
