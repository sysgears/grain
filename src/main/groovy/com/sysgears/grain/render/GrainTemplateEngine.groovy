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

package com.sysgears.grain.render

import com.sysgears.grain.PerfMetrics
import com.sysgears.grain.config.Config
import com.sysgears.grain.highlight.PageHighlighter
import com.sysgears.grain.preview.SiteChangeListener
import com.sysgears.grain.registry.HeaderParser
import com.sysgears.grain.registry.MarkupDetector
import com.sysgears.grain.registry.ResourceLocator
import com.sysgears.grain.registry.ResourceParser
import com.sysgears.grain.taglib.AbsentResourceException
import com.sysgears.grain.translator.ScriptTranslator
import groovy.util.logging.Slf4j

import javax.inject.Inject

/**
 * Grain template engine.
 */
@javax.inject.Singleton
@Slf4j
class GrainTemplateEngine implements TemplateEngine, SiteChangeListener {
    
    /** Site config */
    @Inject private Config config

    /** Site performance metrics */
    @Inject private PerfMetrics perf

    /** Resource locator */
    @Inject private ResourceLocator locator

    /** Source code highlighter */ 
    @Inject private PageHighlighter pageHighlighter

    /** Resource header parser */
    @Inject private HeaderParser headerParser

    /** Script translator */
    @Inject private ScriptTranslator scriptTranslator

    /** Groovy Shell used to execute Groovy scripts */
    @Inject private GroovyShell groovyShell

    /** Markup detector */
    @Inject private MarkupDetector markupDetector

    /** Raw template factory */
    @Inject private RawTemplateFactory rawTemplateFactory

    /** Text template factory */
    @Inject private TextTemplateFactory textTemplateFactory

    /** Groovy script-based template factory */
    @Inject private GroovyTemplateFactory groovyTemplateFactory

    /** Groovy script name counter */
    private volatile def counter = 1

    /**
     * Clears Groovy Shell cache on site change event to prevent out of memory. 
     */
    @Override
    void siteChanged() {
        clearCache()
    }

    /**
     * Clears Groovy Shell cache on site change event
     */
    private void clearCache() {
        groovyShell.classLoader.clearCache()
    }

    /**
     * @inheritDoc 
     */
    public ResourceTemplate createTemplate(final Map resource) throws RenderException {
        long startDocParse = System.currentTimeMillis()
        
        def res = [:] + resource
        
        File file = res.source ? new File('<source>') : res.location

        if (!file)
            throw new AbsentResourceException("Resource was not found: ${res.location}", res.lcation)
        
        if (!res.markup) {
            res.markup = markupDetector.getMarkupType(file) 
        }
        
        log.trace "Creating template for ${file}, markup: ${res.markup}"

        if (res.markup == 'binary') {
            return rawTemplateFactory.create(res.source ? res.source as byte[] : file.bytes)
        }
        
        def text = ''
        if (res.source) {
            text = res.source 
        } else {
            new BufferedReader(new StringReader(file.text)).withReader { reader ->
                def resourceParser = new ResourceParser(file, reader)
                headerParser.parse(file, resourceParser.header)
                text = resourceParser.content
            }
        }

        if (res.markup != 'text') {
            text = pageHighlighter.highlight(text)
        }

        long startScriptParse = System.currentTimeMillis()
        perf.docParseTime += (startScriptParse - startDocParse)
        def scriptName = "GrainScript${counter++}.groovy"
        
        def template
        if (res.script != false) {
            String source = null
            try {
                source = scriptTranslator.translate(text)
                perf.scriptParseTime += (System.currentTimeMillis() - startScriptParse)
                long startScriptCompileTime = System.currentTimeMillis()
                def script = groovyShell.parse(source, scriptName)
                template = groovyTemplateFactory.create(res, source, script)
                perf.scriptCompileTime += (System.currentTimeMillis() - startScriptCompileTime)
            } catch (RenderException gr) {
                throw gr
            } catch (t) {
                def sw = new StringWriter()
                t.printStackTrace(new PrintWriter(sw))
                def src = new StringWriter()
                if (source == null) {
                    source = text
                }
                source.readLines().eachWithIndex { String line, int i -> src.append("${i+1}: ${line}\n")}
                throw new RenderException("Failed to parse ${file} script: " + sw.toString() + "\nScript source:\n${src}")
            }
        } else {
            template = textTemplateFactory.create(res, text)
        }
        
        template
    }
}
