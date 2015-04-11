package com.epolyakov.ffdec4idea.actions;

import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.FileTextWriter;
import com.jpexs.helpers.Helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author epolyakov
 */
public class ScriptExporter {

    /**
     * Exports the given text as the given file.
     *
     * @param text      The text.
     * @param directory The directory.
     * @param fileName  The file name.
     * @return The exported file.
     * @throws IOException
     * @throws InterruptedException
     */
    public static File export(String text, String directory, String fileName) throws IOException, InterruptedException {
        File dir = makeDir(directory);
        File file = new File(dir, fileName + ".as");

        if (file.exists()) {
            file.delete();
        }
        try (FileTextWriter writer = new FileTextWriter(Configuration.getCodeFormatting(), new FileOutputStream(file))) {
            writer.append(text);
        }
        file.setReadOnly();
        return file;
    }

    /**
     * Exports the given script path to a file.
     *
     * @param scriptPack The script pack.
     * @param directory  The root directory.
     * @return The exported file.
     * @throws IOException
     * @throws InterruptedException
     */
    public static File export(ScriptPack scriptPack, String directory) throws IOException, InterruptedException {

        String scriptName = scriptPack.getPathScriptName();
        String packageName = scriptPack.getPathPackage();
        File dir = makeDir(directory + File.separatorChar + makeDirPath(packageName));
        File file = new File(dir, Helper.makeFileName(scriptName) + ".as");

        if (file.exists()) {
            file.delete();
        }
        try (FileTextWriter writer = new FileTextWriter(Configuration.getCodeFormatting(), new FileOutputStream(file))) {
            scriptPack.toSource(writer,
                                scriptPack.abc.script_info.get(scriptPack.scriptIndex).traits.traits,
                                ScriptExportMode.AS, false);
        }
        file.setReadOnly();
        return file;
    }

    private static File makeDir(String path) throws IOException {
        File dir = new File(path);
        if (!dir.exists() && !dir.mkdirs() && !dir.exists()) {
            throw new IOException("Cannot create directory " + dir);
        }
        return dir;
    }

    private static String makeDirPath(String packageName) {
        if (packageName.isEmpty()) {
            return "";
        } else {
            String[] pathParts;
            if (packageName.contains(".")) {
                pathParts = packageName.split("\\.");
            } else {
                pathParts = new String[]{packageName};
            }

            for (int i = 0; i < pathParts.length; ++i) {
                pathParts[i] = Helper.makeFileName(pathParts[i]);
            }

            return Helper.joinStrings(pathParts, File.separator);
        }
    }
}
