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

import com.google.inject.assistedinject.Assisted
import com.sysgears.grain.PerfMetrics
import com.sysgears.grain.registry.HeaderParser
import com.sysgears.grain.registry.ResourceLocator
import com.sysgears.grain.util.FixedBlock
import groovy.util.logging.Slf4j
import org.codehaus.groovy.runtime.InvokerHelper

import javax.inject.Inject

/**
 * Represents template that renders resource by executing
 * Groovy script translated from resource.
 */
@Slf4j
class GroovyTemplate implements ResourceTemplate {
    
    /** Performance metrics */
    @Inject private PerfMetrics perf

    /** Resource locator */
    @Inject private ResourceLocator locator

    /** Template engine */
    @Inject private TemplateEngine engine
    
    /** Layout header parser */
    @Inject private HeaderParser parser

    /** Markup processor */
    @Inject private MarkupProcessor markupProcessor

    /** Resource map */
    private final Map resource 

    /** Source code of the groovy script */
    private final String source
    
    /** Compiled groovy script */
    private final Script script

    /**
     * Creates an instance of GroovyTemplate.
     *  
     * @param map resource map
     * @param source source code of the groovy script
     * @param script compiled groovy script
     */
    @Inject
    public GroovyTemplate(@Assisted final Map resource, 
                          @Assisted final String source,
                          @Assisted final Script script) {
        this.resource = resource
        this.source = source
        this.script = script
    }

    /**
     * Renders template by executing generated Groovy script with specified bindings.
     *
     * @param bindings bindings
     *
     * @return rendered view of this template
     */
    public ResourceView render(final Map bindings) {
        Binding binding
        if (bindings == null)
            binding = new Binding()
        else
            binding = new Binding(bindings)

        try {
            ResourceView view

            long startRenderTime = System.currentTimeMillis()
            def writer = new StringWriter()
            Script scriptObject = InvokerHelper.createScript(script.getClass(), binding)
            if (!binding.hasVariable('content')) {
                scriptObject.setProperty('content', '')
            }
            scriptObject.setProperty('output', writer)
            scriptObject.run()
            String content = writer.toString()
            view = new ResourceView()
            view.content = markupProcessor.process(content, resource.markup)
            view.full = view.content
            view.bytes = view.full.bytes
            def renderTime = System.currentTimeMillis() - startRenderTime
            perf.renderTime += renderTime
            
            final String layout = resource.layout

            log.trace "Rendering Groovy template for ${resource.location ?: '<source>'}, layout: ${layout}, markup: ${resource.markup}"
            if (layout) {
                def layoutFile = locator.findLayout(layout)
                def newView = engine.createTemplate(parser.parse(layoutFile) +
                        [location: layoutFile.toString()]).
                        render(bindings + [content: FixedBlock.escapeText(view.content)])
                view.full = newView.full
                view.bytes = view.full.bytes
            }

            view
        } catch (RenderException gr) {
            throw gr
        } catch (t) {
            def sw = new StringWriter()
            t.printStackTrace(new PrintWriter(sw))
            def src = new StringWriter()
            source.readLines().eachWithIndex { String line, int i -> src.append("${i+1}: ${line}\n")}
            def file = resource.location ? locator.findInclude(resource.location) : '<source>'
            throw new RenderException("Failed to parse ${file} script: " +
                    sw.toString() + "\nScript source:\n${src}")
        }
    }

}
