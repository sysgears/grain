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
import com.sysgears.grain.registry.HeaderParser
import com.sysgears.grain.registry.ResourceLocator
import com.sysgears.grain.registry.ResourceParser
import com.sysgears.grain.taglib.GrainUtils

import javax.inject.Inject

/**
 * Template engine that caches created templates
 */
class CachedTemplateEngine implements TemplateEngine, SiteChangeListener {

    /** Site performance metrics */
    @Inject private PerfMetrics perf

    /** Uncached template engine */
    @Inject @Uncached private TemplateEngine engine

    /** Resource header parser */
    @Inject private HeaderParser headerParser

    /** Resource locator */
    @Inject private ResourceLocator locator

    /**
     * Template cache map for files
     */
    private static Map<String, ResourceTemplate> fileCache = [:]

    /**
     * Template cache map for dynamic resources
     */
    private static Map<String, ResourceTemplate> memCache = [:]

    /**
     * Clears template cache on site change event
     */
    @Override
    void siteChanged() {
        fileCache.clear()
        memCache.clear()
    }

    /**
     * @inheritDoc 
     */
    @Override
    public ResourceTemplate createTemplate(Map resource) throws RenderException {
        long createTemplateStart = System.currentTimeMillis()

        if (resource.markup in ['binary', 'text']) {
            return engine.createTemplate(resource)
        }
        
        def cacheMap, key
        if (resource.source) {
            key = GrainUtils.md5(resource.source.bytes)
            cacheMap = memCache
        } else {
            key = resource.location
            cacheMap = fileCache
        }

        if (!cacheMap[key]) {
            cacheMap[key] = engine.createTemplate(resource)

            perf.createTemplateTime += System.currentTimeMillis() - createTemplateStart
        }

        cacheMap[key]
    }
}
