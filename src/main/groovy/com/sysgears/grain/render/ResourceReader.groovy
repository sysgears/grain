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

import com.sysgears.grain.config.Config

import javax.inject.Inject
import javax.inject.Named

/**
 * Resource file text contents reader.
 */
@Named
@javax.inject.Singleton
class ResourceReader {
    
    /** Site config */
    @Inject private Config config

    /**
     * Reads resource file text and applies source modifier from config if specified.
     * 
     * @param resource resource file
     * 
     * @return modified resource file text
     */
    public String readText(File resource) {
        def sourceModifier = config.source_modifier
        sourceModifier ? sourceModifier(resource) : resource.text as String
    }

    /**
     * Reads resource file bytes
     *
     * @param resource resource file
     *
     * @return modified resource file bytes
     */
    public byte[] readBytes(File resource) {
        resource.bytes
    }
}
