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

package com.sysgears.grain.markup.asciidoc

import com.sysgears.grain.config.Config
import com.sysgears.grain.init.GrainSettings
import com.sysgears.grain.markup.MarkupProcessor
import com.sysgears.grain.rpc.ruby.Ruby
import com.sysgears.grain.service.Service
import groovy.util.logging.Slf4j

import javax.annotation.Nullable
import javax.inject.Inject

/**
 * Implementation of AsciiDoctor integration.
 */
@Slf4j
@javax.inject.Singleton
public class AsciiDoctorProcessor implements Service, MarkupProcessor {

    /** AsciiDoctor gem version used */
    @Inject private static final String VERSION = '1.5.2'

    /** Pygments.rb gem version used */
    @Inject private static final String CODERAY_VERSION = '1.1.0'
    
    /** Ruby implementation */
    @Inject private Ruby ruby
    
    /** Grain settings */
    @Inject private GrainSettings settings

    /** Site config */
    @Inject private Config config

    /**
     * Renders AsciiDoctor content.
     * 
     * @param source source
     * 
     * @return rendered output 
     */
    public String process(String source) {
        def options = config.features?.asciidoc?.opts?: [:]
        ruby.rpc.with {
            AsciidocBridge.convert(source, options)
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    void start() {
        ruby.rpc.Ipc.install_gem('asciidoctor', "=${VERSION}")
        ruby.rpc.Ipc.install_gem('coderay', "=${CODERAY_VERSION}")
        ruby.rpc.Ipc.add_lib_path new File(settings.toolsHome, 'asciidoc-bridge').canonicalPath
        ruby.rpc.Ipc.require('asciidoc_bridge')
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
        "asciidoctor.${VERSION.replace('.', '_')}"
    }
}
