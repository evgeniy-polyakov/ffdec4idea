package com.epolyakov.ffdec4idea.vfs;

import com.intellij.openapi.vfs.VirtualFile;
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

    public SwfHandler(VirtualFile file) throws IOException {
        try {
            swf = new SWF(file.getInputStream(), false);
        } catch (InterruptedException e) {
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

    private ScriptPack getScriptPackByQName(@NotNull String qName) {
        if (scriptPacks == null) {
            scriptPacks = swf.getAS3Packs().stream().map(MyEntry::getValue).collect(Collectors.toList());
        }
        return scriptPacks.stream().filter(s -> s.getClassPath().toString().equals(qName)).findFirst().get();
    }

    @NotNull
    private String getQNameByPath(@NotNull String path) {
        return path.replace('/', '.');
    }
}
