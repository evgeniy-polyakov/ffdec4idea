package com.epolyakov.ffdec4idea.lang;

import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.dialects.ECMAL4ParserDefinition;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.psi.tree.IFileElementType;

/**
 * @author epolyakov
 */
public class DecompiledActionScriptParserDefinition extends ECMAL4ParserDefinition {

    public static final Language LANGUAGE = new Language(JavaScriptSupportLoader.ECMA_SCRIPT_L4, "Decompiled ActionScript", new String[0]) {
    };

    private static final IFileElementType FILE_TYPE = JSFileElementType.create(LANGUAGE);

    public IFileElementType getFileNodeType() {
        return FILE_TYPE;
    }
}
