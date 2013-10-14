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

package com.sysgears.grain.preview

import com.sysgears.grain.compress.ResourceCompressor
import com.sysgears.grain.config.Config
import com.sysgears.grain.config.Service
import com.sysgears.grain.registry.Registry
import com.sysgears.grain.registry.URLRegistry
import groovy.util.logging.Slf4j
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.util.resource.ResourceCollection

import javax.inject.Inject
import javax.inject.Named

/**
 * Grain web server service that shows static site in preview mode with hot rereading of file system changes.
 */
@Named
@javax.inject.Singleton
@Slf4j
class SitePreviewer implements Service {

    /** Site file change watcher */
    @Inject private FileWatcher fileWatcher

    /** Resource registry */
    @Inject private Registry registry

    /** URL registry */
    @Inject private URLRegistry urlRegistry

    /** Site change broadcaster */
    @Inject private SiteChangeBroadcaster siteChangeBroadcaster

    /** Site config */
    @Inject private Config config

    /** Resource compressor */
    @Inject private ResourceCompressor compressor

    /** Rendering mutex */
    @Inject @Named("renderMutex") private Object mutex

    /**
     * Runs Grain web server 
     */
    public void start() {
        fileWatcher.start()
        log.info 'Building resource registry...'
        registry.start()
        try {
            siteChangeBroadcaster.siteChanged()
        } catch (t) {
            t.printStackTrace()
        }
    
        def jettyPort = config.jetty_port ?: 5000
    
        def jetty = new Server(jettyPort)
    
        log.info "Starting Jetty on http://localhost:${jettyPort}, press Ctrl+C to stop."
    
        def context = new ServletContextHandler(jetty, '/', ServletContextHandler.SESSIONS)
        context.setClassLoader(this.getClass().getClassLoader())
        def resourceDirs = new ResourceCollection(config.source_dir as String[])
        context.baseResource = resourceDirs
        context.setAttribute('urlRegistry', urlRegistry)
        context.setAttribute('compressor', compressor)
        context.setAttribute('renderMutex', mutex)
        context.addServlet(GrainServlet, '/')
    
        jetty.start()
        jetty.join()
        
        // Force application shutdown when jetty stopped.
        System.exit(0)
    }

    /**
     * Does nothing
     */
    @Override
    public void stop() {
    }
}
