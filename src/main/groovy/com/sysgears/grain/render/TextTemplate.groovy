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
import com.sysgears.grain.registry.ResourceLocator

import javax.annotation.Nullable
import javax.inject.Inject

/**
 * Represents template that renders resource by returning predefined text
 * with highlighting section insertion.
 */
class TextTemplate implements ResourceTemplate {

    /** Resource text */
    private final String contents
    
    /** Highlighted fragments */
    private final List<String> fragments

    /** Template engine */
    @Inject private TemplateEngine engine

    /** Source file */
    private final File file

    /** Resource locator */
    @Inject private ResourceLocator locator

    /** Layout header parser */
    @Inject HeaderParser parser

    /**
     * Creates instance of the renderer
     *
     * @param file source file
     * @param contents resource file contents
     * @param fragments highlighted fragments
     */
    @Inject
    public TextTemplate(@Assisted final File file,
                        @Assisted final String contents,
                        @Nullable @Assisted final List<String> fragments) {
        this.file = file
        this.contents = contents
        this.fragments = fragments
    }

    /**
     * Returns text file contents with highlighting section insertion
     * as rendered representation of resource. 
     *
     * @param bindings ignored
     * @param isResourcePart whether rendered template is layout or include
     *
     * @return rendered view of resource
     */
    public ResourceView render(final Map bindings, final boolean isResourcePart) {
        def view = new ResourceView()
        int codeIdx = 0

        String content = contents.replaceAll(/```/, {
            this.fragments[codeIdx++]
        })
        view.content = content.replaceAll('<p><figure', '<figure').
                replaceAll('</figure></p>', '</figure>')
        view.full = view.content
        view.bytes = view.full.bytes

        final String layout = isResourcePart ? parser.parse(file).layout : bindings?.page?.layout

        if (layout) {
            def newView = engine.createTemplate(locator.findLayout(layout)).
                    render(bindings + [content: view.content], true)
            view.full = newView.full
            view.bytes = view.full.bytes
        }

        view
    }

}

