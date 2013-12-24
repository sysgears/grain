package com.sysgears.grain.init

import groovy.util.logging.Slf4j
import org.apache.commons.io.IOUtils

import javax.inject.Named
import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * Bundled external tools extractor. Tools extraction should be made at very early stages of
 * Grain initialization.
 */
@Slf4j
@Named
@javax.inject.Singleton
class ToolsExtractor {

    /**
     * Extracts bundled tool sources to .grain/tools/git-rev directory
     * for reusing them with external processes.
     * 
     * @param settings Grain settings
     */
    public void extractTools(GrainSettings settings) {
        if (settings.toolsHome.exists()) {
            if (settings.env != 'cmd')
                log.info "Using tools home: ${settings.toolsHome}"
            return
        }

        if (settings.env != 'cmd')
            log.info "Unpacking bundled tools to ${settings.toolsHome}"

        def className = CmdlineParser.getSimpleName() + ".class"
        String classPath = CmdlineParser.getResource(className).toString()

        if (!classPath.startsWith("jar")) {
            throw new RuntimeException("Tools home directory does not exist " +
                    "and not running Grain from a JAR, please specify tools.home")
        }

        println classPath

        String jarPath = classPath.substring(9, classPath.indexOf("!"))
        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))
        if (!settings.toolsHome.mkdirs()) {
            throw new RuntimeException("Unable to create dir: ${settings.toolsHome}")
        }
        def entries = jar.entries()
        while (entries.hasMoreElements()) {
            def file = (JarEntry) entries.nextElement()
            if (file.name.startsWith('tools/') && !file.isDirectory()) {
                def targetFile = new File(settings.toolsHome, file.name.replace('tools/', ''))
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
