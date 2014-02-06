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

package com.sysgears.grain.highlight.pygments

import com.sysgears.grain.init.GrainSettings
import com.sysgears.grain.rpc.python.Python
import groovy.util.logging.Slf4j

import javax.annotation.Nullable
import javax.inject.Inject
import java.util.concurrent.CountDownLatch

/**
 * Implementation of Pygments highlighter integration.
 */
@Slf4j
@javax.inject.Singleton
public class PythonPygments extends Pygments {

    /** Grain settings */
    @Inject private GrainSettings settings

    /** Python implementation */
    @Inject private Python python

    /** Latch for Pygments initialization */
    private CountDownLatch latch
    
    /** Pygments version */
    private String version

    /**
     * Launches pygments 
     */
    public void start() {
    }

    /**
     * Terminates pygments  
     */
    public void stop() {
        latch = null
    }

    /**
     * Highlights code using Pygments 
     *
     * @param code a code to highlight
     * @param language the language of the code
     *
     * @return highlighted code HTML
     */
    public String highlight(String code, String language) {
        startAndWait()
        python.rpc.pygments_bridge.highlight(code, language)
    }

    /**
     * Starts Python pygments and waits until start will be completed 
     */
    private void startAndWait() {
        if (!latch) {
            latch = new CountDownLatch(1)
            try {
                def rpc = python.rpc
                this.version = rpc.ipc.install_package('pygments>=1.6')
                rpc.ipc.add_lib_path new File(settings.toolsHome, 'pygments-bridge').canonicalPath
            } finally {
                latch.countDown()
            }
        }
        latch.await()
    }

    /**
     * @inheritDoc
     */
    @Nullable String getCacheSubdir() {
        startAndWait()
        "pygments." + version?.replace('.', '_')
    }
}
