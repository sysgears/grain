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

import com.sysgears.grain.registry.URLRegistry
import com.sysgears.grain.render.ResourceView
import com.sysgears.grain.util.FixedBlock
import groovy.util.logging.Slf4j

import javax.inject.Inject

/**
 * Basic tag lib exposed to each resource code.
 */
@Slf4j
class GrainTagLib extends GrainUtils {

    /** Resource locator */
    @Inject private URLRegistry urlRegistry

    /** Site instance exposed to the resource code */
    Site site

    /** Page data exposed to the resource */
    Map page = [:]
    
    /**
     * Creates and initializes an instance of resource tag lib.
     */
    @Inject
    public GrainTagLib(Site site) {
        this.site = site
    }

    /**
     * Looks up resource url by resource location
     * 
     * @attr location resource location
     * 
     * @return resource URL
     */
    def r = { String location ->
        if (!location.startsWith("/")) {
            location = new File(new File(page.url.toString()).parentFile ?: new File("/"), location)
        }
        def absoluteUrl = urlRegistry.getCdnUrl(urlRegistry.getUrl(location))
        if (absoluteUrl == null) {
            log.warn "WARNING: ${page.location} tried to find out url of absent resource: ${location}"
            absoluteUrl = "/not/found/${location}"
        }
        absoluteUrl
    }

    /**
     * Looks up multiple resource urls by their locations
     *
     * @attr locations list of resource locations
     *
     * @return resource URLs list
     */
    def rs = { List<String> locations ->
        urlRegistry.getUrls((String[]) locations.collect { location ->
            def relativeUrl
            if (!location.startsWith("/")) {
                relativeUrl = new File(new File(page.url.toString()).parentFile ?: new File("/"), location).toString()
            } else {
                relativeUrl = location
            }
            urlRegistry.getCdnUrl(relativeUrl)
        })
    }

    /**
     * Returns rendered resource contents with specified location
     * 
     * @attr location template location
     * @attr model (optional) additional model variables added to <code>page</code> map
     */
    def include = { String location, Map model = null ->
        try {
            FixedBlock.wrapText(page.include(location, model) as String)
        } catch (AbsentResourceException e) {
            log.warn "WARNING: ${page.location} tried to include absent resource: ${location}"
            def msg = "Resource not found: ${location}"
            new ResourceView(content: msg, full: msg, bytes: msg.bytes)
        }
    }
}
