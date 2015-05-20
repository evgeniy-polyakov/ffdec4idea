package com.epolyakov.ffdec4idea.vfs;

import com.epolyakov.ffdec4idea.lang.DecompiledActionScriptParserDefinition;
import com.intellij.icons.AllIcons.FileTypes;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author epolyakov
 */
public class DecompiledActionScriptFileType extends LanguageFileType {

    public static final LanguageFileType INSTANCE = new DecompiledActionScriptFileType();

    public DecompiledActionScriptFileType() {
        super(DecompiledActionScriptParserDefinition.LANGUAGE);
    }

    @NotNull
    public String getName() {
        return "Decompiled ActionScript";
    }

    @NotNull
    public String getDescription() {
        return "";
    }

    @NotNull
    public String getDefaultExtension() {
        return "";
    }

    public Icon getIcon() {
        return FileTypes.AS;
    }
}
