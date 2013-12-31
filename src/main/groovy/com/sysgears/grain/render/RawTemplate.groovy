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

import javax.inject.Inject

/**
 * Represents template that renders resource by returning raw contents
 * with highlighting section insertion.
 */
class RawTemplate implements ResourceTemplate {

    /** Resource file */
    private final File resource
    
    /** Resource file reader */
    @Inject private ResourceReader reader

    /**
     * Creates instance of the renderer
     *
     * @param resource resource file
     */
    @Inject
    public RawTemplate(@Assisted final File resource) {
        this.resource = resource
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
    public ResourceView render(final Map bindings, boolean isResourcePart) {
        def view = new ResourceView()
        if (!isBinary(resource)) {
            String content = reader.readText(resource)
            
            view.content = content
            view.full = content
            view.bytes = view.full.bytes
        } else {
            view.bytes = reader.readBytes(resource)
        }
        view
    }

    /**
     * Detects if resource is a binary file
     * 
     * @param resource resource file
     * 
     * @return whether file is binary
     */
    private static final isBinary(File resource) {
        !(resource.getExtension() in ['html', 'rb', 'markdown', 'md', 'rst', 'css', 'txt', 'xml', 'js']) 
    }

    /**
     * Returns layout of this template
     *
     * @return layout
     */
    public String getLayout() {
        null
    }
}

