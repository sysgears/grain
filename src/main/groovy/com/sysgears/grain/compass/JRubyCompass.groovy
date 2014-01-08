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

import com.sysgears.grain.log.LoggingOutputStream
import com.sysgears.grain.taglib.Site
import groovy.util.logging.Slf4j
import org.jruby.Ruby
import org.jruby.RubyInstanceConfig
import org.jruby.exceptions.RaiseException

import javax.inject.Inject
import javax.inject.Named
import java.util.concurrent.CountDownLatch

/**
 * Utilization of Compass as a JRuby process
 */
@javax.inject.Singleton
@Slf4j
class JRubyCompass extends AbstractCompass {

    /** Site */
    @Inject private Site site
    
    /** Ruby interpreter instance */
    private Ruby ruby

    /** Mutex for Compass starting */
    private CountDownLatch latch
    
    /** JRuby Compass thread */ 
    private Thread thread
    
    /** Whether stop is in process */ 
    private volatile stopInProcess = false 

    /**
     * Launches compass in a separate thread 
     * <p>
     * If the thread is interrupted the process will be destroyed.
     * 
     * @param mode compass mode
     */
    public void launchCompass(String mode) {
        latch = new CountDownLatch(1)
        thread = Thread.start {
            try {
                System.setProperty('jruby.compile.fastest', 'true')
                
                def config = new RubyInstanceConfig()
                config.processArguments("-S compass ${mode} ${site.cache_dir}".split(' '))
                config.setOutput(new PrintStream(new LoggingOutputStream()))
                
                ruby = Ruby.newInstance(config)
                
                def inp = config.getScriptSource();
                config.processArguments(config.parseShebangOptions(inp));
                def filename = config.displayedFileName();
    
                log.info 'Launching bundled JRuby Compass...'
                try {
                    latch.countDown()
                    ruby.runFromMain(inp, filename)
                } catch (RaiseException re) {
                    if (re.exception.toString() != "exit") {
                        log.error("Error while running compass", re)
                    } else if (re.exception.toString() == "exit" && mode == "watch" && !stopInProcess) {
                        // Use pressed CTRL-C which was intercepted by JRuby, terminating Grain
                        Thread.startDaemon {
                            System.exit(0)
                        }
                    }
                } catch (t) {
                    log.error("Error while running compass", t)
                }
                log.info 'Bundled JRuby Compass finished'
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
            latch = null
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
            stopInProcess = true
            ruby?.threadService?.mainThread?.internalRaise(ruby?.interrupt)
            thread.join()
            stopInProcess = false
            latch = null
        }
    }
}