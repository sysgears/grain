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
import org.codehaus.groovy.runtime.InvokerHelper

import javax.inject.Inject
import javax.inject.Named

/**
 * Represents template that renders resource by executing
 * Groovy script translated from resource.
 */
@Named
class GroovyTemplate implements ResourceTemplate {
    
    /** Performance metrics */
    @Inject private PerfMetrics perf

    /** Resource locator */
    @Inject private ResourceLocator locator

    /** Template engine */
    @Inject private TemplateEngine engine
    
    /** Header parser */
    @Inject private HeaderParser headerParser 

    /** Source file */
    private final File file

    /** Source code of the groovy script */
    private final String source
    
    /** Compiled groovy script */
    private final Script script
    
    /**
     * Creates an instance of GroovyTemplate.
     *  
     * @param file source file
     * @param source source code of the groovy script
     * @param script compiled groovy script
     */
    @Inject
    public GroovyTemplate(@Assisted final File file,
                          @Assisted final String source,
                          @Assisted final Script script) {
        this.file = file
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
            scriptObject.setProperty('out', writer)
            scriptObject.run()
            String content = writer.toString()
            view = new ResourceView()
            view.content = content.replaceAll('<p><figure', '<figure').replaceAll('</figure></p>', '</figure>')
            view.full = view.content
            view.bytes = view.full.bytes
            def renderTime = System.currentTimeMillis() - startRenderTime
            perf.renderTime += renderTime
            
            final String layout = locator.isResource(file) ? bindings?.page?.layout : headerParser.parse(file).layout

            println "${file} - ${layout}"

            if (layout) {
                def newView = engine.createTemplate(locator.findLayout(layout)).
                        render(bindings + [content: view.content])
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
            throw new RenderException("Failed to parse ${file} script: " + sw.toString() + "\nScript source:\n${src}")
        }
    }
}
