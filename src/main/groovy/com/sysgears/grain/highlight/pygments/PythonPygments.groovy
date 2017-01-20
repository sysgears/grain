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
import org.jetbrains.annotations.Nullable

import javax.inject.Inject

/**
 * Implementation of Pygments highlighter integration.
 */
@Slf4j
@javax.inject.Singleton
public class PythonPygments implements Pygments {

    /** Pygments version used */
    private static final String VERSION = '1.6'

    /** Grain settings */
    @Inject private GrainSettings settings

    /** Python implementation */
    @Inject private Python python

    /**
     * Launches pygments 
     */
    public void start() {
        def rpc = python.rpc
        rpc.ipc.install_package("pygments==${VERSION}")
        rpc.ipc.add_lib_path new File(settings.toolsHome, 'pygments-bridge').canonicalPath
    }

    /**
     * Terminates pygments  
     */
    public void stop() {
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
        python.rpc.pygments_bridge.highlight(code, language)
    }

    /**
     * @inheritDoc
     */
    @Nullable String getCacheSubdir() {
        "pygments.${VERSION.replace('.', '_')}"
    }
}
