package com.epolyakov.ffdec4idea.vfs;

import com.intellij.openapi.util.io.FileAttributes;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
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

    public static final String SWF_SEPARATOR = "!/";
    private static ResourceBundle resources = ResourceBundle.getBundle("com.epolyakov.ffdec4idea.resources.ffdec4idea");

    @Nullable
    @Override
    public VirtualFile findFileByPathIfCached(@NotNull String path) {
        return VfsImplUtil.findFileByPathIfCached(this, path);
    }

    @NotNull
    @Override
    protected String extractRootPath(@NotNull String path) {
        final int jarSeparatorIndex = path.indexOf(SWF_SEPARATOR);
        assert jarSeparatorIndex >= 0 : "Path passed to DecompiledSwfFileSystem must have separator '!/': " + path;
        return path.substring(0, jarSeparatorIndex + SWF_SEPARATOR.length());
    }

    @Override
    public int getRank() {
        return LocalFileSystem.getInstance().getRank() + 1;
    }

    @NotNull
    @Override
    public VirtualFile copyFile(Object requestor, @NotNull VirtualFile file, @NotNull VirtualFile newParent,
                                @NotNull String copyName) throws IOException {
        throw new IOException(MessageFormat.format(resources.getString("swf.modification.not.supported.error"), file.getUrl()));
    }

    @NotNull
    @Override
    public byte[] contentsToByteArray(@NotNull VirtualFile file) throws IOException {
        return getHandler(file).contentsToByteArray(getRelativePath(file));
    }

    @NotNull
    @Override
    public InputStream getInputStream(@NotNull VirtualFile _virtualFile) throws IOException {
        return null;
    }

    @NotNull
    @Override
    public OutputStream getOutputStream(@NotNull VirtualFile _virtualFile, Object o, long l, long l1)
            throws IOException {
        return null;
    }

    @Override
    public long getLength(@NotNull VirtualFile _virtualFile) {
        return 0;
    }

    @Override
    public boolean exists(@NotNull VirtualFile _virtualFile) {
        return false;
    }

    @NotNull
    @Override
    public String[] list(@NotNull VirtualFile _virtualFile) {
        return new String[0];
    }

    @Override
    public boolean isDirectory(@NotNull VirtualFile _virtualFile) {
        return false;
    }

    @Override
    public long getTimeStamp(@NotNull VirtualFile _virtualFile) {
        return 0;
    }

    @Override
    public void setTimeStamp(@NotNull VirtualFile _virtualFile, long l) throws IOException {

    }

    @Override
    public boolean isWritable(@NotNull VirtualFile _virtualFile) {
        return false;
    }

    @Override
    public void setWritable(@NotNull VirtualFile _virtualFile, boolean b) throws IOException {

    }

    @NotNull
    @Override
    public VirtualFile createChildDirectory(Object o, @NotNull VirtualFile _virtualFile, @NotNull String s)
            throws IOException {
        return null;
    }

    @NotNull
    @Override
    public VirtualFile createChildFile(Object o, @NotNull VirtualFile _virtualFile, @NotNull String s)
            throws IOException {
        return null;
    }

    @NotNull
    @Override
    public String getProtocol() {
        return null;
    }

    @Nullable
    @Override
    public VirtualFile findFileByPath(@NotNull String s) {
        return null;
    }

    @Override
    public void refresh(boolean b) {

    }

    @Nullable
    @Override
    public VirtualFile refreshAndFindFileByPath(@NotNull String s) {
        return null;
    }

    @Override
    public void deleteFile(Object o, @NotNull VirtualFile _virtualFile) throws IOException {

    }

    @Override
    public void moveFile(Object o, @NotNull VirtualFile _virtualFile, @NotNull VirtualFile _virtualFile1)
            throws IOException {

    }

    @Override
    public void renameFile(Object o, @NotNull VirtualFile _virtualFile, @NotNull String s) throws IOException {

    }

    @Nullable
    @Override
    public FileAttributes getAttributes(@NotNull VirtualFile _virtualFile) {
        return null;
    }

    @NotNull
    private SwfHandler getHandler(@NotNull VirtualFile file) throws IOException {
        return new SwfHandler(file);
    }

    @NotNull
    protected String getRelativePath(@NotNull VirtualFile file) {
        String path = file.getPath();
        String relativePath = path.substring(extractRootPath(path).length());
        return StringUtil.startsWithChar(relativePath, '/') ? relativePath.substring(1) : relativePath;
    }
}
