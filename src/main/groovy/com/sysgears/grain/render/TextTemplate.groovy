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
import com.sysgears.grain.registry.HeaderParser
import com.sysgears.grain.registry.MarkupDetector
import com.sysgears.grain.registry.ResourceLocator
import com.sysgears.grain.util.FixedBlock
import groovy.util.logging.Slf4j

import javax.inject.Inject
import javax.inject.Named

/**
 * Represents template that renders resource by returning predefined text
 * with highlighting section insertion.
 */
@Slf4j
class TextTemplate implements ResourceTemplate {

    /** Template engine */
    @Inject private TemplateEngine engine

    /** Layout header parser */
    @Inject private HeaderParser parser

    /** Resource locator */
    @Inject private ResourceLocator locator

    /** Markup processor */
    @Inject private MarkupProcessor markupProcessor

    /** Resource map */
    private final Map resource

    /** Resource text */
    private final String contents
    
    /**
     * Creates instance of the renderer
     *
     * @param file source file
     * @param contents resource file contents
     */
    @Inject
    public TextTemplate(@Assisted final Map resource,
                        @Assisted final String contents) {
        this.resource = resource
        this.contents = contents
    }

    /**
     * Returns text file contents with highlighting section insertion
     * as rendered representation of resource. 
     *
     * @param bindings ignored
     *
     * @return rendered view of resource
     */
    public ResourceView render(final Map bindings) {
        def view = new ResourceView()

        view.content = markupProcessor.process(contents, resource.markup) 
        view.full = view.content
        view.bytes = view.full.bytes

        final String layout = resource.layout

        log.trace "Rendering text template for ${resource.location ?: '<source>'}, layout: ${layout}, markup: ${resource.markup}"

        if (layout) {
            def layoutFile = locator.findLayout(layout)
            def newView = engine.createTemplate(parser.parse(layoutFile) + 
                    [location: layoutFile.toString()]).
                    render(bindings + [content: FixedBlock.escapeText(view.content)])
            view.full = newView.full
            view.bytes = view.full.bytes
        }

        view
    }

}

