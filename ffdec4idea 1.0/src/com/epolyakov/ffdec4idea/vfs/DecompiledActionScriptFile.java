package com.epolyakov.ffdec4idea.vfs;

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
public class DecompiledActionScriptFile extends VirtualFile {

    private static final ResourceBundle resources = ResourceBundle.getBundle("com.epolyakov.ffdec4idea.resources.ffdec4idea");

    private SwfHandler handler;
    private VirtualFile parent;
    private String name;
    private boolean isRoot;

    /**
     * Constructs the root file with the name of the swf file.
     *
     * @param handler
     * @param swfFile
     */
    public DecompiledActionScriptFile(@NotNull SwfHandler handler, @NotNull VirtualFile swfFile) {
        this.handler = handler;
        this.parent = swfFile;
        this.name = swfFile.getName();
        this.isRoot = true;
    }

    /**
     * Constructs a package or a class file within the swf.
     *
     * @param handler
     * @param name
     * @param parent
     */
    public DecompiledActionScriptFile(@NotNull SwfHandler handler, @NotNull String name,
                                      @NotNull DecompiledActionScriptFile parent) {
        this.handler = handler;
        this.name = name;
        this.parent = parent;
        this.isRoot = false;
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return DecompiledActionScriptFileType.INSTANCE;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public VirtualFileSystem getFileSystem() {
        return SwfFileSystem.getInstance();
    }

    @NotNull
    @Override
    public String getPath() {
        if (isRoot) {
            return parent.getPath();
        }
        String path = parent.getPath();
        if (((DecompiledActionScriptFile) parent).isRoot()) {
            return path + SwfFileSystem.PATH_SEPARATOR + name;
        }
        return path + '/' + name;
    }

    @NotNull
    public String getQualifiedName() {
        if (isRoot) {
            return "";
        }
        StringBuilder qName = new StringBuilder(name);
        VirtualFile p = parent;
        while (!((DecompiledActionScriptFile) p).isRoot()) {
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

    public boolean isRoot() {
        return isRoot;
    }

    @Override
    public boolean isDirectory() {
        return isRoot || handler.isPackage(getQualifiedName());
    }

    @Override
    public boolean isValid() {
        return isRoot ? parent.isValid() : handler.isPackageOrClass(getQualifiedName());
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
            files[i] = SwfFileSystem.getInstance().getDecompiledFile(handler, names[i], this);
        }
        return files;
    }

    @NotNull
    public String[] getChildrenNames() {
        return isRoot ? handler.getRootContents() : handler.getPackageContents(getQualifiedName());
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
