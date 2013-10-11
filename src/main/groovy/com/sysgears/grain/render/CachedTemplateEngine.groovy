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
import com.sysgears.grain.annotations.Uncached
import com.sysgears.grain.preview.SiteChangeListener

import javax.inject.Inject

/**
 * Template engine that caches created templates
 */
class CachedTemplateEngine implements TemplateEngine, SiteChangeListener {

    /** Site performance metrics */
    @Inject private PerfMetrics perf

    /** Uncached template engine */
    @Inject @Uncached private TemplateEngine engine

    /**
     * Clears template cache on site change event
     */
    @Override
    void siteChanged() {
        templateCache.clear()
    }

    /**
     * Template cache entry.
     */
    private class TemplateCacheEntry {
        /** Resource file last modified time */
        long lastModified
        
        /** Cached resource template */
        ResourceTemplate template
    }

    /**
     * Template cache map
     */
    private static Map<String, TemplateCacheEntry> templateCache = [:]

    /**
     * @inheritDoc 
     */
    @Override
    public ResourceTemplate createTemplate(File file) throws RenderException {
        long createTemplateStart = System.currentTimeMillis()
        def entry = templateCache[file.absolutePath]
        if (entry && file.lastModified() == entry.lastModified) {
            entry.template
        } else {
            def newEntry = new TemplateCacheEntry(
                    lastModified: file.lastModified(),
                    template: engine.createTemplate(file))
            templateCache[file.absolutePath] = newEntry
            perf.createTemplateTime += System.currentTimeMillis() - createTemplateStart
            
            newEntry.template
        }
    }
}
