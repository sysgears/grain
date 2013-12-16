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

import com.sysgears.grain.init.CmdlineOptions
import com.sysgears.grain.config.Config
import com.sysgears.grain.registry.HeaderParser
import com.sysgears.grain.registry.URLRegistry
import groovy.util.logging.Slf4j

import javax.inject.Inject
import javax.inject.Named

/**
 * Site facade exposed to resource code.
 */
@Named
@javax.inject.Singleton
@Slf4j
class Site {
    
    /** Command line options */
    @Inject private CmdlineOptions opts

    /** Resource registry */
    @Inject private URLRegistry registry

    /** Site config */
    @Inject private Config config
    
    /** Resource header parser */
    @Inject HeaderParser headerParser

    /**
     * Returns all the pages in the site
     *
     * @return all the pages in the site
     */
    public Collection<Map<String, Object>> getPages() {
        registry.pages
    }

    /**
     * Returns all the assets in the registry
     *
     * @return all the assets in the registry
     */
    public Collection<Map<String, Object>> getAssets() {
        registry.assets
    }

    /**
     * Returns all the resources in the registry
     *
     * @return all the resources in the registry
     */
    public Collection<Map<String, Object>> getResources() {
        registry.resources
    }

    /**
     * Returns currently used environment
     * 
     * @return currently used site environment
     */
    public String getEnv() {
        opts.env
    }

    /**
     * Returns property from Site config
     * 
     * @param name property name
     * 
     * @return property value
     */
    public Object propertyMissing(String name) {
        config[name]
    }
}