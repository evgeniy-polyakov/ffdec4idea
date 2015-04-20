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

    public SwfHandler(@NotNull VirtualFile swfFile) {
        try {
            swf = new SWF(swfFile.getInputStream(), false);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public SWF getSwf() {
        return swf;
    }

    public boolean isPackage(@NotNull String qName) {
        setScriptPacks();
        String packageName = qName + ".";
        return scriptPacks.stream()
                .map(s -> s.getClassPath().toString())
                .anyMatch(s -> s.startsWith(packageName));
    }

    public boolean isPackageOrClass(@NotNull String qName) {
        setScriptPacks();
        String packageName = qName + ".";
        return scriptPacks.stream()
                .map(s -> s.getClassPath().toString())
                .anyMatch(s -> s.equals(qName) || s.startsWith(packageName));
    }

    public String[] getPackageContents(@NotNull String qName) {
        setScriptPacks();
        String packageName = qName + ".";
        int packageNameLength = packageName.length();
        List<String> names = scriptPacks.stream()
                .map(s -> s.getClassPath().toString())
                .filter(s -> s.startsWith(packageName))
                .collect(Collectors.toList());
        names.replaceAll(s -> {
            int i = s.indexOf('.', packageNameLength);
            if (i > packageNameLength) {
                s = s.substring(packageNameLength, i);
            } else {
                s = s.substring(packageNameLength);
            }
            return s;
        });
        names = names.stream().distinct().collect(Collectors.toList());
        return ArrayUtil.toStringArray(names);
    }

    @NotNull
    public byte[] contentsToByteArray(@NotNull String qName) throws IOException {
        setScriptPacks();
        @NotNull ScriptPack scriptPack = getScriptPackByQName(qName);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             DecompiledSwfTextWriter writer = new DecompiledSwfTextWriter(Configuration.getCodeFormatting(), outputStream)) {

            // Magic code that writes the decompiled AS file to the stream.
            scriptPack.toSource(writer,
                                scriptPack.abc.script_info.get(scriptPack.scriptIndex).traits.traits,
                                ScriptExportMode.AS, false);
            writer.flush();
            return outputStream.toByteArray();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private ScriptPack getScriptPackByQName(@NotNull String qName) {
        setScriptPacks();
        return scriptPacks.stream()
                .filter(s -> s.getClassPath().toString().equals(qName))
                .findFirst().get();
    }

    private void setScriptPacks() {
        if (scriptPacks == null) {
            scriptPacks = swf.getAS3Packs().stream().map(MyEntry::getValue).collect(Collectors.toList());
        }
    }
}
