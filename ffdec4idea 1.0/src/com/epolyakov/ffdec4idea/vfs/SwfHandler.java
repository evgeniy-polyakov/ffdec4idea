package com.epolyakov.ffdec4idea.vfs;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author epolyakov
 */
public class SwfHandler {

    private SWF swf;
    private List<ScriptPack> scriptPacks;

    public SwfHandler(VirtualFile file) {
        try {
            swf = new SWF(file.getInputStream(), false);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public byte[] contentsToByteArray(@NotNull String relativePath) throws IOException {
        String qName = getQNameByPath(relativePath);
        ScriptPack scriptPack = getScriptPackByQName(qName);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             DecompiledSwfTextWriter writer = new DecompiledSwfTextWriter(Configuration.getCodeFormatting(), outputStream)) {
            scriptPack.toSource(writer,
                                scriptPack.abc.script_info.get(scriptPack.scriptIndex).traits.traits,
                                ScriptExportMode.AS, false);
            return outputStream.toByteArray();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public boolean exists(@NotNull String relativePath) {
        String qName = getQNameByPath(relativePath);
        return isPackageOrClass(qName);
    }

    public String[] list(@NotNull String relativePath) {
        String qName = getQNameByPath(relativePath);
        return getPackageContents(qName);
    }

    public boolean isDirectory(@NotNull String relativePath) {
        String qName = getQNameByPath(relativePath);
        return isPackage(qName);
    }

    @NotNull
    private String getQNameByPath(@NotNull String path) {
        return path.replace('/', '.');
    }

    private ScriptPack getScriptPackByQName(@NotNull String qName) {
        setScriptPacks();
        return scriptPacks.stream()
                .filter(s -> s.getClassPath().toString().equals(qName))
                .findFirst().get();
    }

    private boolean isPackageOrClass(@NotNull String qName) {
        setScriptPacks();
        String packageName = qName + ".";
        return scriptPacks.stream()
                .map(s -> s.getClassPath().toString())
                .anyMatch(s -> s.equals(qName) || s.startsWith(packageName));
    }

    private boolean isPackage(@NotNull String qName) {
        setScriptPacks();
        String packageName = qName + ".";
        return scriptPacks.stream()
                .map(s -> s.getClassPath().toString())
                .anyMatch(s -> s.startsWith(packageName));
    }

    private String[] getPackageContents(@NotNull String qName) {
        setScriptPacks();
        String packageName = qName + ".";
        int packageNameLength = packageName.length();
        List<String> containedClasses = scriptPacks.stream()
                .map(s -> s.getClassPath().toString())
                .filter(s -> s.startsWith(packageName))
                .collect(Collectors.toList());
        containedClasses.replaceAll(s -> {
            int i = s.indexOf('.', packageNameLength);
            if (i > packageNameLength) {
                s = s.substring(packageNameLength, i);
            } else {
                s = s.substring(packageNameLength);
            }
            return s;
        });
        return ArrayUtil.toStringArray(containedClasses);
    }

    private void setScriptPacks() {
        if (scriptPacks == null) {
            scriptPacks = swf.getAS3Packs().stream().map(MyEntry::getValue).collect(Collectors.toList());
        }
    }
}
