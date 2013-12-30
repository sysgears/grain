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

package com.sysgears.grain.expando

import com.sysgears.grain.taglib.ResourceRenderer
import groovy.util.logging.Slf4j

import javax.inject.Inject
import javax.inject.Named

/**
 * Map class additional dynamic methods.
 */
@Named
@javax.inject.Singleton
@Slf4j
class MapDynamicMethods {

    /** Resource renderer */
    @Inject private ResourceRenderer renderer

    /**
     * Registers additional methods for Map class
     */
    void register() {

        /**
         * Renders resource with meta information provided in map and model
         * passed as optional argument.
         * 
         * @throws InvalidObjectException in case if meta information doesn't contains location key
         */
        Map.metaClass.render = { Map model = null ->
            renderer.render(delegate as Map, model, false)
        }

        /**
         * Renders included resource with caller resource meta information provided in map,
         * resource to be included location and optional model map.
         *
         * @throws InvalidObjectException in case if meta information doesn't contains location key
         */
        Map.metaClass.include = { String location, Map model = null ->
            renderer.render((delegate as Map) + [location: location], model, true)
        }
    }
}
