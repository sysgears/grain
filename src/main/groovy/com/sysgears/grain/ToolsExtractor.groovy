package com.sysgears.grain

import groovy.util.logging.Slf4j
import org.apache.commons.io.IOUtils

import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * Bundled vendor tools extractor. Tools extraction should be made at very early stages of
 * Grain initialization.
 */
@Slf4j
class ToolsExtractor {

    /**
     * Extracts bundled tool sources to .grain/vendor/git-rev directory
     * for reusing them with external processes.
     */
    public void extractTools(CmdlineOptions options) {
        if (options.vendorHome.exists()) {
            log.info "Using vendor home: ${options.vendorHome}"
            return
        }
        
        log.info("Unpacking bundled tools to ${options.vendorHome}")

        def className = CmdlineParser.getSimpleName() + ".class"
        String classPath = CmdlineParser.getResource(className).toString()

        if (!classPath.startsWith("jar")) {
            throw new RuntimeException("Vendor tools directory does not exist " +
                    "and not running Grain from a JAR, please specify vendor.home")
        }

        println classPath

        String jarPath = classPath.substring(9, classPath.indexOf("!"))
        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))
        if (!options.vendorHome.mkdirs()) {
            throw new RuntimeException("Unable to create dir: ${options.vendorHome}")
        }
        def entries = jar.entries()
        while (entries.hasMoreElements()) {
            def file = (JarEntry) entries.nextElement()
            if (file.name.startsWith('vendor/') && !file.isDirectory()) {
                def targetFile = new File(options.vendorHome, file.name.replace('vendor/', ''))
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
