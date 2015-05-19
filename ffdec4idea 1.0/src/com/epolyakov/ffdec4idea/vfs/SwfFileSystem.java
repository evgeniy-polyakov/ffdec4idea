package com.epolyakov.ffdec4idea.vfs;

import com.intellij.openapi.util.Couple;
import com.intellij.openapi.util.io.FileAttributes;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.vfs.newvfs.NewVirtualFileSystem;
import com.jpexs.decompiler.flash.SWF;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * @author epolyakov
 */
public class SwfFileSystem extends NewVirtualFileSystem {

    public static final String PROTOCOL = "swf";
    public static final String PATH_SEPARATOR = "!/";
    private static final ResourceBundle resources = ResourceBundle.getBundle("com.epolyakov.ffdec4idea.resources.ffdec4idea");

    private final Map<String, SwfHandler> handlers = new HashMap<>();
    private final Map<String, DecompiledActionScriptFile> files = new HashMap<>();

    private boolean isFileListenerSet;

    public static SwfFileSystem getInstance() {
        return (SwfFileSystem) VirtualFileManager.getInstance().getFileSystem("swf");
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
        synchronized (files) {
            if (files.containsKey(path)) {
                return files.get(path);
            }
            Couple<String> couple = splitPath(path);
            VirtualFile swfFile = LocalFileSystem.getInstance().findFileByPath(couple.first);
            return getDecompiledFile(swfFile, couple.second);
        }
    }

    @Nullable
    @Override
    public VirtualFile refreshAndFindFileByPath(@NotNull String path) {
        synchronized (files) {
            files.clear();
            Couple<String> couple = splitPath(path);
            VirtualFile swfFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(couple.first);
            return getDecompiledFile(swfFile, couple.second);
        }
    }

    @Nullable
    @Override
    public VirtualFile findFileByPathIfCached(@NotNull String path) {
        synchronized (files) {
            if (files.containsKey(path)) {
                return files.get(path);
            }
            return null;
        }
    }

    @Override
    public void refresh(boolean asynchronous) {
        synchronized (handlers) {
            handlers.clear();
        }
        synchronized (files) {
            files.clear();
        }
    }

    public void refresh(@NotNull VirtualFile file) {
        synchronized (handlers) {
            String path = file.getPath();
            if (handlers.containsKey(path)) {
                handlers.remove(path);

                synchronized (files) {
                    files.keySet().stream()
                            .filter(key -> key.startsWith(path))
                            .collect(Collectors.toList())
                            .forEach(files::remove);
                }
            }
        }
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
        if (file instanceof DecompiledActionScriptFile) {
            return ((DecompiledActionScriptFile) file).getChildrenNames();
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
    public SWF getSwf(@NotNull VirtualFile file) {
        return getHandler(file).getSwf();
    }

    @NotNull
    private SwfHandler getHandler(@NotNull VirtualFile file) {
        setFileListener();
        synchronized (handlers) {
            String path = file.getPath();
            if (!handlers.containsKey(path)) {
                handlers.put(path, new SwfHandler(file));
            }
            return handlers.get(path);
        }
    }

    @NotNull
    @Override
    protected String extractRootPath(@NotNull String path) {
        final int index = path.indexOf(PATH_SEPARATOR);
        assert index >= 0 : MessageFormat.format(resources.getString("swf.incorrect.path.error"), path);
        return path.substring(0, index + PATH_SEPARATOR.length());
    }

    @NotNull
    private Couple<String> splitPath(@NotNull String path) {
        final int index = path.indexOf(PATH_SEPARATOR);
        assert index >= 0 : MessageFormat.format(resources.getString("swf.incorrect.path.error"), path);
        return Couple.of(path.substring(0, index), path.substring(index + PATH_SEPARATOR.length()));
    }

    @Nullable
    private DecompiledActionScriptFile getDecompiledFile(VirtualFile swfFile, @NotNull String relativePath) {
        if (swfFile == null) {
            return null;
        }
        SwfHandler handler = getHandler(swfFile);
        String[] names = relativePath.split("/");
        DecompiledActionScriptFile file = getDecompiledFile(handler, swfFile);
        for (String name : names) {
            file = getDecompiledFile(handler, name, file);
        }
        return file;
    }

    @NotNull
    private DecompiledActionScriptFile getDecompiledFile(@NotNull SwfHandler handler, @NotNull VirtualFile parent) {
        synchronized (files) {
            String path = parent.getPath();
            if (files.containsKey(path)) {
                return files.get(path);
            }
            DecompiledActionScriptFile file = new DecompiledActionScriptFile(handler, parent);
            files.put(path, file);
            return file;
        }
    }

    @NotNull
    protected DecompiledActionScriptFile getDecompiledFile(@NotNull SwfHandler handler, @NotNull String name,
                                                           @NotNull DecompiledActionScriptFile parent) {
        String path = parent.getPath() + '/' + name;
        synchronized (files) {
            if (files.containsKey(path)) {
                return files.get(path);
            }
            DecompiledActionScriptFile file = new DecompiledActionScriptFile(handler, name, parent);
            files.put(path, file);
            return file;
        }
    }

    private void setFileListener() {
        if (!isFileListenerSet) {
            VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
                @Override
                public void propertyChanged(@NotNull VirtualFilePropertyEvent virtualFilePropertyEvent) {
                }

                @Override
                public void contentsChanged(@NotNull VirtualFileEvent virtualFileEvent) {
                    refresh(virtualFileEvent.getFile());
                }

                @Override
                public void fileCreated(@NotNull VirtualFileEvent virtualFileEvent) {
                }

                @Override
                public void fileDeleted(@NotNull VirtualFileEvent virtualFileEvent) {
                    refresh(virtualFileEvent.getFile());
                }

                @Override
                public void fileMoved(@NotNull VirtualFileMoveEvent virtualFileMoveEvent) {
                    refresh(virtualFileMoveEvent.getFile());
                }

                @Override
                public void fileCopied(@NotNull VirtualFileCopyEvent virtualFileCopyEvent) {
                }

                @Override
                public void beforePropertyChange(@NotNull VirtualFilePropertyEvent virtualFilePropertyEvent) {
                }

                @Override
                public void beforeContentsChange(@NotNull VirtualFileEvent virtualFileEvent) {
                }

                @Override
                public void beforeFileDeletion(@NotNull VirtualFileEvent virtualFileEvent) {
                }

                @Override
                public void beforeFileMovement(@NotNull VirtualFileMoveEvent virtualFileMoveEvent) {
                }
            });
            isFileListenerSet = true;
        }
    }
}
