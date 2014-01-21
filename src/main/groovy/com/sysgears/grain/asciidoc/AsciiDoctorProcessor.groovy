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

package com.sysgears.grain.asciidoc

import com.sysgears.grain.compass.AbstractCompass
import com.sysgears.grain.config.Config
import com.sysgears.grain.init.GrainSettings
import com.sysgears.grain.rpc.ruby.Ruby
import com.sysgears.grain.service.Service
import groovy.util.logging.Slf4j

import javax.inject.Inject
import java.util.concurrent.CountDownLatch

/**
 * Implementation of AsciiDoctor integration.
 */
@Slf4j
@javax.inject.Singleton
public class AsciiDoctorProcessor implements Service {
    
    /** Site config */
    @Inject private Config config
    
    /** Grain settings */
    @Inject private GrainSettings settings
    
    /** Ruby implementation */
    @Inject private Ruby ruby
    
    /** Latch for AsciiDoctor initialization */
    private CountDownLatch latch

    /**
     * Renders AsciiDoctor content.
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
        ruby.rpc.with {
            Asciidoctor.render(source)
        }
    }

    /**
     * Does nothing.
     */
    @Override
    public void configChanged() {
    }

    /**
     * Does nothing.
     */
    @Override
    void start() {
        latch = new CountDownLatch(1)
        ruby.rpc.with {
            Ipc.install_gem('asciidoctor')
            Ipc.require('asciidoctor')
        }
        latch.countDown()
    }

    /**
     * Does nothing.
     */
    @Override
    void stop() {
        latch = null
    }
}
