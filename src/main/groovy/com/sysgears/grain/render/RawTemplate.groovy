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
    
    /** Resource file bytes */
    private final byte[] bytes
    
    /**
     * Creates instance of the renderer
     *
     * @param resource resource bytes
     */
    @Inject
    public RawTemplate(@Assisted final byte[] bytes) {
        this.bytes = bytes
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
        new ResourceView(bytes: bytes)
    }

}
