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

package com.sysgears.grain.registry

import com.sysgears.grain.PerfMetrics
import com.sysgears.grain.config.Config
import com.sysgears.grain.init.GrainSettings
import com.sysgears.grain.preview.SiteChangeListener
import groovy.util.logging.Slf4j

import javax.inject.Inject

/**
 * Site URL registry
 * <p>
 * Class responsible for maintaining URL map of the site
 */
@javax.inject.Singleton
@Slf4j
class URLRegistry implements SiteChangeListener {

    /** Max number of connection to different domains that web browser supposedly use */
    private final int MAX_BROWSER_CONNECTS = 6

    /** Site URL map */
    private Map<String, Map<String, Object>> urlMap = [:]

    /** Resource location to url map */
    private Map<String, String> locationMap = [:]

    /** Page list */
    private ArrayList<String> pages = []

    /** Asset list */
    private ArrayList<String> assets = []

    /** Site config instance */
    @Inject private Config config

    /** Grain settings */
    @Inject private GrainSettings settings

    /** Performance metrics */
    @Inject private PerfMetrics perf

    /** Resource registry */
    @Inject private Registry registry

    /** Current CDN no to use */
    private int cdnIndex = 0

    /** CDN url cache */
    private def cdnCache = [:]

    /**
     * @inheritDoc
     */
    void siteChanged() {
        cdnIndex = 0
        cdnCache = [:]

        rebuildUrlResourceMap()
    }

    /**
     * Rebuilds site's URL to resource map.
     */
    private void rebuildUrlResourceMap() {
        long resourceMapStart = System.currentTimeMillis()

        log.info 'Rebuilding url->resource map'

        urlMap = registry.resources.
                collectEntries { resource ->
                    [resource.defaultUrl ?: resource.location, resource]
                } as Map< String, Map<String, Object> >
        locationMap = urlMap.values().collectEntries { [it.location as String, it.url as String] }

        def resourceMapper = config.resource_mapper
        if (resourceMapper) {
            // Gets data from ResourceMapper first.
            def resources = resourceMapper(urlMap.values())
            // Checks whether resources contain maps with the same urls.
            // If so, RuntimeException is thrown.
            resources.groupBy { it.url }.find { it.value.size() > 1 }?.value?.head()?.with { res ->
                throw new RuntimeException("Encountered duplicate resource URL: ${res.url}")
            }
            urlMap = resources.collectEntries {
                [it.url as String, it]
            }
            locationMap = urlMap.values().collectEntries { [it.location as String, it.url as String] }
        }

        pages = urlMap.values().findAll { it.type == 'page' }
        assets = urlMap.values().findAll { it.type == 'asset' }

        perf.resourceMapTime += (System.currentTimeMillis() - resourceMapStart)

        if (log.debugEnabled) {
            urlMap.each { url, resource ->
                log.debug url
            }
        }
    }

    /**
     * Returns CDN url that should be used for the given resource url to speed
     * up parallel resource download in browser as much as possible
     *
     * @param url resource url
     *
     * @return CDN url that should be used to access the resource
     */
    public String getCdnUrl(String url) {
        String cdnUrl = null

        if (config.cdn_urls && url) {
            if (!cdnCache.containsKey(url)) {
                cdnCache[url] = config.cdn_urls[(cdnIndex / MAX_BROWSER_CONNECTS).toInteger()] + url
                cdnIndex = (cdnIndex + 1) % (config.cdn_urls.size() * MAX_BROWSER_CONNECTS)
            }
            cdnUrl = cdnCache[url]
        }

        cdnUrl
    }

    /**
     * Returns url by resource location
     *
     * @param location resource location
     *
     * @return resource url
     */
    public String getUrl(String location) {
        def res = locationMap[location]
        if (res == null && settings.command != 'preview') {
            throw new RuntimeException("Failed to find resource with location: ${location}")
        }
        res
    }

    /**
     * Returns all the resources registered in URL map 
     */
    public Collection<Map> getResources() {
        urlMap.values()
    }

    /**
     * Returns all the pages registered in URL map 
     */
    public Collection<Map> getPages() {
        pages
    }

    /**
     * Returns all the pages registered in URL map 
     */
    public Collection<Map> getAssets() {
        assets
    }

    /**
     * Returns resource for the given url
     *
     * @param url an url
     *
     * @return a resource that has specified url
     */
    public Map getResource(String url) {
        urlMap[url] ?: urlMap[url + '/']
    }
}
