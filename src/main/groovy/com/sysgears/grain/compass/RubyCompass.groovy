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

package com.sysgears.grain.compass

import com.sysgears.grain.CmdlineOptions
import com.sysgears.grain.log.StreamLogger
import com.sysgears.grain.log.StreamLoggerFactory
import com.sysgears.grain.taglib.Site
import groovy.io.FileType
import groovy.util.logging.Slf4j

import javax.inject.Inject
import javax.inject.Named
import java.util.concurrent.CountDownLatch

/**
 * Compass integration as external Ruby process.
 */
@Named
@javax.inject.Singleton
@Slf4j
class RubyCompass extends AbstractCompass {

    /** Site */
    @Inject private Site site

    /** Grain command-line options */
    @Inject private CmdlineOptions opts

    /** Stream logger factory */
    @Inject private StreamLoggerFactory streamLoggerFactory
    
    /** Process streams logger */
    private StreamLogger streamLogger

    /** Mutex for Compass starting */
    private CountDownLatch latch
    
    /** Compass process thread */
    private Thread thread
    
    /**
     * Launches compass in a separate thread.
     * <p>
     * If the thread is interrupted the process will be destroyed.
     *
     * @param mode compass mode
     */
    public void launchCompass(String mode) {
        latch = new CountDownLatch(1)
        thread = Thread.start {
            try {
                log.info "Launching Ruby compass process in ${mode} mode..."
                File gemDir = new File(opts.vendorHome, 'compass/gems')
                File compassDir
                def gemIncludes = [] as List<String>

                gemDir.eachFileMatch(~'compass.*') { f ->
                    compassDir = f
                }

                gemDir.eachFile(FileType.DIRECTORIES) {
                    gemIncludes += ['-I', new File(it, 'lib').canonicalPath]
                }

                def cmdline = [RubyFinder.rubyCmd] + gemIncludes +
                        [new File(compassDir, 'bin/compass').canonicalPath, mode] as List<String>
                
                log.info cmdline.join(' ')

                def process = cmdline.execute([], new File(site.cache_dir.toString()))
                streamLogger = streamLoggerFactory.create(process.in, process.err)
                streamLogger.start()
                latch.countDown()
                def watcher = Thread.start {
                    process.waitFor()
                    streamLogger.interrupt()
                }
                streamLogger.join()
                process.destroy()
                watcher.join()
                log.info 'Ruby compass process finished.'
            } catch (t) {
                log.error("Error launching Compass", t)
                latch.countDown()
            }
        }
    }

    /**
     * Awaits termination of Compass process
     */
    public void awaitTermination() {
        if (latch) {
            latch.await()
            thread.join()
        }
    }

    /**
     * Shuts down Compass process 
     *
     * @throws Exception in case some error occur
     */
    @Override
    public void stop() {
        if (latch) {
            latch.await()
            streamLogger?.interrupt()
            thread.join()
            latch = null
        }
    }
}
