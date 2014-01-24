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

package com.sysgears.grain.rst

import com.sysgears.grain.config.Config
import com.sysgears.grain.init.GrainSettings
import com.sysgears.grain.rpc.python.Python
import com.sysgears.grain.service.Service
import groovy.util.logging.Slf4j

import javax.inject.Inject
import java.util.concurrent.CountDownLatch

/**
 * Implementation of reStructuredText integration using Python docutils.
 */
@Slf4j
@javax.inject.Singleton
public class RstProcessor implements Service {
    
    /** Site config */
    @Inject private Config config
    
    /** Grain settings */
    @Inject private GrainSettings settings
    
    /** Python implementation */
    @Inject private Python python
    
    /** Latch for DocUtils initialization */
    private CountDownLatch latch

    /**
     * Renders reStructuredText content.
     * 
     * @param source source
     * 
     * @return rendered output 
     */
    public String process(String source) {
        if (!latch) {
            start()
        }
        latch.await()
        python.rpc.with {
            docutils_bridge.process(source)
        }
    }

    /**
     * Does nothing.
     */
    @Override
    public void configChanged() {
    }

    /**
     * @inheritDoc
     */
    @Override
    void start() {
        latch = new CountDownLatch(1)
        def ipc = python.rpc.ipc  
        ipc.install_package('docutils>=0.11')
        ipc.add_lib_path new File(settings.toolsHome, 'docutils-bridge').canonicalPath
        latch.countDown()
    }

    /**
     * @inheritDoc
     */
    @Override
    void stop() {
        latch = null
    }
}
