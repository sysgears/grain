package com.sysgears.grain.init

import groovy.util.logging.Slf4j
import org.apache.commons.io.IOUtils

import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * Bundled external tools extractor. Tools extraction should be made at very early stages of
 * Grain initialization.
 */
@Slf4j
class ToolsExtractor {

    /**
     * Extracts bundled tool sources to .grain/tools/git-rev directory
     * for reusing them with external processes.
     */
    public void extractTools(CmdlineOptions options) {
        if (options.toolsHome.exists()) {
            log.info "Using tools home: ${options.toolsHome}"
            return
        }
        
        log.info("Unpacking bundled tools to ${options.toolsHome}")

        def className = CmdlineParser.getSimpleName() + ".class"
        String classPath = CmdlineParser.getResource(className).toString()

        if (!classPath.startsWith("jar")) {
            throw new RuntimeException("Tools home directory does not exist " +
                    "and not running Grain from a JAR, please specify tools.home")
        }

        println classPath

        String jarPath = classPath.substring(9, classPath.indexOf("!"))
        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))
        if (!options.toolsHome.mkdirs()) {
            throw new RuntimeException("Unable to create dir: ${options.toolsHome}")
        }
        def entries = jar.entries()
        while (entries.hasMoreElements()) {
            def file = (JarEntry) entries.nextElement()
            if (file.name.startsWith('tools/') && !file.isDirectory()) {
                def targetFile = new File(options.toolsHome, file.name.replace('tools/', ''))
                def targetDir = targetFile.parentFile
                if (!targetDir.exists() && !targetDir.mkdirs()) {
                    throw new RuntimeException("Unable to create dir: ${targetDir}")
                }

                def is = jar.getInputStream(file)
                def out = new FileOutputStream(targetFile)
                try {
                    IOUtils.copy(is, out)
                } finally {
                    out.close()
                    is.close()
                }
            }
        }
    }
}
