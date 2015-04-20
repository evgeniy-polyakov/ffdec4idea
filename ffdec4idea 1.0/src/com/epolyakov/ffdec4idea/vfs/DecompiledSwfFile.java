package com.epolyakov.ffdec4idea.vfs;

import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.io.BufferExposingByteArrayInputStream;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author epolyakov
 */
public class DecompiledSwfFile extends VirtualFile {

    private static final ResourceBundle resources = ResourceBundle.getBundle("com.epolyakov.ffdec4idea.resources.ffdec4idea");

    private SwfHandler handler;
    private VirtualFile parent;
    private String name;

    public DecompiledSwfFile(@NotNull SwfHandler handler, @NotNull String name, @NotNull VirtualFile parent) {
        this.handler = handler;
        this.name = name;
        this.parent = parent;
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return ActionScriptFileType.INSTANCE;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public VirtualFileSystem getFileSystem() {
        return DecompiledSwfFileSystem.getInstance();
    }

    @NotNull
    @Override
    public String getPath() {
        if (parent instanceof DecompiledSwfFile) {
            String path = parent.getPath();
            return path.endsWith("/") ? path + name : path + '/' + name;
        }
        return parent.getPath() + DecompiledSwfFileSystem.PATH_SEPARATOR + name;
    }

    @NotNull
    public String getQualifiedName() {
        StringBuilder qName = new StringBuilder(name);
        VirtualFile p = parent;
        while (p instanceof DecompiledSwfFile) {
            qName.insert(0, '.');
            qName.insert(0, p.getName());
            p = p.getParent();
        }
        return qName.toString();
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return handler.isPackage(getQualifiedName());
    }

    @Override
    public boolean isValid() {
        return handler.isPackageOrClass(getQualifiedName());
    }

    @Override
    public VirtualFile getParent() {
        return parent;
    }

    @Override
    public VirtualFile[] getChildren() {
        String[] names = getChildrenNames();
        VirtualFile[] files = new VirtualFile[names.length];
        for (int i = 0; i < names.length; i++) {
            files[i] = DecompiledSwfFileSystem.getInstance().getDecompiledFile(handler, names[i], this);
        }
        return files;
    }

    @NotNull
    public String[] getChildrenNames() {
        return handler.getPackageContents(getQualifiedName());
    }

    @NotNull
    @Override
    public byte[] contentsToByteArray() throws IOException {
        return handler.contentsToByteArray(getQualifiedName());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new BufferExposingByteArrayInputStream(contentsToByteArray());
    }

    @NotNull
    @Override
    public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp)
            throws IOException {
        throw new IOException(MessageFormat.format(resources.getString("swf.modification.not.supported.error"), getUrl()));
    }

    @Override
    public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {
    }

    @Override
    public long getTimeStamp() {
        return 0L;
    }

    @Override
    public long getLength() {
        try {
            return contentsToByteArray().length;
        } catch (IOException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    @Override
    public long getModificationStamp() {
        return 0L;
    }
}
