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
import com.sysgears.grain.exceptions.RenderExceptionFactory
import com.sysgears.grain.registry.ResourceLocator
import groovy.util.logging.Slf4j
import org.codehaus.groovy.runtime.InvokerHelper

import javax.annotation.Nullable
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

    /** Render Exception factory instance */
    @Inject private RenderExceptionFactory renderExceptionFactory

    /** Source file */
    private final File file

    /** Source code of the groovy script */
    private final String source
    
    /** Compiled groovy script */
    private final Script script

    /** Layout to be used for this resource if any */
    private final String layout

    /**
     * Creates an instance of GroovyTemplate.
     *  
     * @param file source file
     * @param source source code of the groovy script
     * @param script compiled groovy script
     * @param layout layout to be used for this resource if any 
     */
    @Inject
    public GroovyTemplate(@Assisted final File file,
                          @Assisted("source") final String source,
                          @Assisted final Script script,
                          @Nullable @Assisted("layout") final String layout) {
        this.file = file
        this.source = source
        this.script = script
        this.layout = layout
    }

    /**
     * Renders template by executing generated Groovy script with specified bindings.
     * 
     * @param bindings bindings
     * 
     * @return rendered view of this template
     */
    public ResourceView render(final Map bindings) {
        Binding binding = bindings == null ? new Binding() : new Binding(bindings)
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
            
            if (layout) {
                def newView = engine.createTemplate(locator.findLayout(layout)).
                        render(bindings + [content: view.content])
                view.full = newView.full
                view.bytes = view.full.bytes
            }

            view
        } catch (RenderException re) {
            throw re
        } catch (t) {
            throw renderExceptionFactory.create("Failed to parse ${file}",
                    t, file)
        }
    }

    /**
     * Returns layout of the resource
     *
     * @return layout
     */
    public String getLayout() {
        layout
    }
}
