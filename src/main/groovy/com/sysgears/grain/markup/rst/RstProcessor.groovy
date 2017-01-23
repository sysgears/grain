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

package com.sysgears.grain.markup.rst

import com.sysgears.grain.init.GrainSettings
import com.sysgears.grain.markup.MarkupProcessor
import com.sysgears.grain.rpc.python.Python
import com.sysgears.grain.service.Service
import groovy.util.logging.Slf4j
import org.jetbrains.annotations.Nullable

import javax.inject.Inject

/**
 * Implementation of reStructuredText integration using Python docutils.
 */
@Slf4j
@javax.inject.Singleton
public class RstProcessor implements Service, MarkupProcessor {

    /** Docutils version. */
    private static final String VERSION = "0.11"
    
    /** Grain settings */
    @Inject private GrainSettings settings
    
    /** Python implementation */
    @Inject private Python python
    
    /**
     * Renders reStructuredText content.
     * 
     * @param source source
     * 
     * @return rendered output 
     */
    public String process(String source) {
        python.rpc.with {
            docutils_bridge.process(source)
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    void start() {
        def ipc = python.rpc.ipc
        ipc.install_package("docutils==${VERSION}")
        ipc.add_lib_path new File(settings.toolsHome, 'docutils-bridge').canonicalPath
    }

    /**
     * @inheritDoc
     */
    @Override
    void stop() {
    }

    /**
     * @inheritDoc
     */
    @Nullable String getCacheSubdir() {
        "rst.${VERSION.replace('.', '_')}"
    }
}
