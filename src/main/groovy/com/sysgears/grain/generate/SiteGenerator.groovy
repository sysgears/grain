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

package com.sysgears.grain.generate

import com.sysgears.grain.PerfMetrics
import com.sysgears.grain.css.compass.Compass
import com.sysgears.grain.compress.ResourceCompressor
import com.sysgears.grain.config.Config
import com.sysgears.grain.log.StreamLoggerFactory
import com.sysgears.grain.preview.SiteChangeBroadcaster
import com.sysgears.grain.registry.Registry
import com.sysgears.grain.registry.URLRegistry
import com.sysgears.grain.util.FileUtils
import groovy.util.logging.Slf4j

import javax.inject.Inject

/**
 * Grain static web site generator.
 */
@javax.inject.Singleton
@Slf4j
class SiteGenerator {

    /** Performance metrics */
    @Inject private PerfMetrics perf

    /** Site config */
    @Inject private Config config

    /** Compass launcher */
    @Inject private Compass compass

    /** Stream logger factory */
    @Inject private StreamLoggerFactory streamLoggerFactory

    /** Resource compressor */
    @Inject private ResourceCompressor compressor

    /** Resource registry */
    @Inject private Registry registry

    /** Resource url registry */
    @Inject private URLRegistry urlRegistry

    /** Site change broadcaster */
    @Inject private SiteChangeBroadcaster siteChangeBroadcaster

    /**
     * Generates static web site on a file system.
     */
    public void generate() {
        long startTime = System.currentTimeMillis()

        def destDir = new File(config.destination_dir.toString())
        FileUtils.removeDir(destDir)
        FileUtils.createDirs(destDir)

        compass.configureAndLaunch('compile')
        log.info 'Building resource registry...'
        registry.compile()
        compass.awaitTermination()
        registry.start()
        siteChangeBroadcaster.siteChanged()

        log.info "Generating resources... Time elapsed: ${System.currentTimeMillis() - startTime}"
        urlRegistry.resources.each { resource ->
            def url = resource.url
            def name = url.endsWith('/') ? url + 'index.html' : url
            def file = new File(destDir, name)
            def parentDir = file.parentFile
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new RuntimeException("Failed to create dirs for file: ${file}")
            }
            file.withOutputStream { os ->
                try {
                    os.write(compressor.compress(resource.location, resource.render().bytes))
                } catch (Throwable t) {
                    throw new RuntimeException("While generating ${resource.location}", t)
                }
            }
        }
        log.info "Perf data: ${perf}"
    }
}
