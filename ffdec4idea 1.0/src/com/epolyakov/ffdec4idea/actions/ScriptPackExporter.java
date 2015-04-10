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
public class ScriptPackExporter {

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
        File outDir = new File(directory + File.separatorChar + makeDirPath(packageName));

        if (!outDir.exists() && !outDir.mkdirs() && !outDir.exists()) {
            throw new IOException("Cannot create directory " + outDir);
        } else {
            String fileName = outDir.toString() + File.separator + Helper.makeFileName(scriptName) + ".as";
            File file = new File(fileName);
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
