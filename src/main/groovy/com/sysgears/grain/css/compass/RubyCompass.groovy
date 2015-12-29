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

package com.sysgears.grain.css.compass

import com.sysgears.grain.config.Config
import com.sysgears.grain.init.GrainSettings
import com.sysgears.grain.rpc.ruby.Ruby
import groovy.util.logging.Slf4j

import javax.inject.Inject

/**
 * Implementation of Compass integration.
 */
@Slf4j
@javax.inject.Singleton
public class RubyCompass extends AbstractCompass {
    
    /** Site config */
    @Inject private Config config
    
    /** Grain settings */
    @Inject private GrainSettings settings
    
    /** Ruby implementation */
    @Inject private Ruby ruby

    /**
     * Launches compass in a separate Ruby thread 
     * <p>
     * If the thread is interrupted the process will be destroyed.
     *
     * @param mode compass mode
     */
    protected void launchCompass(String mode) {
        log.info 'Launching Ruby Compass process...'

        def rpc = ruby.rpc

        rpc.Ipc.install_gem('sass', '3.4.19')
        rpc.Ipc.install_gem('compass', '1.0.3')
        
        rpc.Ipc.add_lib_path new File(settings.toolsHome, 'compass-bridge').canonicalPath
        rpc.Ipc.require('compass_bridge')
        
        rpc.CompassBridge.start(mode, "${config.cache_dir}")

        if (mode == 'compile') {
            // Wait for the compass to compile stylesheets before returning control to a caller
            rpc.CompassBridge.await()
        }
    }

    /**
     * Awaits termination of Compass process
     */
    public void awaitTermination() {
        ruby.rpc.CompassBridge.await()
    }

    /**
     * Shuts down Compass process 
     *
     * @throws Exception in case some error occur
     */
    @Override
    public void stop() {
        log.info 'Stopping Ruby Compass process...'
        ruby.rpc.CompassBridge.stop()
        log.info 'Ruby Compass process finished.'
    }
}
