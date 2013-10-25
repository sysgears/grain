/*
 * Copyright (c) 2013 SysGears, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sysgears.grain

import com.sysgears.grain.config.Config
import com.sysgears.grain.deploy.SiteDeployer
import com.sysgears.grain.expando.GrainDynamicMethods
import com.sysgears.grain.generate.SiteGenerator
import com.sysgears.grain.preview.SitePreviewer
import com.sysgears.grain.preview.ConfigChangeBroadcaster
import com.sysgears.grain.util.FileUtils
import groovy.util.logging.Slf4j
import org.apache.commons.io.IOUtils

import javax.inject.Inject
import javax.inject.Named
import java.util.jar.JarEntry
import java.util.jar.JarFile

/**
 * Top level logic of the Grain application.
 */
@Named
@javax.inject.Singleton
@Slf4j
class Application {
    
    /** Site config */
    @Inject private Config config

    /** Site config change broadcaster */
    @Inject private ConfigChangeBroadcaster configChangeBroadcaster

    /** Command line options */
    @Inject private CmdlineOptions options

    /** Grain web server for running in preview mode */
    @Inject private SitePreviewer previewer

    /** Grain site generator */
    @Inject private SiteGenerator generator

    /** Grain site deployer */
    @Inject private SiteDeployer deployer

    /** Grain dynamic methods registrar */
    @Inject private GrainDynamicMethods grainDynamicMethods

    /**
     * Extract bundled tool sources to .grain/vendor/git-rev directory for reusing them with external processes.
     */
    private void extractTools() {
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

    /**
     * Prepares Grain engine to launch a command: binds additional methods
     * to standard Groovy classes, creates site cache directories.
     */
    private void prepare() {        
        if (!options.vendorHome.exists()) {
            log.info("Unpacking bundled tools to ${options.vendorHome}")
            extractTools()
        } else {
            log.info "Using vendor home: ${options.vendorHome}"
        }

        // Register Grain extension methods to Groovy classes
        grainDynamicMethods.register()
        
        // Trigger config changed event to read Site config
        // and select implementations for Site features specified in config
        try {
            configChangeBroadcaster.configChanged()
        } catch (t) {
            t.printStackTrace()
        }
        
        

        // Create cache directories
        def cacheDir = new File(config.cache_dir.toString())
        def dirs = (File[]) ([] + cacheDir +
                config.source_dir.collect { new File(it as String) }.findAll { !it.exists() })
        FileUtils.createDirs(dirs)
    }

    /**
     * Launches the command specified in command line arguments. 
     * <p> 
     * This method contains top level logic of Grain application.
     */
    public void run() {
        long startTime = System.currentTimeMillis()
        prepare()
        switch (options.command) {
            case 'preview':
                previewer.start()
                break
            case 'generate':
                generator.generate()
                break
            case 'gendeploy':
                generator.generate()
                deployer.deploy()
                break
            case 'deploy':
                deployer.deploy()
                break
            default:
                def command = config.commands[options.command] as Closure
                if (!command) {
                    throw new RuntimeException("Unknown command: ${options.command}")
                } else {
                    command(options.args)
                }
                break
        }
        log.info "Total time: ${System.currentTimeMillis() - startTime}"
    }
}
