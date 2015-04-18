package com.epolyakov.ffdec4idea.vfs;

import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.io.FileAttributes;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.NewVirtualFileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author epolyakov
 */
public class DecompiledSwfFileSystem extends NewVirtualFileSystem {

    public static final String PROTOCOL = "swf";
    public static final String PATH_SEPARATOR = "!/";
    private static ResourceBundle resources = ResourceBundle.getBundle("com.epolyakov.ffdec4idea.resources.ffdec4idea");

    public static DecompiledSwfFileSystem getInstance() {
        return (DecompiledSwfFileSystem) VirtualFileManager.getInstance().getFileSystem("swf");
    }

    @NotNull
    @Override
    public String getProtocol() {
        return PROTOCOL;
    }

    @Override
    public int getRank() {
        return LocalFileSystem.getInstance().getRank() + 1;
    }

    @Nullable
    @Override
    public VirtualFile findFileByPath(@NotNull String path) {
        Couple<String> couple = splitPath(path);
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(couple.first);
        return getDecompiledFile(file, couple.second);
    }

    @Nullable
    @Override
    public VirtualFile refreshAndFindFileByPath(@NotNull String path) {
        Couple<String> couple = splitPath(path);
        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(couple.first);
        return getDecompiledFile(file, couple.second);
    }

    @Nullable
    @Override
    public VirtualFile findFileByPathIfCached(@NotNull String path) {
        Couple<String> couple = splitPath(path);
        VirtualFile file = LocalFileSystem.getInstance().findFileByPathIfCached(couple.first);
        return getDecompiledFile(file, couple.second);
    }

    @Override
    public void refresh(boolean asynchronous) {
    }

    @NotNull
    @Override
    public byte[] contentsToByteArray(@NotNull VirtualFile file) throws IOException {
        return file.contentsToByteArray();
    }

    @NotNull
    @Override
    public InputStream getInputStream(@NotNull VirtualFile file) throws IOException {
        return file.getInputStream();
    }

    @NotNull
    @Override
    public OutputStream getOutputStream(@NotNull VirtualFile file, Object requestor, long modStamp, long timeStamp)
            throws IOException {
        throw new IOException(MessageFormat.format(resources.getString("swf.modification.not.supported.error"), file.getUrl()));
    }

    @Override
    public long getLength(@NotNull VirtualFile file) {
        return file.getLength();
    }

    @Nullable
    @Override
    public FileAttributes getAttributes(@NotNull VirtualFile file) {
        return new FileAttributes(file.isDirectory(), false, false, false, file.getLength(), 0L, false);
    }

    @Override
    public boolean exists(@NotNull VirtualFile file) {
        return file.exists();
    }

    @Override
    public boolean isDirectory(@NotNull VirtualFile file) {
        return file.isDirectory();
    }

    @NotNull
    @Override
    public String[] list(@NotNull VirtualFile file) {
        if (file instanceof DecompiledSwfFile) {
            return ((DecompiledSwfFile) file).getChildrenNames();
        }
        return new String[0];
    }

    @Override
    public long getTimeStamp(@NotNull VirtualFile file) {
        return -1L;
    }

    @Override
    public void setTimeStamp(@NotNull VirtualFile file, long timeStamp) throws IOException {
        throw new IOException(MessageFormat.format(resources.getString("swf.modification.not.supported.error"), file.getUrl()));
    }

    @Override
    public boolean isWritable(@NotNull VirtualFile file) {
        return false;
    }

    @Override
    public void setWritable(@NotNull VirtualFile file, boolean writableFlag) throws IOException {
        throw new IOException(MessageFormat.format(resources.getString("swf.modification.not.supported.error"), file.getUrl()));
    }

    @Override
    public void deleteFile(Object requestor, @NotNull VirtualFile file) throws IOException {
        throw new IOException(MessageFormat.format(resources.getString("swf.modification.not.supported.error"), file.getUrl()));
    }

    @NotNull
    @Override
    public VirtualFile copyFile(Object requestor, @NotNull VirtualFile file, @NotNull VirtualFile newParent,
                                @NotNull String copyName) throws IOException {
        throw new IOException(MessageFormat.format(resources.getString("swf.modification.not.supported.error"), file.getUrl()));
    }

    @Override
    public void moveFile(Object o, @NotNull VirtualFile file, @NotNull VirtualFile newParent)
            throws IOException {
        throw new IOException(MessageFormat.format(resources.getString("swf.modification.not.supported.error"), file.getUrl()));
    }

    @Override
    public void renameFile(Object o, @NotNull VirtualFile file, @NotNull String newName) throws IOException {
        throw new IOException(MessageFormat.format(resources.getString("swf.modification.not.supported.error"), file.getUrl()));
    }

    @NotNull
    @Override
    public VirtualFile createChildDirectory(Object requestor, @NotNull VirtualFile parent, @NotNull String dir)
            throws IOException {
        throw new IOException(MessageFormat.format(resources.getString("swf.modification.not.supported.error"), parent.getUrl()));
    }

    @NotNull
    @Override
    public VirtualFile createChildFile(Object requestor, @NotNull VirtualFile parent, @NotNull String dir)
            throws IOException {
        throw new IOException(MessageFormat.format(resources.getString("swf.modification.not.supported.error"), parent.getUrl()));
    }

    @NotNull
    private SwfHandler getHandler(@NotNull VirtualFile file) {
        // todo cache
        return new SwfHandler(file);
    }

    @NotNull
    @Override
    protected String extractRootPath(@NotNull String path) {
        final int index = path.indexOf(PATH_SEPARATOR);
        assert index >= 0 : MessageFormat.format(resources.getString("swf.incorrect.path.error"), path);
        return path.substring(0, index + PATH_SEPARATOR.length());
    }

    @NotNull
    protected Couple<String> splitPath(@NotNull String path) {
        final int index = path.indexOf(PATH_SEPARATOR);
        assert index >= 0 : MessageFormat.format(resources.getString("swf.incorrect.path.error"), path);
        return Couple.of(path.substring(0, index), path.substring(index + PATH_SEPARATOR.length()));
    }

    protected VirtualFile getDecompiledFile(VirtualFile swfFile, @NotNull String relativePath) {
        if (swfFile == null) {
            return null;
        }
        SwfHandler handler = getHandler(swfFile);
        String[] names = relativePath.split("/");
        VirtualFile file = swfFile;
        for (String name : names) {
            file = new DecompiledSwfFile(handler, name, file);
        }
        return file;
    }
}
