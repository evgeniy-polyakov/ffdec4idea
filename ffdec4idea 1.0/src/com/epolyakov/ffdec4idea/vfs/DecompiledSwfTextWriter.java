package com.epolyakov.ffdec4idea.vfs;

import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightSpecialType;
import com.jpexs.helpers.utf8.Utf8OutputStreamWriter;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The copy of com.jpexs.decompiler.flash.helpers.FileTextWriter that supports any output streams.
 *
 * @author epolyakov
 */
public class DecompiledSwfTextWriter extends GraphTextWriter implements AutoCloseable {

    private final Writer writer;

    private boolean newLine = true;

    private int indent;

    private int writtenBytes;

    public DecompiledSwfTextWriter(CodeFormatting formatting, OutputStream outputStream) {
        super(formatting);
        this.writer = new BufferedWriter(new Utf8OutputStreamWriter(outputStream));
    }

    @Override
    public GraphTextWriter hilightSpecial(String text, HighlightSpecialType type, String specialValue,
                                          HighlightData data) {
        writeToFile(text);
        return this;
    }

    @Override
    public GraphTextWriter append(String str) {
        writeToFile(str);
        return this;
    }

    @Override
    public GraphTextWriter appendWithData(String str, HighlightData data) {
        writeToFile(str);
        return this;
    }

    @Override
    public GraphTextWriter append(String str, long offset) {
        writeToFile(str);
        return this;
    }

    @Override
    public GraphTextWriter appendNoHilight(int i) {
        writeToFile(Integer.toString(i));
        return this;
    }

    @Override
    public GraphTextWriter appendNoHilight(String str) {
        writeToFile(str);
        return this;
    }

    @Override
    public GraphTextWriter indent() {
        indent++;
        return this;
    }

    @Override
    public GraphTextWriter unindent() {
        indent--;
        return this;
    }

    @Override
    public GraphTextWriter newLine() {
        writeToFile(formatting.newLineChars);
        newLine = true;
        return this;
    }

    @Override
    public int getLength() {
        return writtenBytes;
    }

    @Override
    public int getIndent() {
        return indent;
    }

    private void writeToFile(String str) {
        if (newLine) {
            newLine = false;
            appendIndent();
        }
        try {
            writer.write(str);
            writtenBytes += str.length();
        } catch (IOException ex) {
            Logger.getLogger(FileTextWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void appendIndent() {
        for (int i = 0; i < indent; i++) {
            writeToFile(formatting.indentString);
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    public void flush() throws IOException {
        writer.flush();
    }
}
