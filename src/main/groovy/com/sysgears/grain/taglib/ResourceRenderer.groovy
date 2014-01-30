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

package com.sysgears.grain.taglib

import com.sysgears.grain.config.Config
import com.sysgears.grain.registry.HeaderParser
import com.sysgears.grain.registry.ResourceLocator
import com.sysgears.grain.render.ResourceView
import com.sysgears.grain.render.TemplateEngine

import javax.inject.Inject
import javax.inject.Provider

/**
 * Resource renderer
 */
@javax.inject.Singleton
class ResourceRenderer {

    /** Resource taglib factory */
    @Inject private Provider<GrainTagLib> taglibProvider

    /** Site config */
    @Inject private Config config

    /** Resource locator */
    @Inject private ResourceLocator locator

    /** Template engine */
    @Inject private TemplateEngine templateEngine
    
    /** Header parser */
    @Inject HeaderParser parser

    /**
     * Renders resource with a given model.
     * 
     * @param resource resource metadata
     * @param model resource dynamic model
     * @param isResourcePart whether rendered resource is include 
     *
     * @return result of resource rendering
     */
    public ResourceView render(Map resource, Map model, boolean isResourcePart) {
        if (!resource.location && (!resource.source || !resource.markup))
            throw new InvalidObjectException("Not a resource map, the map should have either 'location' key " +
                    "or 'source' and 'markup' keys, map:\n${resource}")

        def updatedResource = [:] + resource
        if (resource.location) {
            def file = locator.findInclude(resource.location)            

            if (!file)
                throw new AbsentResourceException("Resource was not found: ${resource.location}", resource.lcation)

            if (isResourcePart) {
                updatedResource.remove('markup')
                updatedResource.remove('layout')
                updatedResource += parser.parse(file)
            }
        }
        
        def page = resource
        
        def taglib = taglibProvider.get()

        if (model) {
            page = (resource as ConfigObject).merge(model as ConfigObject)
        }
        taglib.page.putAll(page)

        def tagLibs = (config.tag_libs ?: []).collect { it.newInstance(taglib) }
        def bindings = [:]

        ([taglib, new GrainUtils()] + tagLibs).each {
            it.properties.findAll { !(it.key in ['class', 'metaClass']) }.each {
                bindings.put(it.key, it.value)
            }
        }

        templateEngine.createTemplate(updatedResource).render(bindings)        
    }
}
