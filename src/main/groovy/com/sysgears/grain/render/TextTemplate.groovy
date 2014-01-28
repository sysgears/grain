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

    /** Layout used for this template if any */
    private final String layout

    /** Resource locator */
    @Inject private ResourceLocator locator

    /**
     * Creates instance of the renderer
     *
     * @param contents resource file contents
     * @param fragments highlighted fragments
     * @param layout template layout
     */
    @Inject
    public TextTemplate(@Assisted("contents") final String contents,
                       @Nullable @Assisted final List<String> fragments,
                       @Nullable @Assisted("layout") final String layout) {
        this.contents = contents
        this.fragments = fragments
        this.layout = layout
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
        int codeIdx = 0

        String content = contents.replaceAll(/```/, {
            this.fragments[codeIdx++]
        })
        view.content = content.replaceAll(/(?s)<p>(<figure.+?<\/figure>)<\/p>/, '$1')
        view.full = view.content
        view.bytes = view.full.bytes

        if (layout) {
            def newView = engine.createTemplate(locator.findLayout(layout)).
                    render(bindings + [content: view.content])
            view.full = newView.full
            view.bytes = view.full.bytes
        }
        
        view
    }

    /**
     * Returns layout of this template
     *
     * @return layout
     */
    public String getLayout() {
        layout
    }
}

