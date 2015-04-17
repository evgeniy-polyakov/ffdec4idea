package com.epolyakov.ffdec4idea.vfs;

import com.intellij.openapi.util.io.BufferExposingByteArrayInputStream;
import com.intellij.openapi.util.io.FileAttributes;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.NewVirtualFileSystem;
import com.intellij.openapi.vfs.newvfs.VfsImplUtil;
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
    public static final String SWF_SEPARATOR = "!/";
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
        return VfsImplUtil.findFileByPath(this, path);
    }

    @Nullable
    @Override
    public VirtualFile refreshAndFindFileByPath(@NotNull String path) {
        return VfsImplUtil.refreshAndFindFileByPath(this, path);
    }

    @Nullable
    @Override
    public VirtualFile findFileByPathIfCached(@NotNull String path) {
        return VfsImplUtil.findFileByPathIfCached(this, path);
    }

    @Override
    public void refresh(boolean asynchronous) {
        VfsImplUtil.refresh(this, asynchronous);
    }

    @NotNull
    @Override
    public byte[] contentsToByteArray(@NotNull VirtualFile file) throws IOException {
        return getHandler(file).contentsToByteArray(getRelativePath(file));
    }

    @NotNull
    @Override
    public InputStream getInputStream(@NotNull VirtualFile file) throws IOException {
        return new BufferExposingByteArrayInputStream(contentsToByteArray(file));
    }

    @NotNull
    @Override
    public OutputStream getOutputStream(@NotNull VirtualFile file, Object requestor, long modStamp, long timeStamp)
            throws IOException {
        throw new IOException(MessageFormat.format(resources.getString("swf.modification.not.supported.error"), file.getUrl()));
    }

    @Override
    public long getLength(@NotNull VirtualFile file) {
        try {
            return contentsToByteArray(file).length;
        } catch (IOException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    @Nullable
    @Override
    public FileAttributes getAttributes(@NotNull VirtualFile file) {
        return new FileAttributes(isDirectory(file), false, false, false, getLength(file), 0L, false);
    }

    @Override
    public boolean exists(@NotNull VirtualFile file) {
        return getHandler(file).exists(getRelativePath(file));
    }

    @Override
    public boolean isDirectory(@NotNull VirtualFile file) {
        return getHandler(file).isDirectory(getRelativePath(file));
    }

    @NotNull
    @Override
    public String[] list(@NotNull VirtualFile file) {
        return getHandler(file).list(getRelativePath(file));
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
        // todo add cache
        return new SwfHandler(file);
    }

    @NotNull
    protected String getRelativePath(@NotNull VirtualFile file) {
        String path = file.getPath();
        String relativePath = path.substring(extractRootPath(path).length());
        return StringUtil.startsWithChar(relativePath, '/') ? relativePath.substring(1) : relativePath;
    }

    @NotNull
    @Override
    protected String extractRootPath(@NotNull String path) {
        final int swfSeparatorIndex = path.indexOf(SWF_SEPARATOR);
        assert swfSeparatorIndex >= 0 : MessageFormat.format(resources.getString("swf.incorrect.path.error"), path);
        return path.substring(0, swfSeparatorIndex + SWF_SEPARATOR.length());
    }
}
